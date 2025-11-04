package com.example.booking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

/**
 * Tests for BankAccountService covering BA-005 .. BA-014
 */
class BankAccountServiceTest {

    private BankAccountService service;
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        service = new BankAccountService();
        restTemplate = Mockito.mock(RestTemplate.class);
        // Inject mock RestTemplate and properties
        ReflectionTestUtils.setField(service, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(service, "payosClientId", "client-id");
        ReflectionTestUtils.setField(service, "payosApiKey", "api-key");
    }

    // TC BA-005
    @Test
    @DisplayName("should return account holder from PayOS when API success (BA-005)")
    void shouldReturnFromPayOS_whenApiSuccess() {
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("accountHolderName", "NGUYEN VAN A");
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(body));

        String name = service.getAccountHolderName("0123456789", "970436");

        assertThat(name).isEqualTo("NGUYEN VAN A");
        verify(restTemplate, times(1)).exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class));
    }

    // TC BA-006
    @Test
    @DisplayName("should fallback to bank API when PayOS fails (BA-006)")
    void shouldFallbackToBankApi_whenPayOsFails() {
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new RuntimeException("PayOS down"))
                .thenReturn(ResponseEntity.ok(new HashMap<String, Object>() {{
                    put("success", true);
                    put("accountHolderName", "B");
                }}));

        String name = service.getAccountHolderName("0123456789", "970436");

        assertThat(name).isEqualTo("B");
        verify(restTemplate, times(2)).exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class));
    }

    // TC BA-007
    @Test
    @DisplayName("should return null when both PayOS and bank API fail (BA-007)")
    void shouldReturnNull_whenAllFail() {
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new RuntimeException("PayOS down"))
                .thenThrow(new RuntimeException("Bank API down"));

        String name = service.getAccountHolderName("0123456789", "970436");
        assertThat(name).isNull();
    }

    // TC BA-008
    @Test
    @DisplayName("should not call bank API when bank code unmapped (BA-008)")
    void shouldSkipBankApi_whenBankCodeUnknown() {
        // First call PayOS fails
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new RuntimeException("PayOS down"));

        String name = service.getAccountHolderName("0123456789", "999999");
        assertThat(name).isNull();
        // Only one exchange attempted (PayOS)
        verify(restTemplate, times(1)).exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class));
    }

    // TC BA-009
    @Test
    @DisplayName("isValidBankAccount returns true for valid input (BA-009)")
    void isValid_true() {
        assertThat(service.isValidBankAccount("01234567", "970436")).isTrue();
    }

    // TC BA-010
    @Test
    @DisplayName("isValidBankAccount returns false for length out of range (BA-010)")
    void isValid_lengthOutOfRange() {
        assertThat(service.isValidBankAccount("0123456", "970436")).isFalse();
        assertThat(service.isValidBankAccount("01234567890123456", "970436")).isFalse();
    }

    // TC BA-011
    @Test
    @DisplayName("isValidBankAccount returns false for non-digit (BA-011)")
    void isValid_nonDigit() {
        assertThat(service.isValidBankAccount("0123ABCD", "970436")).isFalse();
    }

    // TC BA-012
    @Test
    @DisplayName("isValidBankAccount returns false for unsupported bank code (BA-012)")
    void isValid_unknownBank() {
        assertThat(service.isValidBankAccount("01234567", "999999")).isFalse();
    }

    // TC BA-013
    @Test
    @DisplayName("getBankName returns name for known code (BA-013)")
    void getBankName_known() {
        assertThat(service.getBankName("970436")).isEqualTo("Vietcombank");
    }

    // TC BA-014
    @Test
    @DisplayName("getBankName returns null for unknown code (BA-014)")
    void getBankName_unknown() {
        assertThat(service.getBankName("999999")).isNull();
    }
}


