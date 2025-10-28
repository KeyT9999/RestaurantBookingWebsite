package com.example.booking.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Resolve approximate coordinates for a restaurant based on its address.
 * This MVP maps known Vietnamese cities to city-center coordinates and
 * detects them by simple substring matching in the address.
 */
public class CityGeoResolver {
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

    /**
     * Try to extract city coordinates from a freeform address string.
     * Returns null if no known city is detected.
     */
    public LatLng resolveFromAddress(String address) {
        if (address == null || address.isBlank()) return null;
        String norm = normalize(address);
        for (Map.Entry<String, LatLng> e : CITY_COORDS.entrySet()) {
            if (norm.contains(e.getKey())) {
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

