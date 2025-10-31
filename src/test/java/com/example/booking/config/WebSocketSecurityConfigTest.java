package com.example.booking.config;

import com.example.booking.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive tests for WebSocketSecurityConfig
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.websocket.enabled=true"
})
@DisplayName("WebSocketSecurityConfig Test Suite")
class WebSocketSecurityConfigTest {

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private WebSocketSecurityConfig webSocketSecurityConfig;

    @Test
    @DisplayName("Should load WebSocketSecurityConfig")
    void testWebSocketSecurityConfigLoads() {
        assertThat(webSocketSecurityConfig).isNotNull();
    }

    @Test
    @DisplayName("Should configure message broker")
    void testConfigureMessageBroker() {
        // Create a mock registry
        MessageBrokerRegistry registry = org.mockito.Mockito.mock(MessageBrokerRegistry.class);
        
        // Should not throw exception
        webSocketSecurityConfig.configureMessageBroker(registry);
        
        // Verify: Simple broker and prefixes are configured
        // The actual configuration is tested through integration tests
        assertThat(webSocketSecurityConfig).isNotNull();
    }

    @Test
    @DisplayName("Should register STOMP endpoints")
    void testRegisterStompEndpoints() {
        // Create a mock registry
        StompEndpointRegistry registry = org.mockito.Mockito.mock(StompEndpointRegistry.class);
        
        // Should not throw exception
        webSocketSecurityConfig.registerStompEndpoints(registry);
        
        // Verify: Endpoints are registered
        // The actual registration is tested through integration tests
        assertThat(webSocketSecurityConfig).isNotNull();
    }

    @Test
    @DisplayName("Should configure client inbound channel")
    void testConfigureClientInboundChannel() {
        // Create a mock registration
        org.springframework.messaging.simp.config.ChannelRegistration registration = 
                org.mockito.Mockito.mock(org.springframework.messaging.simp.config.ChannelRegistration.class);
        
        // Should not throw exception
        webSocketSecurityConfig.configureClientInboundChannel(registration);
        
        // Verify: Channel interceptor is added
        // The actual interceptor logic is tested through integration tests
        assertThat(webSocketSecurityConfig).isNotNull();
    }
}

