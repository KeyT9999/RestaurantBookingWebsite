package com.example.booking.common.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * Test class for RestaurantApprovalStatus enum
 */
public class RestaurantApprovalStatusTest {
    
    @Test
    public void testEnumValues() {
        // Test all enum values exist
        assertEquals(4, RestaurantApprovalStatus.values().length);
        
        // Test specific values
        assertEquals(RestaurantApprovalStatus.PENDING, RestaurantApprovalStatus.valueOf("PENDING"));
        assertEquals(RestaurantApprovalStatus.APPROVED, RestaurantApprovalStatus.valueOf("APPROVED"));
        assertEquals(RestaurantApprovalStatus.REJECTED, RestaurantApprovalStatus.valueOf("REJECTED"));
        assertEquals(RestaurantApprovalStatus.SUSPENDED, RestaurantApprovalStatus.valueOf("SUSPENDED"));
    }
    
    @Test
    public void testDisplayNames() {
        assertEquals("Chờ duyệt", RestaurantApprovalStatus.PENDING.getDisplayName());
        assertEquals("Đã duyệt", RestaurantApprovalStatus.APPROVED.getDisplayName());
        assertEquals("Bị từ chối", RestaurantApprovalStatus.REJECTED.getDisplayName());
        assertEquals("Tạm dừng", RestaurantApprovalStatus.SUSPENDED.getDisplayName());
    }
    
    @Test
    public void testIsTerminal() {
        assertFalse(RestaurantApprovalStatus.PENDING.isTerminal());
        assertTrue(RestaurantApprovalStatus.APPROVED.isTerminal());
        assertTrue(RestaurantApprovalStatus.REJECTED.isTerminal());
        assertTrue(RestaurantApprovalStatus.SUSPENDED.isTerminal());
    }
    
    @Test
    public void testIsPending() {
        assertTrue(RestaurantApprovalStatus.PENDING.isPending());
        assertFalse(RestaurantApprovalStatus.APPROVED.isPending());
        assertFalse(RestaurantApprovalStatus.REJECTED.isPending());
        assertFalse(RestaurantApprovalStatus.SUSPENDED.isPending());
    }
    
    @Test
    public void testCanBeApproved() {
        assertTrue(RestaurantApprovalStatus.PENDING.canBeApproved());
        assertFalse(RestaurantApprovalStatus.APPROVED.canBeApproved());
        assertFalse(RestaurantApprovalStatus.REJECTED.canBeApproved());
        assertFalse(RestaurantApprovalStatus.SUSPENDED.canBeApproved());
    }
    
    @Test
    public void testCanBeRejected() {
        assertTrue(RestaurantApprovalStatus.PENDING.canBeRejected());
        assertTrue(RestaurantApprovalStatus.APPROVED.canBeRejected());
        assertFalse(RestaurantApprovalStatus.REJECTED.canBeRejected());
        assertFalse(RestaurantApprovalStatus.SUSPENDED.canBeRejected());
    }
    
    @Test
    public void testCanBeSuspended() {
        assertFalse(RestaurantApprovalStatus.PENDING.canBeSuspended());
        assertTrue(RestaurantApprovalStatus.APPROVED.canBeSuspended());
        assertFalse(RestaurantApprovalStatus.REJECTED.canBeSuspended());
        assertFalse(RestaurantApprovalStatus.SUSPENDED.canBeSuspended());
    }
}
