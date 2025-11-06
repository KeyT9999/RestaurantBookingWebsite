package com.example.booking.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * EnvironmentPostProcessor to load .env file before Spring Boot reads configuration
 * This ensures that .env file is loaded when running mvn spring-boot:run
 */
public class DotenvConfig implements EnvironmentPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(DotenvConfig.class);
    
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        try {
            File envFile = findEnvFile();
            
            if (envFile != null && envFile.exists() && envFile.isFile()) {
                logger.info("Loading .env file from: {}", envFile.getAbsolutePath());
                Map<String, Object> envProperties = parseEnvFile(envFile);
                
                if (!envProperties.isEmpty()) {
                    PropertySource<?> propertySource = new MapPropertySource("dotenv", envProperties);
                    // Add as first property source so it takes precedence
                    environment.getPropertySources().addFirst(propertySource);
                    logger.info("Loaded {} properties from .env file", envProperties.size());
                    
                    // Also set as system properties for compatibility with System.getenv() calls
                    for (Map.Entry<String, Object> entry : envProperties.entrySet()) {
                        String key = entry.getKey();
                        String value = String.valueOf(entry.getValue());
                        if (System.getProperty(key) == null) {
                            System.setProperty(key, value);
                        }
                    }
                }
            } else {
                logger.warn(".env file not found. Searched in multiple locations.");
            }
        } catch (Exception e) {
            logger.error("Error loading .env file", e);
        }
    }
    
    private File findEnvFile() {
        // List of possible locations to search for .env file
        String[] searchPaths = {
            ".env",                                    // Current directory
            "../.env",                                 // Parent directory
            System.getProperty("user.dir") + "/.env", // Project root
            System.getProperty("user.dir") + "/../.env", // Parent of project root
            "src/main/resources/.env"                 // Resources directory (non-standard but check anyway)
        };
        
        for (String path : searchPaths) {
            File file = new File(path);
            if (file.exists() && file.isFile()) {
                logger.debug("Found .env file at: {}", file.getAbsolutePath());
                return file;
            }
        }
        
        return null;
    }
    
    private Map<String, Object> parseEnvFile(File envFile) throws IOException {
        Map<String, Object> properties = new HashMap<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(envFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                // Skip empty lines and comments
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                
                // Parse KEY=VALUE format
                int equalsIndex = line.indexOf('=');
                if (equalsIndex > 0) {
                    String key = line.substring(0, equalsIndex).trim();
                    String value = line.substring(equalsIndex + 1).trim();
                    
                    // Remove quotes if present
                    if ((value.startsWith("\"") && value.endsWith("\"")) ||
                        (value.startsWith("'") && value.endsWith("'"))) {
                        value = value.substring(1, value.length() - 1);
                    }
                    
                    if (!key.isEmpty() && !value.isEmpty()) {
                        properties.put(key, value);
                        logger.debug("Loaded property: {} = {}", key, maskSensitiveValue(key, value));
                    }
                }
            }
        }
        
        return properties;
    }
    
    private String maskSensitiveValue(String key, String value) {
        // Mask sensitive values like API keys
        if (key.toUpperCase().contains("KEY") || key.toUpperCase().contains("SECRET") || 
            key.toUpperCase().contains("PASSWORD") || key.toUpperCase().contains("TOKEN")) {
            if (value.length() > 10) {
                return value.substring(0, 6) + "..." + value.substring(value.length() - 4);
            }
            return "***";
        }
        return value;
    }
}

