package com.example.booking.service.ai;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.booking.domain.RestaurantProfile;
import com.example.booking.dto.ai.AISearchRequest;
import com.example.booking.dto.ai.AISearchResponse;
import com.example.booking.service.RestaurantManagementService;
import com.example.booking.util.CityGeoResolver;
import com.example.booking.util.GeoUtils;

/**
 * Simplified Recommendation Service for MVP
 */
@Service
public class RecommendationService {
    
    @Autowired
    private OpenAIService openAIService;
    
    @Autowired
    private RestaurantManagementService restaurantService;

    private final CityGeoResolver cityGeoResolver = new CityGeoResolver();

    private static final Set<String> DEFAULT_STOP_WORDS = Set.of(
        "toi", "minh", "ban", "chungtoi",
        "muon", "an", "uong", "can",
        "o", "tai", "quan", "phuong", "thanh", "thanhpho",
        "mon", "gi", "hom", "nay", "ngay", "cho", "nguoi",
        "gia", "tam", "khoang", "dat", "nhahang", "nha", "hang"
    );

    private static final Pattern LAT_LNG_PATTERN = Pattern.compile("^\\s*(-?\\d+(?:\\.\\d+)?)\\s*,\\s*(-?\\d+(?:\\.\\d+)?)\\s*$");
    private static final Pattern DISTRICT_PATTERN = Pattern.compile("(quan|qu?n|q\\.)\\s*(\\d{1,2})", Pattern.CASE_INSENSITIVE);
    private static final Pattern PRICE_PATTERN = Pattern.compile("(\\d+(?:[.,]\\d+)?)\\s*(k|nghin|nghìn|ngan|ngàn|tr|trieu|triệu|million|m|vnđ|vnd|đ)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final double PRICE_TOLERANCE = 50_000d;
    
    /**
     * Main search method - simplified version
     */
    public AISearchResponse search(AISearchRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Search request is required");
        }

        String query = request.getQuery();
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Query text is required");
        }
        query = query.trim();

        request.setQuery(query);

        try {
            System.out.println("🔍 AI Search started for query: " + query);
            
            // 1. Parse intent with timeout
            Map<String, Object> intent = parseIntentWithTimeout(request);
            System.out.println("📊 Parsed intent: " + intent);
            
            // 2. Find candidates based on intent
            PricePreference pricePreference = resolvePricePreference(request, intent);
            List<RestaurantProfile> candidates = findCandidates(intent, request, pricePreference);
            System.out.println("🏪 Found " + candidates.size() + " candidate restaurants");

            LocationContext locationContext = buildLocationContext(request, intent);
            List<RestaurantMatch> rankedMatches = applyLocationEnhancements(candidates, locationContext);
            
            // 3. Take top results
            List<RestaurantMatch> topResults = rankedMatches.stream()
                .limit(request.getMaxResults() != null ? request.getMaxResults() : 5)
                .collect(Collectors.toList());
            
            System.out.println("🎯 Selected " + topResults.size() + " top results");
            
            // 4. Generate recommendations
            List<AISearchResponse.RestaurantRecommendation> recommendations = 
                generateRecommendations(topResults, intent);
            System.out.println("✨ Generated " + recommendations.size() + " recommendations");
            
            // 5. Build response
            AISearchResponse response = buildResponse(request, recommendations, intent, rankedMatches.size());
            System.out.println("📤 Built response with totalFound: " + rankedMatches.size());
            
            return response;
            
        } catch (Exception e) {
            System.err.println("❌ AI Search Error: " + e.getMessage());
            e.printStackTrace();
            // Fallback to simple search
            return fallbackSearch(request);
        }
    }
    
    /**
     * Parse intent with timeout
     */
    private Map<String, Object> parseIntentWithTimeout(AISearchRequest request) throws Exception {
        java.util.concurrent.CompletableFuture<Map<String, Object>> intentFuture =
            openAIService.parseIntent(request.getQuery(), request.getUserId());

        if (intentFuture == null) {
            throw new IllegalStateException("Intent parsing service unavailable");
        }

        return intentFuture.get(800, java.util.concurrent.TimeUnit.MILLISECONDS);
    }
    
    /**
     * Find restaurant candidates based on intent
     */
    private List<RestaurantProfile> findCandidates(Map<String, Object> intent, AISearchRequest request, PricePreference pricePreference) {
        System.out.println("🔍 Finding restaurant candidates...");
        List<RestaurantProfile> allRestaurants = restaurantService.findAllRestaurants();
        if (allRestaurants == null) {
            allRestaurants = List.of();
        }
        System.out.println("📊 Total restaurants in DB: " + allRestaurants.size());
        
        List<RestaurantProfile> filtered = allRestaurants.stream()
            .filter(restaurant -> matchesCuisine(restaurant, intent))
            .filter(restaurant -> matchesPriceRange(restaurant, pricePreference))
            .filter(restaurant -> matchesDietary(restaurant, intent))
            .collect(Collectors.toList());

        List<RestaurantProfile> queryFiltered = applyQueryFiltering(filtered, request);
            
        System.out.println("✅ Filtered restaurants: " + queryFiltered.size());
        return queryFiltered;
    }
    
    /**
     * Generate recommendations with explanations
     */
    private List<AISearchResponse.RestaurantRecommendation> generateRecommendations(
            List<RestaurantMatch> matches, Map<String, Object> intent) {
        
        try {
            if (matches.isEmpty()) {
                return List.of();
            }

            List<String> restaurantNames = matches.stream()
                .map(match -> match.restaurant().getRestaurantName())
                .collect(Collectors.toList());
            
            List<String> explanations = openAIService.explainRestaurants(restaurantNames)
                .get(800, java.util.concurrent.TimeUnit.MILLISECONDS);
            
            return java.util.stream.IntStream.range(0, matches.size())
                .mapToObj(index -> {
                    RestaurantMatch match = matches.get(index);
                    return createRecommendation(match.restaurant(), match.distanceKm());
                })
                .collect(Collectors.toList());
            
        } catch (Exception e) {
            // Fallback to simple recommendations
            return matches.stream()
                .map(match -> createSimpleRecommendation(match.restaurant(), match.distanceKm()))
                .collect(Collectors.toList());
        }
    }
    
    /**
     * Fallback search when AI fails
     */
    private AISearchResponse fallbackSearch(AISearchRequest request) {
        List<RestaurantProfile> restaurants = restaurantService.findAllRestaurants();
        if (restaurants == null) {
            restaurants = List.of();
        }

        restaurants = applyQueryFiltering(restaurants, request);
        PricePreference pricePreference = resolvePricePreference(request, Map.of());
        if (pricePreference != null && !pricePreference.isEmpty()) {
            restaurants = restaurants.stream()
                .filter(restaurant -> matchesPriceRange(restaurant, pricePreference))
                .collect(Collectors.toList());
        }
        LocationContext locationContext = buildLocationContext(request, Map.of());
        List<RestaurantMatch> matches = applyLocationEnhancements(restaurants, locationContext);
        int limit = request.getMaxResults() != null ? request.getMaxResults() : 5;
        
        List<AISearchResponse.RestaurantRecommendation> recommendations = matches.stream()
            .limit(limit)
            .map(match -> createSimpleRecommendation(match.restaurant(), match.distanceKm()))
            .collect(Collectors.toList());
        
        AISearchResponse response = new AISearchResponse();
        response.setOriginalQuery(request.getQuery());
        response.setTotalFound(matches.size());
        response.setTotalReturned(recommendations.size());
        response.setExplanation("Tìm thấy nhà hàng phù hợp (chế độ đơn giản)");
        response.setRecommendations(recommendations);
        
        return response;
    }
    
    // Helper methods
    private boolean matchesCuisine(RestaurantProfile restaurant, Map<String, Object> intent) {
        @SuppressWarnings("unchecked")
        List<String> cuisines = (List<String>) intent.get("cuisine");
        if (cuisines == null || cuisines.isEmpty()) return true;
        
        String restaurantCuisine = normalize(restaurant.getCuisineType());
        if (restaurantCuisine.isEmpty()) {
            return false;
        }
        
        return cuisines.stream()
            .map(this::normalize)
            .anyMatch(restaurantCuisine::contains);
    }
    
    private boolean matchesPriceRange(RestaurantProfile restaurant, PricePreference pricePreference) {
        if (pricePreference == null || pricePreference.isEmpty()) {
            return true;
        }
        if (pricePreference.isInvalid()) {
            return false;
        }
        if (restaurant.getAveragePrice() == null) {
            return false;
        }
        double price = restaurant.getAveragePrice().doubleValue();
        if (pricePreference.min() != null && price < pricePreference.min()) {
            return false;
        }
        if (pricePreference.max() != null && price > pricePreference.max()) {
            return false;
        }
        return true;
    }
    
    private boolean matchesDietary(RestaurantProfile restaurant, Map<String, Object> intent) {
        // Simple dietary matching - can be enhanced later
        return true;
    }

    private List<RestaurantProfile> applyQueryFiltering(List<RestaurantProfile> restaurants, AISearchRequest request) {
        if (restaurants == null || restaurants.isEmpty()) {
            return List.of();
        }

        String query = request.getQuery();
        if (query == null || query.isBlank()) {
            return restaurants;
        }

        String normalizedQuery = normalize(query);
        List<String> keywords = extractKeywords(normalizedQuery);

        return restaurants.stream()
            .filter(restaurant -> matchesQueryText(restaurant, normalizedQuery, keywords))
            .collect(Collectors.toList());
    }

    private boolean matchesQueryText(RestaurantProfile restaurant, String normalizedQuery, List<String> keywords) {
        if (normalizedQuery == null || normalizedQuery.isBlank()) {
            return true;
        }

        String normalizedName = normalize(restaurant.getRestaurantName());
        String normalizedCuisine = normalize(restaurant.getCuisineType());

        if (!normalizedName.isEmpty() && normalizedName.contains(normalizedQuery)) {
            return true;
        }

        if (!normalizedCuisine.isEmpty() && normalizedCuisine.contains(normalizedQuery)) {
            return true;
        }

        if (keywords.isEmpty()) {
            return true;
        }

        for (String keyword : keywords) {
            if (!normalizedName.isEmpty() && normalizedName.contains(keyword)) {
                return true;
            }
            if (!normalizedCuisine.isEmpty() && normalizedCuisine.contains(keyword)) {
                return true;
            }
        }

        return false;
    }

    private List<String> extractKeywords(String normalizedQuery) {
        if (normalizedQuery == null || normalizedQuery.isBlank()) {
            return List.of();
        }

        return Arrays.stream(normalizedQuery.split("\\s+"))
            .map(String::trim)
            .filter(token -> token.length() > 1)
            .filter(token -> !DEFAULT_STOP_WORDS.contains(token))
            .distinct()
            .collect(Collectors.toList());
    }

    private String normalize(String input) {
        if (input == null) {
            return "";
        }
        String lower = input.toLowerCase();
        String normalized = Normalizer.normalize(lower, Normalizer.Form.NFD)
            .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
            .replace('đ', 'd');
        return normalized.trim();
    }
    
    private AISearchResponse.RestaurantRecommendation createRecommendation(
            RestaurantProfile restaurant, Double distanceKm) {
        AISearchResponse.RestaurantRecommendation rec = new AISearchResponse.RestaurantRecommendation();
        rec.setRestaurantId(restaurant.getId().toString());
        rec.setRestaurantName(restaurant.getRestaurantName());
        rec.setCuisineType(restaurant.getCuisineType());
        rec.setPriceRange(restaurant.getAveragePrice() != null ? 
            restaurant.getAveragePrice().toString() : "Liên hệ");
        rec.setImageUrl(restaurant.getMainImageUrl());
        rec.setRating(restaurant.getFormattedAverageRating());
        rec.setDistanceKm(distanceKm);
        rec.setBookingUrl("/booking/new?restaurantId=" + restaurant.getId());
        rec.setViewDetailsUrl("/restaurants/" + restaurant.getId());
        return rec;
    }
    
    private AISearchResponse.RestaurantRecommendation createSimpleRecommendation(RestaurantProfile restaurant, Double distanceKm) {
        AISearchResponse.RestaurantRecommendation rec = new AISearchResponse.RestaurantRecommendation();
        rec.setRestaurantId(restaurant.getId().toString());
        rec.setRestaurantName(restaurant.getRestaurantName());
        rec.setCuisineType(restaurant.getCuisineType());
        rec.setPriceRange(restaurant.getAveragePrice() != null ? 
            restaurant.getAveragePrice().toString() : "Liên hệ");
        rec.setImageUrl(restaurant.getMainImageUrl());
        rec.setRating(restaurant.getFormattedAverageRating());
        rec.setDistanceKm(distanceKm);
        rec.setBookingUrl("/booking/new?restaurantId=" + restaurant.getId());
        rec.setViewDetailsUrl("/restaurants/" + restaurant.getId());
        return rec;
    }
    
    private AISearchResponse buildResponse(AISearchRequest request, 
                                         List<AISearchResponse.RestaurantRecommendation> recommendations,
                                         Map<String, Object> intent,
                                         int totalFound) {
        AISearchResponse response = new AISearchResponse();
        response.setOriginalQuery(request.getQuery());
        response.setTotalFound(totalFound);
        response.setTotalReturned(recommendations.size());
        response.setExplanation("Tìm thấy " + recommendations.size() + " nhà hàng phù hợp");
        response.setRecommendations(recommendations);
        
        // Set confidence from intent
        Object confidence = intent.get("confidence");
        if (confidence instanceof Number) {
            response.setConfidenceScore(java.math.BigDecimal.valueOf(((Number) confidence).doubleValue()));
        }
        
        return response;
    }

    private PricePreference resolvePricePreference(AISearchRequest request, Map<String, Object> intent) {
        Double min = null;
        Double max = null;

        if (intent != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> priceRange = (Map<String, Object>) intent.get("price_range");
            if (priceRange != null) {
                Number minIntent = extractNumber(priceRange.get("min"));
                Number maxIntent = extractNumber(priceRange.get("max"));
                if (minIntent != null) {
                    min = minIntent.doubleValue();
                }
                if (maxIntent != null) {
                    max = maxIntent.doubleValue();
                }
            }
        }

        if (request.getMinPrice() != null) {
            min = request.getMinPrice().doubleValue();
        }
        if (request.getMaxPrice() != null) {
            max = request.getMaxPrice().doubleValue();
        }

        PricePreference queryHint = extractPriceHint(request.getQuery());
        if (queryHint != null) {
            if (queryHint.min() != null) {
                min = queryHint.min();
            }
            if (queryHint.max() != null) {
                max = queryHint.max();
            }
        }

        return new PricePreference(min, max);
    }

    private Number extractNumber(Object value) {
        if (value instanceof Number number) {
            return number;
        }
        if (value instanceof String stringValue) {
            try {
                return Double.parseDouble(stringValue);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private PricePreference extractPriceHint(String query) {
        if (query == null || query.isBlank()) {
            return null;
        }

        String lowered = query.toLowerCase(Locale.ROOT);
        Matcher matcher = PRICE_PATTERN.matcher(lowered);
        while (matcher.find()) {
            String numberPart = matcher.group(1);
            String unitPart = matcher.group(2);
            if (unitPart == null || unitPart.isBlank()) {
                continue;
            }
            double numericValue = parsePriceNumber(numberPart);
            Double multiplier = resolvePriceUnitMultiplier(unitPart);
            if (Double.isNaN(numericValue) || multiplier == null) {
                continue;
            }
            double baseValue = numericValue * multiplier;
            double min = Math.max(0, baseValue - PRICE_TOLERANCE);
            double max = baseValue + PRICE_TOLERANCE;
            return new PricePreference(min, max);
        }

        return null;
    }

    private double parsePriceNumber(String raw) {
        if (raw == null) {
            return Double.NaN;
        }
        String sanitized = raw.trim().toLowerCase(Locale.ROOT);
        sanitized = sanitized.replace(",", ".");
        int dotIndex = sanitized.indexOf('.');
        if (dotIndex >= 0) {
            String fractional = sanitized.substring(dotIndex + 1);
            if (fractional.length() == 3 && fractional.chars().allMatch(Character::isDigit)) {
                sanitized = sanitized.replace(".", "");
            }
        }
        try {
            return Double.parseDouble(sanitized);
        } catch (NumberFormatException ex) {
            return Double.NaN;
        }
    }

    private Double resolvePriceUnitMultiplier(String unitRaw) {
        if (unitRaw == null) {
            return null;
        }
        String unit = normalize(unitRaw);
        switch (unit) {
            case "k":
            case "nghin":
            case "ngan":
                return 1_000d;
            case "tr":
            case "trieu":
            case "trio":
            case "million":
            case "m":
                return 1_000_000d;
            case "ty":
            case "ti":
                return 1_000_000_000d;
            case "vnd":
            case "vnđ":
            case "d":
            case "đ":
            case "":
                return 1d;
            default:
                return null;
        }
    }

    private LocationContext buildLocationContext(AISearchRequest request, Map<String, Object> intent) {
        Double latitude = null;
        Double longitude = null;
        Double maxDistance = request.getMaxDistance() != null ? request.getMaxDistance().doubleValue() : null;

        String userLocation = request.getUserLocation();
        if (userLocation != null) {
            Matcher matcher = LAT_LNG_PATTERN.matcher(userLocation);
            if (matcher.matches()) {
                latitude = Double.parseDouble(matcher.group(1));
                longitude = Double.parseDouble(matcher.group(2));
            }
        }

        if (latitude == null || longitude == null) {
            CityGeoResolver.LatLng approx = cityGeoResolver.resolveFromAddress(request.getQuery());
            if (approx != null) {
                latitude = approx.lat;
                longitude = approx.lng;
            }
        }

        if (latitude == null || longitude == null) {
            // Without coordinates we cannot enforce distance filters
            maxDistance = null;
        } else if (maxDistance == null && intent != null) {
            Object intentDistance = intent.get("distance");
            if (intentDistance instanceof Number) {
                maxDistance = ((Number) intentDistance).doubleValue();
            }
        }

        List<String> locationKeywords = extractLocationKeywords(request);
        return new LocationContext(latitude, longitude, maxDistance, locationKeywords);
    }

    private List<String> extractLocationKeywords(AISearchRequest request) {
        List<String> keywords = new ArrayList<>();

        if (request.getPreferredDistricts() != null) {
            request.getPreferredDistricts().stream()
                .filter(java.util.Objects::nonNull)
                .map(this::normalize)
                .filter(token -> !token.isBlank())
                .forEach(keywords::add);
        }

        String query = request.getQuery();
        if (query != null) {
            String normalizedQuery = normalize(query);
            Matcher matcher = DISTRICT_PATTERN.matcher(normalizedQuery);
            while (matcher.find()) {
                String token = matcher.group(0)
                    .replaceAll("\\s+", " ")
                    .trim();
                if (!token.isEmpty()) {
                    keywords.add(token);
                }
            }
        }

        return keywords.stream()
            .filter(token -> !token.isBlank())
            .distinct()
            .collect(Collectors.toList());
    }

    private List<RestaurantMatch> applyLocationEnhancements(List<RestaurantProfile> restaurants,
                                                            LocationContext locationContext) {
        if (restaurants == null || restaurants.isEmpty()) {
            return List.of();
        }

        List<RestaurantMatch> matches = new ArrayList<>();
        for (RestaurantProfile restaurant : restaurants) {
            if (!matchesLocationKeywords(restaurant, locationContext.locationKeywords())) {
                continue;
            }

            Double distance = null;
            if (locationContext.hasCoordinates()) {
                CityGeoResolver.LatLng restaurantCoords = resolveRestaurantLocation(restaurant);
                if (restaurantCoords != null) {
                    distance = GeoUtils.haversineKm(
                        locationContext.latitude(),
                        locationContext.longitude(),
                        restaurantCoords.lat,
                        restaurantCoords.lng
                    );
                }
            }

            matches.add(new RestaurantMatch(restaurant, distance));
        }

        if (locationContext.hasCoordinates() && locationContext.maxDistanceKm() != null) {
            matches = matches.stream()
                .filter(match -> match.distanceKm() != null && match.distanceKm() <= locationContext.maxDistanceKm())
                .collect(Collectors.toList());
        }

        matches.sort(Comparator
            .comparing((RestaurantMatch match) -> match.distanceKm() == null ? 1 : 0)
            .thenComparingDouble(match -> match.distanceKm() != null ? match.distanceKm() : Double.MAX_VALUE)
            .thenComparingDouble(match -> -restaurantRating(match.restaurant()))
            .thenComparing(match -> normalize(match.restaurant().getRestaurantName())));

        return matches;
    }

    private boolean matchesLocationKeywords(RestaurantProfile restaurant, List<String> locationKeywords) {
        if (locationKeywords == null || locationKeywords.isEmpty()) {
            return true;
        }
        String normalizedAddress = normalize(restaurant.getAddress());
        String normalizedHeroCity = normalize(restaurant.getHeroCity());

        return locationKeywords.stream().anyMatch(keyword ->
            (!normalizedAddress.isEmpty() && normalizedAddress.contains(keyword)) ||
            (!normalizedHeroCity.isEmpty() && normalizedHeroCity.contains(keyword))
        );
    }

    private CityGeoResolver.LatLng resolveRestaurantLocation(RestaurantProfile restaurant) {
        CityGeoResolver.LatLng coords = cityGeoResolver.resolveFromAddress(restaurant.getAddress());
        if (coords == null && restaurant.getHeroCity() != null) {
            coords = cityGeoResolver.resolveFromAddress(restaurant.getHeroCity());
        }
        return coords;
    }

    private double restaurantRating(RestaurantProfile restaurant) {
        try {
            return restaurant.getAverageRating();
        } catch (Exception ex) {
            return 0.0;
        }
    }

    private record RestaurantMatch(RestaurantProfile restaurant, Double distanceKm) {}

    private static final class PricePreference {
        private final Double min;
        private final Double max;

        PricePreference(Double min, Double max) {
            this.min = min;
            this.max = max;
        }

        Double min() {
            return min;
        }

        Double max() {
            return max;
        }

        boolean isEmpty() {
            return min == null && max == null;
        }

        boolean isInvalid() {
            return min != null && max != null && min > max;
        }
    }

    private static final class LocationContext {
        private final Double latitude;
        private final Double longitude;
        private final Double maxDistanceKm;
        private final List<String> locationKeywords;

        LocationContext(Double latitude, Double longitude, Double maxDistanceKm, List<String> locationKeywords) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.maxDistanceKm = maxDistanceKm;
            this.locationKeywords = locationKeywords != null ? List.copyOf(locationKeywords) : List.of();
        }

        boolean hasCoordinates() {
            return latitude != null && longitude != null;
        }

        Double latitude() {
            return latitude;
        }

        Double longitude() {
            return longitude;
        }

        Double maxDistanceKm() {
            return maxDistanceKm;
        }

        List<String> locationKeywords() {
            return locationKeywords;
        }
    }
}
