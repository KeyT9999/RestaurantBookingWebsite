package com.example.booking.web.controller.api;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import com.example.booking.config.TestRateLimitingConfig;
import com.example.booking.domain.Customer;
import com.example.booking.domain.User;
import com.example.booking.domain.Waitlist;
import com.example.booking.dto.AvailabilityCheckResponse;
import com.example.booking.dto.AvailabilityCheckResponse.WaitlistInfo;
import com.example.booking.dto.WaitlistDetailDto;
import com.example.booking.service.CustomerService;
import com.example.booking.service.SmartWaitlistService;
import com.example.booking.service.WaitlistService;

@WebMvcTest(SmartWaitlistApiController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(TestRateLimitingConfig.class)
class SmartWaitlistApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SmartWaitlistService smartWaitlistService;

    @MockBean
    private WaitlistService waitlistService;

    @MockBean
    private CustomerService customerService;

    @Test
    @DisplayName("checkAvailability should call general availability when no table ids provided")
    void shouldCheckGeneralAvailabilityWhenNoSpecificTables() throws Exception {
        AvailabilityCheckResponse response = new AvailabilityCheckResponse(false, "NONE");
        response.setWaitlistInfo(new WaitlistInfo(true, LocalDateTime.now().plusMinutes(20), "Available"));
        when(smartWaitlistService.checkGeneralAvailability(eq(42), any(LocalDateTime.class), eq(4)))
                .thenReturn(response);

        mockMvc.perform(get("/api/booking/availability-check")
                        .param("restaurantId", "42")
                        .param("bookingTime", "2024-05-19T18:30")
                        .param("guestCount", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasConflict", is(false)))
                .andExpect(jsonPath("$.waitlistInfo.canJoinWaitlist", is(true)));

        verify(smartWaitlistService)
                .checkGeneralAvailability(eq(42), any(LocalDateTime.class), eq(4));
    }

    @Test
    @DisplayName("checkAvailability should delegate to specific table path when ids provided")
    void shouldCheckSpecificTablesWhenIdsProvided() throws Exception {
        AvailabilityCheckResponse response = new AvailabilityCheckResponse(true, "SPECIFIC_TABLE");
        when(smartWaitlistService.checkSpecificTables(eq("1,2"), any(LocalDateTime.class), eq(6)))
                .thenReturn(response);

        mockMvc.perform(get("/api/booking/availability-check")
                        .param("restaurantId", "7")
                        .param("bookingTime", "2024-05-19T20:30")
                        .param("guestCount", "6")
                        .param("selectedTableIds", "1,2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conflictType", equalTo("SPECIFIC_TABLE")));

        verify(smartWaitlistService)
                .checkSpecificTables(eq("1,2"), any(LocalDateTime.class), eq(6));
    }

    @Test
    @DisplayName("checkAvailability should return 400 for invalid booking time formats")
    void shouldReturnBadRequestForInvalidBookingTime() throws Exception {
        mockMvc.perform(get("/api/booking/availability-check")
                        .param("restaurantId", "7")
                        .param("bookingTime", "not-a-date")
                        .param("guestCount", "4"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.hasConflict", is(false)));
    }

    @Test
    @DisplayName("joinWaitlist should persist via waitlist service when customer exists")
    @WithMockUser(username = "customer@test.com", roles = "CUSTOMER")
    void shouldJoinWaitlistWhenCustomerFound() throws Exception {
        String username = "customer@test.com";
        UUID customerId = UUID.randomUUID();

        User user = new User();
        user.setUsername(username);
        user.setEmail(username);
        user.setPassword("password");
        user.setFullName("Customer Test");

        Customer customer = new Customer();
        customer.setCustomerId(customerId);
        customer.setUser(user);

        Waitlist waitlist = new Waitlist();
        waitlist.setWaitlistId(99);
        waitlist.setEstimatedWaitTime(25);

        when(customerService.findByUsername(username)).thenReturn(Optional.of(customer));
        when(waitlistService.addToWaitlistWithDetails(eq(15), eq(3), eq(customerId),
                eq("1,2"), eq("3,4"), eq("5"), any(LocalDateTime.class)))
                .thenReturn(waitlist);

        mockMvc.perform(post("/api/booking/join-waitlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "restaurantId": 15,
                                  "guestCount": 3,
                                  "preferredBookingTime": "2024-05-20T19:00:00",
                                  "dishIds": "1,2",
                                  "serviceIds": "3,4",
                                  "tableIds": "5"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.waitlistId", is(99)))
                .andExpect(jsonPath("$.estimatedWaitTime", is(25)));

        verify(waitlistService).addToWaitlistWithDetails(eq(15), eq(3), eq(customerId),
                eq("1,2"), eq("3,4"), eq("5"), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("joinWaitlist should return 400 when customer lookup fails")
    @WithMockUser(username = "ghost@test.com", roles = "CUSTOMER")
    void shouldReturnBadRequestWhenCustomerMissing() throws Exception {
        when(customerService.findByUsername("ghost@test.com")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/booking/join-waitlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "restaurantId": 15,
                                  "guestCount": 2
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Customer not found")));
    }

    @Test
    @DisplayName("getWaitlistDetails should return details when authentication present")
    @WithMockUser(username = "owner@test.com", roles = "CUSTOMER")
    void shouldReturnWaitlistDetails() throws Exception {
        WaitlistDetailDto dto = new WaitlistDetailDto();
        dto.setWaitlistId(321);
        dto.setCustomerName("Jane");
        dto.setStatus("WAITING");
        dto.setEstimatedWaitTime(30);
        when(waitlistService.getWaitlistDetails(321)).thenReturn(dto);

        mockMvc.perform(get("/api/booking/waitlist/321/details"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.waitlistId", is(321)))
                .andExpect(jsonPath("$.status", is("WAITING")));
    }

    @Test
    @DisplayName("getWaitlistDetails should fail when authentication missing")
    void shouldRejectWaitlistDetailsWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/booking/waitlist/55/details"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Authentication required")));
    }

    @TestConfiguration
    static class NoSecurityConfig {
        @Bean
        SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authz -> authz.anyRequest().permitAll())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());
            return http.build();
        }
    }
}

