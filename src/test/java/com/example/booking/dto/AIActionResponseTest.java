package com.example.booking.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for AIActionResponse.
 * Coverage Target: ≥80% Branch Coverage
 * Test Strategy: 3 cases per method (happy, edge, error)
 * 
 * @author Senior SDET
 */
@DisplayName("AIActionResponse Business Logic Tests")
class AIActionResponseTest {

    // ==================== success() static factory Tests ====================

    @Test
    @DisplayName("success - Happy path: creates success response with message")
    void success_HappyPath_CreatesSuccessResponseWithMessage() {
        // Given
        String message = "Operation completed successfully";

        // When
        AIActionResponse response = AIActionResponse.success(message);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo(message);
        assertThat(response.getData()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @ParameterizedTest
    @DisplayName("success - Edge cases: handles various message formats")
    @MethodSource("messageProvider")
    void success_EdgeCases_HandlesVariousMessageFormats(String message) {
        // When
        AIActionResponse response = AIActionResponse.success(message);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo(message);
    }

    @Test
    @DisplayName("success - Error case: null message creates response with null")
    void success_ErrorCase_NullMessageCreatesResponseWithNull() {
        // When
        AIActionResponse response = AIActionResponse.success(null);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isNull();
    }

    // ==================== success(String, Map) static factory Tests ====================

    @Test
    @DisplayName("success with data - Happy path: creates success response with message and data")
    void successWithData_HappyPath_CreatesSuccessResponseWithMessageAndData() {
        // Given
        String message = "Data retrieved successfully";
        Map<String, Object> data = new HashMap<>();
        data.put("key1", "value1");
        data.put("key2", 123);

        // When
        AIActionResponse response = AIActionResponse.success(message, data);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo(message);
        assertThat(response.getData()).isEqualTo(data);
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    @DisplayName("success with data - Edge case: empty map data")
    void successWithData_EdgeCase_EmptyMapData() {
        // Given
        String message = "No data available";
        Map<String, Object> data = new HashMap<>();

        // When
        AIActionResponse response = AIActionResponse.success(message, data);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isEmpty();
    }

    @Test
    @DisplayName("success with data - Error case: null data creates response with null data")
    void successWithData_ErrorCase_NullDataCreatesResponseWithNullData() {
        // Given
        String message = "Response with null data";

        // When
        AIActionResponse response = AIActionResponse.success(message, null);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isNull();
    }

    // ==================== error() static factory Tests ====================

    @Test
    @DisplayName("error - Happy path: creates error response with message")
    void error_HappyPath_CreatesErrorResponseWithMessage() {
        // Given
        String message = "Operation failed";

        // When
        AIActionResponse response = AIActionResponse.error(message);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(message);
        assertThat(response.getData()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @ParameterizedTest
    @DisplayName("error - Edge cases: handles various error message formats")
    @MethodSource("errorMessageProvider")
    void error_EdgeCases_HandlesVariousErrorMessageFormats(String message) {
        // When
        AIActionResponse response = AIActionResponse.error(message);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(message);
    }

    @Test
    @DisplayName("error - Error case: null message creates error response with null")
    void error_ErrorCase_NullMessageCreatesErrorResponseWithNull() {
        // When
        AIActionResponse response = AIActionResponse.error(null);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isNull();
    }

    // ==================== error(String, String) static factory Tests ====================

    @Test
    @DisplayName("error with errorCode - Happy path: creates error response with message and errorCode")
    void errorWithErrorCode_HappyPath_CreatesErrorResponseWithMessageAndErrorCode() {
        // Given
        String message = "Validation failed";
        String errorCode = "VALIDATION_ERROR";

        // When
        AIActionResponse response = AIActionResponse.error(message, errorCode);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(message);
        assertThat(response.getErrorCode()).isEqualTo(errorCode);
        assertThat(response.getData()).isNull();
    }

    @ParameterizedTest
    @DisplayName("error with errorCode - Edge cases: various error codes")
    @MethodSource("errorCodeProvider")
    void errorWithErrorCode_EdgeCases_VariousErrorCodes(String message, String errorCode) {
        // When
        AIActionResponse response = AIActionResponse.error(message, errorCode);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getErrorCode()).isEqualTo(errorCode);
    }

    @Test
    @DisplayName("error with errorCode - Error case: null errorCode creates response with null errorCode")
    void errorWithErrorCode_ErrorCase_NullErrorCodeCreatesResponseWithNullErrorCode() {
        // Given
        String message = "Error occurred";

        // When
        AIActionResponse response = AIActionResponse.error(message, null);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getErrorCode()).isNull();
    }

    // ==================== Parameterized Test Data Providers ====================

    private static Stream<Arguments> messageProvider() {
        return Stream.of(
                Arguments.of(""),
                Arguments.of("Simple message"),
                Arguments.of("Message with special chars: !@#$%^&*()"),
                Arguments.of("Multi-word message with spaces"),
                Arguments.of("Very long message " + "x".repeat(100))
        );
    }

    private static Stream<Arguments> errorMessageProvider() {
        return Stream.of(
                Arguments.of(""),
                Arguments.of("Error occurred"),
                Arguments.of("Error: Invalid input"),
                Arguments.of("HTTP 500 Internal Server Error"),
                Arguments.of("Database connection failed")
        );
    }

    private static Stream<Arguments> errorCodeProvider() {
        return Stream.of(
                Arguments.of("Error message", "ERROR_001"),
                Arguments.of("Validation failed", "VALIDATION_ERROR"),
                Arguments.of("Not found", "NOT_FOUND"),
                Arguments.of("Unauthorized", "UNAUTHORIZED"),
                Arguments.of("Internal error", "INTERNAL_ERROR")
        );
    }
}

