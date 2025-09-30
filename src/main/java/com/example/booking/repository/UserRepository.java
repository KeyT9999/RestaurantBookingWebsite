package com.example.booking.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // ===== Basic finders =====
    Optional<User> findByUsername(String username);
    Optional<User> findByUsernameIgnoreCase(String username);

    Optional<User> findByEmail(String email);
    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByGoogleId(String googleId);

    Optional<User> findByEmailVerificationToken(String token);
    Optional<User> findByPasswordResetToken(String token);

    // ===== Exists check =====
    boolean existsByUsername(String username);
    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByUsernameIgnoreCaseAndIdNot(String username, UUID id);

    boolean existsByEmail(String email);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, UUID id);

    // ===== Password reset token validation =====
    @Query("SELECT u FROM User u WHERE u.passwordResetToken = :token AND u.passwordResetTokenExpiry > :now")
    Optional<User> findByValidPasswordResetToken(@Param("token") String token, @Param("now") LocalDateTime now);

    // ===== Unverified users =====
    @Query("SELECT u FROM User u WHERE u.emailVerified = false AND u.createdAt < :cutoffTime")
    List<User> findUnverifiedUsersOlderThan(@Param("cutoffTime") LocalDateTime cutoffTime);

    // ===== Soft delete aware queries =====
    @Query("SELECT u FROM User u WHERE u.username = :username AND u.deletedAt IS NULL")
    Optional<User> findByUsernameAndNotDeleted(@Param("username") String username);

    @Query("SELECT u FROM User u WHERE u.email = :email AND u.deletedAt IS NULL")
    Optional<User> findByEmailAndNotDeleted(@Param("email") String email);

    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL")
    Page<User> findAllActive(Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.role = :role AND u.deletedAt IS NULL")
    Page<User> findByRoleAndNotDeleted(@Param("role") UserRole role, Pageable pageable);

    @Query("SELECT u FROM User u WHERE (u.username LIKE %:search% OR u.email LIKE %:search%) AND u.deletedAt IS NULL")
    Page<User> findByUsernameOrEmailContainingAndNotDeleted(@Param("search") String search, Pageable pageable);

    // ===== Pageable admin queries =====
    Page<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(String username, String email, Pageable pageable);

    Page<User> findByRole(UserRole role, Pageable pageable);

    Page<User> findByRoleAndUsernameContainingIgnoreCaseOrRoleAndEmailContainingIgnoreCase(
        UserRole role1, String username,
        UserRole role2, String email,
        Pageable pageable
    );

    long countByRole(UserRole role);
}
