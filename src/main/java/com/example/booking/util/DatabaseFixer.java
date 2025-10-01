package com.example.booking.util;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * Utility to fix database schema - makes owner_id nullable
 * Run this using: java -cp target/classes com.example.booking.util.DatabaseFixer
 * Or use the provided scripts: fix-database.bat / fix-database.ps1
 */
// @SpringBootApplication(scanBasePackages = "com.example.booking")
public class DatabaseFixer {

    public static void main(String[] args) {
        SpringApplication.run(DatabaseFixer.class, args);
    }

    @Bean
    CommandLineRunner fixDatabase(DataSource dataSource) {
        return args -> {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            
            System.out.println("========================================");
            System.out.println("🔧 FIXING DATABASE SCHEMA");
            System.out.println("========================================");
            System.out.println();
            
            try {
                // Check current state
                System.out.println("📊 Checking current owner_id constraint...");
                String checkSql = "SELECT is_nullable FROM information_schema.columns " +
                                "WHERE table_name = 'restaurant_profile' AND column_name = 'owner_id'";
                String isNullable = jdbcTemplate.queryForObject(checkSql, String.class);
                System.out.println("   Current is_nullable: " + isNullable);
                System.out.println();
                
                if ("NO".equals(isNullable)) {
                    // Fix the constraint
                    System.out.println("📝 Making owner_id nullable...");
                    String alterSql = "ALTER TABLE restaurant_profile ALTER COLUMN owner_id DROP NOT NULL";
                    jdbcTemplate.execute(alterSql);
                    
                    System.out.println("✅ Database fixed successfully!");
                    System.out.println();
                    
                    // Verify the change
                    System.out.println("📊 Verifying changes...");
                    isNullable = jdbcTemplate.queryForObject(checkSql, String.class);
                    System.out.println("   New is_nullable: " + isNullable);
                    System.out.println();
                } else {
                    System.out.println("✅ owner_id is already nullable. No changes needed!");
                    System.out.println();
                }
                
                System.out.println("========================================");
                System.out.println("🎉 DONE! You can now run: mvn spring-boot:run");
                System.out.println("========================================");
                
            } catch (Exception e) {
                System.err.println("❌ Error: " + e.getMessage());
                e.printStackTrace();
            }
            
            // Exit after fixing
            System.exit(0);
        };
    }
}

