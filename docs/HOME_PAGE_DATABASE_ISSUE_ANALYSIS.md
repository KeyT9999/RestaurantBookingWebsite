# PHÂN TÍCH VẤN ĐỀ KHÔNG GỌI ĐƯỢC DATABASE ĐỂ HIỂN THỊ NHÀ HÀNG NỔI BẬT

## 📋 TỔNG QUAN

Báo cáo này phân tích các nguyên nhân có thể khiến trang home không thể gọi database để hiển thị các nhà hàng nổi bật (popular restaurants).

---

## 🔴 NGUYÊN NHÂN NGHIÊM TRỌNG (Critical Causes)

### 1. **THIẾU EXCEPTION HANDLING trong HomeController.home()**

**📍 Địa chỉ file:** 
`src/main/java/com/example/booking/web/controller/HomeController.java`

**📍 Dòng code có vấn đề:** **Line 122**

```java
// ❌ VẤN ĐỀ: Không có try-catch, nếu query fail thì page sẽ crash
// Popular restaurants for home page
List<RestaurantProfile> topRestaurants = restaurantService.findTopRatedRestaurants(6);
List<PopularRestaurantDto> popularRestaurants = buildPopularRestaurantCards(topRestaurants);
model.addAttribute("popularRestaurants", popularRestaurants);
```

**🔍 Phân tích:**
- Method `home()` **KHÔNG CÓ try-catch** cho việc gọi database
- Nếu `findTopRatedRestaurants()` throw exception (SQL error, connection error, etc.), **toàn bộ page sẽ crash**
- User sẽ thấy error page thay vì empty list

**⚠️ Hậu quả:**
- Application crash khi có database error
- Không có fallback mechanism
- User experience rất tệ

**✅ Giải pháp:**
```java
// Popular restaurants for home page
try {
    List<RestaurantProfile> topRestaurants = restaurantService.findTopRatedRestaurants(6);
    List<PopularRestaurantDto> popularRestaurants = buildPopularRestaurantCards(topRestaurants);
    model.addAttribute("popularRestaurants", popularRestaurants);
} catch (Exception e) {
    log.error("Error loading popular restaurants: {}", e.getMessage(), e);
    model.addAttribute("popularRestaurants", Collections.emptyList());
    // Page vẫn render được, chỉ không có data
}
```

---

### 2. **JPA QUERY CÓ THỂ GÂY SQL ERROR**

**📍 Địa chỉ file:** 
`src/main/java/com/example/booking/repository/RestaurantProfileRepository.java`

**📍 Dòng code có vấn đề:** **Lines 131-136**

```java
@Query("SELECT r FROM RestaurantProfile r " +
       "LEFT JOIN r.reviews rv " +
       "WHERE r.approvalStatus = 'APPROVED' AND r.restaurantId <> 37 " +
       "GROUP BY r " +
       "ORDER BY COALESCE(AVG(rv.rating), 0) DESC, COUNT(rv) DESC, r.approvedAt DESC")
List<RestaurantProfile> findTopRatedRestaurants(Pageable pageable);
```

**🔍 Các vấn đề tiềm ẩn:**

#### 2.1. **Enum Comparison bằng String thay vì Enum Type**
```java
// ❌ SAI: So sánh enum bằng string literal
"WHERE r.approvalStatus = 'APPROVED'"

// ✅ ĐÚNG: Sử dụng enum parameter
"WHERE r.approvalStatus = :approvalStatus"
```

**Vấn đề:**
- JPA có thể không parse đúng enum value từ database
- Nếu database lưu enum khác format (ví dụ: số thay vì string), query sẽ fail
- Không type-safe

**✅ Giải pháp:**
```java
@Query("SELECT r FROM RestaurantProfile r " +
       "LEFT JOIN r.reviews rv " +
       "WHERE r.approvalStatus = :approvalStatus AND r.restaurantId <> :excludeId " +
       "GROUP BY r " +
       "ORDER BY COALESCE(AVG(rv.rating), 0) DESC, COUNT(rv) DESC, r.approvedAt DESC")
List<RestaurantProfile> findTopRatedRestaurants(
    @Param("approvalStatus") RestaurantApprovalStatus approvalStatus,
    @Param("excludeId") Integer excludeId,
    Pageable pageable);
```

#### 2.2. **LEFT JOIN với Lazy Loading có thể gây N+1 Problem**
- `r.reviews` có thể là `LAZY` fetch
- Query LEFT JOIN có thể không fetch reviews đúng cách
- Khi access `rv.rating` trong GROUP BY, có thể gây LazyInitializationException

#### 2.3. **GROUP BY có thể không đầy đủ**
- JPA spec yêu cầu tất cả non-aggregated columns phải có trong GROUP BY
- Query này chỉ có `GROUP BY r` nhưng ORDER BY có `r.approvedAt` - có thể gây SQL error trên một số database

**✅ Giải pháp cải thiện:**
```java
@Query("SELECT r FROM RestaurantProfile r " +
       "LEFT JOIN FETCH r.reviews rv " +  // FETCH để load reviews
       "WHERE r.approvalStatus = :approvalStatus " +
       "AND r.restaurantId <> :excludeId " +
       "GROUP BY r.restaurantId, r.appaurantName, r.approvedAt " +  // Explicit GROUP BY
       "ORDER BY COALESCE(AVG(rv.rating), 0) DESC, COUNT(rv) DESC, r.approvedAt DESC")
List<RestaurantProfile> findTopRatedRestaurants(
    @Param("approvalStatus") RestaurantApprovalStatus approvalStatus,
    @Param("excludeId") Integer excludeId,
    Pageable pageable);
```

---

### 3. **THIẾU DỮ LIỆU TRONG DATABASE**

**🔍 Nguyên nhân có thể:**

#### 3.1. **Không có nhà hàng nào có approvalStatus = 'APPROVED'**
```sql
-- Kiểm tra dữ liệu
SELECT COUNT(*) FROM restaurant_profile 
WHERE approval_status = 'APPROVED' AND restaurant_id <> 37;
-- Nếu kết quả = 0 → Không có nhà hàng để hiển thị
```

#### 3.2. **Dữ liệu approval_status không đúng format**
```sql
-- Kiểm tra format
SELECT DISTINCT approval_status FROM restaurant_profile;
-- Nếu không có 'APPROVED' → Query sẽ không trả về gì
```

#### 3.3. **Restaurant ID 37 vẫn bị filter ra**
- Query loại bỏ restaurant ID = 37 (AI restaurant)
- Nếu tất cả nhà hàng đều là ID 37 → Không có gì để hiển thị

---

### 4. **DATABASE CONNECTION ISSUES**

**📍 Địa chỉ file kiểm tra:**
- `src/main/resources/application.properties` hoặc `application.yml`
- Database configuration files

**🔍 Vấn đề có thể:**

#### 4.1. **Connection Pool Exhausted**
- Nếu connection pool hết, query sẽ timeout hoặc fail
- Không có retry mechanism

#### 4.2. **Transaction Timeout**
- Query phức tạp với LEFT JOIN và GROUP BY có thể chạy lâu
- Nếu timeout ngắn → Query bị cancel

#### 4.3. **Database Lock**
- Nếu có transaction khác đang lock table `restaurant_profile`
- Query sẽ bị block hoặc timeout

---

### 5. **NULL POINTER EXCEPTION trong buildPopularRestaurantCards()**

**📍 Địa chỉ file:** 
`src/main/java/com/example/booking/web/controller/HomeController.java`

**📍 Dòng code có vấn đề:** **Lines 134-140**

```java
List<RestaurantMedia> coverMedia = restaurantMediaRepository.findByRestaurantsAndType(restaurants, "cover");
Map<Integer, String> coverMap = coverMedia.stream()
        .collect(Collectors.toMap(
                media -> media.getRestaurant().getRestaurantId(),  // ❌ Có thể NPE nếu restaurant = null
                RestaurantMedia::getUrl,
                (existing, ignored) -> existing,
                LinkedHashMap::new));
```

**🔍 Vấn đề:**
- Nếu `media.getRestaurant()` = null → **NullPointerException**
- Nếu `restaurants` list có null elements → Query sẽ fail

**✅ Giải pháp:**
```java
List<RestaurantMedia> coverMedia = restaurantMediaRepository.findByRestaurantsAndType(restaurants, "cover");
Map<Integer, String> coverMap = coverMedia.stream()
        .filter(media -> media.getRestaurant() != null)  // Filter null
        .collect(Collectors.toMap(
                media -> media.getRestaurant().getRestaurantId(),
                RestaurantMedia::getUrl,
                (existing, ignored) -> existing,
                LinkedHashMap::new));
```

---

## 🟡 NGUYÊN NHÂN QUAN TRỌNG (Important Causes)

### 6. **ReviewService.getRestaurantReviewStatistics() CÓ THỂ FAIL**

**📍 Địa chỉ file:** 
`src/main/java/com/example/booking/web/controller/HomeController.java`

**📍 Dòng code có vấn đề:** **Lines 145-151**

```java
ReviewStatisticsDto statistics = null;
try {
    statistics = reviewService.getRestaurantReviewStatistics(restaurant.getRestaurantId());
} catch (Exception ex) {
    System.err.println("⚠️ Unable to load review statistics for restaurant "
            + restaurant.getRestaurantId() + ": " + ex.getMessage());
}
// Sử dụng statistics sau đó, có thể null
```

**🔍 Vấn đề:**
- Nếu `getRestaurantReviewStatistics()` fail, `statistics` = null
- Code vẫn tiếp tục nhưng có thể gây vấn đề logic sau đó

---

### 7. **PAGEABLE KHÔNG ĐƯỢC XỬ LÝ ĐÚNG**

**📍 Địa chỉ file:** 
`src/main/java/com/example/booking/service/RestaurantManagementService.java`

**📍 Dòng code:** **Lines 128-134**

```java
@Transactional(readOnly = true)
public List<RestaurantProfile> findTopRatedRestaurants(int limit) {
    if (limit <= 0) {
        return Collections.emptyList();
    }
    Pageable pageable = PageRequest.of(0, limit);
    return restaurantProfileRepository.findTopRatedRestaurants(pageable);
}
```

**🔍 Vấn đề:**
- Nếu `limit` > số lượng restaurants có sẵn → OK
- Nhưng nếu database không support LIMIT (một số old databases) → Error

---

## 📊 CHECKLIST KIỂM TRA

### ✅ Kiểm tra Database:

```sql
-- 1. Kiểm tra có nhà hàng APPROVED không
SELECT COUNT(*) FROM restaurant_profile 
WHERE approval_status = 'APPROVED' AND restaurant_id <> 37;

-- 2. Kiểm tra format của approval_status
SELECT DISTINCT approval_status FROM restaurant_profile;

-- 3. Kiểm tra có reviews không
SELECT COUNT(*) FROM review;

-- 4. Kiểm tra relationship reviews với restaurants
SELECT r.restaurant_id, COUNT(rv.review_id) 
FROM restaurant_profile r 
LEFT JOIN review rv ON r.restaurant_id = rv.restaurant_id 
WHERE r.approval_status = 'APPROVED' 
GROUP BY r.restaurant_id;
```

### ✅ Kiểm tra Application Logs:

```bash
# Tìm error logs
grep -i "error" logs/application.log | grep -i "restaurant"
grep -i "exception" logs/application.log | grep -i "findTopRatedRestaurants"
grep -i "sql" logs/application.log | grep -i "restaurant_profile"
```

### ✅ Kiểm tra Code:

1. [ ] HomeController có try-catch cho findTopRatedRestaurants() không?
2. [ ] Repository query có đúng syntax không?
3. [ ] Enum comparison có đúng không?
4. [ ] Có null check cho restaurant và media không?
5. [ ] Database connection có ổn không?

---

## 🎯 GIẢI PHÁP ƯU TIÊN

### 🔥 **Ưu tiên cao (Sửa ngay):**

1. ✅ **Thêm try-catch trong HomeController.home()** (Line 122)
2. ✅ **Fix enum comparison trong Repository query** (Line 133)
3. ✅ **Thêm null checks trong buildPopularRestaurantCards()** (Line 134)
4. ✅ **Kiểm tra dữ liệu trong database**

### 📈 **Ưu tiên trung bình:**

5. ✅ **Cải thiện JPA query** (GROUP BY, FETCH JOIN)
6. ✅ **Thêm logging để debug**
7. ✅ **Kiểm tra database connection pool**

---

## 📝 CODE FIX ĐỀ XUẤT

### Fix 1: HomeController.java - Thêm Exception Handling

**File:** `src/main/java/com/example/booking/web/controller/HomeController.java`
**Line:** 121-126

```java
// Popular restaurants for home page
try {
    List<RestaurantProfile> topRestaurants = restaurantService.findTopRatedRestaurants(6);
    if (topRestaurants != null && !topRestaurants.isEmpty()) {
        List<PopularRestaurantDto> popularRestaurants = buildPopularRestaurantCards(topRestaurants);
        model.addAttribute("popularRestaurants", popularRestaurants);
    } else {
        log.warn("No top-rated restaurants found");
        model.addAttribute("popularRestaurants", Collections.emptyList());
    }
} catch (Exception e) {
    log.error("Error loading popular restaurants: {}", e.getMessage(), e);
    model.addAttribute("popularRestaurants", Collections.emptyList());
}
```

### Fix 2: RestaurantProfileRepository.java - Fix Enum Comparison

**File:** `src/main/java/com/example/booking/repository/RestaurantProfileRepository.java`
**Line:** 131-136

```java
/**
 * Find top-rated approved restaurants sorted by rating, review count, and approval time
 */
@Query("SELECT r FROM RestaurantProfile r " +
       "LEFT JOIN FETCH r.reviews rv " +
       "WHERE r.approvalStatus = :approvalStatus " +
       "AND r.restaurantId <> :excludeId " +
       "GROUP BY r.restaurantId " +
       "ORDER BY COALESCE(AVG(rv.rating), 0) DESC, COUNT(rv) DESC, r.approvedAt DESC")
List<RestaurantProfile> findTopRatedRestaurants(
    @Param("approvalStatus") RestaurantApprovalStatus approvalStatus,
    @Param("excludeId") Integer excludeId,
    Pageable pageable);
```

Và update RestaurantManagementService.java:

```java
@Transactional(readOnly = true)
public List<RestaurantProfile> findTopRatedRestaurants(int limit) {
    if (limit <= 0) {
        return Collections.emptyList();
    }
    Pageable pageable = PageRequest.of(0, limit);
    return restaurantProfileRepository.findTopRatedRestaurants(
        RestaurantApprovalStatus.APPROVED, 
        37,  // Exclude AI restaurant
        pageable);
}
```

### Fix 3: HomeController.java - Fix Null Checks

**File:** `src/main/java/com/example/booking/web/controller/HomeController.java`
**Line:** 134-140

```java
List<RestaurantMedia> coverMedia = restaurantMediaRepository.findByRestaurantsAndType(
    restaurants.stream()
        .filter(r -> r != null && r.getRestaurantId() != null)
        .collect(Collectors.toList()), 
    "cover");

Map<Integer, String> coverMap = coverMedia.stream()
        .filter(media -> media != null && media.getRestaurant() != null)
        .collect(Collectors.toMap(
                media -> media.getRestaurant().getRestaurantId(),
                RestaurantMedia::getUrl,
                (existing, ignored) -> existing,
                LinkedHashMap::new));
```

---

## 🎯 KẾT LUẬN

**Nguyên nhân có thể gây lỗi (theo thứ tự ưu tiên):**

1. **❌ Thiếu Exception Handling** - 90% khả năng
2. **❌ JPA Query sai** - 70% khả năng (enum comparison)
3. **❌ Không có dữ liệu APPROVED** - 50% khả năng
4. **❌ Database Connection Issues** - 30% khả năng
5. **❌ Null Pointer Exception** - 20% khả năng

**Hành động ngay:**
1. Thêm try-catch và logging
2. Kiểm tra dữ liệu database
3. Fix JPA query enum comparison
4. Test lại flow end-to-end

