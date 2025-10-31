package com.example.booking.web.controller.api;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.example.booking.config.TestRateLimitingConfig;
import com.example.booking.service.BankAccountService;
import org.springframework.boot.test.mock.mockito.MockBean;

/**
 * Tests for BankAccountApiController covering BA-002, BA-003, BA-004.
 * Note: BA-001 (happy path calling real VietQR) requires refactor for injectable URL/RestTemplate; TODO.
 */

@WebMvcTest(controllers = BankAccountApiController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(TestRateLimitingConfig.class)
class BankAccountApiControllerTest {

    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private BankAccountService bankAccountService;

    // TC BA-002
    @Test
    @DisplayName("should return fallback banks when external API fails (BA-002)")
    void shouldReturnFallback_whenExternalFails() throws Exception {
        // Since controller calls external HTTPS directly, the call will fail in test env -> fallback is used
        mockMvc.perform(get("/api/v2/banks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("00")))
                .andExpect(jsonPath("$.desc", containsString("success")))
                .andExpect(jsonPath("$.data", not(empty())));
    }

    // TC BA-003
    @Test
    @DisplayName("should produce fallback payload structure (BA-003)")
    void shouldProduceFallbackPayload() throws Exception {
        mockMvc.perform(get("/api/v2/banks"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.data[*].name", hasItem("Vietcombank")))
                .andExpect(jsonPath("$.data[*].bin", hasItem("970436")));
    }

    // TC BA-004
    @Test
    @DisplayName("should include predefined banks in fallback (BA-004)")
    void shouldIncludePredefinedBanks() throws Exception {
        mockMvc.perform(get("/api/v2/banks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].shortName", hasItems("VCB","TCB","CTG","BID")));
    }
}


