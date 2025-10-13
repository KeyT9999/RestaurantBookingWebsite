package com.example.booking.audit;

/**
 * Enum for audit actions
 * Defines all possible actions that can be audited
 */
public enum AuditAction {
    
    // CRUD Operations
    CREATE("CREATE", "Create a new resource"),
    READ("READ", "Read/view a resource"),
    UPDATE("UPDATE", "Update an existing resource"),
    DELETE("DELETE", "Delete a resource"),
    
    // Authentication & Authorization
    LOGIN("LOGIN", "User login"),
    LOGOUT("LOGOUT", "User logout"),
    LOGIN_FAILED("LOGIN_FAILED", "Failed login attempt"),
    PASSWORD_CHANGE("PASSWORD_CHANGE", "Password changed"),
    PASSWORD_RESET("PASSWORD_RESET", "Password reset requested"),
    
    // Payment Operations
    PAYMENT_CREATE("PAYMENT_CREATE", "Payment created"),
    PAYMENT_PROCESS("PAYMENT_PROCESS", "Payment processed"),
    PAYMENT_COMPLETE("PAYMENT_COMPLETE", "Payment completed"),
    PAYMENT_FAILED("PAYMENT_FAILED", "Payment failed"),
    REFUND("REFUND", "Refund processed"),
    REFUND_PARTIAL("REFUND_PARTIAL", "Partial refund processed"),
    REFUND_FAILED("REFUND_FAILED", "Refund failed"),
    
    // Booking Operations
    BOOKING_CREATE("BOOKING_CREATE", "Booking created"),
    BOOKING_CONFIRM("BOOKING_CONFIRM", "Booking confirmed"),
    BOOKING_CANCEL("BOOKING_CANCEL", "Booking cancelled"),
    BOOKING_MODIFY("BOOKING_MODIFY", "Booking modified"),
    BOOKING_NO_SHOW("BOOKING_NO_SHOW", "Customer no-show"),
    
    // Restaurant Operations
    RESTAURANT_CREATE("RESTAURANT_CREATE", "Restaurant created"),
    RESTAURANT_UPDATE("RESTAURANT_UPDATE", "Restaurant updated"),
    RESTAURANT_DELETE("RESTAURANT_DELETE", "Restaurant deleted"),
    TABLE_UPDATE("TABLE_UPDATE", "Table status updated"),
    MENU_UPDATE("MENU_UPDATE", "Menu updated"),
    
    // User Management
    USER_CREATE("USER_CREATE", "User created"),
    USER_UPDATE("USER_UPDATE", "User updated"),
    USER_DELETE("USER_DELETE", "User deleted"),
    USER_ACTIVATE("USER_ACTIVATE", "User activated"),
    USER_DEACTIVATE("USER_DEACTIVATE", "User deactivated"),
    ROLE_CHANGE("ROLE_CHANGE", "User role changed"),
    
    // Voucher Operations
    VOUCHER_CREATE("VOUCHER_CREATE", "Voucher created"),
    VOUCHER_UPDATE("VOUCHER_UPDATE", "Voucher updated"),
    VOUCHER_DELETE("VOUCHER_DELETE", "Voucher deleted"),
    VOUCHER_REDEEM("VOUCHER_REDEEM", "Voucher redeemed"),
    VOUCHER_EXPIRE("VOUCHER_EXPIRE", "Voucher expired"),
    
    // System Operations
    SYSTEM_STARTUP("SYSTEM_STARTUP", "System startup"),
    SYSTEM_SHUTDOWN("SYSTEM_SHUTDOWN", "System shutdown"),
    SYSTEM_MAINTENANCE("SYSTEM_MAINTENANCE", "System maintenance"),
    SYSTEM_BACKUP("SYSTEM_BACKUP", "System backup"),
    SYSTEM_RESTORE("SYSTEM_RESTORE", "System restore"),
    
    // Reconciliation Operations
    RECONCILIATION_START("RECONCILIATION_START", "Reconciliation started"),
    RECONCILIATION_COMPLETE("RECONCILIATION_COMPLETE", "Reconciliation completed"),
    RECONCILIATION_FAILED("RECONCILIATION_FAILED", "Reconciliation failed"),
    RECONCILIATION_DISCREPANCY("RECONCILIATION_DISCREPANCY", "Reconciliation discrepancy found"),
    
    // Data Export/Import
    DATA_EXPORT("DATA_EXPORT", "Data exported"),
    DATA_IMPORT("DATA_IMPORT", "Data imported"),
    DATA_SYNC("DATA_SYNC", "Data synchronized"),
    
    // Security Operations
    ACCESS_DENIED("ACCESS_DENIED", "Access denied"),
    PERMISSION_DENIED("PERMISSION_DENIED", "Permission denied"),
    SUSPICIOUS_ACTIVITY("SUSPICIOUS_ACTIVITY", "Suspicious activity detected"),
    RATE_LIMIT_EXCEEDED("RATE_LIMIT_EXCEEDED", "Rate limit exceeded"),
    IP_BLOCKED("IP_BLOCKED", "IP address blocked"),
    
    // Configuration Changes
    CONFIG_UPDATE("CONFIG_UPDATE", "Configuration updated"),
    SETTINGS_CHANGE("SETTINGS_CHANGE", "Settings changed"),
    
    // Communication
    EMAIL_SEND("EMAIL_SEND", "Email sent"),
    SMS_SEND("SMS_SEND", "SMS sent"),
    NOTIFICATION_SEND("NOTIFICATION_SEND", "Notification sent"),
    
    // File Operations
    FILE_UPLOAD("FILE_UPLOAD", "File uploaded"),
    FILE_DOWNLOAD("FILE_DOWNLOAD", "File downloaded"),
    FILE_DELETE("FILE_DELETE", "File deleted"),
    
    // API Operations
    API_CALL("API_CALL", "API called"),
    API_ERROR("API_ERROR", "API error"),
    WEBHOOK_RECEIVED("WEBHOOK_RECEIVED", "Webhook received"),
    WEBHOOK_SENT("WEBHOOK_SENT", "Webhook sent"),
    
    // Audit Operations
    AUDIT_VIEW("AUDIT_VIEW", "Audit log viewed"),
    AUDIT_EXPORT("AUDIT_EXPORT", "Audit log exported"),
    AUDIT_CLEANUP("AUDIT_CLEANUP", "Audit log cleanup"),
    
    // Generic Operations
    SEARCH("SEARCH", "Search performed"),
    FILTER("FILTER", "Data filtered"),
    SORT("SORT", "Data sorted"),
    EXPORT("EXPORT", "Data exported"),
    IMPORT("IMPORT", "Data imported");
    
    private final String code;
    private final String description;
    
    AuditAction(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Get AuditAction by code
     */
    public static AuditAction fromCode(String code) {
        for (AuditAction action : values()) {
            if (action.code.equals(code)) {
                return action;
            }
        }
        throw new IllegalArgumentException("Unknown audit action code: " + code);
    }
    
    /**
     * Check if action is a CRUD operation
     */
    public boolean isCrudOperation() {
        return this == CREATE || this == READ || this == UPDATE || this == DELETE;
    }
    
    /**
     * Check if action is a security-related operation
     */
    public boolean isSecurityOperation() {
        return this == LOGIN || this == LOGOUT || this == LOGIN_FAILED ||
               this == PASSWORD_CHANGE || this == PASSWORD_RESET ||
               this == ACCESS_DENIED || this == PERMISSION_DENIED ||
               this == SUSPICIOUS_ACTIVITY || this == RATE_LIMIT_EXCEEDED ||
               this == IP_BLOCKED;
    }
    
    /**
     * Check if action is a payment-related operation
     */
    public boolean isPaymentOperation() {
        return this == PAYMENT_CREATE || this == PAYMENT_PROCESS ||
               this == PAYMENT_COMPLETE || this == PAYMENT_FAILED ||
               this == REFUND || this == REFUND_PARTIAL || this == REFUND_FAILED;
    }
    
    /**
     * Check if action is a booking-related operation
     */
    public boolean isBookingOperation() {
        return this == BOOKING_CREATE || this == BOOKING_CONFIRM ||
               this == BOOKING_CANCEL || this == BOOKING_MODIFY ||
               this == BOOKING_NO_SHOW;
    }
    
    /**
     * Check if action is a system operation
     */
    public boolean isSystemOperation() {
        return this == SYSTEM_STARTUP || this == SYSTEM_SHUTDOWN ||
               this == SYSTEM_MAINTENANCE || this == SYSTEM_BACKUP ||
               this == SYSTEM_RESTORE || this == RECONCILIATION_START ||
               this == RECONCILIATION_COMPLETE || this == RECONCILIATION_FAILED ||
               this == RECONCILIATION_DISCREPANCY;
    }
    
    @Override
    public String toString() {
        return code;
    }
}
