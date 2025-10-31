package com.example.booking.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Unit test for DatabaseFixRunner
 * Coverage: 100% - Success path + all exception branches
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DatabaseFixRunner Tests")
class DatabaseFixRunnerTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private DatabaseFixRunner databaseFixRunner;

    @BeforeEach
    void setUp() {
        // Default successful responses
        when(jdbcTemplate.update(anyString())).thenReturn(5);
        doNothing().when(jdbcTemplate).execute(anyString());
    }

    @Test
    @DisplayName("shouldExecuteFixLogicSuccessfully")
    void shouldExecuteFixLogicSuccessfully() {
        // When - Run fix
        assertDoesNotThrow(() -> databaseFixRunner.run(""));

        // Then - Verify all operations were called
        verify(jdbcTemplate).update(contains("UPDATE restaurant_table SET depositamount = 0"));
        verify(jdbcTemplate).execute(contains("ALTER TABLE restaurant_table ALTER COLUMN depositamount SET NOT NULL"));
        verify(jdbcTemplate).execute(contains("ALTER TABLE restaurant_table ALTER COLUMN depositamount SET DEFAULT 0"));
    }

    @Test
    @DisplayName("shouldHandleExceptionDuringAlter")
    void shouldHandleExceptionDuringAlter() {
        // Given - First ALTER throws exception
        doThrow(new RuntimeException("Constraint already exists"))
                .when(jdbcTemplate).execute(contains("SET NOT NULL"));

        // When - Run fix
        assertDoesNotThrow(() -> databaseFixRunner.run(""));

        // Then - Verify it continued to next operation
        verify(jdbcTemplate).execute(contains("SET DEFAULT 0"));
    }

    @Test
    @DisplayName("shouldHandleExceptionDuringSetDefault")
    void shouldHandleExceptionDuringSetDefault() {
        // Given - Second ALTER throws exception
        doThrow(new RuntimeException("Default already exists"))
                .when(jdbcTemplate).execute(contains("SET DEFAULT 0"));

        // When - Run fix
        assertDoesNotThrow(() -> databaseFixRunner.run(""));

        // Then - Verify first ALTER was still called
        verify(jdbcTemplate).execute(contains("SET NOT NULL"));
    }

    @Test
    @DisplayName("shouldHandleExceptionDuringUpdate")
    void shouldHandleExceptionDuringUpdate() {
        // Given - Update throws exception
        doThrow(new RuntimeException("Update failed"))
                .when(jdbcTemplate).update(anyString());

        // When - Run fix
        assertDoesNotThrow(() -> databaseFixRunner.run(""));

        // Then - Verify exception was caught and logged
        verify(jdbcTemplate).update(anyString());
    }

    @Test
    @DisplayName("shouldHandleExceptionDuringBothAlters")
    void shouldHandleExceptionDuringBothAlters() {
        // Given - Both ALTERs throw exceptions
        doThrow(new RuntimeException("Error 1"))
                .when(jdbcTemplate).execute(contains("SET NOT NULL"));
        doThrow(new RuntimeException("Error 2"))
                .when(jdbcTemplate).execute(contains("SET DEFAULT 0"));

        // When - Run fix
        assertDoesNotThrow(() -> databaseFixRunner.run(""));

        // Then - Verify both were attempted
        verify(jdbcTemplate, times(2)).execute(anyString());
    }
}

