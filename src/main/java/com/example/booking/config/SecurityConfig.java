package com.example.booking.config;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;

import com.example.booking.domain.User;
import com.example.booking.service.SimpleUserService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    private final UserDetailsService userDetailsService;
    private final OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService;
    private final OidcUserService oidcUserService;
    
    @Autowired
    public SecurityConfig(@Lazy UserDetailsService userDetailsService, 
                         @Lazy OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService,
                         @Lazy OidcUserService oidcUserService) {
        this.userDetailsService = userDetailsService;
        this.oAuth2UserService = oAuth2UserService;
        this.oidcUserService = oidcUserService;
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/css/**", "/js/**", "/images/**", "/uploads/**", 
                               "/login", "/error", "/h2-console/**", 
                               "/actuator/**", "/oauth2/**", "/", "/about", "/contact", "/restaurants").permitAll()
                .requestMatchers("/auth/register", "/auth/register-success", "/auth/verify-email", 
                               "/auth/verify-result", "/auth/forgot-password", "/auth/reset-password").permitAll()
                .requestMatchers("/auth/**").authenticated()
                .requestMatchers("/booking/**").authenticated()
                .anyRequest().authenticated()
            )
            .userDetailsService(userDetailsService)
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .permitAll()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(oAuth2UserService)
                    .oidcUserService(oidcUserService)
                )
                .defaultSuccessUrl("/", true)
                .permitAll()
            )
            .exceptionHandling(exceptions -> exceptions
                .accessDeniedPage("/403")
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .permitAll()
            )
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**")
                .disable())
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.sameOrigin()));
        
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // OAuth2 (non-OIDC) user service
    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService(SimpleUserService userService) {
        return request -> {
            DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
            OAuth2User oauth2User = delegate.loadUser(request);

            try { System.out.println("🔎 OAuth2 attrs: " + oauth2User.getAttributes()); } catch (Exception ignore) {}

            String email = oauth2User.getAttribute("email");
            String name = oauth2User.getAttribute("name");
            String googleSub = oauth2User.getAttribute("sub");

            User user = userService.upsertGoogleUser(googleSub, email, name);
            System.out.println("✅ OAuth2 upserted user id=" + user.getId());

            Set<? extends GrantedAuthority> authorities = (Set<? extends GrantedAuthority>) user.getAuthorities();
            return new DefaultOAuth2User(authorities, oauth2User.getAttributes(), "email");
        };
    }

    // OIDC (Google) user service – đây là service sẽ được Spring gọi khi scope có 'openid'
    @Bean
    public OidcUserService oidcUserService(SimpleUserService userService) {
        return new OidcUserService() {
            @Override
            public OidcUser loadUser(OidcUserRequest userRequest) {
                OidcUser oidcUser = super.loadUser(userRequest);
                try { System.out.println("🔎 OIDC attrs: " + oidcUser.getAttributes()); } catch (Exception ignore) {}
                String sub = oidcUser.getSubject();
                String email = (String) oidcUser.getAttributes().get("email");
                String name = (String) oidcUser.getAttributes().get("name");
                User user = userService.upsertGoogleUser(sub, email, name);
                System.out.println("✅ OIDC upserted user id=" + user.getId());
                return new DefaultOidcUser(user.getAuthorities(), userRequest.getIdToken(), oidcUser.getUserInfo(), "email");
            }
        };
    }
}