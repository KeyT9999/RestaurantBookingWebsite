package com.example.booking.web.controller;

import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.repository.UserRepository;
import com.example.booking.service.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AdminChatController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
        com.example.booking.config.AuthRateLimitFilter.class,
        com.example.booking.config.GeneralRateLimitFilter.class,
        com.example.booking.config.LoginRateLimitFilter.class,
        com.example.booking.config.PermanentlyBlockedIpFilter.class,
        com.example.booking.web.advice.NotificationHeaderAdvice.class
    }),
    excludeAutoConfiguration = {org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AdminChatController WebMvc Tests - Full Branch Coverage")
class AdminChatControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatService chatService;

    @MockBean
    private UserRepository userRepository;

    private User adminUser;
    private User regularUser;

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setId(UUID.randomUUID());
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@test.com");
        adminUser.setFullName("Admin User");
        adminUser.setRole(UserRole.ADMIN);

        regularUser = new User();
        regularUser.setId(UUID.randomUUID());
        regularUser.setUsername("user");
        regularUser.setEmail("user@test.com");
        regularUser.setFullName("Regular User");
        regularUser.setRole(UserRole.CUSTOMER);
    }

    // ========== Prompt 1: Happy path (ADMIN) ==========
    
    @Test
    @DisplayName("GET /admin/chat - Happy path with ADMIN role")
    void testAdminChatPage_HappyPath_Admin() throws Exception {
        // Given - Build TestingAuthenticationToken with principal = User(ADMIN)
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(adminUser, null, "ROLE_ADMIN");
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        try {
            // When & Then
            mockMvc.perform(get("/admin/chat"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/chat"))
                .andExpect(model().attributeExists("admin"))
                .andExpect(model().attributeExists("adminId"))
                .andExpect(model().attributeExists("adminName"))
                .andExpect(model().attributeExists("adminEmail"))
                .andExpect(model().attribute("adminId", adminUser.getId()))
                .andExpect(model().attribute("adminName", adminUser.getFullName()))
                .andExpect(model().attribute("adminEmail", adminUser.getEmail()));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    // ========== Prompt 2: Access denied (USER role) ==========

    @Test
    @DisplayName("GET /admin/chat - Access denied when role is USER")
    void testAdminChatPage_AccessDenied_UserRole() throws Exception {
        // Given - Principal role = USER
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(regularUser, null, "ROLE_USER");
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        try {
            // When & Then
            mockMvc.perform(get("/admin/chat"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/error?message=Access denied. Admin role required."));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    // ========== Prompt 3: Auth null → catch/redirect ==========

    @Test
    @DisplayName("GET /admin/chat - Redirect error when authentication is null")
    void testAdminChatPage_AuthNull_RedirectError() throws Exception {
        // Given - Clear security context (no authentication)
        SecurityContextHolder.clearContext();

        // When & Then
        mockMvc.perform(get("/admin/chat"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrlPattern("/error?message=Error loading admin chat: *"));
    }

    // ========== Prompt 4: Principal là User trực tiếp ==========

    @Test
    @DisplayName("GET /admin/chat - Principal is User directly")
    void testAdminChatPage_PrincipalIsUser() throws Exception {
        // Given - Build TestingAuthenticationToken with principal = User(ADMIN)
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(adminUser, null, "ROLE_ADMIN");
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        try {
            // When & Then - This covers branch: principal instanceof User
            mockMvc.perform(get("/admin/chat"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/chat"))
                .andExpect(model().attributeExists("admin"));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    // ========== Prompt 5: Principal là UsernamePasswordAuthenticationToken (User) ==========

    @Test
    @DisplayName("GET /admin/chat - Principal is UsernamePasswordAuthenticationToken with User")
    void testAdminChatPage_PrincipalIsUsernamePasswordToken_WithUser() throws Exception {
        // Given - UsernamePasswordAuthenticationToken whose principal is User
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
            adminUser, null, adminUser.getAuthorities());
        securityContext.setAuthentication(authToken);
        SecurityContextHolder.setContext(securityContext);

        try {
            // When & Then - This covers branch: principal instanceof UsernamePasswordAuthenticationToken
            // and authPrincipal instanceof User
            mockMvc.perform(get("/admin/chat"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/chat"))
                .andExpect(model().attributeExists("admin"));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    // ========== Prompt 6: Principal là UsernamePasswordAuthenticationToken (String username) ==========

    @Test
    @DisplayName("GET /admin/chat - Principal is UsernamePasswordAuthenticationToken with String username (success)")
    void testAdminChatPage_PrincipalIsUsernamePasswordToken_WithUsername_Success() throws Exception {
        // Given - Token where principal is a username (email)
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
            "admin@test.com", null, java.util.Collections.emptyList());
        securityContext.setAuthentication(authToken);
        SecurityContextHolder.setContext(securityContext);
        
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(adminUser));

        try {
            // When & Then - This covers branch: UsernamePasswordAuthenticationToken with String principal
            mockMvc.perform(get("/admin/chat"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/chat"))
                .andExpect(model().attributeExists("admin"));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    @DisplayName("GET /admin/chat - Principal is UsernamePasswordAuthenticationToken with String username (not found)")
    void testAdminChatPage_PrincipalIsUsernamePasswordToken_WithUsername_NotFound() throws Exception {
        // Given - Token where principal is username, but repository returns empty
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
            "notfound@test.com", null, java.util.Collections.emptyList());
        securityContext.setAuthentication(authToken);
        SecurityContextHolder.setContext(securityContext);
        
        when(userRepository.findByEmail("notfound@test.com")).thenReturn(Optional.empty());

        try {
            // When & Then - Should redirect to error (RuntimeException: User not found)
            mockMvc.perform(get("/admin/chat"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/error?message=Error loading admin chat: *"));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    // ========== Prompt 7: Unsupported principal type ==========

    @Test
    @DisplayName("GET /admin/chat - Unsupported principal type")
    void testAdminChatPage_UnsupportedPrincipalType() throws Exception {
        // Given - Custom principal type (not User nor UsernamePasswordAuthenticationToken)
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(
            new Object(), null, "ROLE_ADMIN"); // Principal is Object, not User
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        try {
            // When & Then - Should hit "Unsupported authentication type" branch
            mockMvc.perform(get("/admin/chat"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/error?message=Error loading admin chat: *"));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    // ========== Additional: Test with UserRole.admin (lowercase) ==========

    @Test
    @DisplayName("GET /admin/chat - Should accept UserRole.admin (lowercase)")
    void testAdminChatPage_UserRoleAdmin_Lowercase() throws Exception {
        // Given - User with role = admin (lowercase enum)
        User adminLowercase = new User();
        adminLowercase.setId(UUID.randomUUID());
        adminLowercase.setUsername("admin2");
        adminLowercase.setEmail("admin2@test.com");
        adminLowercase.setFullName("Admin 2");
        adminLowercase.setRole(UserRole.admin); // lowercase admin

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(adminLowercase, null, "ROLE_ADMIN");
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        try {
            // When & Then
            mockMvc.perform(get("/admin/chat"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/chat"));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
