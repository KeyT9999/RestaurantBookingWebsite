package com.example.booking.web.controller.api;

import com.example.booking.domain.Customer;
import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.domain.Waitlist;
import com.example.booking.dto.AvailabilityCheckResponse;
import com.example.booking.dto.WaitlistDetailDto;
import com.example.booking.service.CustomerService;
import com.example.booking.service.SmartWaitlistService;
import com.example.booking.service.WaitlistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SmartWaitlistApiController.class)
class SmartWaitlistApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SmartWaitlistService smartWaitlistService;

    @MockBean
    private WaitlistService waitlistService;

    @MockBean
    private CustomerService customerService;

    private AvailabilityCheckResponse availabilityResponse;
    private Waitlist waitlist;
    private WaitlistDetailDto waitlistDetail;
    private User customerUser;
    private Customer customer;

    @BeforeEach
    void setUp() {
        customerUser = new User();
        customerUser.setId(UUID.randomUUID());
        customerUser.setUsername("testuser");
        customerUser.setEmail("test@example.com");
        customerUser.setRole(UserRole.CUSTOMER);

        customer = new Customer();
        customer.setCustomerId(UUID.randomUUID());
        customer.setUser(customerUser);

        availabilityResponse = new AvailabilityCheckResponse();
        availabilityResponse.setHasConflict(false);
        availabilityResponse.setConflictType(null);

        waitlist = new Waitlist();
        waitlist.setWaitlistId(1);
        waitlist.setPartySize(4);
        waitlist.setEstimatedWaitTime(15);

        waitlistDetail = new WaitlistDetailDto();
        waitlistDetail.setWaitlistId(1);
        waitlistDetail.setPartySize(4);
    }

    @Test
    @WithMockUser(username = "testuser", roles = "CUSTOMER")
    void shouldCheckAvailability_General() throws Exception {
        when(smartWaitlistService.checkGeneralAvailability(any(Integer.class), any(LocalDateTime.class), any(Integer.class)))
                .thenReturn(availabilityResponse);

        mockMvc.perform(get("/api/booking/availability-check")
                        .param("restaurantId", "1")
                        .param("bookingTime", "2024-12-31T19:00:00")
                        .param("guestCount", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasConflict").value(false));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "CUSTOMER")
    void shouldCheckAvailability_SpecificTables() throws Exception {
        when(smartWaitlistService.checkSpecificTables(anyString(), any(LocalDateTime.class), any(Integer.class)))
                .thenReturn(availabilityResponse);

        mockMvc.perform(get("/api/booking/availability-check")
                        .param("restaurantId", "1")
                        .param("bookingTime", "2024-12-31T19:00:00")
                        .param("guestCount", "4")
                        .param("selectedTableIds", "1,2,3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasConflict").value(false));

        verify(smartWaitlistService).checkSpecificTables(eq("1,2,3"), any(LocalDateTime.class), eq(4));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "CUSTOMER")
    void shouldCheckAvailability_WithURLEncodedTime() throws Exception {
        when(smartWaitlistService.checkGeneralAvailability(any(Integer.class), any(LocalDateTime.class), any(Integer.class)))
                .thenReturn(availabilityResponse);

        mockMvc.perform(get("/api/booking/availability-check")
                        .param("restaurantId", "1")
                        .param("bookingTime", "2024-12-31+19:00:00")
                        .param("guestCount", "4"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "CUSTOMER")
    void shouldCheckAvailability_InvalidTimeFormat() throws Exception {
        mockMvc.perform(get("/api/booking/availability-check")
                        .param("restaurantId", "1")
                        .param("bookingTime", "invalid-time")
                        .param("guestCount", "4"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "CUSTOMER")
    void shouldJoinWaitlist() throws Exception {
        when(customerService.findByUsername("testuser")).thenReturn(Optional.of(customer));
        when(waitlistService.addToWaitlistWithDetails(
                anyInt(), anyInt(), any(UUID.class), anyString(), anyString(), 
                anyString(), any(LocalDateTime.class))).thenReturn(waitlist);

        mockMvc.perform(post("/api/booking/join-waitlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"restaurantId\":1,\"guestCount\":4,\"preferredBookingTime\":\"2024-12-31T19:00:00\"}")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.waitlistId").value(1));

        verify(waitlistService).addToWaitlistWithDetails(anyInt(), anyInt(), eq(customer.getCustomerId()), 
                any(), any(), any(), any(LocalDateTime.class));
    }

    @Test
    void shouldJoinWaitlist_Unauthenticated() throws Exception {
        mockMvc.perform(post("/api/booking/join-waitlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"restaurantId\":1,\"guestCount\":4}")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "CUSTOMER")
    void shouldJoinWaitlist_CustomerNotFound() throws Exception {
        when(customerService.findByUsername("testuser")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/booking/join-waitlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"restaurantId\":1,\"guestCount\":4}")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "CUSTOMER")
    void shouldJoinWaitlist_WithAllFields() throws Exception {
        when(customerService.findByUsername("testuser")).thenReturn(Optional.of(customer));
        when(waitlistService.addToWaitlistWithDetails(
                anyInt(), anyInt(), any(UUID.class), anyString(), anyString(), 
                anyString(), any(LocalDateTime.class))).thenReturn(waitlist);

        mockMvc.perform(post("/api/booking/join-waitlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"restaurantId\":1,\"guestCount\":4,\"preferredBookingTime\":\"2024-12-31T19:00:00\"," +
                                "\"specialRequests\":\"Window seat\",\"dishIds\":\"1:2\",\"serviceIds\":\"1,2\",\"tableIds\":\"1,2\"}")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "CUSTOMER")
    void shouldGetWaitlistDetails() throws Exception {
        when(waitlistService.getWaitlistDetails(1)).thenReturn(waitlistDetail);

        mockMvc.perform(get("/api/booking/waitlist/1/details"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.waitlistId").value(1))
                .andExpect(jsonPath("$.restaurantId").value(1));
    }

    @Test
    void shouldGetWaitlistDetails_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/booking/waitlist/1/details"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "CUSTOMER")
    void shouldHandleServiceException() throws Exception {
        when(smartWaitlistService.checkGeneralAvailability(any(Integer.class), any(LocalDateTime.class), any(Integer.class)))
                .thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(get("/api/booking/availability-check")
                        .param("restaurantId", "1")
                        .param("bookingTime", "2024-12-31T19:00:00")
                        .param("guestCount", "4"))
                .andExpect(status().isBadRequest());
    }
}

