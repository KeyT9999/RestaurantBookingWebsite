package com.example.booking.web.controller.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.booking.config.TestRateLimitingConfig;
import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.dto.AIActionRequest;
import com.example.booking.dto.AIActionResponse;
import com.example.booking.service.AIIntentDispatcherService;
import com.example.booking.service.SimpleUserService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(AIActionsController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(TestRateLimitingConfig.class)
class AIActionsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AIIntentDispatcherService intentDispatcherService;

    @MockBean
    private SimpleUserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(java.util.UUID.randomUUID());
        testUser.setUsername("customer@test.com");
        testUser.setEmail("customer@test.com");
        testUser.setRole(UserRole.CUSTOMER);
    }

    @Test
    // TC AI-001
    @WithMockUser(username = "customer@test.com", roles = {"CUSTOMER"})
    void shouldExecuteAIAction_whenValidRequest() throws Exception {
        // Given
        Map<String, Object> data = new HashMap<>();
        data.put("value", "test");
        AIActionRequest request = new AIActionRequest("test_intent", data);
        AIActionResponse response = AIActionResponse.success("Action completed");
        
        when(userService.loadUserByUsername(anyString())).thenReturn(testUser);
        when(intentDispatcherService.dispatchIntent(anyString(), any(Map.class), any(User.class))).thenReturn(response);
        
        // When & Then
        mockMvc.perform(post("/api/ai/actions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    // TC AI-002
    @WithMockUser(username = "customer@test.com", roles = {"CUSTOMER"})
    void shouldReturnBadRequest_whenIntentIsMissing() throws Exception {
        // Given
        AIActionRequest request = new AIActionRequest("", new HashMap<>());
        
        when(userService.loadUserByUsername(anyString())).thenReturn(testUser);
        
        // When & Then
        mockMvc.perform(post("/api/ai/actions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    // TC AI-003
    @WithMockUser(username = "customer@test.com", roles = {"CUSTOMER"})
    void shouldReturnBadRequest_whenDataIsMissing() throws Exception {
        // Given
        AIActionRequest request = new AIActionRequest();
        request.setIntent("test_intent");
        request.setData(null);
        
        when(userService.loadUserByUsername(anyString())).thenReturn(testUser);
        
        // When & Then
        mockMvc.perform(post("/api/ai/actions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    // TC AI-004
    @WithMockUser(username = "customer@test.com", roles = {"CUSTOMER"})
    void shouldReturnInternalServerError_whenDispatchThrowsException() throws Exception {
        // Given
        AIActionRequest request = new AIActionRequest("test_intent", new HashMap<>());
        
        when(userService.loadUserByUsername(anyString())).thenReturn(testUser);
        when(intentDispatcherService.dispatchIntent(anyString(), any(Map.class), any(User.class)))
            .thenThrow(new RuntimeException("Service error"));
        
        // When & Then
        mockMvc.perform(post("/api/ai/actions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    // TC AI-005
    @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
    void shouldReturnForbidden_whenUserIsNotCustomer() throws Exception {
        // Given
        AIActionRequest request = new AIActionRequest("test_intent", new HashMap<>());
        
        // When & Then
        mockMvc.perform(post("/api/ai/actions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}

