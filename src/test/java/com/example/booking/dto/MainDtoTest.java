package com.example.booking.dto;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for Main DTOs: DishDto, RestaurantDto, BookingForm, BookingServiceDto, AIActionRequest
 */
class MainDtoTest {

    // ========== DishDto Tests ==========
    @Test
    void testDishDto_DefaultConstructor() {
        DishDto dto = new DishDto();
        assertThat(dto).isNotNull();
    }

    @Test
    void testDishDto_AllArgsConstructor() {
        BigDecimal price = new BigDecimal("150000");
        DishDto dto = new DishDto(1, "Pho Bo", "Vietnamese noodle soup", price, 
                                   "Main Course", "AVAILABLE", 5);

        assertThat(dto.getDishId()).isEqualTo(1);
        assertThat(dto.getName()).isEqualTo("Pho Bo");
        assertThat(dto.getDescription()).isEqualTo("Vietnamese noodle soup");
        assertThat(dto.getPrice()).isEqualByComparingTo(price);
        assertThat(dto.getCategory()).isEqualTo("Main Course");
        assertThat(dto.getStatus()).isEqualTo("AVAILABLE");
        assertThat(dto.getRestaurantId()).isEqualTo(5);
    }

    @Test
    void testDishDto_SettersAndGetters() {
        DishDto dto = new DishDto();
        BigDecimal price = new BigDecimal("200000");

        dto.setDishId(2);
        dto.setName("Bun Cha");
        dto.setDescription("Grilled pork with noodles");
        dto.setPrice(price);
        dto.setCategory("Main Course");
        dto.setStatus("UNAVAILABLE");
        dto.setRestaurantId(6);

        assertThat(dto.getDishId()).isEqualTo(2);
        assertThat(dto.getName()).isEqualTo("Bun Cha");
        assertThat(dto.getDescription()).isEqualTo("Grilled pork with noodles");
        assertThat(dto.getPrice()).isEqualByComparingTo(price);
        assertThat(dto.getCategory()).isEqualTo("Main Course");
        assertThat(dto.getStatus()).isEqualTo("UNAVAILABLE");
        assertThat(dto.getRestaurantId()).isEqualTo(6);
    }

    // ========== RestaurantDto Tests ==========
    @Test
    void testRestaurantDto_DefaultConstructor() {
        RestaurantDto dto = new RestaurantDto();
        assertThat(dto).isNotNull();
    }

    @Test
    void testRestaurantDto_AllArgsConstructor() {
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now().plusHours(1);
        UUID ownerId = UUID.randomUUID();
        BigDecimal averagePrice = new BigDecimal("300000");

        RestaurantDto dto = new RestaurantDto(1, "Test Restaurant", "Test Description",
                                               "123 Test St", "0123456789", "Vietnamese",
                                               "9:00-22:00", averagePrice, "https://test.com",
                                               createdAt, updatedAt, ownerId);

        assertThat(dto.getRestaurantId()).isEqualTo(1);
        assertThat(dto.getRestaurantName()).isEqualTo("Test Restaurant");
        assertThat(dto.getDescription()).isEqualTo("Test Description");
        assertThat(dto.getAddress()).isEqualTo("123 Test St");
        assertThat(dto.getPhone()).isEqualTo("0123456789");
        assertThat(dto.getCuisineType()).isEqualTo("Vietnamese");
        assertThat(dto.getOpeningHours()).isEqualTo("9:00-22:00");
        assertThat(dto.getAveragePrice()).isEqualByComparingTo(averagePrice);
        assertThat(dto.getWebsiteUrl()).isEqualTo("https://test.com");
        assertThat(dto.getCreatedAt()).isEqualTo(createdAt);
        assertThat(dto.getUpdatedAt()).isEqualTo(updatedAt);
        assertThat(dto.getOwnerId()).isEqualTo(ownerId);
    }

    @Test
    void testRestaurantDto_SettersAndGetters() {
        RestaurantDto dto = new RestaurantDto();
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now().plusHours(2);
        UUID ownerId = UUID.randomUUID();
        BigDecimal averagePrice = new BigDecimal("250000");

        dto.setRestaurantId(2);
        dto.setRestaurantName("Another Restaurant");
        dto.setDescription("Another Description");
        dto.setAddress("456 Another St");
        dto.setPhone("0987654321");
        dto.setCuisineType("Italian");
        dto.setOpeningHours("10:00-23:00");
        dto.setAveragePrice(averagePrice);
        dto.setWebsiteUrl("https://another.com");
        dto.setCreatedAt(createdAt);
        dto.setUpdatedAt(updatedAt);
        dto.setOwnerId(ownerId);

        assertThat(dto.getRestaurantId()).isEqualTo(2);
        assertThat(dto.getRestaurantName()).isEqualTo("Another Restaurant");
        assertThat(dto.getDescription()).isEqualTo("Another Description");
        assertThat(dto.getAddress()).isEqualTo("456 Another St");
        assertThat(dto.getPhone()).isEqualTo("0987654321");
        assertThat(dto.getCuisineType()).isEqualTo("Italian");
        assertThat(dto.getOpeningHours()).isEqualTo("10:00-23:00");
        assertThat(dto.getAveragePrice()).isEqualByComparingTo(averagePrice);
        assertThat(dto.getWebsiteUrl()).isEqualTo("https://another.com");
        assertThat(dto.getCreatedAt()).isEqualTo(createdAt);
        assertThat(dto.getUpdatedAt()).isEqualTo(updatedAt);
        assertThat(dto.getOwnerId()).isEqualTo(ownerId);
    }

    // ========== BookingForm Tests ==========
    @Test
    void testBookingForm_DefaultConstructor() {
        BookingForm form = new BookingForm();
        assertThat(form).isNotNull();
        assertThat(form.getDepositAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(form.getVoucherDiscountAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void testBookingForm_Constructor() {
        LocalDateTime bookingTime = LocalDateTime.now().plusDays(1);
        BigDecimal depositAmount = new BigDecimal("100000");

        BookingForm form = new BookingForm(1, 5, 4, bookingTime, depositAmount, "Test note");

        assertThat(form.getRestaurantId()).isEqualTo(1);
        assertThat(form.getTableId()).isEqualTo(5);
        assertThat(form.getGuestCount()).isEqualTo(4);
        assertThat(form.getBookingTime()).isEqualTo(bookingTime);
        assertThat(form.getDepositAmount()).isEqualByComparingTo(depositAmount);
        assertThat(form.getNote()).isEqualTo("Test note");
    }

    @Test
    void testBookingForm_ConstructorWithNullDeposit() {
        LocalDateTime bookingTime = LocalDateTime.now().plusDays(1);
        BookingForm form = new BookingForm(1, 5, 4, bookingTime, null, "Test note");

        assertThat(form.getDepositAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void testBookingForm_SettersAndGetters() {
        BookingForm form = new BookingForm();
        LocalDateTime bookingTime = LocalDateTime.now().plusDays(2);
        BigDecimal depositAmount = new BigDecimal("200000");
        BigDecimal voucherDiscount = new BigDecimal("50000");

        form.setRestaurantId(3);
        form.setTableId(7);
        form.setTableIds("1,2,3");
        form.setGuestCount(6);
        form.setBookingTime(bookingTime);
        form.setDepositAmount(depositAmount);
        form.setNote("Special request");
        form.setVoucherCode("DISCOUNT10");
        form.setVoucherCodeApplied("DISCOUNT10");
        form.setVoucherDiscountAmount(voucherDiscount);
        form.setDishIds("1:2,2:3");
        form.setServiceIds("1,2,3");

        assertThat(form.getRestaurantId()).isEqualTo(3);
        assertThat(form.getTableId()).isEqualTo(7);
        assertThat(form.getTableIds()).isEqualTo("1,2,3");
        assertThat(form.getGuestCount()).isEqualTo(6);
        assertThat(form.getBookingTime()).isEqualTo(bookingTime);
        assertThat(form.getDepositAmount()).isEqualByComparingTo(depositAmount);
        assertThat(form.getNote()).isEqualTo("Special request");
        assertThat(form.getVoucherCode()).isEqualTo("DISCOUNT10");
        assertThat(form.getVoucherCodeApplied()).isEqualTo("DISCOUNT10");
        assertThat(form.getVoucherDiscountAmount()).isEqualByComparingTo(voucherDiscount);
        assertThat(form.getDishIds()).isEqualTo("1:2,2:3");
        assertThat(form.getServiceIds()).isEqualTo("1,2,3");
    }

    // ========== BookingServiceDto Tests ==========
    @Test
    void testBookingServiceDto_DefaultConstructor() {
        BookingServiceDto dto = new BookingServiceDto();
        assertThat(dto).isNotNull();
    }

    @Test
    void testBookingServiceDto_AllArgsConstructor() {
        BigDecimal price = new BigDecimal("50000");
        BigDecimal totalPrice = new BigDecimal("100000");

        BookingServiceDto dto = new BookingServiceDto(1, "Table Service", 
                                                      "Dedicated table service", 2, 
                                                      price, totalPrice, "Service");

        assertThat(dto.getServiceId()).isEqualTo(1);
        assertThat(dto.getServiceName()).isEqualTo("Table Service");
        assertThat(dto.getDescription()).isEqualTo("Dedicated table service");
        assertThat(dto.getQuantity()).isEqualTo(2);
        assertThat(dto.getPrice()).isEqualByComparingTo(price);
        assertThat(dto.getTotalPrice()).isEqualByComparingTo(totalPrice);
        assertThat(dto.getCategory()).isEqualTo("Service");
    }

    @Test
    void testBookingServiceDto_SettersAndGetters() {
        BookingServiceDto dto = new BookingServiceDto();
        BigDecimal price = new BigDecimal("75000");
        BigDecimal totalPrice = new BigDecimal("150000");

        dto.setServiceId(2);
        dto.setServiceName("Premium Service");
        dto.setDescription("Premium table service");
        dto.setQuantity(2);
        dto.setPrice(price);
        dto.setTotalPrice(totalPrice);
        dto.setCategory("Premium");

        assertThat(dto.getServiceId()).isEqualTo(2);
        assertThat(dto.getServiceName()).isEqualTo("Premium Service");
        assertThat(dto.getDescription()).isEqualTo("Premium table service");
        assertThat(dto.getQuantity()).isEqualTo(2);
        assertThat(dto.getPrice()).isEqualByComparingTo(price);
        assertThat(dto.getTotalPrice()).isEqualByComparingTo(totalPrice);
        assertThat(dto.getCategory()).isEqualTo("Premium");
    }

    @Test
    void testBookingServiceDto_FormattedPrice() {
        BigDecimal price = new BigDecimal("50000");
        BigDecimal totalPrice = new BigDecimal("100000");
        BookingServiceDto dto = new BookingServiceDto(1, "Service", "Description", 
                                                      1, price, totalPrice, "Category");

        String formattedPrice = dto.getFormattedPrice();
        String formattedTotalPrice = dto.getFormattedTotalPrice();

        assertThat(formattedPrice).contains("50,000");
        assertThat(formattedPrice).contains("VND");
        assertThat(formattedTotalPrice).contains("100,000");
        assertThat(formattedTotalPrice).contains("VND");
    }

    // ========== AIActionRequest Tests ==========
    @Test
    void testAIActionRequest_DefaultConstructor() {
        AIActionRequest request = new AIActionRequest();
        assertThat(request).isNotNull();
    }

    @Test
    void testAIActionRequest_Constructor() {
        Map<String, Object> data = new HashMap<>();
        data.put("restaurantId", 1);
        data.put("guestCount", 4);

        AIActionRequest request = new AIActionRequest("BOOK_TABLE", data);

        assertThat(request.getIntent()).isEqualTo("BOOK_TABLE");
        assertThat(request.getData()).isEqualTo(data);
        assertThat(request.getData().get("restaurantId")).isEqualTo(1);
        assertThat(request.getData().get("guestCount")).isEqualTo(4);
    }

    @Test
    void testAIActionRequest_SettersAndGetters() {
        AIActionRequest request = new AIActionRequest();
        Map<String, Object> data = new HashMap<>();
        data.put("bookingId", 10);
        data.put("status", "CONFIRMED");

        request.setIntent("UPDATE_BOOKING");
        request.setData(data);

        assertThat(request.getIntent()).isEqualTo("UPDATE_BOOKING");
        assertThat(request.getData()).isEqualTo(data);
        assertThat(request.getData().get("bookingId")).isEqualTo(10);
        assertThat(request.getData().get("status")).isEqualTo("CONFIRMED");
    }

    @Test
    void testAIActionRequest_ToString() {
        Map<String, Object> data = new HashMap<>();
        data.put("test", "value");

        AIActionRequest request = new AIActionRequest("TEST_INTENT", data);
        String toString = request.toString();

        assertThat(toString).contains("AIActionRequest");
        assertThat(toString).contains("TEST_INTENT");
        assertThat(toString).contains("data");
    }

    @Test
    void testAIActionRequest_NullData() {
        AIActionRequest request = new AIActionRequest();
        request.setIntent("TEST");
        request.setData(null);

        assertThat(request.getIntent()).isEqualTo("TEST");
        assertThat(request.getData()).isNull();
    }
}

