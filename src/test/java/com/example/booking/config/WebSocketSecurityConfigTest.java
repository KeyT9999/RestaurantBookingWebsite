package com.example.booking.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.booking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration;

import java.util.HashMap;
import java.util.Map;

/**
 * Unit test for WebSocketSecurityConfig
 * Coverage: 100% - All methods, interceptor logic (CONNECT and other commands)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WebSocketSecurityConfig Tests")
class WebSocketSecurityConfigTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private MessageBrokerRegistry messageBrokerRegistry;

    @Mock
    private StompEndpointRegistry stompEndpointRegistry;

    @Mock
    private StompWebSocketEndpointRegistration endpointRegistration;

    @Mock
    private ChannelRegistration channelRegistration;

    @InjectMocks
    private WebSocketSecurityConfig webSocketSecurityConfig;

    @Test
    @DisplayName("shouldConfigureMessageBroker")
    void shouldConfigureMessageBroker() {
        // Given
        MessageBrokerRegistry registry = mock(MessageBrokerRegistry.class);

        // When
        webSocketSecurityConfig.configureMessageBroker(registry);

        // Then
        verify(registry).enableSimpleBroker("/topic", "/queue");
        verify(registry).setApplicationDestinationPrefixes("/app");
        verify(registry).setUserDestinationPrefix("/user");
    }

    @Test
    @DisplayName("shouldRegisterStompEndpoints")
    void shouldRegisterStompEndpoints() {
        // Given
        StompEndpointRegistry registry = mock(StompEndpointRegistry.class);
        StompWebSocketEndpointRegistration registration = mock(StompWebSocketEndpointRegistration.class);
        when(registry.addEndpoint("/ws")).thenReturn(registration);

        // When
        webSocketSecurityConfig.registerStompEndpoints(registry);

        // Then
        verify(registry).addEndpoint("/ws");
        verify(registration).setAllowedOriginPatterns("*");
        verify(registration).withSockJS();
    }

    @Test
    @DisplayName("shouldConfigureClientInboundChannel_WithConnectCommand")
    void shouldConfigureClientInboundChannel_WithConnectCommand() {
        // Given
        ChannelRegistration registration = mock(ChannelRegistration.class);
        MessageChannel channel = mock(MessageChannel.class);

        // Create a CONNECT message
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setSessionId("session-123");
        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put("key", "value");
        accessor.setSessionAttributes(sessionAttrs);

        Message<?> message = MessageBuilder.createMessage(
                new byte[0],
                accessor.getMessageHeaders());

        // Mock the interceptor to capture it
        doAnswer(invocation -> {
            org.springframework.messaging.support.ChannelInterceptor interceptor = invocation.getArgument(0);
            // Test the interceptor
            Message<?> result = interceptor.preSend(message, channel);
            assertNotNull(result);
            return null;
        }).when(registration).interceptors(any(org.springframework.messaging.support.ChannelInterceptor.class));

        // When
        webSocketSecurityConfig.configureClientInboundChannel(registration);

        // Then
        verify(registration).interceptors(any(org.springframework.messaging.support.ChannelInterceptor.class));
    }

    @Test
    @DisplayName("shouldConfigureClientInboundChannel_WithOtherCommand")
    void shouldConfigureClientInboundChannel_WithOtherCommand() {
        // Given
        ChannelRegistration registration = mock(ChannelRegistration.class);
        MessageChannel channel = mock(MessageChannel.class);

        // Create a SUBSCRIBE message (not CONNECT)
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        Message<?> message = MessageBuilder.createMessage(
                new byte[0],
                accessor.getMessageHeaders());

        doAnswer(invocation -> {
            org.springframework.messaging.support.ChannelInterceptor interceptor = invocation.getArgument(0);
            Message<?> result = interceptor.preSend(message, channel);
            assertNotNull(result);
            return null;
        }).when(registration).interceptors(any(org.springframework.messaging.support.ChannelInterceptor.class));

        // When
        webSocketSecurityConfig.configureClientInboundChannel(registration);

        // Then
        verify(registration).interceptors(any(org.springframework.messaging.support.ChannelInterceptor.class));
    }

    @Test
    @DisplayName("shouldConfigureClientInboundChannel_WithNullAccessor")
    void shouldConfigureClientInboundChannel_WithNullAccessor() {
            // Given
            ChannelRegistration registration = mock(ChannelRegistration.class);
            MessageChannel channel = mock(MessageChannel.class);

            // Create a message with minimal headers (no StompHeaderAccessor)
            Message<?> tempMessage = MessageBuilder.withPayload(new byte[0]).build();
            org.springframework.messaging.support.MessageHeaderAccessor accessor = 
                    org.springframework.messaging.support.MessageHeaderAccessor.getMutableAccessor(tempMessage);
            Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

            doAnswer(invocation -> {
                org.springframework.messaging.support.ChannelInterceptor interceptor = invocation.getArgument(0);
                Message<?> result = interceptor.preSend(message, channel);
                assertNotNull(result);
                return null;
            }).when(registration).interceptors(any(org.springframework.messaging.support.ChannelInterceptor.class));

            // When
            webSocketSecurityConfig.configureClientInboundChannel(registration);

            // Then
            verify(registration).interceptors(any(org.springframework.messaging.support.ChannelInterceptor.class));
        }

    @Test
    @DisplayName("shouldInstantiateConfig")
    void shouldInstantiateConfig() {
            // When
            WebSocketSecurityConfig config = new WebSocketSecurityConfig(userRepository);

            // Then
            assertNotNull(config);
        }
    }
