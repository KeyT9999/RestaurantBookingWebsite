package com.example.booking.service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.example.booking.audit.AuditAction;
import com.example.booking.audit.AuditEvent;
import com.example.booking.config.AiSyncProperties;
import com.example.booking.domain.Dish;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.repository.DishRepository;
import com.example.booking.repository.RestaurantProfileRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Publishes CRUD sync events to the AI service.
 */
@Service
public class AiSyncEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(AiSyncEventPublisher.class);
    private static final String RESOURCE_RESTAURANT = "restaurant";
    private static final String RESOURCE_MENU = "menu";

    private final AiSyncProperties properties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final RestaurantProfileRepository restaurantProfileRepository;
    private final DishRepository dishRepository;

    public AiSyncEventPublisher(
            AiSyncProperties properties,
            @Qualifier("aiSyncRestTemplate") RestTemplate restTemplate,
            ObjectMapper objectMapper,
            RestaurantProfileRepository restaurantProfileRepository,
            DishRepository dishRepository) {
        this.properties = properties;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.restaurantProfileRepository = restaurantProfileRepository;
        this.dishRepository = dishRepository;
    }

    /**
     * Publish audit event to AI service if applicable.
     */
    public void publish(AuditEvent event) {
        if (!shouldPublish(event)) {
            return;
        }

        Optional<SyncEventPayload> payloadOpt = buildPayload(event);
        if (payloadOpt.isEmpty()) {
            logger.debug("Skipping AI sync for event {} due to unsupported payload", event);
            return;
        }

        dispatch(payloadOpt.get());
    }

    private boolean shouldPublish(AuditEvent event) {
        if (event == null) {
            return false;
        }
        if (!properties.isEnabled()) {
            return false;
        }
        if (!StringUtils.hasText(properties.getUrl())) {
            logger.debug("AI sync URL not configured, skipping publish");
            return false;
        }
        if (!event.isSuccess()) {
            logger.debug("Skipping AI sync because event marked as failed: {}", event);
            return false;
        }
        AuditAction action = event.getAction();
        if (action == null) {
            return false;
        }
        return switch (action) {
            case CREATE, UPDATE, DELETE -> true;
            default -> false;
        };
    }

    private Optional<SyncEventPayload> buildPayload(AuditEvent event) {
        Map<String, Object> metadata = new HashMap<>();
        if (event.getMetadata() != null) {
            metadata.putAll(event.getMetadata());
        }

        String className = metadata.getOrDefault("className", "").toString();
        if (!className.endsWith("Service")) {
            // Avoid double-publishing for repository layer
            return Optional.empty();
        }

        String normalizedResource = determineResourceType(event, metadata);
        if (normalizedResource == null) {
            return Optional.empty();
        }

        String action = event.getAction().name();
        Map<String, Object> data;
        if (AuditAction.DELETE.equals(event.getAction())) {
            data = Collections.emptyMap();
        } else {
            data = loadResourceData(normalizedResource, event, metadata);
            if (data == null) {
                return Optional.empty();
            }
        }

        Map<String, Object> syncMetadata = buildMetadata(event, normalizedResource, metadata);

        SyncEventPayload payload = new SyncEventPayload(
                UUID.randomUUID().toString(),
                normalizedResource,
                action,
                data,
                syncMetadata,
                event.getTimestamp() == null ? null
                        : event.getTimestamp().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        );
        return Optional.of(payload);
    }

    private String determineResourceType(AuditEvent event, Map<String, Object> metadata) {
        String resourceType = event.getResourceType();
        if (resourceType != null) {
            resourceType = resourceType.toUpperCase(Locale.ROOT);
        }
        String methodName = metadata.getOrDefault("method", "").toString().toLowerCase(Locale.ROOT);
        String className = metadata.getOrDefault("className", "").toString();

        if ("MENU".equals(resourceType)) {
            return RESOURCE_MENU;
        }
        if ("RESTAURANT".equals(resourceType) && methodName.contains("dish")) {
            return RESOURCE_MENU;
        }
        if ("RESTAURANT".equals(resourceType)) {
            return RESOURCE_RESTAURANT;
        }
        if (className.contains("Restaurant") && methodName.contains("dish")) {
            return RESOURCE_MENU;
        }
        if (className.contains("Restaurant")) {
            return RESOURCE_RESTAURANT;
        }
        return null;
    }

    private Map<String, Object> loadResourceData(
            String resourceType,
            AuditEvent event,
            Map<String, Object> metadata) {
        if (RESOURCE_RESTAURANT.equals(resourceType)) {
            return loadRestaurantData(event.getResourceId());
        } else if (RESOURCE_MENU.equals(resourceType)) {
            return loadMenuData(event.getResourceId(), metadata);
        }
        return null;
    }

    private Map<String, Object> loadRestaurantData(String resourceId) {
        if (restaurantProfileRepository == null) {
            return null;
        }
        Integer restaurantId = parseInteger(resourceId);
        if (restaurantId == null) {
            return null;
        }
        Optional<RestaurantProfile> restaurantOpt = restaurantProfileRepository.findById(restaurantId);
        if (restaurantOpt.isEmpty()) {
            return null;
        }
        RestaurantProfile restaurant = restaurantOpt.get();
        Map<String, Object> data = new HashMap<>();
        data.put("id", restaurant.getRestaurantId());
        data.put("name", restaurant.getRestaurantName());
        data.put("address", restaurant.getAddress());
        data.put("cuisineType", restaurant.getCuisineType());
        data.put("description", restaurant.getDescription());
        data.put("rating", safeRestaurantRating(restaurant));
        data.put("priceRange", buildPriceRange(restaurant));
        return data;
    }

    private Map<String, Object> loadMenuData(String resourceId, Map<String, Object> metadata) {
        if (dishRepository == null) {
            return null;
        }
        Integer dishId = parseInteger(resourceId);
        if (dishId == null) {
            dishId = parseInteger(metadata.get("dishId"));
        }
        if (dishId == null) {
            return null;
        }

        Optional<Dish> dishOpt = dishRepository.findById(dishId);
        if (dishOpt.isEmpty()) {
            return null;
        }
        Dish dish = dishOpt.get();
        Map<String, Object> data = new HashMap<>();
        data.put("id", dish.getDishId());
        data.put("restaurantId",
                dish.getRestaurant() != null ? dish.getRestaurant().getRestaurantId() : metadata.get("dishRestaurantId"));
        data.put("name", dish.getName());
        data.put("description", dish.getDescription());
        data.put("price", dish.getPrice());
        data.put("category", dish.getCategory());
        return data;
    }

    private Map<String, Object> buildMetadata(
            AuditEvent event,
            String resourceType,
            Map<String, Object> metadata) {
        Map<String, Object> syncMetadata = new HashMap<>();
        syncMetadata.put("resourceId", event.getResourceId());
        syncMetadata.put("resourceTypeOriginal", event.getResourceType());
        syncMetadata.put("restaurantId", event.getRestaurantId());
        syncMetadata.put("userId", event.getUserId());
        syncMetadata.put("username", event.getUsername());
        syncMetadata.put("userRole", event.getUserRole());
        if (metadata.containsKey("dishRestaurantId")) {
            syncMetadata.put("restaurantId", metadata.get("dishRestaurantId"));
        }
        if (metadata.containsKey("dishId")) {
            syncMetadata.put("dishId", metadata.get("dishId"));
        }
        syncMetadata.putAll(metadata);
        syncMetadata.put("resourceType", resourceType);
        return syncMetadata;
    }

    private void dispatch(SyncEventPayload payload) {
        String body;
        try {
            body = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize AI sync payload {}", payload, e);
            return;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (StringUtils.hasText(properties.getSecret())) {
            headers.add("X-AI-Signature", calculateHmac(body, properties.getSecret()));
        }
        if (StringUtils.hasText(properties.getApiKey())) {
            headers.add("X-AI-Key", properties.getApiKey());
        }

        HttpEntity<String> request = new HttpEntity<>(body, headers);
        int maxAttempts = Math.max(1, properties.getRetry().getMaxAttempts());
        long backoffMs = Math.max(0, properties.getRetry().getBackoffMs());

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                ResponseEntity<String> response = restTemplate.postForEntity(properties.getUrl(), request, String.class);
                if (response.getStatusCode().is2xxSuccessful()) {
                    logger.debug("AI sync event {} delivered successfully with status {}", payload.eventId(),
                            response.getStatusCode());
                    return;
                }

                if (response.getStatusCode().is4xxClientError()) {
                    logger.error("AI sync rejected payload {} with status {} and body {}", payload.eventId(),
                            response.getStatusCode(), response.getBody());
                    return;
                }

                logger.warn("AI sync attempt {} for event {} returned status {}",
                        attempt, payload.eventId(), response.getStatusCode());
            } catch (HttpStatusCodeException ex) {
                if (ex.getStatusCode().is4xxClientError()) {
                    logger.error("AI sync returned client error {} for event {}, body: {}",
                            ex.getStatusCode(), payload.eventId(), ex.getResponseBodyAsString());
                    return;
                }
                logger.warn("AI sync attempt {} for event {} failed with status {}",
                        attempt, payload.eventId(), ex.getStatusCode(), ex);
            } catch (RestClientException ex) {
                logger.warn("AI sync attempt {} for event {} failed: {}", attempt, payload.eventId(), ex.getMessage());
            }

            if (attempt < maxAttempts && backoffMs > 0) {
                try {
                    TimeUnit.MILLISECONDS.sleep(backoffMs);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }

        logger.error("Exhausted retries, AI sync event {} not delivered", payload.eventId());
    }

    private String calculateHmac(String body, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] rawHmac = mac.doFinal(body.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(rawHmac);
        } catch (Exception e) {
            logger.error("Unable to compute HMAC signature", e);
            return "";
        }
    }

    private Double safeRestaurantRating(RestaurantProfile restaurant) {
        try {
            return restaurant.getAverageRating();
        } catch (Exception ex) {
            logger.debug("Unable to compute average rating for restaurant {}: {}",
                    restaurant.getRestaurantId(), ex.getMessage());
            return null;
        }
    }

    private String buildPriceRange(RestaurantProfile restaurant) {
        BigDecimal min = restaurant.getPriceRangeMin();
        BigDecimal max = restaurant.getPriceRangeMax();
        if (min != null && max != null) {
            return min.toPlainString() + "-" + max.toPlainString();
        }
        if (restaurant.getAveragePrice() != null) {
            return restaurant.getAveragePrice().toPlainString();
        }
        return null;
    }

    private Integer parseInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Integer integer) {
            return integer;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private record SyncEventPayload(
            String eventId,
            String resourceType,
            String action,
            Map<String, Object> data,
            Map<String, Object> metadata,
            Long timestamp) {
    }
}
