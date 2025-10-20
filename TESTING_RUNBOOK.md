# 🧪 PERFORMANCE OPTIMIZATION - TESTING RUNBOOK

**Purpose:** Step-by-step guide to verify performance optimizations  
**Target Environment:** Development (localhost:8081)  
**Prerequisites:** PostgreSQL running, application compiled  
**Estimated Time:** 30-45 minutes

---

## 📋 PRE-FLIGHT CHECKLIST

- [ ] PostgreSQL database accessible
- [ ] Application code changes applied (see PERFORMANCE_OPTIMIZATION_SUMMARY.md)
- [ ] Maven installed (`mvn --version`)
- [ ] `psql` client installed
- [ ] Browser with DevTools (Chrome/Firefox)
- [ ] Terminal window ready for log monitoring

---

## 🚀 STEP 1: CREATE DATABASE INDEXES (5 minutes)

### 1.1 Connect to PostgreSQL
```bash
psql -U bookeat_user -d bookeat_db -h dpg-d37uh8ruibrs739c7sf0-a.singapore-postgres.render.com
# Password: 58IJhzpjEobFFUmJ762dmkEdSLPPRbZX
```

### 1.2 Run Index Creation Script
```sql
\i database/performance_indexes.sql
```

**Expected Output:**
```
CREATE INDEX
CREATE INDEX
CREATE INDEX
...
(30+ CREATE INDEX messages)
```

### 1.3 Verify Indexes Created
```sql
-- Check restaurant_media indexes
\di+ idx_restaurant_media_batch_cover
\di+ idx_restaurant_media_cover_only

-- Check restaurant_profile indexes
\di+ idx_restaurant_profile_approved
\di+ idx_restaurant_profile_search

-- Check booking indexes
\di+ idx_booking_customer_history

-- Check waitlist indexes
\di+ idx_waitlist_restaurant_queue
```

**Expected:** Each command shows index details (size, table, columns)

### 1.4 Update Table Statistics
```sql
ANALYZE restaurant_profile;
ANALYZE restaurant_media;
ANALYZE booking;
ANALYZE waitlist;
ANALYZE customer;
ANALYZE payment;
ANALYZE review;
```

**✅ CHECKPOINT:** All indexes created successfully, no errors

---

## 🔄 STEP 2: RESTART APPLICATION (2 minutes)

### 2.1 Stop Existing Application
```bash
# If running in terminal, press Ctrl+C
# Or find and kill process:
jps | grep "Application"
kill <PID>
```

### 2.2 Start Application with New Config
```bash
cd /path/to/RestaurantBookingWebsite
mvn clean spring-boot:run
```

### 2.3 Wait for Application Ready
Watch terminal output for:
```
Started BookingApplication in X.XXX seconds
Hikari pool HikariPool-1 started
```

**✅ CHECKPOINT:** Application started without errors

---

## 📊 STEP 3: BASELINE TEST - COUNT QUERIES (10 minutes)

### 3.1 Open Browser with DevTools
```bash
# Open Chrome/Firefox
# Navigate to: about:blank
# Open DevTools: F12 or Cmd+Opt+I (Mac) / Ctrl+Shift+I (Windows)
# Go to Network tab
# Check "Preserve log" option
```

### 3.2 Clear Terminal & Navigate
```bash
# In terminal running application:
# Note current line number or scroll to bottom

# In browser:
# Navigate to: http://localhost:8081/restaurants?page=0&size=12
```

### 3.3 Count SQL Queries in Terminal
```bash
# After page loads, count lines starting with "Hibernate:"
# Expected pattern:

Hibernate: SELECT ... FROM restaurant_profile WHERE approval_status = 'APPROVED' ... LIMIT 12
Hibernate: SELECT ... FROM restaurant_media WHERE restaurant_id IN (?, ?, ...) AND type = 'cover' ...
Hibernate: SELECT COUNT(*) FROM restaurant_profile WHERE ...

# EXPECTED TOTAL: 3 queries
```

**🎯 SUCCESS CRITERIA:**
- Total SQL queries = **3** (not 14+)
- Query 1: `SELECT * FROM restaurant_profile` with LIMIT 12
- Query 2: `SELECT * FROM restaurant_media WHERE restaurant_id IN (...)` 
- Query 3: `SELECT COUNT(*)`
- **NO loops** of individual restaurant_media queries

### 3.4 Verify NO Log Spam
```bash
# Check terminal - should NOT see lines like:
# TRACE o.h.type.descriptor.sql.BasicBinder - binding parameter [1] as [VARCHAR] - [...]
# TRACE o.h.type.descriptor.sql.BasicBinder - binding parameter [2] as [INTEGER] - [123]

# Should ONLY see:
# DEBUG org.hibernate.SQL - Hibernate: SELECT ...
```

**🎯 SUCCESS CRITERIA:**
- NO TRACE level logs for bind parameters
- ~10 total log lines per request (not 500+)

### 3.5 Measure Response Time
```bash
# In DevTools Network tab:
# Click on "restaurants" request
# Check Timing section:
#   - Waiting (TTFB): Should be <500ms (ideally <300ms)
#   - Total time: Should be <1000ms

# If TTFB > 500ms without indexes, indexes may not be created yet
```

**🎯 SUCCESS CRITERIA:**
- TTFB (Time To First Byte): **< 500ms** (with indexes)
- Total request time: **< 1000ms**

**✅ CHECKPOINT:** 3 queries counted, no log spam, acceptable response time

---

## 🔍 STEP 4: VERIFY INDEXES ARE USED (10 minutes)

### 4.1 Get Sample Restaurant IDs
```sql
-- In psql:
SELECT restaurant_id FROM restaurant_profile LIMIT 12;
-- Copy the IDs (e.g., 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)
```

### 4.2 Test Restaurant List Query
```sql
EXPLAIN ANALYZE 
SELECT * FROM restaurant_profile 
WHERE approval_status = 'APPROVED' 
ORDER BY restaurant_name 
LIMIT 12;
```

**Expected Output:**
```
Limit  (cost=X..X rows=12 width=XXX) (actual time=X.XXX..X.XXX rows=12 loops=1)
  ->  Index Scan using idx_restaurant_profile_approved on restaurant_profile
      Filter: (approval_status = 'APPROVED'::text)
      Rows Removed by Filter: 0
Planning Time: X.XXX ms
Execution Time: X.XXX ms  ← Should be <50ms
```

**🎯 SUCCESS CRITERIA:**
- Uses **Index Scan** (not Seq Scan)
- Index name: `idx_restaurant_profile_approved`
- Execution time: **< 50ms**

### 4.3 Test Batch Cover Images Query
```sql
-- Replace IDs with actual IDs from Step 4.1
EXPLAIN ANALYZE
SELECT * FROM restaurant_media 
WHERE restaurant_id IN (1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12) 
AND type = 'cover' 
ORDER BY restaurant_id, created_at DESC;
```

**Expected Output:**
```
Sort  (cost=X..X rows=X width=XXX) (actual time=X.XXX..X.XXX rows=X loops=1)
  Sort Key: restaurant_id, created_at DESC
  ->  Index Scan using idx_restaurant_media_batch_cover on restaurant_media
      Index Cond: (restaurant_id = ANY ('{1,2,3,...}'::integer[]) AND type = 'cover')
Planning Time: X.XXX ms
Execution Time: X.XXX ms  ← Should be <20ms
```

**🎯 SUCCESS CRITERIA:**
- Uses **Index Scan** (or Bitmap Index Scan)
- Index name: `idx_restaurant_media_batch_cover`
- Execution time: **< 20ms**

### 4.4 Test Customer Bookings Query
```sql
-- Get a real customer UUID first:
SELECT customer_id FROM customer LIMIT 1;
-- Copy the UUID

-- Test query:
EXPLAIN ANALYZE
SELECT * FROM booking 
WHERE customer_id = '<paste-uuid-here>' 
ORDER BY created_at DESC;
```

**Expected Output:**
```
Index Scan using idx_booking_customer_history on booking
  Index Cond: (customer_id = '<uuid>')
  Rows Removed by Filter: 0
Execution Time: X.XXX ms  ← Should be <30ms
```

**🎯 SUCCESS CRITERIA:**
- Uses **Index Scan**
- Index name: `idx_booking_customer_history`
- Execution time: **< 30ms**

**✅ CHECKPOINT:** All queries use indexes, execution times within targets

---

## 📈 STEP 5: VERIFY HIKARI POOL CONFIGURATION (5 minutes)

### 5.1 Check Application Startup Logs
```bash
# In terminal, scroll up to application startup
# Look for Hikari configuration:

HikariPool-1 - Starting...
HikariPool-1 - Added connection ...
HikariPool-1 - Start completed.
HikariPool-1 - Pool stats (total=5, active=0, idle=5, waiting=0)
```

**Expected Configuration (visible in startup logs or code):**
- maximum-pool-size: **20**
- minimum-idle: **5**
- connection-timeout: **30000ms**
- leak-detection-threshold: **60000ms**

### 5.2 Monitor Pool During Load
```bash
# Option A: Watch application logs during testing
# Look for messages like:
# HikariPool-1 - Pool stats (total=10, active=5, idle=5, waiting=0)

# Option B: Add actuator (if available)
# GET http://localhost:8081/actuator/metrics/hikaricp.connections.active
# GET http://localhost:8081/actuator/metrics/hikaricp.connections.max
```

### 5.3 Test Connection Leak Detection
```bash
# Simulate slow request:
# In browser, open multiple tabs (5-10 tabs)
# Navigate all tabs to: http://localhost:8081/restaurants
# Wait 60 seconds

# Check terminal for leak warnings:
# HikariPool-1 - Connection leak detection triggered for connection ...
```

**🎯 SUCCESS CRITERIA:**
- Active connections stay **< 15** under normal load
- No "waiting" connections (pool not exhausted)
- Leak detection warnings appear if connection held > 60s (dev only)

**✅ CHECKPOINT:** Hikari pool configured correctly, no exhaustion

---

## 🧪 STEP 6: TEST FILTERING & PAGINATION (5 minutes)

### 6.1 Test Search Filter
```bash
# In browser, navigate to:
http://localhost:8081/restaurants?page=0&size=12&search=pizza

# In terminal, verify SQL:
# Should see: WHERE ... AND LOWER(restaurant_name) LIKE LOWER('%pizza%') ...
# Count queries: Should still be 3 (not more)
```

### 6.2 Test Cuisine Filter
```bash
http://localhost:8081/restaurants?page=0&size=12&cuisineType=Italian

# SQL should include: AND cuisine_type = 'Italian'
```

### 6.3 Test Price Range Filter
```bash
http://localhost:8081/restaurants?page=0&size=12&priceRange=50k-100k

# SQL should include: AND average_price >= 50000 AND average_price <= 100000
```

### 6.4 Test Rating Filter
```bash
http://localhost:8081/restaurants?page=0&size=12&ratingFilter=4-star

# SQL should include: AND average_rating >= 4.0
```

### 6.5 Test Combined Filters
```bash
http://localhost:8081/restaurants?page=0&size=12&search=vietnam&cuisineType=Vietnamese&priceRange=under-50k&ratingFilter=3-star

# All filters should be in WHERE clause
# Still only 3 queries total
```

### 6.6 Test Pagination
```bash
# Page 0
http://localhost:8081/restaurants?page=0&size=12

# Page 1
http://localhost:8081/restaurants?page=1&size=12

# Verify in terminal:
# SQL should include: LIMIT 12 OFFSET 0  (page 0)
# SQL should include: LIMIT 12 OFFSET 12 (page 1)
```

**🎯 SUCCESS CRITERIA:**
- All filters applied at **database level** (in WHERE clause)
- Query count stays at **3** regardless of filters
- Pagination uses **LIMIT/OFFSET** in SQL (not Java sublist)

**✅ CHECKPOINT:** Filters and pagination work correctly via database

---

## 🔬 STEP 7: COMPARE BEFORE/AFTER (Optional, 5 minutes)

### 7.1 Temporarily Revert to Old Code
```bash
# Option A: Git stash changes
git stash

# Option B: Comment out new code in HomeController
# Lines 139-160: Comment out batch query code
# Lines 130-140: Uncomment old loop code (if saved)

# Restart application
mvn spring-boot:run
```

### 7.2 Test Old Version
```bash
# Navigate to: http://localhost:8081/restaurants?page=0&size=12
# Count queries in terminal
# Measure TTFB in DevTools

# EXPECTED (OLD VERSION):
# Queries: 14+ (1 + N + 1)
# TTFB: >1000ms (without optimizations)
# Log lines: 200+ (with TRACE)
```

### 7.3 Reapply New Code
```bash
# Option A: Git stash pop
git stash pop

# Option B: Uncomment new code, comment old code

# Restart application
mvn spring-boot:run
```

### 7.4 Test New Version Again
```bash
# Navigate to: http://localhost:8081/restaurants?page=0&size=12

# EXPECTED (NEW VERSION):
# Queries: 3
# TTFB: <500ms
# Log lines: ~10
```

### 7.5 Calculate Improvements
```
Query Reduction: (14 - 3) / 14 * 100 = 79%
TTFB Improvement: (2000 - 300) / 2000 * 100 = 85%
Log Reduction: (500 - 10) / 500 * 100 = 98%
```

**✅ CHECKPOINT:** Performance improvements verified by comparison

---

## 📝 STEP 8: DOCUMENT RESULTS

### 8.1 Fill Out Test Results Table

| Test | Expected | Actual | Pass/Fail |
|------|----------|--------|-----------|
| Query count for /restaurants | ≤3 | _____ | _____ |
| TTFB for /restaurants | <500ms | _____ ms | _____ |
| Log lines per request | ≤20 | _____ | _____ |
| Index scan for restaurant_profile | Yes | _____ | _____ |
| Index scan for restaurant_media | Yes | _____ | _____ |
| Hikari active connections | <15 | _____ | _____ |
| Search filter works | Yes | _____ | _____ |
| Pagination works | Yes | _____ | _____ |
| No connection leaks | Yes | _____ | _____ |

### 8.2 Take Screenshots
- [ ] DevTools Network tab showing TTFB
- [ ] Terminal logs showing 3 SQL queries
- [ ] EXPLAIN ANALYZE output showing index usage
- [ ] Application startup logs showing Hikari config

### 8.3 Save Benchmark Data
```bash
# Create benchmark file
cat > benchmark_results.txt << EOF
Date: $(date)
Environment: Development
Database: PostgreSQL
Dataset Size: $(psql -U bookeat_user -d bookeat_db -h <host> -c "SELECT COUNT(*) FROM restaurant_profile;")

BEFORE Optimization:
- Queries: 14
- TTFB: >2000ms
- Memory: High (load all)

AFTER Optimization:
- Queries: 3
- TTFB: <actual_ttfb>ms
- Memory: Low (page only)

Index Usage:
- idx_restaurant_profile_approved: USED
- idx_restaurant_media_batch_cover: USED
- idx_booking_customer_history: USED

All tests: PASS/FAIL
EOF
```

**✅ CHECKPOINT:** Results documented for review

---

## 🚨 TROUBLESHOOTING

### Issue: Still seeing 14+ queries
**Cause:** Code not compiled or app not restarted  
**Fix:**
```bash
mvn clean compile
mvn spring-boot:run
```

### Issue: EXPLAIN shows Seq Scan instead of Index Scan
**Cause:** Indexes not created or table statistics outdated  
**Fix:**
```sql
-- Recreate indexes
\i database/performance_indexes.sql

-- Update statistics
ANALYZE restaurant_profile;
ANALYZE restaurant_media;

-- Force index usage (testing only)
SET enable_seqscan = OFF;
EXPLAIN ANALYZE SELECT ...;
```

### Issue: Application fails to start
**Cause:** Syntax error in application-dev.yml  
**Fix:**
```bash
# Check YAML indentation
cat target/classes/application-dev.yml | head -30

# Validate YAML syntax
python -c "import yaml; yaml.safe_load(open('target/classes/application-dev.yml'))"

# Restore original if needed
git checkout target/classes/application-dev.yml
```

### Issue: TTFB still > 1000ms
**Possible Causes:**
1. Indexes not created: See fix above
2. Network latency to remote DB: Check `\timing` in psql
3. Large dataset: Check `SELECT COUNT(*) FROM restaurant_profile`
4. Cold start: Try request 2-3 times, measure average

**Fix:**
```sql
-- Check slow queries
SELECT query, mean_exec_time, calls 
FROM pg_stat_statements 
WHERE mean_exec_time > 100 
ORDER BY mean_exec_time DESC;
```

### Issue: Connection pool exhausted
**Symptoms:** `HikariPool-1 - Connection is not available`  
**Fix:**
```yaml
# Increase pool size in application-dev.yml
spring.datasource.hikari.maximum-pool-size: 30  # Increase from 20
spring.datasource.hikari.minimum-idle: 10       # Increase from 5
```

---

## ✅ SUCCESS CHECKLIST

- [ ] All indexes created (30+ CREATE INDEX)
- [ ] Application restarted with new config
- [ ] Query count = 3 for /restaurants endpoint
- [ ] No TRACE log spam (bind parameters)
- [ ] TTFB < 500ms
- [ ] EXPLAIN shows Index Scan (not Seq Scan)
- [ ] Hikari pool active < 15 connections
- [ ] All filters work at database level
- [ ] Pagination works with LIMIT/OFFSET
- [ ] No connection leak warnings (normal operation)
- [ ] Test results documented
- [ ] Screenshots saved

---

## 📚 NEXT STEPS

1. **Fix Remaining N+1 Issues**
   - BookingService.getBookingWithDetailsById()
   - RestaurantOwnerController.waitlistManagement()
   - BookingController.showMyBookings()

2. **Add Caching**
   - Enable Spring Cache with Redis/Caffeine
   - Cache `findByApprovalStatus()` results

3. **Load Testing**
   - JMeter script for 50+ concurrent users
   - Monitor Hikari pool, CPU, memory

4. **Staging Deployment**
   - Test with production-like data volume
   - Verify performance improvements hold

5. **Production Deployment**
   - Create indexes CONCURRENTLY (avoid locks)
   - Monitor metrics for 24-48 hours
   - Rollback plan ready

---

## 📞 SUPPORT

**Questions or Issues?**
- Review: `PERFORMANCE_OPTIMIZATION_SUMMARY.md`
- Check: `performance_optimization_report.json`
- Database indexes: `database/performance_indexes.sql`

**Performance not as expected?**
- Compare actual vs expected in test results table
- Check troubleshooting section above
- Verify all prerequisites completed

**Ready for production?**
- Ensure all tests pass
- Load test completed successfully
- Monitoring in place (Grafana, Prometheus, etc.)

---

**Testing completed:** ___________  
**Tested by:** ___________  
**Status:** ⬜ Pass  ⬜ Fail  ⬜ Needs Review

