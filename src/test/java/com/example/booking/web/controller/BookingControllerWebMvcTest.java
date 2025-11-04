package com.example.booking.web.controller;

import com.example.booking.domain.Customer;
import com.example.booking.domain.User;
import com.example.booking.exception.BookingConflictException;
import com.example.booking.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
@AutoConfigureMockMvc
@org.springframework.context.annotation.Import(com.example.booking.config.TestRateLimitingConfig.class)
class BookingControllerWebMvcTest {
    @Autowired private MockMvc mockMvc;

    @MockBean private BookingService bookingService;
    @MockBean private CustomerService customerService;
    @MockBean private RestaurantManagementService restaurantService;
    @MockBean private WaitlistService waitlistService;
    @MockBean private SimpleUserService userService;
    @MockBean private RestaurantOwnerService restaurantOwnerService;
    @MockBean private com.example.booking.config.AdvancedRateLimitingInterceptor advancedRateLimitingInterceptor;
    @MockBean private com.example.booking.service.EndpointRateLimitingService endpointRateLimitingService;
    @MockBean private com.example.booking.config.AuthRateLimitFilter authRateLimitFilter;
    @MockBean private com.example.booking.service.AuthRateLimitingService authRateLimitingService;
    @MockBean private com.example.booking.config.GeneralRateLimitFilter generalRateLimitFilter;
    @MockBean private com.example.booking.service.GeneralRateLimitingService generalRateLimitingService;
    @MockBean private com.example.booking.config.LoginRateLimitFilter loginRateLimitFilter;
    @MockBean private com.example.booking.service.LoginRateLimitingService loginRateLimitingService;
    @MockBean private com.example.booking.service.DatabaseRateLimitingService databaseRateLimitingService;
    @MockBean private com.example.booking.service.NotificationService notificationService;

    @Test
    @WithAnonymousUser
    void get_new_redirects_to_oauth_when_unauthenticated() throws Exception {
        mockMvc.perform(get("/booking/new"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/oauth2/authorization/google"));
    }

    @Test
    @WithMockUser(username = "u")
    void post_booking_with_binding_errors_redirects_back() throws Exception {
        Customer customer = new Customer();
        customer.setCustomerId(UUID.randomUUID());
        User user = new User();
        user.setUsername("u");
        customer.setUser(user);

        when(customerService.findByUsername(anyString())).thenReturn(Optional.of(customer));
        when(bookingService.createBooking(any(), any(UUID.class)))
                .thenThrow(new BookingConflictException(BookingConflictException.ConflictType.TABLE_OCCUPIED, "Table occupied"));

        mockMvc.perform(post("/booking").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/booking/new"));
    }
}
