package com.example.booking.service;

import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2UserServiceTest {

    @Mock
    private SimpleUserService simpleUserService;

    @Mock
    private OAuth2UserRequest userRequest;

    @InjectMocks
    private OAuth2UserService oAuth2UserService;

    private OAuth2User oAuth2User;
    private User user;
    private Map<String, Object> attributes;

    @BeforeEach
    void setUp() {
        attributes = new HashMap<>();
        attributes.put("email", "test@example.com");
        attributes.put("name", "Test User");
        attributes.put("sub", "google-id-123");

        oAuth2User = new DefaultOAuth2User(
                Collections.emptyList(),
                attributes,
                "email"
        );

        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");
        user.setFullName("Test User");
        user.setRole(UserRole.CUSTOMER);
        user.setEmailVerified(true);
    }

    @Test
    void shouldLoadUser_Success() throws OAuth2AuthenticationException {
        when(simpleUserService.upsertGoogleUser("google-id-123", "test@example.com", "Test User"))
                .thenReturn(user);

        // Use reflection or create a spy to mock super.loadUser
        OAuth2UserService spyService = spy(oAuth2UserService);
        doReturn(oAuth2User).when(spyService).loadUser(any(OAuth2UserRequest.class));

        OAuth2User result = spyService.loadUser(userRequest);

        assertThat(result).isNotNull();
        assertThat(result.getAttributes()).containsEntry("email", "test@example.com");
        verify(simpleUserService).upsertGoogleUser("google-id-123", "test@example.com", "Test User");
    }

    @Test
    void shouldLoadUser_MissingEmail() throws OAuth2AuthenticationException {
        Map<String, Object> invalidAttributes = new HashMap<>();
        invalidAttributes.put("name", "Test User");
        invalidAttributes.put("sub", "google-id-123");

        OAuth2User invalidOAuth2User = new DefaultOAuth2User(
                Collections.emptyList(),
                invalidAttributes,
                "email"
        );

        OAuth2UserService spyService = spy(oAuth2UserService);
        doReturn(invalidOAuth2User).when(spyService).loadUser(any(OAuth2UserRequest.class));

        assertThatThrownBy(() -> spyService.loadUser(userRequest))
                .isInstanceOf(OAuth2AuthenticationException.class)
                .hasMessageContaining("Email not found");
    }

    @Test
    void shouldLoadUser_EmptyEmail() throws OAuth2AuthenticationException {
        Map<String, Object> emptyEmailAttributes = new HashMap<>();
        emptyEmailAttributes.put("email", "");
        emptyEmailAttributes.put("name", "Test User");
        emptyEmailAttributes.put("sub", "google-id-123");

        OAuth2User emptyEmailOAuth2User = new DefaultOAuth2User(
                Collections.emptyList(),
                emptyEmailAttributes,
                "email"
        );

        OAuth2UserService spyService = spy(oAuth2UserService);
        doReturn(emptyEmailOAuth2User).when(spyService).loadUser(any(OAuth2UserRequest.class));

        assertThatThrownBy(() -> spyService.loadUser(userRequest))
                .isInstanceOf(OAuth2AuthenticationException.class)
                .hasMessageContaining("Email not found");
    }

    @Test
    void shouldLoadUser_ServiceException() throws OAuth2AuthenticationException {
        OAuth2UserService spyService = spy(oAuth2UserService);
        doReturn(oAuth2User).when(spyService).loadUser(any(OAuth2UserRequest.class));
        when(simpleUserService.upsertGoogleUser(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Database error"));

        assertThatThrownBy(() -> spyService.loadUser(userRequest))
                .isInstanceOf(OAuth2AuthenticationException.class)
                .hasMessageContaining("Error processing OAuth2 user");
    }
}

