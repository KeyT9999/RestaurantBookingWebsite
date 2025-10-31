package com.example.booking.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

public class SecurityUtilsTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void constructorShouldThrowUnsupportedOperationException() throws Exception {
        Constructor<SecurityUtils> constructor = SecurityUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        InvocationTargetException thrown = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertTrue(thrown.getCause() instanceof UnsupportedOperationException);
    }

    @Test
    void getCurrentUserLoginShouldReturnUsernameFromUserDetails() {
        UserDetails userDetails = User.withUsername("alice")
            .password("password")
            .authorities("ROLE_USER")
            .build();

        Authentication authentication = new TestingAuthenticationToken(userDetails, null, "ROLE_USER");
        setAuthentication(authentication);

        Optional<String> result = SecurityUtils.getCurrentUserLogin();

        assertTrue(result.isPresent());
        assertEquals("alice", result.get());
    }

    @Test
    void getCurrentUserLoginShouldReturnUsernameWhenPrincipalIsString() {
        Authentication authentication = new TestingAuthenticationToken("bob", null, "ROLE_ADMIN");
        setAuthentication(authentication);

        Optional<String> result = SecurityUtils.getCurrentUserLogin();

        assertTrue(result.isPresent());
        assertEquals("bob", result.get());
    }

    @Test
    void getCurrentUserLoginShouldReturnEmptyWhenAuthenticationMissing() {
        SecurityContextHolder.clearContext();

        Optional<String> result = SecurityUtils.getCurrentUserLogin();

        assertTrue(result.isEmpty());
    }

    @Test
    void isAuthenticatedShouldReflectAuthenticationState() {
        assertFalse(SecurityUtils.isAuthenticated());

        Authentication authentication = new TestingAuthenticationToken("bob", null, "ROLE_ADMIN");
        authentication.setAuthenticated(true);
        setAuthentication(authentication);

        assertTrue(SecurityUtils.isAuthenticated());
    }

    @Test
    void isAuthenticatedShouldReturnFalseWhenAuthenticationIsPresentButNotAuthenticated() {
        Authentication authentication = new TestingAuthenticationToken("bob", null, "ROLE_ADMIN");
        authentication.setAuthenticated(false);
        setAuthentication(authentication);

        assertFalse(SecurityUtils.isAuthenticated());
    }

    private void setAuthentication(Authentication authentication) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }
}

