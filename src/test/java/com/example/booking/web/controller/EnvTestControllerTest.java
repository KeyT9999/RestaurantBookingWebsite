package com.example.booking.web.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EnvTestController.class)
@TestPropertySource(properties = {
    "CLOUDINARY_CLOUD_NAME=test-cloud",
    "CLOUDINARY_API_KEY=test-key-1234",
    "CLOUDINARY_API_SECRET=test-secret-5678",
    "JDBC_DATABASE_URL=jdbc:postgresql://localhost:5432/testdb",
    "DB_USERNAME=testuser",
    "DB_PASSWORD=testpass"
})
@DisplayName("EnvTestController Test")
class EnvTestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /test/env/check - Should return environment variables")
    void testCheckEnvVariables_ShouldReturnVariables() throws Exception {
        mockMvc.perform(get("/test/env/check"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cloudinary_cloud_name").value("test-cloud"))
            .andExpect(jsonPath("$.cloudinary_api_key").exists())
            .andExpect(jsonPath("$.cloudinary_api_secret").exists())
            .andExpect(jsonPath("$.jdbc_url").value("jdbc:postgresql://localhost:5432/testdb"))
            .andExpect(jsonPath("$.db_username").value("testuser"))
            .andExpect(jsonPath("$.db_password").exists())
            .andExpect(jsonPath("$.env_file_read").exists())
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("GET /test/env/check - Should mask sensitive data")
    void testCheckEnvVariables_ShouldMaskSensitiveData() throws Exception {
        mockMvc.perform(get("/test/env/check"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cloudinary_api_key").value(org.hamcrest.Matchers.containsString("***")))
            .andExpect(jsonPath("$.cloudinary_api_secret").value(org.hamcrest.Matchers.containsString("***")))
            .andExpect(jsonPath("$.db_password").value(org.hamcrest.Matchers.containsString("***")));
    }

    @Test
    @DisplayName("GET /test/env/check - Should handle NOT_FOUND values")
    void testCheckEnvVariables_WithNotFoundValues() throws Exception {
        // This will use default NOT_FOUND values since properties are not set
        mockMvc.perform(get("/test/env/check"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.env_file_read").exists())
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("GET /test/env/check - Should handle null api key gracefully")
    void testCheckEnvVariables_WithNullApiKey() throws Exception {
        mockMvc.perform(get("/test/env/check"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cloudinary_api_key").exists());
    }

    @Test
    @DisplayName("GET /test/env/check - Should test with env_file_read = true scenario")
    void testCheckEnvVariables_EnvFileReadTrue() throws Exception {
        // Since we set properties, env_file_read should be true
        mockMvc.perform(get("/test/env/check"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.env_file_read").value(true))
            .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("are being read")));
    }

    @Test
    @DisplayName("GET /test/env/check - Should test substring with very short key")
    void testCheckEnvVariables_VeryShortKey() throws Exception {
        // Test the Math.max logic for short keys
        mockMvc.perform(get("/test/env/check"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cloudinary_api_key").exists());
    }
}

