package com.example.booking.web.controller.api;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.booking.domain.Booking;
import com.example.booking.domain.Customer;
import com.example.booking.domain.RestaurantTable;
import com.example.booking.dto.RestaurantServiceDto;
import com.example.booking.domain.RestaurantService;
import com.example.booking.domain.User;
import com.example.booking.dto.BookingDetailsDto;
import com.example.booking.dto.BookingDishDto;
import com.example.booking.dto.BookingServiceDto;
import com.example.booking.dto.DishWithImageDto;
import com.example.booking.service.RestaurantManagementService;
import com.example.booking.service.SimpleUserService;
import com.example.booking.util.CityGeoResolver;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.HashMap;
import com.example.booking.service.BookingService;
import com.example.booking.service.CustomerService;
import com.example.booking.domain.RestaurantMedia;
import com.example.booking.dto.RestaurantDto;
import com.example.booking.dto.NearbyRestaurantDto;
import com.example.booking.util.GeoUtils;

@RestController
@RequestMapping("/api/booking")
public class BookingApiController {
    
    @Autowired
    private RestaurantManagementService restaurantService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private SimpleUserService userService;
    
    @Autowired
    private org.springframework.web.client.RestTemplate restTemplate;

    private CityGeoResolver cityGeoResolver;
    
    @PostConstruct
    private void initCityGeoResolver() {
        this.cityGeoResolver = new CityGeoResolver(restTemplate);
    }
    
    /**
     * API endpoint để lấy table layouts theo nhà hàng
     */
    @GetMapping("/restaurants/{restaurantId}/table-layouts")
    public ResponseEntity<List<Map<String, Object>>> getTableLayoutsByRestaurant(
            @PathVariable("restaurantId") Integer restaurantId) {
        try {
            System.out.println("🔍 API: Getting table layouts for restaurant ID: " + restaurantId);

            List<RestaurantMedia> layouts = restaurantService.findMediaByRestaurantAndType(restaurantId,
                    "table_layout");

            System.out.println("✅ API: Found " + layouts.size() + " table layouts");

            // Convert to Map to avoid lazy loading issues
            List<Map<String, Object>> layoutMaps = layouts.stream()
                    .map(layout -> {
                        Map<String, Object> layoutMap = new HashMap<>();
                        layoutMap.put("mediaId", layout.getMediaId());
                        layoutMap.put("url", layout.getUrl());
                        layoutMap.put("type", layout.getType());
                        layoutMap.put("createdAt", layout.getCreatedAt());
                        return layoutMap;
                    })
                    .collect(Collectors.toList());

            System.out.println("✅ API: Returning " + layoutMaps.size() + " layout maps");

            return ResponseEntity.ok(layoutMaps);
        } catch (Exception e) {
            System.err.println("❌ API Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * API endpoint để lấy danh sách bàn theo nhà hàng
     */
    @GetMapping("/restaurants/{restaurantId}/tables")
    public ResponseEntity<List<Map<String, Object>>> getTablesByRestaurant(
            @PathVariable("restaurantId") Integer restaurantId) {
        try {
            System.out.println("🔍 API: Getting tables for restaurant ID: " + restaurantId);

            List<RestaurantTable> tables = restaurantService.findTablesByRestaurant(restaurantId);

            System.out.println("✅ API: Found " + tables.size() + " tables");

            List<Map<String, Object>> tableMaps = new ArrayList<>();
            for (RestaurantTable table : tables) {
                Map<String, Object> tableMap = new HashMap<>();
                tableMap.put("tableId", table.getTableId());
                tableMap.put("tableName", table.getTableName());
                tableMap.put("capacity", table.getCapacity());
                tableMap.put("status", table.getStatus());
                tableMap.put("depositAmount", table.getDepositAmount());
                tableMap.put("restaurantId", table.getRestaurant().getRestaurantId());

                try {
                    String mainImage = table.getMainTableImage();
                    List<Map<String, Object>> images = table.getTableImages().stream()
                            .map(img -> {
                                Map<String, Object> imgMap = new HashMap<>();
                                imgMap.put("url", img.getUrl());
                                imgMap.put("mediaId", img.getMediaId());
                                return imgMap;
                            })
                            .collect(Collectors.toList());

                    tableMap.put("mainImage", mainImage);
                    tableMap.put("images", images);
                } catch (Exception mediaEx) {
                    System.err.println("⚠️ Unable to load images for table ID "
                            + table.getTableId() + ": " + mediaEx.getMessage());
                    tableMap.put("mainImage", null);
                    tableMap.put("images", java.util.Collections.emptyList());
                }

                tableMaps.add(tableMap);
            }

            System.out.println("✅ API: Returning " + tableMaps.size() + " table maps");

            return ResponseEntity.ok(tableMaps);
        } catch (Exception e) {
            System.err.println("❌ API Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * API endpoint để lấy logo của nhà hàng
     */
    @GetMapping("/restaurants/{restaurantId}/logo")
    public ResponseEntity<Map<String, String>> getRestaurantLogo(
            @PathVariable("restaurantId") Integer restaurantId) {
        try {
            List<RestaurantMedia> logos = restaurantService.findMediaByRestaurantAndType(restaurantId, "logo");
            
            Map<String, String> response = new HashMap<>();
            if (!logos.isEmpty()) {
                response.put("logoUrl", logos.get(0).getUrl());
            } else {
                response.put("logoUrl", null);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("❌ API Error getting logo: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * API endpoint để lấy thông tin nhà hàng
     */
    @GetMapping("/restaurants/{restaurantId}")
    public ResponseEntity<RestaurantDto> getRestaurant(@PathVariable("restaurantId") Integer restaurantId) {
        try {
            return restaurantService.findRestaurantById(restaurantId)
                    .map(restaurant -> {
                        RestaurantDto dto = new RestaurantDto(
                                restaurant.getRestaurantId(),
                                restaurant.getRestaurantName(),
                                restaurant.getDescription(),
                                restaurant.getAddress(),
                                restaurant.getPhone(),
                                restaurant.getCuisineType(),
                                restaurant.getOpeningHours(),
                                restaurant.getAveragePrice(),
                                restaurant.getWebsiteUrl(),
                                restaurant.getCreatedAt(),
                                restaurant.getUpdatedAt(),
                                restaurant.getOwner() != null ? restaurant.getOwner().getOwnerId() : null);
                        return ResponseEntity.ok(dto);
                    })
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            System.err.println("❌ API Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * API endpoint để lấy danh sách nhà hàng
     */
    @GetMapping("/restaurants")
    public ResponseEntity<List<RestaurantDto>> getAllRestaurants() {
        try {
            List<?> restaurants = restaurantService.findAllRestaurants();

            // Convert to DTO to avoid Hibernate proxy issues
            List<RestaurantDto> restaurantDtos = restaurants.stream()
                    .map(restaurant -> {
                        // Cast to RestaurantProfile (assuming that's the type)
                        var restaurantProfile = (com.example.booking.domain.RestaurantProfile) restaurant;
                        return new RestaurantDto(
                                restaurantProfile.getRestaurantId(),
                                restaurantProfile.getRestaurantName(),
                                restaurantProfile.getDescription(),
                                restaurantProfile.getAddress(),
                                restaurantProfile.getPhone(),
                                restaurantProfile.getCuisineType(),
                                restaurantProfile.getOpeningHours(),
                                restaurantProfile.getAveragePrice(),
                                restaurantProfile.getWebsiteUrl(),
                                restaurantProfile.getCreatedAt(),
                                restaurantProfile.getUpdatedAt(),
                                restaurantProfile.getOwner() != null ? restaurantProfile.getOwner().getOwnerId()
                                        : null);
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(restaurantDtos);
        } catch (Exception e) {
            System.err.println("❌ API Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * API: Get restaurants near a given location (approximate, city-level)
     * Example: /api/booking/restaurants/nearby?lat=10.77&lng=106.70&radius=3000&limit=10
     * radius in meters (default 3000m). Uses city-center approximation derived from address.
     */
    @GetMapping("/restaurants/nearby")
    public ResponseEntity<List<NearbyRestaurantDto>> getNearbyRestaurants(
            @org.springframework.web.bind.annotation.RequestParam("lat") double lat,
            @org.springframework.web.bind.annotation.RequestParam("lng") double lng,
            @org.springframework.web.bind.annotation.RequestParam(value = "radius", required = false, defaultValue = "3000") int radiusMeters,
            @org.springframework.web.bind.annotation.RequestParam(value = "limit", required = false, defaultValue = "10") int limit
    ) {
        try {
            List<?> restaurants = restaurantService.findAllRestaurants();

            double radiusKm = Math.max(0, radiusMeters) / 1000.0;

            List<NearbyRestaurantDto> results = restaurants.stream()
                .map(r -> (com.example.booking.domain.RestaurantProfile) r)
                .map(r -> {
                    CityGeoResolver.LatLng approx = cityGeoResolver.resolveFromAddress(r.getAddress());
                    if (approx == null) return null; // skip unknown city in MVP
                    double distKm = GeoUtils.haversineKm(lat, lng, approx.lat, approx.lng);
                    return new NearbyRestaurantDto(
                        r.getRestaurantId(),
                        r.getRestaurantName(),
                        r.getAddress(),
                        r.getCuisineType(),
                        r.getAveragePrice(),
                        r.getMainImageUrl(),
                        distKm,
                        r.getCreatedAt()
                    );
                })
                .filter(java.util.Objects::nonNull)
                .filter(d -> d.getDistanceKm() <= radiusKm)
                .sorted(java.util.Comparator.comparingDouble(NearbyRestaurantDto::getDistanceKm))
                .limit(Math.max(1, limit))
                .collect(java.util.stream.Collectors.toList());

            return ResponseEntity.ok(results);
        } catch (Exception e) {
            System.err.println("\uFFFD?O API Error (nearby): " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * API endpoint để lấy danh sách món ăn theo nhà hàng (với ảnh)
     */
    @GetMapping("/restaurants/{restaurantId}/dishes")
    public ResponseEntity<List<DishWithImageDto>> getDishesByRestaurant(
            @PathVariable("restaurantId") Integer restaurantId) {
        try {
            System.out.println("🔍 API: Getting dishes with images for restaurant ID: " + restaurantId);

            List<DishWithImageDto> dishesWithImages = restaurantService.getDishesByRestaurantWithImages(restaurantId);
            System.out.println("✅ API: Found " + dishesWithImages.size() + " dishes with images");

            System.out.println("✅ API: Returning " + dishesWithImages.size() + " dish DTOs with images");
            return ResponseEntity.ok(dishesWithImages);
        } catch (Exception e) {
            System.err.println("❌ API Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * API endpoint để lấy danh sách dịch vụ theo nhà hàng
     */
    @GetMapping("/restaurants/{restaurantId}/services")
    public ResponseEntity<List<RestaurantServiceDto>> getServicesByRestaurant(
            @PathVariable("restaurantId") Integer restaurantId) {
        try {
            System.out.println("🔍 API: Getting services for restaurant ID: " + restaurantId);

            List<RestaurantService> services = restaurantService.findServicesByRestaurant(restaurantId);
            System.out.println("✅ API: Found " + services.size() + " services");

            // Convert to DTO to avoid Hibernate proxy issues
            List<RestaurantServiceDto> serviceDtos = services.stream()
                    .map(service -> new RestaurantServiceDto(
                            service.getServiceId(),
                            service.getName(),
                            service.getCategory(),
                            service.getDescription(),
                            service.getPrice(),
                            service.getStatus() != null ? service.getStatus().toString() : "UNKNOWN",
                            service.getRestaurant() != null ? service.getRestaurant().getRestaurantId() : null))
                    .collect(Collectors.toList());

            System.out.println("✅ API: Returning " + serviceDtos.size() + " service DTOs");
            return ResponseEntity.ok(serviceDtos);
        } catch (Exception e) {
            System.err.println("❌ API Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * API endpoint để lấy chi tiết booking
     */
    @GetMapping("/{bookingId}/details")
    public ResponseEntity<BookingDetailsDto> getBookingDetails(
            @PathVariable("bookingId") Integer bookingId,
            Authentication authentication) {
        try {
            UUID customerId = getCurrentCustomerId(authentication);
            Optional<Booking> bookingOpt = bookingService.findBookingById(bookingId);

            if (bookingOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Booking booking = bookingOpt.get();

            // Validate ownership
            if (!booking.getCustomer().getCustomerId().equals(customerId)) {
                return ResponseEntity.status(403).build();
            }

            // Calculate total amount
            BigDecimal totalAmount = bookingService.calculateTotalAmount(booking);

            // Create BookingDetailsDto
            BookingDetailsDto detailsDto = new BookingDetailsDto();
            detailsDto.setBookingId(booking.getBookingId());
            detailsDto.setRestaurantName(booking.getRestaurant().getRestaurantName());

            // Get all table names and calculate table fees total
            BigDecimal tableFeesTotal = BigDecimal.ZERO;
            if (booking.getBookingTables() != null && !booking.getBookingTables().isEmpty()) {
                // Remove duplicates by using distinct() and preserve order
                List<String> tableNames = booking.getBookingTables().stream()
                        .map(bt -> bt.getTable().getTableName())
                        .distinct()
                        .collect(Collectors.toList());
                detailsDto.setTableNames(tableNames);

                // Set first table name for backward compatibility
                detailsDto.setTableName(tableNames.get(0));
                
                // Calculate total table fees
                tableFeesTotal = booking.getBookingTables().stream()
                        .map(bt -> bt.getTableFee() != null ? bt.getTableFee() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
            }
            detailsDto.setTableFeesTotal(tableFeesTotal);

            // Get dishes
            if (booking.getBookingDishes() != null && !booking.getBookingDishes().isEmpty()) {
                List<BookingDishDto> dishDtos = booking.getBookingDishes().stream()
                        .map(bd -> {
                            BookingDishDto dto = new BookingDishDto();
                            dto.setDishId(bd.getDish().getDishId());
                            dto.setDishName(bd.getDish().getName());
                            dto.setDescription(bd.getDish().getDescription());
                            dto.setQuantity(bd.getQuantity());
                            dto.setPrice(bd.getDish().getPrice());
                            dto.setTotalPrice(bd.getDish().getPrice().multiply(BigDecimal.valueOf(bd.getQuantity())));
                            dto.setCategory(bd.getDish().getCategory());
                            return dto;
                        })
                        .collect(Collectors.toList());
                detailsDto.setDishes(dishDtos);
            }

            // Get services
            if (booking.getBookingServices() != null && !booking.getBookingServices().isEmpty()) {
                List<BookingServiceDto> serviceDtos = booking.getBookingServices().stream()
                        .map(bs -> {
                            BookingServiceDto dto = new BookingServiceDto();
                            dto.setServiceId(bs.getService().getServiceId());
                            dto.setServiceName(bs.getService().getName());
                            dto.setDescription(bs.getService().getDescription());
                            dto.setQuantity(1); // Services typically have quantity 1
                            dto.setPrice(bs.getService().getPrice());
                            dto.setTotalPrice(bs.getService().getPrice());
                            dto.setCategory(bs.getService().getCategory());
                            return dto;
                        })
                        .collect(Collectors.toList());
                detailsDto.setServices(serviceDtos);
            }

            detailsDto.setBookingTime(booking.getBookingTime());
            detailsDto.setGuestCount(booking.getNumberOfGuests());
            detailsDto.setDepositAmount(booking.getDepositAmount());
            detailsDto.setTotalAmount(totalAmount);
            detailsDto.setStatus(booking.getStatus());
            detailsDto.setCreatedAt(booking.getCreatedAt());
            detailsDto.setUpdatedAt(booking.getUpdatedAt());

            return ResponseEntity.ok(detailsDto);

        } catch (Exception e) {
            System.err.println("❌ Error getting booking details: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Helper method to get current customer ID from authentication
     */
    private UUID getCurrentCustomerId(Authentication authentication) {
        System.out.println("🔍 getCurrentCustomerId called");
        String username = authentication.getName();
        System.out.println("   Username: " + username);

        // Tìm customer theo username
        System.out.println("🔍 Looking for customer by username...");
        Optional<Customer> customerOpt = customerService.findByUsername(username);

        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            System.out.println("✅ Customer found: " + customer.getCustomerId());
            return customer.getCustomerId();
        }

        // Nếu chưa có Customer record, tạo mới
        System.out.println("ℹ️ Customer not found, creating new customer...");
        // Lấy User từ authentication - xử lý cả User và OAuth2User
        User user = getUserFromAuthentication(authentication);
        System.out.println("✅ User found: " + user.getUsername());

        // Tạo Customer mới
        System.out.println("🔍 Creating new customer...");
        Customer customer = new Customer(user);
        // updatedAt sẽ được set tự động bởi @PrePersist
        System.out.println("🔍 Saving new customer...");
        customer = customerService.save(customer);

        System.out.println("✅ Created new Customer record for user: " + username);
        return customer.getCustomerId();
    }

    /**
     * Helper method để lấy User từ authentication (xử lý cả User và OAuth2User)
     */
    private User getUserFromAuthentication(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        // Nếu là User object trực tiếp (regular login)
        if (principal instanceof User) {
            return (User) principal;
        }

        // Nếu là OAuth2User hoặc OidcUser (OAuth2 login)
        if (principal instanceof org.springframework.security.oauth2.core.user.OAuth2User) {
            String username = authentication.getName(); // username = email cho OAuth users

            // Thử tìm User trực tiếp từ UserService
            // Vì OAuth users có username = email, và UserService có thể tìm theo username
            try {
                User user = (User) userService.loadUserByUsername(username);
                return user;
            } catch (Exception e) {
                throw new RuntimeException("User not found for OAuth username: " + username +
                        ". Error: " + e.getMessage());
            }
        }

        throw new RuntimeException(
                "Unsupported authentication principal type: " + principal.getClass().getSimpleName());
    }
}
