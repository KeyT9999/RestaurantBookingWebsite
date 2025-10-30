package com.example.booking.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.booking.domain.UserRole;
import com.example.booking.web.config.UserRoleStringConverter;

class UserRoleStringConverterTest {

    // TC MVC-001
    @Test
    @DisplayName("converts ADMIN/admin to UserRole.ADMIN (MVC-001)")
    void convertsAdmin() {
        UserRoleStringConverter c = new UserRoleStringConverter();
        assertThat(c.convert("ADMIN")).isEqualTo(UserRole.ADMIN);
        assertThat(c.convert("admin")).isEqualTo(UserRole.ADMIN);
    }

    // TC MVC-002
    @Test
    @DisplayName("converts value-based string to correct enum (MVC-002)")
    void convertsValueBased() {
        UserRoleStringConverter c = new UserRoleStringConverter();
        assertThat(c.convert(UserRole.CUSTOMER.getValue())).isEqualTo(UserRole.CUSTOMER);
    }

    // TC MVC-003
    @Test
    @DisplayName("throws on unknown role (MVC-003)")
    void throwsOnUnknown() {
        UserRoleStringConverter c = new UserRoleStringConverter();
        assertThatThrownBy(() -> c.convert("unknown-role")).isInstanceOf(IllegalArgumentException.class);
    }

    // TC MVC-004
    @Test
    @DisplayName("null source returns null (MVC-004)")
    void nullReturnsNull() {
        UserRoleStringConverter c = new UserRoleStringConverter();
        // TODO: behavior for blank strings is unclear; asserting only null -> null
        assertThat(c.convert(null)).isNull();
    }
}


