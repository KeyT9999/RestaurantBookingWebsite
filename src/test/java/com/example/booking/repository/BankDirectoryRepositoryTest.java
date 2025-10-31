package com.example.booking.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.example.booking.domain.BankDirectory;

@DataJpaTest
class BankDirectoryRepositoryTest {

    @Autowired
    private BankDirectoryRepository repository;

    @Test
    @DisplayName("should persist and query bank directory entries by bin and activity")
    void shouldQueryBanks() {
        BankDirectory active = new BankDirectory();
        active.setBin("970400");
        active.setCode("VCB");
        active.setName("Vietcombank");
        active.setShortName("VCB");
        active.setTransferSupported(true);

        BankDirectory inactive = new BankDirectory();
        inactive.setBin("970410");
        inactive.setCode("TPB");
        inactive.setName("Tien Phong Bank");
        inactive.setShortName("TPB");
        inactive.setIsActive(false);

        repository.saveAll(List.of(active, inactive));

        assertThat(repository.findByBin("970400"))
                .isPresent()
                .get()
                .extracting(BankDirectory::getCode)
                .isEqualTo("VCB");

        assertThat(repository.existsByBin("970410")).isTrue();

        List<BankDirectory> activeBanks = repository.findByIsActiveTrueOrderByShortNameAsc();
        assertThat(activeBanks).hasSize(1);
        assertThat(activeBanks.get(0).getShortName()).isEqualTo("VCB");
    }
}
