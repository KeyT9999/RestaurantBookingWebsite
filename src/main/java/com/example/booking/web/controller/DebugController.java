package com.example.booking.web.controller;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.booking.domain.Voucher;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.service.VoucherService;
import com.example.booking.service.RestaurantManagementService;
import com.example.booking.util.CityGeoResolver;

@RestController
@RequestMapping("/debug")
public class DebugController {

    @Autowired
    private VoucherService voucherService;
    
    @Autowired
    private RestaurantManagementService restaurantService;
    
    private final CityGeoResolver cityGeoResolver = new CityGeoResolver();

    @GetMapping("/vouchers")
    public String debugVouchers() {
        try {
            // Get all vouchers for restaurant ID 16
            List<Voucher> allVouchers = voucherService.getVouchersByRestaurant(16);
            
            System.out.println("DEBUG: All vouchers count: " + allVouchers.size());
            for (Voucher v : allVouchers) {
                System.out.println("DEBUG: Voucher - ID: " + v.getVoucherId() + 
                    ", Code: " + v.getCode() + 
                    ", Status: " + v.getStatus() + 
                    ", Restaurant: " + (v.getRestaurant() != null ? v.getRestaurant().getRestaurantId() : "null"));
            }
            
            // Also check all vouchers in database
            List<Voucher> allVouchersInDb = voucherService.getAllVouchers();
            System.out.println("DEBUG: Total vouchers in DB: " + allVouchersInDb.size());
            for (Voucher v : allVouchersInDb) {
                System.out.println("DEBUG: DB Voucher - ID: " + v.getVoucherId() + 
                    ", Code: " + v.getCode() + 
                    ", Status: " + v.getStatus() + 
                    ", Restaurant: " + (v.getRestaurant() != null ? v.getRestaurant().getRestaurantId() : "null"));
            }
            
            return "Found " + allVouchers.size() + " vouchers for restaurant 16, Total in DB: " + allVouchersInDb.size();
        } catch (Exception e) {
            System.out.println("DEBUG ERROR: " + e.getMessage());
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
    
    /**
     * Debug endpoint to check restaurants with coordinates
     * Usage: /debug/restaurants-coords?lat=16.047079&lon=108.206230
     */
    @GetMapping("/restaurants-coords")
    public Map<String, Object> debugRestaurantsCoordinates(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<?> allRestaurants = restaurantService.findAllRestaurants();
            
            int totalRestaurants = allRestaurants.size();
            int withDbCoords = 0;
            int withAddressCoords = 0;
            int withoutCoords = 0;
            
            List<Map<String, Object>> restaurantsInfo = new java.util.ArrayList<>();
            
            for (Object obj : allRestaurants) {
                RestaurantProfile r = (RestaurantProfile) obj;
                Map<String, Object> info = new HashMap<>();
                info.put("id", r.getRestaurantId());
                info.put("name", r.getRestaurantName());
                info.put("address", r.getAddress());
                
                if (r.getLatitude() != null && r.getLongitude() != null) {
                    withDbCoords++;
                    info.put("hasDbCoords", true);
                    info.put("latitude", r.getLatitude());
                    info.put("longitude", r.getLongitude());
                    
                    // Calculate distance if user location is provided
                    if (lat != null && lon != null) {
                        double distance = calculateDistance(lat, lon, 
                            r.getLatitude().doubleValue(), r.getLongitude().doubleValue());
                        info.put("distance", distance);
                    }
                } else {
                    // Try to resolve from address
                    CityGeoResolver.LatLng approx = cityGeoResolver.resolveFromAddress(r.getAddress());
                    if (approx != null) {
                        withAddressCoords++;
                        info.put("hasDbCoords", false);
                        info.put("hasAddressCoords", true);
                        info.put("latitude", approx.lat);
                        info.put("longitude", approx.lng);
                        
                        // Calculate distance if user location is provided
                        if (lat != null && lon != null) {
                            double distance = calculateDistance(lat, lon, approx.lat, approx.lng);
                            info.put("distance", distance);
                        }
                    } else {
                        withoutCoords++;
                        info.put("hasDbCoords", false);
                        info.put("hasAddressCoords", false);
                    }
                }
                
                restaurantsInfo.add(info);
            }
            
            result.put("totalRestaurants", totalRestaurants);
            result.put("withDbCoords", withDbCoords);
            result.put("withAddressCoords", withAddressCoords);
            result.put("withoutCoords", withoutCoords);
            result.put("restaurants", restaurantsInfo);
            
            if (lat != null && lon != null) {
                result.put("userLocation", Map.of("latitude", lat, "longitude", lon));
                result.put("daNangCenter", Map.of("latitude", 16.047079, "longitude", 108.206230));
                double distanceToDaNang = calculateDistance(lat, lon, 16.047079, 108.206230);
                result.put("distanceToDaNang", distanceToDaNang);
            }
            
            return result;
        } catch (Exception e) {
            result.put("error", e.getMessage());
            e.printStackTrace();
            return result;
        }
    }
    
    /**
     * Calculate distance between two coordinates using Haversine formula
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS_KM = 6371;
        
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLatRad = Math.toRadians(lat2 - lat1);
        double deltaLonRad = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(deltaLatRad / 2) * Math.sin(deltaLatRad / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLonRad / 2) * Math.sin(deltaLonRad / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS_KM * c;
    }
}