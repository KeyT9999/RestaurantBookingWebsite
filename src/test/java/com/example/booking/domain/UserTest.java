package com.example.booking.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    @DisplayName("isPasswordResetTokenValid should check expiry bounds")
    void shouldValidatePasswordResetToken() {
        User user = new User();
        user.setPasswordResetToken("token");
        user.setPasswordResetTokenExpiry(LocalDateTime.now().plusMinutes(5));

        assertThat(user.isPasswordResetTokenValid()).isTrue();

        user.setPasswordResetTokenExpiry(LocalDateTime.now().minusMinutes(1));
        assertThat(user.isPasswordResetTokenValid()).isFalse();

        user.setPasswordResetToken(null);
        assertThat(user.isPasswordResetTokenValid()).isFalse();
    }

    @Test
    @DisplayName("updateLastLogin should stamp current time")
    void shouldUpdateLastLogin() {
        User user = new User();
        assertThat(user.getLastLogin()).isNull();

        user.updateLastLogin();
        assertThat(user.getLastLogin()).isNotNull();
    }
}
