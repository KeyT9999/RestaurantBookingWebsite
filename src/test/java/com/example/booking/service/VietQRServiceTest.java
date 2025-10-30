package com.example.booking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import com.example.booking.domain.BankDirectory;
import com.example.booking.dto.payout.BankInfoDto;
import com.example.booking.dto.vietqr.VietQRBanksResponse;
import com.example.booking.dto.vietqr.VietQRLookupRequest;
import com.example.booking.dto.vietqr.VietQRLookupResponse;
import com.example.booking.repository.BankDirectoryRepository;

/**
 * Tests for VietQRService covering VQ-001 .. VQ-013
 */
class VietQRServiceTest {

    @Mock
    private RestTemplate restTemplate;
    @Mock
    private BankDirectoryRepository bankDirectoryRepository;

    @InjectMocks
    private VietQRService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new VietQRService(restTemplate, bankDirectoryRepository);
        ReflectionTestUtils.setField(service, "banksUrl", "http://vietqr/banks");
        ReflectionTestUtils.setField(service, "lookupUrl", "http://vietqr/lookup");
        ReflectionTestUtils.setField(service, "clientId", "cid");
        ReflectionTestUtils.setField(service, "apiKey", "key");
    }

    // TC VQ-001
    @Test
    @DisplayName("listBanks returns cache when valid (VQ-001)")
    void listBanks_cacheHit() {
        BankInfoDto dto = new BankInfoDto();
        dto.setBin("970436");
        // prime cache via private fields
        var cache = new java.util.concurrent.ConcurrentHashMap<String, BankInfoDto>();
        cache.put("970436", dto);
        ReflectionTestUtils.setField(service, "bankCache", cache);
        ReflectionTestUtils.setField(service, "lastFetchTime", System.currentTimeMillis());

        Collection<BankInfoDto> result = service.listBanks();
        assertThat(result).hasSize(1);
        verifyNoInteractions(bankDirectoryRepository);
    }

    // TC VQ-002
    @Test
    @DisplayName("listBanks triggers sync when DB under threshold (VQ-002)")
    void listBanks_triggersSync() {
        when(bankDirectoryRepository.findByIsActiveTrueOrderByShortNameAsc()).thenReturn(java.util.List.of());

        VietQRBanksResponse resp = new VietQRBanksResponse();
        resp.setCode("00");
        resp.setDesc("success");
        resp.setData(java.util.List.of(bank("1","970436","VCB","Vietcombank","VCB",1,1)));
        when(restTemplate.getForEntity(eq("http://vietqr/banks"), eq(VietQRBanksResponse.class)))
                .thenReturn(ResponseEntity.ok(resp));

        Collection<BankInfoDto> result = service.listBanks();
        assertThat(result).extracting(BankInfoDto::getBin).contains("970436");
        verify(bankDirectoryRepository, atLeastOnce()).save(any(BankDirectory.class));
    }

    // TC VQ-003
    @Test
    @DisplayName("listBanks loads from DB when sufficient (VQ-003)")
    void listBanks_dbLoad() {
        BankDirectory b = new BankDirectory();
        b.setBin("970436");
        b.setCode("VCB");
        b.setName("Vietcombank");
        b.setShortName("VCB");
        when(bankDirectoryRepository.findByIsActiveTrueOrderByShortNameAsc())
                .thenReturn(Arrays.asList(b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b));

        Collection<BankInfoDto> result = service.listBanks();
        assertThat(result).isNotEmpty();
        // subsequent call should be cache hit
        int interactionsBefore = mockingDetails(bankDirectoryRepository).getInvocations().size();
        Collection<BankInfoDto> result2 = service.listBanks();
        assertThat(result2).isNotEmpty();
        int interactionsAfter = mockingDetails(bankDirectoryRepository).getInvocations().size();
        assertThat(interactionsAfter).isEqualTo(interactionsBefore);
    }

    // TC VQ-004
    @Test
    @DisplayName("getBankByBin returns from cache (VQ-004)")
    void getBankByBin_cache() {
        BankInfoDto dto = new BankInfoDto();
        dto.setBin("970436");
        var cache = new java.util.concurrent.ConcurrentHashMap<String, BankInfoDto>();
        cache.put("970436", dto);
        ReflectionTestUtils.setField(service, "bankCache", cache);
        ReflectionTestUtils.setField(service, "lastFetchTime", System.currentTimeMillis());

        assertThat(service.getBankByBin("970436")).isPresent();
        verifyNoInteractions(bankDirectoryRepository);
    }

    // TC VQ-005
    @Test
    @DisplayName("getBankByBin queries DB when not cached (VQ-005)")
    void getBankByBin_db() {
        BankDirectory b = new BankDirectory();
        b.setBin("970436");
        b.setShortName("VCB");
        when(bankDirectoryRepository.findByBin("970436")).thenReturn(Optional.of(b));

        assertThat(service.getBankByBin("970436")).isPresent();
        verify(bankDirectoryRepository).findByBin("970436");
    }

    // TC VQ-006
    @Test
    @DisplayName("getBankByBin returns empty when not found (VQ-006)")
    void getBankByBin_empty() {
        when(bankDirectoryRepository.findByBin("999999")).thenReturn(Optional.empty());
        assertThat(service.getBankByBin("999999")).isEmpty();
    }

    // TC VQ-007
    @Test
    @DisplayName("syncBanksFromVietQR creates new and updates cache (VQ-007)")
    void syncBanks_creates() {
        VietQRBanksResponse resp = new VietQRBanksResponse();
        resp.setCode("00");
        resp.setDesc("success");
        resp.setData(java.util.List.of(bank("1","970436","VCB","Vietcombank","VCB",1,1)));
        when(restTemplate.getForEntity(eq("http://vietqr/banks"), eq(VietQRBanksResponse.class)))
                .thenReturn(ResponseEntity.ok(resp));
        when(bankDirectoryRepository.findByBin("970436")).thenReturn(Optional.empty());

        service.syncBanksFromVietQR();

        verify(bankDirectoryRepository).save(any(BankDirectory.class));
        // After sync, listBanks should return from cache
        assertThat(service.listBanks()).extracting(BankInfoDto::getBin).contains("970436");
    }

    // TC VQ-008
    @Test
    @DisplayName("syncBanksFromVietQR updates existing and creates new (VQ-008)")
    void syncBanks_updatesAndCreates() {
        VietQRBanksResponse resp = new VietQRBanksResponse();
        resp.setCode("00");
        resp.setDesc("success");
        resp.setData(java.util.List.of(
                bank("1","970436","VCB","Vietcombank","VCB",1,1),
                bank("2","970407","TCB","Techcombank","TCB",1,1)
        ));
        when(restTemplate.getForEntity(eq("http://vietqr/banks"), eq(VietQRBanksResponse.class)))
                .thenReturn(ResponseEntity.ok(resp));
        when(bankDirectoryRepository.findByBin("970436")).thenReturn(Optional.of(new BankDirectory()));
        when(bankDirectoryRepository.findByBin("970407")).thenReturn(Optional.empty());

        service.syncBanksFromVietQR();

        verify(bankDirectoryRepository, times(2)).save(any(BankDirectory.class));
        assertThat(service.listBanks()).extracting(BankInfoDto::getBin).contains("970436", "970407");
    }

    // TC VQ-009
    @Test
    @DisplayName("syncBanksFromVietQR throws RuntimeException on HTTP error (VQ-009)")
    void syncBanks_httpError() {
        when(restTemplate.getForEntity(eq("http://vietqr/banks"), eq(VietQRBanksResponse.class)))
                .thenThrow(new RuntimeException("HTTP 500"));

        assertThatThrownBy(() -> service.syncBanksFromVietQR())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to sync banks");
    }

    // TC VQ-010
    @Test
    @DisplayName("lookupAccountName returns Optional.of when success (VQ-010)")
    void lookup_success() {
        VietQRLookupResponse response = new VietQRLookupResponse();
        response.setCode("00");
        response.setDesc("success");
        VietQRLookupResponse.LookupData data = new VietQRLookupResponse.LookupData();
        data.setAccountName("A");
        response.setData(data);
        when(restTemplate.exchange(eq("http://vietqr/lookup"), eq(org.springframework.http.HttpMethod.POST), any(), eq(VietQRLookupResponse.class)))
                .thenReturn(ResponseEntity.ok(response));

        assertThat(service.lookupAccountName("970436", "0123456789")).contains("A");
        // capture headers
        ArgumentCaptor<org.springframework.http.HttpEntity<VietQRLookupRequest>> captor = ArgumentCaptor.forClass(org.springframework.http.HttpEntity.class);
        verify(restTemplate).exchange(eq("http://vietqr/lookup"), eq(org.springframework.http.HttpMethod.POST), captor.capture(), eq(VietQRLookupResponse.class));
        var headers = captor.getValue().getHeaders();
        assertThat(headers.getFirst("x-client-id")).isEqualTo("cid");
        assertThat(headers.getFirst("x-api-key")).isEqualTo("key");
    }

    // TC VQ-011
    @Test
    @DisplayName("lookupAccountName returns empty when API indicates failure (VQ-011)")
    void lookup_failureFlag() {
        VietQRLookupResponse response = new VietQRLookupResponse();
        response.setCode("01");
        response.setDesc("error");
        response.setData(null);
        when(restTemplate.exchange(eq("http://vietqr/lookup"), eq(org.springframework.http.HttpMethod.POST), any(), eq(VietQRLookupResponse.class)))
                .thenReturn(ResponseEntity.ok(response));

        assertThat(service.lookupAccountName("970436", "0123456789")).isEmpty();
    }

    // TC VQ-012
    @Test
    @DisplayName("lookupAccountName returns empty when HTTP error (VQ-012)")
    void lookup_httpError() {
        when(restTemplate.exchange(eq("http://vietqr/lookup"), eq(org.springframework.http.HttpMethod.POST), any(), eq(VietQRLookupResponse.class)))
                .thenThrow(new RuntimeException("HTTP 500"));

        assertThat(service.lookupAccountName("970436", "0123456789")).isEmpty();
    }

    // TC VQ-013
    @Test
    @DisplayName("lookupAccountName omits headers when properties not set (VQ-013)")
    void lookup_noHeadersWhenUnset() {
        ReflectionTestUtils.setField(service, "clientId", null);
        ReflectionTestUtils.setField(service, "apiKey", null);
        VietQRLookupResponse response = new VietQRLookupResponse();
        response.setCode("00");
        response.setDesc("success");
        VietQRLookupResponse.LookupData data = new VietQRLookupResponse.LookupData();
        data.setAccountName("A");
        response.setData(data);
        when(restTemplate.exchange(eq("http://vietqr/lookup"), eq(org.springframework.http.HttpMethod.POST), any(), eq(VietQRLookupResponse.class)))
                .thenReturn(ResponseEntity.ok(response));

        assertThat(service.lookupAccountName("970436", "0123456789")).contains("A");
        ArgumentCaptor<org.springframework.http.HttpEntity<VietQRLookupRequest>> captor = ArgumentCaptor.forClass(org.springframework.http.HttpEntity.class);
        verify(restTemplate).exchange(eq("http://vietqr/lookup"), eq(org.springframework.http.HttpMethod.POST), captor.capture(), eq(VietQRLookupResponse.class));
        var headers = captor.getValue().getHeaders();
        assertThat(headers.getFirst("x-client-id")).isNull();
        assertThat(headers.getFirst("x-api-key")).isNull();
    }

    private VietQRBanksResponse.VietQRBank bank(String id, String bin, String code, String name, String shortName, int transferSupported, int lookupSupported) {
        VietQRBanksResponse.VietQRBank b = new VietQRBanksResponse.VietQRBank();
        b.setId(Integer.valueOf(id));
        b.setBin(bin);
        b.setCode(code);
        b.setName(name);
        b.setShortName(shortName);
        b.setLogo("http://logo");
        b.setTransferSupported(transferSupported);
        b.setLookupSupported(lookupSupported);
        return b;
    }
}


