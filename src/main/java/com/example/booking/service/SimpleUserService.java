package com.example.booking.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.dto.ProfileEditForm;
import com.example.booking.dto.RegisterForm;
import com.example.booking.repository.UserRepository;

@Service("simpleUserService")
@Transactional
public class SimpleUserService implements UserDetailsService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    
    @Autowired
    public SimpleUserService(UserRepository userRepository, 
                           PasswordEncoder passwordEncoder,
                           EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }
    
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("User không tồn tại: " + username));
    }
    
    public User registerUser(RegisterForm form) {
        // Validate form
        if (!form.isPasswordMatching()) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp");
        }
        
        if (userRepository.existsByUsernameIgnoreCase(form.getUsername())) {
            throw new IllegalArgumentException("Username đã tồn tại");
        }
        
        if (userRepository.existsByEmailIgnoreCase(form.getEmail())) {
            throw new IllegalArgumentException("Email đã được sử dụng");
        }
        
        // Create user
        User user = new User();
        user.setUsername(form.getUsername());
        user.setEmail(form.getEmail());
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        user.setFullName(form.getFullName());
        user.setPhoneNumber(form.getPhoneNumber());
        user.setRole(UserRole.CUSTOMER);
        user.setEmailVerified(false); // Require email verification
        
        // Generate email verification token
        String verificationToken = UUID.randomUUID().toString();
        user.setEmailVerificationToken(verificationToken);
        
        User savedUser = userRepository.save(user);
        
        // Send verification email
        emailService.sendVerificationEmail(savedUser.getEmail(), verificationToken);
        
        System.out.println("✅ User registered successfully: " + savedUser.getUsername());
        System.out.println("📧 Verification email sent to: " + savedUser.getEmail());
        
        return savedUser;
    }
    
    @Transactional(readOnly = true)
    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User không tồn tại"));
    }
    
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        if (email == null) return Optional.empty();
        return userRepository.findByEmailIgnoreCase(email);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        if (username == null)
            return Optional.empty();
        return userRepository.findByUsernameIgnoreCase(username);
    }

    @Transactional(readOnly = true)
    public Optional<User> getCurrentUser() {
        // Removed security-related imports, so this method is no longer functional
        // as it relies on SecurityContextHolder.getContext().getAuthentication()
        // and findByUsername(authentication.getName()).
        // For now, returning empty as a placeholder.
        return Optional.empty();
    }

    @Transactional(readOnly = true)
    public Optional<User> findByGoogleId(String googleId) {
        return userRepository.findByGoogleId(googleId);
    }
    
    public boolean verifyEmail(String token) {
        Optional<User> userOpt = userRepository.findByEmailVerificationToken(token);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setEmailVerified(true);
            user.setEmailVerificationToken(null); // Clear token after verification
            userRepository.save(user);
            System.out.println("✅ Email verified for user: " + user.getEmail());
            return true;
        }
        System.out.println("❌ Invalid verification token: " + token);
        return false;
    }
    
    public void sendPasswordResetToken(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String resetToken = UUID.randomUUID().toString();
            user.setPasswordResetToken(resetToken);
            user.setPasswordResetTokenExpiry(LocalDateTime.now().plusHours(1)); // 1 hour expiry
            
            userRepository.save(user);
            emailService.sendPasswordResetEmail(user.getEmail(), resetToken);
            
            System.out.println("✅ Password reset token sent to: " + email);
        } else {
            System.out.println("❌ No user found with email: " + email);
            // Don't throw exception for security - don't reveal if email exists
        }
    }
    
    public boolean resetPassword(String token, String newPassword) {
        Optional<User> userOpt = userRepository.findByValidPasswordResetToken(token, LocalDateTime.now());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setPasswordResetToken(null);
            user.setPasswordResetTokenExpiry(null);
            userRepository.save(user);
            
            System.out.println("✅ Password reset successfully for user: " + user.getEmail());
            return true;
        }
        System.out.println("❌ Invalid or expired reset token: " + token);
        return false;
    }
    
    public boolean changePassword(User user, String currentPassword, String newPassword) {
        if (passwordEncoder.matches(currentPassword, user.getPassword())) {
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            System.out.println("✅ Password changed successfully for user: " + user.getUsername());
            return true;
        }
        System.out.println("❌ Current password incorrect for user: " + user.getUsername());
        return false;
    }
    
    public User updateProfile(User user, ProfileEditForm form) {
        user.setFullName(form.getFullName());
        user.setPhoneNumber(form.getPhoneNumber());
        user.setAddress(form.getAddress());
        user.setUpdatedAt(LocalDateTime.now());
        
        User updatedUser = userRepository.save(user);
        System.out.println("✅ Profile updated for user: " + user.getUsername());
        return updatedUser;
    }
    
    public User updateProfileImage(User user, String imageUrl) {
        user.setProfileImageUrl(imageUrl);
        user.setUpdatedAt(LocalDateTime.now());
        
        User updatedUser = userRepository.save(user);
        System.out.println("✅ Profile image updated for user: " + user.getUsername());
        return updatedUser;
    }
    
    public User upsertGoogleUser(String googleId, String email, String name) {
        // Try to find existing user by Google ID first
        Optional<User> existingUser = userRepository.findByGoogleId(googleId);
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            user.setLastLogin(LocalDateTime.now());
            User saved = userRepository.save(user);
            System.out.println("✅ Google upsert (by googleId) id=" + saved.getId());
            return saved;
        }
        
        // Try to find existing user by email
        Optional<User> userByEmail = userRepository.findByEmailIgnoreCase(email);
        if (userByEmail.isPresent()) {
            User user = userByEmail.get();
            user.setGoogleId(googleId); // Link Google account
            user.setEmailVerified(true); // Google accounts are verified
            user.setLastLogin(LocalDateTime.now());
            if (user.getRole() == null) user.setRole(UserRole.CUSTOMER);
            User saved = userRepository.save(user);
            System.out.println("✅ Google upsert (link by email) id=" + saved.getId());
            return saved;
        }
        
        // Create new user
        User newUser = new User();
        String normalizedEmail = email != null ? email.toLowerCase() : null;
        newUser.setUsername(normalizedEmail != null ? normalizedEmail : googleId);
        newUser.setEmail(normalizedEmail);
        newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        newUser.setFullName(name != null ? name : "Google User");
        newUser.setGoogleId(googleId);
        newUser.setRole(UserRole.CUSTOMER);
        newUser.setEmailVerified(true);
        newUser.setLastLogin(LocalDateTime.now());
        
        User savedUser = userRepository.save(newUser);
        System.out.println("✅ New Google user created id=" + savedUser.getId() + " email=" + savedUser.getEmail());
        return savedUser;
    }
} 