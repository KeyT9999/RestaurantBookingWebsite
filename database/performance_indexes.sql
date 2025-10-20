-- =====================================================
-- PERFORMANCE OPTIMIZATION: Database Indexes
-- =====================================================
-- Purpose: Create indexes to support common queries and eliminate sequential scans
-- Database: PostgreSQL
-- Author: Performance Optimization Task
-- Date: 2025-10-20
-- =====================================================

-- Index naming convention: idx_<table>_<column(s)>_<purpose>

-- =====================================================
-- 1. RESTAURANT_MEDIA TABLE
-- =====================================================

-- Index for batch query: findByRestaurantsAndType(restaurants, "cover")
-- Used in: HomeController.restaurants() - batch fetch cover images
-- Query pattern: WHERE restaurant_id IN (...) AND type = 'cover' ORDER BY restaurant_id, created_at DESC
-- Impact: Eliminates N+1 queries for loading restaurant cover images
CREATE INDEX IF NOT EXISTS idx_restaurant_media_batch_cover 
ON restaurant_media (restaurant_id, type, created_at DESC);

-- Partial index specifically for cover images (most common query)
-- Smaller index size, faster lookups
CREATE INDEX IF NOT EXISTS idx_restaurant_media_cover_only 
ON restaurant_media (restaurant_id, created_at DESC)
WHERE type = 'cover';

-- Index for media by type (general lookup)
CREATE INDEX IF NOT EXISTS idx_restaurant_media_type 
ON restaurant_media (type, created_at DESC);


-- =====================================================
-- 2. RESTAURANT_PROFILE TABLE
-- =====================================================

-- Partial index for APPROVED restaurants (most common filter)
-- Used in: RestaurantProfileRepository.findApprovedWithFilters()
-- Query pattern: WHERE approval_status = 'APPROVED' AND <filters> ORDER BY <sortBy>
-- Impact: Significantly faster for customer-facing restaurant list
CREATE INDEX IF NOT EXISTS idx_restaurant_profile_approved 
ON restaurant_profile (approval_status, restaurant_name, average_price, average_rating, created_at)
WHERE approval_status = 'APPROVED';

-- Composite index for search queries (name, address, cuisine)
-- Used in: search filter with LIKE '%term%'
-- Note: PostgreSQL supports trigram indexes for faster LIKE queries
-- If pg_trgm extension is available, consider GIN index instead
CREATE INDEX IF NOT EXISTS idx_restaurant_profile_search 
ON restaurant_profile (restaurant_name, address, cuisine_type);

-- Index for filtering by cuisine type
CREATE INDEX IF NOT EXISTS idx_restaurant_profile_cuisine 
ON restaurant_profile (cuisine_type, approval_status);

-- Index for price range filtering
CREATE INDEX IF NOT EXISTS idx_restaurant_profile_price 
ON restaurant_profile (average_price, approval_status);

-- Index for rating filtering
CREATE INDEX IF NOT EXISTS idx_restaurant_profile_rating 
ON restaurant_profile (average_rating DESC, approval_status);

-- Index for owner lookup
CREATE INDEX IF NOT EXISTS idx_restaurant_profile_owner 
ON restaurant_profile (owner_id);


-- =====================================================
-- 3. BOOKING TABLE
-- =====================================================

-- Index for customer bookings lookup
-- Used in: BookingController.showMyBookings() - load customer's bookings
-- Query pattern: WHERE customer_id = ? ORDER BY created_at DESC
-- Impact: Fast retrieval of user's booking history
CREATE INDEX IF NOT EXISTS idx_booking_customer_history 
ON booking (customer_id, created_at DESC);

-- Index for restaurant bookings lookup
-- Used in: Restaurant owner dashboard - view all bookings
CREATE INDEX IF NOT EXISTS idx_booking_restaurant 
ON booking (restaurant_id, booking_time DESC);

-- Composite index for booking status queries
-- Used in: Filter bookings by status (PENDING, CONFIRMED, etc.)
CREATE INDEX IF NOT EXISTS idx_booking_status 
ON booking (status, customer_id, created_at DESC);

-- Index for upcoming bookings query
-- Query pattern: WHERE booking_time >= NOW() AND status = 'CONFIRMED'
CREATE INDEX IF NOT EXISTS idx_booking_upcoming 
ON booking (booking_time, status)
WHERE status IN ('CONFIRMED', 'PENDING');


-- =====================================================
-- 4. WAITLIST TABLE
-- =====================================================

-- Index for restaurant waitlist lookup
-- Used in: WaitlistService.getWaitlistByRestaurant()
-- Query pattern: WHERE restaurant_id = ? AND status = 'WAITING' ORDER BY queue_position
-- Impact: Fast retrieval of current waitlist for restaurant
CREATE INDEX IF NOT EXISTS idx_waitlist_restaurant_queue 
ON waitlist (restaurant_id, status, queue_position);

-- Index for customer waitlist lookup
-- Used in: WaitlistService.getWaitlistByCustomer()
CREATE INDEX IF NOT EXISTS idx_waitlist_customer 
ON waitlist (customer_id, status, created_at DESC);

-- Index for active waitlist entries
CREATE INDEX IF NOT EXISTS idx_waitlist_active 
ON waitlist (status, preferred_booking_time)
WHERE status IN ('WAITING', 'CALLED');


-- =====================================================
-- 5. CUSTOMER TABLE
-- =====================================================

-- Index for user lookup (most common query)
-- Used in: CustomerService.findByUsername(), findByUserId()
-- Query pattern: WHERE user_id = ?
CREATE INDEX IF NOT EXISTS idx_customer_user 
ON customer (user_id);


-- =====================================================
-- 6. PAYMENT TABLE
-- =====================================================

-- Index for booking payments lookup
-- Used in: Load payments for a specific booking
CREATE INDEX IF NOT EXISTS idx_payment_booking 
ON payment (booking_id, created_at DESC);

-- Index for customer payments history
CREATE INDEX IF NOT EXISTS idx_payment_customer 
ON payment (customer_id, payment_status, created_at DESC);

-- Index for payment status filtering
CREATE INDEX IF NOT EXISTS idx_payment_status 
ON payment (payment_status, created_at DESC);


-- =====================================================
-- 7. REVIEW TABLE
-- =====================================================

-- Index for restaurant reviews lookup
-- Used in: ReviewService.getReviewsByRestaurant()
-- Query pattern: WHERE restaurant_id = ? ORDER BY created_at DESC
CREATE INDEX IF NOT EXISTS idx_review_restaurant 
ON review (restaurant_id, created_at DESC);

-- Index for customer reviews lookup
CREATE INDEX IF NOT EXISTS idx_review_customer 
ON review (customer_id, created_at DESC);


-- =====================================================
-- 8. CHAT_ROOM TABLE
-- =====================================================

-- Index for customer chat rooms
-- Used in: ChatRoomRepository.findByCustomerId()
CREATE INDEX IF NOT EXISTS idx_chatroom_customer 
ON chat_room (customer_id, last_message_at DESC)
WHERE is_active = true;

-- Index for restaurant chat rooms
CREATE INDEX IF NOT EXISTS idx_chatroom_restaurant 
ON chat_room (restaurant_id, last_message_at DESC)
WHERE is_active = true;


-- =====================================================
-- 9. MESSAGE TABLE
-- =====================================================

-- Index for chat room messages
-- Used in: Load messages for a specific chat room
CREATE INDEX IF NOT EXISTS idx_message_chatroom 
ON message (chat_room_id, created_at ASC);


-- =====================================================
-- VERIFICATION QUERIES
-- =====================================================
-- Run these queries to verify indexes are being used

-- Test 1: Restaurant list query (should use idx_restaurant_profile_approved)
-- EXPLAIN ANALYZE 
-- SELECT * FROM restaurant_profile 
-- WHERE approval_status = 'APPROVED' 
-- ORDER BY restaurant_name 
-- LIMIT 12;

-- Test 2: Batch cover images query (should use idx_restaurant_media_batch_cover)
-- EXPLAIN ANALYZE
-- SELECT * FROM restaurant_media 
-- WHERE restaurant_id IN (1, 2, 3, 4, 5) AND type = 'cover' 
-- ORDER BY restaurant_id, created_at DESC;

-- Test 3: Customer bookings query (should use idx_booking_customer_history)
-- EXPLAIN ANALYZE
-- SELECT * FROM booking 
-- WHERE customer_id = '<some-uuid>' 
-- ORDER BY created_at DESC;

-- Test 4: Restaurant waitlist query (should use idx_waitlist_restaurant_queue)
-- EXPLAIN ANALYZE
-- SELECT * FROM waitlist 
-- WHERE restaurant_id = 1 AND status = 'WAITING' 
-- ORDER BY queue_position;


-- =====================================================
-- INDEX MAINTENANCE
-- =====================================================

-- Check index usage statistics
-- SELECT 
--     schemaname,
--     tablename,
--     indexname,
--     idx_scan as scans,
--     idx_tup_read as tuples_read,
--     idx_tup_fetch as tuples_fetched
-- FROM pg_stat_user_indexes
-- WHERE schemaname = 'public'
-- ORDER BY idx_scan DESC;

-- Check index sizes
-- SELECT
--     schemaname,
--     tablename,
--     indexname,
--     pg_size_pretty(pg_relation_size(indexrelid)) as index_size
-- FROM pg_stat_user_indexes
-- WHERE schemaname = 'public'
-- ORDER BY pg_relation_size(indexrelid) DESC;

-- Rebuild indexes if needed (after bulk inserts/updates)
-- REINDEX TABLE restaurant_profile;
-- REINDEX TABLE restaurant_media;
-- REINDEX TABLE booking;
-- REINDEX TABLE waitlist;


-- =====================================================
-- NOTES
-- =====================================================
-- 1. These indexes are designed for PostgreSQL. For MySQL, adjust syntax:
--    - Remove "IF NOT EXISTS" (not supported in older MySQL versions)
--    - Remove "WHERE" clauses (partial indexes not supported)
--    - Remove "INCLUDE" columns (not supported)
--
-- 2. Monitor index usage regularly. Unused indexes waste space and slow down writes.
--
-- 3. For very large tables (>1M rows), consider:
--    - Partitioning by date (booking, payment, review)
--    - Materialized views for complex aggregations
--    - Read replicas for reporting queries
--
-- 4. After creating indexes, run ANALYZE to update table statistics:
--    ANALYZE restaurant_profile;
--    ANALYZE restaurant_media;
--    ANALYZE booking;
--    ANALYZE waitlist;
--
-- 5. Before creating indexes in production:
--    - Test on staging with production-like data volume
--    - Create indexes CONCURRENTLY to avoid table locks:
--      CREATE INDEX CONCURRENTLY idx_name ON table(column);
--
-- 6. Estimated impact:
--    - Restaurant list query: 2000ms -> <100ms (20x faster)
--    - Batch cover images: N queries -> 1 query (eliminates N+1)
--    - Customer bookings: 500ms -> <50ms (10x faster)
--    - Waitlist lookup: 300ms -> <30ms (10x faster)

