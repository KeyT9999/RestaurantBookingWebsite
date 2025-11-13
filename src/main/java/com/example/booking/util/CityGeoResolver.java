package com.example.booking.util;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Resolve coordinates for a restaurant based on its address.
 * Uses OpenStreetMap Nominatim API for accurate geocoding, with fallback to city-center coordinates.
 */
public class CityGeoResolver {
    private static final Logger logger = LoggerFactory.getLogger(CityGeoResolver.class);
    
    public static final class LatLng {
        public final double lat;
        public final double lng;
        public LatLng(double lat, double lng) { this.lat = lat; this.lng = lng; }
    }

    private static final Map<String, LatLng> CITY_COORDS = new HashMap<>();
    static {
        CITY_COORDS.put("ho chi minh", new LatLng(10.776889, 106.700806)); // HCMC District 1
        CITY_COORDS.put("hcm", new LatLng(10.776889, 106.700806));
        CITY_COORDS.put("hồ chí minh", new LatLng(10.776889, 106.700806));
        CITY_COORDS.put("sai gon", new LatLng(10.776889, 106.700806));

        CITY_COORDS.put("ha noi", new LatLng(21.027763, 105.834160));
        CITY_COORDS.put("hà nội", new LatLng(21.027763, 105.834160));

        CITY_COORDS.put("da nang", new LatLng(16.047079, 108.206230));
        CITY_COORDS.put("đà nẵng", new LatLng(16.047079, 108.206230));

        CITY_COORDS.put("hai phong", new LatLng(20.844911, 106.688084));
        CITY_COORDS.put("hải phòng", new LatLng(20.844911, 106.688084));

        CITY_COORDS.put("can tho", new LatLng(10.045162, 105.746857));
        CITY_COORDS.put("cần thơ", new LatLng(10.045162, 105.746857));
    }

    // Cache for geocoding results to avoid repeated API calls
    private final Map<String, LatLng> geocodeCache = new ConcurrentHashMap<>();
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    // OpenStreetMap Nominatim API endpoint
    private static final String NOMINATIM_API_URL = "https://nominatim.openstreetmap.org/search";
    
    public CityGeoResolver(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Try to resolve coordinates from a freeform address string.
     * First attempts geocoding via OpenStreetMap Nominatim API for accurate results.
     * Falls back to city-center coordinates if geocoding fails.
     * Returns null if no coordinates can be determined.
     */
    public LatLng resolveFromAddress(String address) {
        if (address == null || address.isBlank()) return null;
        
        // Use original address as cache key to ensure each unique address gets its own cache entry
        // This prevents different addresses from sharing the same cache entry
        if (geocodeCache.containsKey(address)) {
            LatLng cached = geocodeCache.get(address);
            logger.debug("📍 Using cached coordinates for address: {} -> ({}, {})", address, cached.lat, cached.lng);
            return cached;
        }
        
        // Try geocoding via OpenStreetMap Nominatim API
        logger.info("🌐 Attempting to geocode address: {}", address);
        LatLng geocoded = geocodeAddress(address);
        if (geocoded != null) {
            // Only cache if the result is significantly different from city center
            // This helps identify when geocoding actually found a specific location
            String normalizedAddress = normalize(address);
            LatLng cityCenter = getCityCenterCoordinates(normalizedAddress);
            if (cityCenter == null || isSignificantlyDifferent(geocoded, cityCenter)) {
                geocodeCache.put(address, geocoded);
                logger.info("✅ Geocoded address '{}' to specific coordinates: ({}, {})", address, geocoded.lat, geocoded.lng);
                return geocoded;
            } else {
                // Geocoding returned city center - don't cache, try again next time
                logger.warn("⚠️ Geocoding returned city-center coordinates for '{}', not caching", address);
                return geocoded;
            }
        }
        
        // Fallback to city-center coordinates
        String normalizedAddress = normalize(address);
        LatLng cityCenter = getCityCenterCoordinates(normalizedAddress);
        if (cityCenter != null) {
            // Don't cache city-center coordinates - this allows retry on next request
            // This ensures that if geocoding API becomes available, we'll try again
            logger.warn("📍 Falling back to city-center coordinates for address '{}': ({}, {})", 
                address, cityCenter.lat, cityCenter.lng);
            return cityCenter;
        }
        
        logger.warn("⚠️ Could not resolve coordinates for address: {}", address);
        return null;
    }
    
    /**
     * Check if two coordinates are significantly different (more than 1km apart)
     */
    private boolean isSignificantlyDifferent(LatLng coord1, LatLng coord2) {
        double distance = GeoUtils.haversineKm(coord1.lat, coord1.lng, coord2.lat, coord2.lng);
        return distance > 1.0; // More than 1km difference
    }
    
    /**
     * Geocode address using OpenStreetMap Nominatim API
     * Tries multiple query strategies to find accurate coordinates
     */
    private LatLng geocodeAddress(String address) {
        try {
            String normalizedAddress = normalize(address);
            
            // Extract key parts from address for better geocoding
            // Try to extract street number and street name
            String[] addressParts = address.split(",");
            String streetPart = addressParts.length > 0 ? addressParts[0].trim() : address;
            
            // Detect city and add context
            String cityContext = "";
            if (normalizedAddress.contains("đà nẵng") || normalizedAddress.contains("da nang")) {
                cityContext = ", Đà Nẵng";
            } else if (normalizedAddress.contains("hồ chí minh") || normalizedAddress.contains("ho chi minh") 
                    || normalizedAddress.contains("hcm") || normalizedAddress.contains("tp.hcm")) {
                cityContext = ", Hồ Chí Minh";
            } else if (normalizedAddress.contains("hà nội") || normalizedAddress.contains("ha noi")) {
                cityContext = ", Hà Nội";
            }
            
            // Try multiple query strategies in order of specificity
            String[] queries = {
                address + cityContext + ", Vietnam",  // Full address with city and country
                streetPart + cityContext + ", Vietnam",  // Street part with city
                address + ", Vietnam",  // Full address with country only
                streetPart + ", Vietnam"  // Street part with country only
            };
            
            // If we have a specific street address (contains numbers), prioritize it
            if (streetPart.matches(".*\\d+.*")) {
                // Street has numbers, try street-specific queries first
                queries = new String[]{
                    streetPart + cityContext + ", Vietnam",
                    address + cityContext + ", Vietnam",
                    streetPart + ", Vietnam",
                    address + ", Vietnam"
                };
            }
            
            for (String query : queries) {
                String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
                String url = String.format("%s?q=%s&format=json&limit=1&addressdetails=1&countrycodes=vn&extratags=1", 
                    NOMINATIM_API_URL, encodedQuery);
                
                // Add User-Agent header (required by Nominatim usage policy)
                org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
                headers.set("User-Agent", "RestaurantBookingApp/1.0");
                headers.set("Accept-Language", "vi,en");
                org.springframework.http.HttpEntity<?> entity = new org.springframework.http.HttpEntity<>(headers);
                
                logger.debug("🌐 Geocoding address via Nominatim: {}", address);
                logger.debug("🌐 Query URL: {}", url);
                
                // Add small delay to respect rate limiting (Nominatim allows 1 request per second)
                try {
                    Thread.sleep(1100); // 1.1 seconds between requests
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return null;
                }
                
                try {
                    ResponseEntity<String> response = restTemplate.exchange(
                        URI.create(url),
                        org.springframework.http.HttpMethod.GET,
                        entity,
                        String.class
                    );
                    
                    if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                        JsonNode json = objectMapper.readTree(response.getBody());
                        if (json.isArray() && json.size() > 0) {
                            JsonNode firstResult = json.get(0);
                            double lat = firstResult.get("lat").asDouble();
                            double lon = firstResult.get("lon").asDouble();
                            
                            // Check if result is city-center (reject it)
                            String normalizedAddr = normalize(address);
                            LatLng cityCenter = getCityCenterCoordinates(normalizedAddr);
                            if (cityCenter != null) {
                                double distToCityCenter = GeoUtils.haversineKm(lat, lon, cityCenter.lat, cityCenter.lng);
                                if (distToCityCenter < 1.0) {
                                    logger.warn("⚠️ Geocoding returned city-center for '{}' (distance: {} km), trying next query", 
                                        address, String.format("%.2f", distToCityCenter));
                                    continue; // Try next query
                                }
                            }
                            
                            // Check the importance/rank of the result
                            double importance = firstResult.has("importance") ? 
                                firstResult.get("importance").asDouble() : 0.0;
                            
                            logger.info("✅ Geocoding successful for '{}': ({}, {}), importance: {}", 
                                address, lat, lon, String.format("%.3f", importance));
                            return new LatLng(lat, lon);
                        }
                    }
                } catch (RestClientException e) {
                    logger.debug("⚠️ Query '{}' failed, trying next: {}", query, e.getMessage());
                    continue; // Try next query
                }
            }
            
            logger.warn("⚠️ Geocoding returned no valid results for address: {}", address);
            return null;
            
        } catch (RestClientException e) {
            logger.warn("⚠️ Geocoding API error for address '{}': {}", address, e.getMessage());
            return null;
        } catch (Exception e) {
            logger.warn("⚠️ Error during geocoding for address '{}': {}", address, e.getMessage());
            if (logger.isDebugEnabled()) {
                logger.debug("Exception details:", e);
            }
            return null;
        }
    }
    
    /**
     * Fallback: Extract city-center coordinates from address string
     */
    private LatLng getCityCenterCoordinates(String normalizedAddress) {
        for (Map.Entry<String, LatLng> e : CITY_COORDS.entrySet()) {
            if (normalizedAddress.contains(e.getKey())) {
                return e.getValue();
            }
        }
        return null;
    }

    private String normalize(String s) {
        String lower = s.toLowerCase(Locale.ROOT);
        // Minimal normalization; accents are kept but we also include unaccented keys above
        return lower;
    }
}

