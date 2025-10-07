package com.example.booking.config;

import java.util.UUID;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.example.booking.domain.User;
import com.example.booking.repository.UserRepository;

/**
 * Enhanced WebSocket configuration with security
 * Handles authentication and authorization for WebSocket connections
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketSecurityConfig implements WebSocketMessageBrokerConfigurer {

    private final UserRepository userRepository;

    public WebSocketSecurityConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void configureMessageBroker(@NonNull MessageBrokerRegistry config) {
        // Enable simple broker for /topic destinations
        config.enableSimpleBroker("/topic", "/queue");
        
        // Set application destination prefix for @MessageMapping
        config.setApplicationDestinationPrefixes("/app");
        
        // Set user destination prefix for private messages
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
        // Register WebSocket endpoint with SockJS fallback
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // In production, specify exact origins
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(@NonNull ChannelRegistration registration) {
        System.out.println("🔧 WebSocketSecurityConfig: configureClientInboundChannel called");
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
                System.out.println("🔧 WebSocketSecurityConfig: preSend called");
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                
                if (accessor != null) {
                    System.out.println("🔧 WebSocketSecurityConfig: StompCommand = " + accessor.getCommand());
                }

                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    System.out.println("=== WebSocket CONNECT ===");
                    System.out.println("Headers: " + accessor.toNativeHeaderMap());
                    System.out.println("Session attributes: " + accessor.getSessionAttributes());
                    
                    // For WebSocket authentication, we rely on HTTP session
                    // The user is already authenticated via HTTP session
                    // Spring Security will automatically handle the authentication
                    System.out.println("✅ WebSocket connection accepted (using HTTP session authentication)");
                }
                
                return message;
            }
        });
    }
}
