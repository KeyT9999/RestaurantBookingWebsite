package com.example.booking.web.controller;

import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.repository.UserRepository;
import com.example.booking.service.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AdminChatController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
        com.example.booking.config.AuthRateLimitFilter.class,
        com.example.booking.config.GeneralRateLimitFilter.class,
        com.example.booking.config.LoginRateLimitFilter.class,
        com.example.booking.config.PermanentlyBlockedIpFilter.class,
        com.example.booking.web.advice.NotificationHeaderAdvice.class
    }),
    excludeAutoConfiguration = {org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AdminChatController WebMvc Tests")
class AdminChatControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatService chatService;

    @MockBean
    private UserRepository userRepository;

    private User adminUser;

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setId(UUID.randomUUID());
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@test.com");
        adminUser.setFullName("Admin User");
        adminUser.setRole(UserRole.ADMIN);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("GET /admin/chat - should show admin chat page")
    void testAdminChatPage_Success() throws Exception {
        // Given
        when(userRepository.findByEmail("admin")).thenReturn(Optional.of(adminUser));

        // When & Then
        mockMvc.perform(get("/admin/chat"))
            .andExpect(status().isOk())
            .andExpect(view().name("admin/chat"))
            .andExpect(model().attributeExists("admin"));
    }
}

