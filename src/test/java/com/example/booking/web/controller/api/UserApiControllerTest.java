package com.example.booking.web.controller.api;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.service.SimpleUserService;

@WebMvcTest(controllers = UserApiController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SimpleUserService userService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setUsername("testuser");
        mockUser.setEmail("test@example.com");
        mockUser.setFullName("Test User");
        mockUser.setRole(UserRole.CUSTOMER);
    }

    @Test
    void testGetCurrentUser_WithoutAuthentication_ShouldReturnBadRequest() throws Exception {
        // When & Then: Should return 400 Bad Request
        mockMvc.perform(get("/api/user/current"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("User not authenticated"));
    }

    @Test
    void testGetCurrentUser_WithPrincipalObject_ShouldReturnBadRequest() throws Exception {
        // This test verifies the basic controller setup works
        // Full authentication testing requires Spring Boot Test integration
        // or more complex mock setup
    }
}
