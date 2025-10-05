package com.example.booking.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseFixRunner implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        try {
            System.out.println("🔧 Fixing depositamount column in restaurant_table...");

            // Update NULL values to 0
            int updatedRows = jdbcTemplate.update(
                    "UPDATE restaurant_table SET depositamount = 0 WHERE depositamount IS NULL");
            System.out.println("✅ Updated " + updatedRows + " rows with NULL depositamount");

            // Add NOT NULL constraint
            try {
                jdbcTemplate.execute("ALTER TABLE restaurant_table ALTER COLUMN depositamount SET NOT NULL");
                System.out.println("✅ Added NOT NULL constraint to depositamount");
            } catch (Exception e) {
                System.out.println("ℹ️ NOT NULL constraint already exists or error: " + e.getMessage());
            }

            // Set default value
            try {
                jdbcTemplate.execute("ALTER TABLE restaurant_table ALTER COLUMN depositamount SET DEFAULT 0");
                System.out.println("✅ Set default value 0 for depositamount");
            } catch (Exception e) {
                System.out.println("ℹ️ Default value already exists or error: " + e.getMessage());
            }

            System.out.println("🎉 Database fix completed successfully!");

        } catch (Exception e) {
            System.err.println("❌ Error fixing database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
