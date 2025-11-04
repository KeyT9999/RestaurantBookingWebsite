package com.example.booking;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

class RestaurantBookingApplicationTest {

    // TC CI-005
    @Test
    @DisplayName("main delegates to SpringApplication.run (CI-005)")
    void main_delegatesToSpringRun() {
        try (MockedStatic<SpringApplication> mocked = mockStatic(SpringApplication.class)) {
            RestaurantBookingApplication.main(new String[]{});
            mocked.verify(() -> SpringApplication.run(RestaurantBookingApplication.class, new String[]{}));
        }
    }
}


