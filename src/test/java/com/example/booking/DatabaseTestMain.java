package com.example.booking;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseTestMain {
    
    public static void main(String[] args) {
        System.out.println("🔍 Testing Database Connection and Users...");
        
        // Database connection details
        String url = "jdbc:postgresql://dpg-d37uh8ruibrs739c7sf0-a.singapore-postgres.render.com:5432/bookeat_db";
        String username = "bookeat_user";
        String password = "58IJhzpjEobFFUmJ762dmkEdSLPPRbZX";
        
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            System.out.println("✅ Database connection successful!");
            
            // Test basic connection
            testBasicConnection(connection);
            
            // Get all users
            getAllUsers(connection);
            
            // Get users with specific roles
            getUsersByRole(connection);
            
        } catch (SQLException e) {
            System.err.println("❌ Database connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void testBasicConnection(Connection connection) throws SQLException {
        System.out.println("\n📊 Testing basic connection...");
        
        String query = "SELECT COUNT(*) as total FROM users";
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                int total = rs.getInt("total");
                System.out.println("✅ Total users in database: " + total);
            }
        }
    }
    
    private static void getAllUsers(Connection connection) throws SQLException {
        System.out.println("\n👥 Getting all users...");
        
        String query = """
            SELECT 
                id,
                username,
                email,
                full_name,
                phone_number,
                role,
                active,
                email_verified,
                created_at,
                updated_at,
                last_login,
                google_id
            FROM users 
            ORDER BY created_at DESC
            """;
        
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            int count = 0;
            while (rs.next()) {
                count++;
                System.out.println("\n--- User #" + count + " ---");
                System.out.println("ID: " + rs.getString("id"));
                System.out.println("Username: " + rs.getString("username"));
                System.out.println("Email: " + rs.getString("email"));
                System.out.println("Full Name: " + rs.getString("full_name"));
                System.out.println("Phone: " + rs.getString("phone_number"));
                System.out.println("Role: " + rs.getString("role"));
                System.out.println("Active: " + rs.getBoolean("active"));
                System.out.println("Email Verified: " + rs.getBoolean("email_verified"));
                System.out.println("Created: " + rs.getTimestamp("created_at"));
                System.out.println("Updated: " + rs.getTimestamp("updated_at"));
                System.out.println("Last Login: " + rs.getTimestamp("last_login"));
                System.out.println("Google ID: " + rs.getString("google_id"));
            }
            
            System.out.println("\n✅ Found " + count + " users total");
        }
    }
    
    private static void getUsersByRole(Connection connection) throws SQLException {
        System.out.println("\n🎭 Getting users by role...");
        
        String[] roles = {"admin", "customer", "restaurant_owner", "RESTAURANT"};
        
        for (String role : roles) {
            String query = "SELECT COUNT(*) as count FROM users WHERE role = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, role);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int count = rs.getInt("count");
                        System.out.println("Role '" + role + "': " + count + " users");
                    }
                }
            }
        }
        
        // Get users with NULL or empty roles
        String query = "SELECT COUNT(*) as count FROM users WHERE role IS NULL OR role = ''";
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                int count = rs.getInt("count");
                System.out.println("Role NULL/Empty: " + count + " users");
            }
        }
        
        // Get all unique roles
        System.out.println("\n🔍 All unique roles in database:");
        query = "SELECT DISTINCT role FROM users ORDER BY role";
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String role = rs.getString("role");
                System.out.println("- '" + role + "'");
            }
        }
    }
} 