package com.example.booking.dto;

import java.time.LocalDateTime;
import java.util.List;

public class AvailabilityCheckResponse {
    private boolean hasConflict;
    private String conflictType; // "SPECIFIC_TABLE" | "NO_AVAILABLE_TABLES"
    private ConflictDetails conflictDetails;
    private WaitlistInfo waitlistInfo;
    private List<AlternativeTable> alternativeTables;

    // Constructors
    public AvailabilityCheckResponse() {}

    public AvailabilityCheckResponse(boolean hasConflict, String conflictType) {
        this.hasConflict = hasConflict;
        this.conflictType = conflictType;
    }

    // Getters and Setters
    public boolean isHasConflict() {
        return hasConflict;
    }

    public void setHasConflict(boolean hasConflict) {
        this.hasConflict = hasConflict;
    }

    public String getConflictType() {
        return conflictType;
    }

    public void setConflictType(String conflictType) {
        this.conflictType = conflictType;
    }

    public ConflictDetails getConflictDetails() {
        return conflictDetails;
    }

    public void setConflictDetails(ConflictDetails conflictDetails) {
        this.conflictDetails = conflictDetails;
    }

    public WaitlistInfo getWaitlistInfo() {
        return waitlistInfo;
    }

    public void setWaitlistInfo(WaitlistInfo waitlistInfo) {
        this.waitlistInfo = waitlistInfo;
    }

    public List<AlternativeTable> getAlternativeTables() {
        return alternativeTables;
    }

    public void setAlternativeTables(List<AlternativeTable> alternativeTables) {
        this.alternativeTables = alternativeTables;
    }

    // Inner classes
    public static class ConflictDetails {
        private List<TableConflictInfo> selectedTables;

        public List<TableConflictInfo> getSelectedTables() {
            return selectedTables;
        }

        public void setSelectedTables(List<TableConflictInfo> selectedTables) {
            this.selectedTables = selectedTables;
        }
    }

    public static class TableConflictInfo {
        private Integer tableId;
        private String tableName;
        private Integer capacity;
        private List<BookingConflict> conflicts;

        // Constructors
        public TableConflictInfo() {}

        public TableConflictInfo(Integer tableId, String tableName, Integer capacity) {
            this.tableId = tableId;
            this.tableName = tableName;
            this.capacity = capacity;
        }

        // Getters and Setters
        public Integer getTableId() {
            return tableId;
        }

        public void setTableId(Integer tableId) {
            this.tableId = tableId;
        }

        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public Integer getCapacity() {
            return capacity;
        }

        public void setCapacity(Integer capacity) {
            this.capacity = capacity;
        }

        public List<BookingConflict> getConflicts() {
            return conflicts;
        }

        public void setConflicts(List<BookingConflict> conflicts) {
            this.conflicts = conflicts;
        }
    }

    public static class BookingConflict {
        private Integer bookingId;
        private LocalDateTime bookingTime;
        private LocalDateTime endTime;
        private Integer guestCount;
        private String status;

        // Constructors
        public BookingConflict() {}

        public BookingConflict(Integer bookingId, LocalDateTime bookingTime, LocalDateTime endTime, Integer guestCount, String status) {
            this.bookingId = bookingId;
            this.bookingTime = bookingTime;
            this.endTime = endTime;
            this.guestCount = guestCount;
            this.status = status;
        }

        // Getters and Setters
        public Integer getBookingId() {
            return bookingId;
        }

        public void setBookingId(Integer bookingId) {
            this.bookingId = bookingId;
        }

        public LocalDateTime getBookingTime() {
            return bookingTime;
        }

        public void setBookingTime(LocalDateTime bookingTime) {
            this.bookingTime = bookingTime;
        }

        public LocalDateTime getEndTime() {
            return endTime;
        }

        public void setEndTime(LocalDateTime endTime) {
            this.endTime = endTime;
        }

        public Integer getGuestCount() {
            return guestCount;
        }

        public void setGuestCount(Integer guestCount) {
            this.guestCount = guestCount;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    public static class WaitlistInfo {
        private boolean canJoinWaitlist;
        private LocalDateTime estimatedWaitTime;
        private String reason;
        private Integer waitTimeMinutes;

        // Constructors
        public WaitlistInfo() {}

        public WaitlistInfo(boolean canJoinWaitlist, LocalDateTime estimatedWaitTime, String reason) {
            this.canJoinWaitlist = canJoinWaitlist;
            this.estimatedWaitTime = estimatedWaitTime;
            this.reason = reason;
        }

        // Getters and Setters
        public boolean isCanJoinWaitlist() {
            return canJoinWaitlist;
        }

        public void setCanJoinWaitlist(boolean canJoinWaitlist) {
            this.canJoinWaitlist = canJoinWaitlist;
        }

        public LocalDateTime getEstimatedWaitTime() {
            return estimatedWaitTime;
        }

        public void setEstimatedWaitTime(LocalDateTime estimatedWaitTime) {
            this.estimatedWaitTime = estimatedWaitTime;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }

        public Integer getWaitTimeMinutes() {
            return waitTimeMinutes;
        }

        public void setWaitTimeMinutes(Integer waitTimeMinutes) {
            this.waitTimeMinutes = waitTimeMinutes;
        }
    }

    public static class AlternativeTable {
        private Integer tableId;
        private String tableName;
        private Integer capacity;
        private boolean isAvailable;
        private String reason;

        // Constructors
        public AlternativeTable() {}

        public AlternativeTable(Integer tableId, String tableName, Integer capacity, boolean isAvailable, String reason) {
            this.tableId = tableId;
            this.tableName = tableName;
            this.capacity = capacity;
            this.isAvailable = isAvailable;
            this.reason = reason;
        }

        // Getters and Setters
        public Integer getTableId() {
            return tableId;
        }

        public void setTableId(Integer tableId) {
            this.tableId = tableId;
        }

        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public Integer getCapacity() {
            return capacity;
        }

        public void setCapacity(Integer capacity) {
            this.capacity = capacity;
        }

        public boolean isAvailable() {
            return isAvailable;
        }

        public void setAvailable(boolean available) {
            isAvailable = available;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}
