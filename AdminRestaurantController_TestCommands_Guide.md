# Hướng dẫn chạy Tests - AdminRestaurantController

## 📋 Tổng quan

File này hướng dẫn chi tiết cách chạy tests cho AdminRestaurantController theo từng phần để dễ dàng debug khi có lỗi xảy ra.

---

## 🎯 Các câu lệnh test (có trong file: run_admin_restaurant_controller_tests.md)

### 1️⃣ Chạy TẤT CẢ tests (40 tests)
```bash
mvn test -Dtest=AdminRestaurantControllerTest
```
**Khi nào dùng:** Chạy lần đầu để kiểm tra toàn bộ, hoặc khi đã fix xong tất cả bugs

**Output mong đợi:**
```
Tests run: 40, Failures: 0, Errors: 0, Skipped: 0
```

---

### 2️⃣ Chạy riêng approveRestaurant() tests (8 tests)
```bash
mvn test -Dtest=AdminRestaurantControllerTest$ApproveRestaurantTests
```
**Khi nào dùng:** 
- Khi có bug liên quan đến approve restaurant
- Khi sửa logic trong `approveRestaurant()` method
- Debug các issues về approval flow

**Tests bao gồm:**
- ✅ testApproveRestaurant_WithPendingStatus_ShouldApproveSuccessfully
- ✅ testApproveRestaurant_WithoutReason_ShouldApproveSuccessfully
- ✅ testApproveRestaurant_WithRejectedStatus_ShouldNotApprove
- ✅ testApproveRestaurant_WithApprovedStatus_ShouldNotApprove
- ✅ testApproveRestaurant_WithSuspendedStatus_ShouldNotApprove
- ✅ testApproveRestaurant_WithNonExistentId_ShouldReturnFalse
- ✅ testApproveRestaurant_ShouldSendNotification
- ✅ testApproveRestaurant_ServiceException_ShouldHandleGracefully

**Các file liên quan cần check khi có bug:**
- `AdminRestaurantController.java` → method `approveRestaurant()`
- `RestaurantApprovalService.java` → method `approveRestaurant()`
- `RestaurantNotificationService.java` → notification logic

---

### 3️⃣ Chạy riêng rejectRestaurant() tests (9 tests)
```bash
mvn test -Dtest=AdminRestaurantControllerTest$RejectRestaurantTests
```
**Khi nào dùng:**
- Khi có bug liên quan đến reject restaurant
- Khi sửa validation cho rejection reason
- Debug các issues về rejection flow

**Tests bao gồm:**
- ✅ testRejectRestaurant_WithPendingStatus_ShouldRejectSuccessfully
- ✅ testRejectRestaurant_WithEmptyReason_ShouldReturnError
- ✅ testRejectRestaurant_WithNullReason_ShouldReturnError
- ✅ testRejectRestaurant_WithAlreadyApproved_ShouldNotReject
- ✅ testRejectRestaurant_WithSuspendedStatus_ShouldNotReject
- ✅ testRejectRestaurant_WithNonExistentId_ShouldReturnFalse
- ✅ testRejectRestaurant_ShouldSendNotificationWithReason
- ✅ testRejectRestaurant_ShouldClearApprovalReason
- ✅ testRejectRestaurant_ServiceException_ShouldHandleGracefully

**Các file liên quan cần check khi có bug:**
- `AdminRestaurantController.java` → method `rejectRestaurant()`
- `RestaurantApprovalService.java` → method `rejectRestaurant()`
- Validation logic cho rejection reason

---

### 4️⃣ Chạy riêng getRestaurants() tests (12 tests)
```bash
mvn test -Dtest=AdminRestaurantControllerTest$GetRestaurantsTests
```
**Khi nào dùng:**
- Khi có bug liên quan đến listing/filtering
- Khi sửa search functionality
- Debug các issues về query và filter logic

**Tests bao gồm:**
- ✅ testGetRestaurants_WithPendingStatus_ShouldReturnPendingOnly
- ✅ testGetRestaurants_WithAllStatuses_ShouldReturnCounts
- ✅ testGetRestaurants_WithApprovedFilter_ShouldReturnApprovedOnly
- ✅ testGetRestaurants_WithRejectedFilter_ShouldReturnRejectedOnly
- ✅ testGetRestaurants_WithSuspendedFilter_ShouldReturnSuspendedOnly
- ✅ testGetRestaurants_WithSearchByName_ShouldFilterRestaurants
- ✅ testGetRestaurants_WithSearchByAddress_ShouldFindRestaurants
- ✅ testGetRestaurants_WithSearchByCuisine_ShouldFindRestaurants
- ✅ testGetRestaurants_WithSearchByOwner_ShouldFindRestaurants
- ✅ testGetRestaurants_WithEmptyDatabase_ShouldReturnEmptyList
- ✅ testGetRestaurants_WithInvalidStatus_ShouldHandleGracefully
- ✅ testGetRestaurants_WithDatabaseException_ShouldHandleGracefully

**Các file liên quan cần check khi có bug:**
- `AdminRestaurantController.java` → method `restaurantRequests()`
- `RestaurantApprovalService.java` → methods: `getAllRestaurantsWithApprovalInfo()`, `searchRestaurants()`
- Filter và search logic

---

### 5️⃣ Chạy riêng Security tests (7 tests)
```bash
mvn test -Dtest=AdminRestaurantControllerTest$SecurityTests
```
**Khi nào dùng:**
- Khi có bug liên quan đến authentication/authorization
- Khi sửa security configuration
- Debug các issues về role-based access control

**Tests bao gồm:**
- ✅ testApproveRestaurant_WithoutAuthentication_ShouldRedirectToLogin
- ✅ testApproveRestaurant_WithCustomerRole_ShouldBeDenied
- ✅ testApproveRestaurant_WithRestaurantOwnerRole_ShouldBeDenied
- ✅ testRejectRestaurant_WithoutAuthentication_ShouldRedirectToLogin
- ✅ testRejectRestaurant_WithCustomerRole_ShouldBeDenied
- ✅ testGetRestaurants_WithoutAuthentication_ShouldRedirectToLogin
- ✅ testGetRestaurants_WithCustomerRole_ShouldBeDenied

**Các file liên quan cần check khi có bug:**
- Security Configuration
- `@PreAuthorize("hasRole('ADMIN')")` annotation
- Authentication filter

---

### 6️⃣ Chạy MỘT test cụ thể (để debug chi tiết)
```bash
mvn test -Dtest=AdminRestaurantControllerTest$ApproveRestaurantTests#testApproveRestaurant_WithPendingStatus_ShouldApproveSuccessfully
```
**Khi nào dùng:**
- Khi đã biết chính xác test nào bị fail
- Khi muốn focus vào một scenario cụ thể
- Chạy nhanh hơn, tiết kiệm thời gian

**Cách tìm tên test:**
- Xem log từ lần chạy trước để biết test nào fail
- Format: `ClassName$NestedClassName#testMethodName`

**Ví dụ khác:**
```bash
# Test reject with empty reason
mvn test -Dtest=AdminRestaurantControllerTest$RejectRestaurantTests#testRejectRestaurant_WithEmptyReason_ShouldReturnError

# Test search by name
mvn test -Dtest=AdminRestaurantControllerTest$GetRestaurantsTests#testGetRestaurants_WithSearchByName_ShouldFilterRestaurants
```

---

### 7️⃣ Chạy với VERBOSE output (để debug sâu)
```bash
mvn test -Dtest=AdminRestaurantControllerTest -X
```
**Khi nào dùng:**
- Khi cần xem chi tiết stacktrace
- Khi cần debug dependency injection issues
- Khi cần xem Spring Boot startup logs

**Output:** Rất chi tiết, bao gồm:
- Full stacktrace của tất cả exceptions
- Spring context initialization
- Bean creation logs
- SQL queries (nếu có)

---

### 8️⃣ Chạy và IGNORE failures (để xem tất cả kết quả)
```bash
mvn test -Dtest=AdminRestaurantControllerTest -Dmaven.test.failure.ignore=true
```
**Khi nào dùng:**
- Khi muốn xem tất cả tests run, kể cả khi có test fail
- Để biết có bao nhiêu tests pass/fail tổng cộng
- Để tạo full test report

**Lưu ý:** Build vẫn sẽ SUCCESS ngay cả khi có test fail

---

## 🔍 Workflow Debug khi có Bug

### Bước 1: Chạy toàn bộ tests
```bash
mvn test -Dtest=AdminRestaurantControllerTest
```
Xem có bao nhiêu tests fail và thuộc phần nào

### Bước 2: Chạy riêng phần bị lỗi
Ví dụ nếu lỗi ở approve:
```bash
mvn test -Dtest=AdminRestaurantControllerTest$ApproveRestaurantTests
```

### Bước 3: Chạy cụ thể test bị fail với verbose
```bash
mvn test -Dtest=AdminRestaurantControllerTest$ApproveRestaurantTests#testApproveRestaurant_WithPendingStatus_ShouldApproveSuccessfully -X
```

### Bước 4: Đọc log và fix code
- Xem stacktrace
- Check file controller
- Check file service
- Fix bug

### Bước 5: Rerun test đó
```bash
mvn test -Dtest=AdminRestaurantControllerTest$ApproveRestaurantTests#testApproveRestaurant_WithPendingStatus_ShouldApproveSuccessfully
```

### Bước 6: Chạy lại toàn bộ nhóm đó
```bash
mvn test -Dtest=AdminRestaurantControllerTest$ApproveRestaurantTests
```

### Bước 7: Chạy lại toàn bộ tests
```bash
mvn test -Dtest=AdminRestaurantControllerTest
```

---

## 📊 Đọc Test Output

### ✅ Khi test PASS
```
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

### ❌ Khi test FAIL

**⚠️ LƯU Ý:** Log rất dài (hàng trăm dòng)! **KHÔNG CẦN** chụp/copy tất cả!

#### 🎯 CHỈ CẦN 3 phần này:

**1. Summary Line (dòng đầu tiên - quan trọng nhất)**
```
[ERROR] Tests run: 8, Failures: 1, Errors: 0, Skipped: 0
```
→ Cho biết: 8 tests chạy, 1 fail, 0 error

**2. Test Name + Line Number**
```
[ERROR] Failures: 
[ERROR]   AdminRestaurantControllerTest$ApproveRestaurantTests.testApproveRestaurant_WithPendingStatus_ShouldApproveSuccessfully:102
```
→ Cho biết: Test nào fail, dòng code nào (line 102)

**3. Error Message (phần Expected vs Actual)**
```
Expected: redirect to "/admin/restaurant/requests/1"
     but: was redirect to "/"
```
→ Cho biết: Mong đợi gì, thực tế là gì

#### 📝 Ví dụ đầy đủ (CHỈ cần 3 phần này):
```
[ERROR] Tests run: 8, Failures: 1, Errors: 0, Skipped: 0
[ERROR] Failures: 
[ERROR]   AdminRestaurantControllerTest$ApproveRestaurantTests.testApproveRestaurant_WithPendingStatus_ShouldApproveSuccessfully:102
Expected: redirect to "/admin/restaurant/requests/1"
     but: was redirect to "/"
```

#### ❌ KHÔNG CẦN những phần này (có thể bỏ qua):
```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.example.booking.web.controller.AdminRestaurantControllerTest
... (hàng trăm dòng Spring Boot startup logs)
... (hàng trăm dòng bean initialization)
... (hàng trăm dòng DEBUG logs)
```

### ⚠️ Khi có ERROR (không phải FAILURE)

#### 🎯 CHỈ CẦN 3 phần này:

**1. Summary Line**
```
[ERROR] Tests run: 8, Failures: 0, Errors: 1, Skipped: 0
```

**2. Test Name + Exception Type**
```
[ERROR] Errors: 
[ERROR]   AdminRestaurantControllerTest$ApproveRestaurantTests.testApproveRestaurant_WithPendingStatus_ShouldApproveSuccessfully
  java.lang.NullPointerException
```

**3. Root Cause (5-10 dòng đầu của stacktrace)**
```
  java.lang.NullPointerException: Cannot invoke "RestaurantProfile.getApprovalStatus()" because "restaurant" is null
    at com.example.booking.service.RestaurantApprovalService.approveRestaurant(RestaurantApprovalService.java:145)
    at com.example.booking.web.controller.AdminRestaurantController.approveRestaurant(AdminRestaurantController.java:178)
```

#### ❌ KHÔNG CẦN phần còn lại của stacktrace (30-50 dòng framework code):
```
    at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
    at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
    at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
    at java.lang.reflect.Method.invoke(Method.java:498)
    at org.springframework.web.method.support.InvocableHandlerMethod.doInvoke(...)
    ... (còn 40+ dòng nữa - KHÔNG CẦN!)
```

---

### 🔍 Cách lọc log nhanh

#### Option 1: Scroll xuống cuối log
```bash
mvn test -Dtest=AdminRestaurantControllerTest$ApproveRestaurantTests
```
→ Scroll xuống **CUỐI CÙNG**, tìm phần `[ERROR] Failures:` hoặc `[ERROR] Errors:`

#### Option 2: Dùng grep (Linux/Mac) hoặc findstr (Windows)
```bash
# Linux/Mac
mvn test -Dtest=AdminRestaurantControllerTest | grep -A 10 "Failures:"

# Windows PowerShell
mvn test -Dtest=AdminRestaurantControllerTest | Select-String "Failures:" -Context 0,10
```

#### Option 3: Lưu log ra file, chỉ xem phần cuối
```bash
# Lưu ra file
mvn test -Dtest=AdminRestaurantControllerTest > test-output.log 2>&1

# Xem 50 dòng cuối (chứa error message)
tail -50 test-output.log        # Linux/Mac
Get-Content test-output.log -Tail 50   # Windows PowerShell
```

---

### 📋 Template để report bug

Khi report bug, CHỈ CẦN gửi:

```
Command đã chạy:
mvn test -Dtest=AdminRestaurantControllerTest$ApproveRestaurantTests

Kết quả:
[ERROR] Tests run: 8, Failures: 1, Errors: 0, Skipped: 0

Test bị lỗi:
testApproveRestaurant_WithPendingStatus_ShouldApproveSuccessfully:102

Error message:
Expected: redirect to "/admin/restaurant/requests/1"
     but: was redirect to "/"

File cần check:
- AdminRestaurantController.java line 178
- RestaurantApprovalService.java line 145
```

**Tổng cộng: ~10 dòng thay vì 500+ dòng log!** ✅

---

### 🎯 Quick Reference: Phần nào quan trọng?

| Phần của Log | Quan trọng? | Giải thích |
|--------------|-------------|------------|
| `[ERROR] Tests run: X, Failures: Y` | ⭐⭐⭐ Rất quan trọng | Tổng quan lỗi |
| Test name + line number | ⭐⭐⭐ Rất quan trọng | Biết test nào fail |
| `Expected:` vs `but was:` | ⭐⭐⭐ Rất quan trọng | Root cause |
| 5-10 dòng đầu stacktrace | ⭐⭐ Quan trọng | Vị trí lỗi trong code |
| Spring Boot startup logs | ❌ Không cần | Chỉ là framework initialization |
| Full stacktrace (30+ dòng) | ❌ Không cần | Framework internal calls |
| Bean creation logs | ❌ Không cần | Framework internal |

---

### 💡 Pro Tips

1. **Chạy với `-Dsurefire.useFile=false`** để output ngắn gọn hơn:
   ```bash
   mvn test -Dtest=AdminRestaurantControllerTest -Dsurefire.useFile=false
   ```

2. **Chỉ xem failed tests:**
   ```bash
   mvn test -Dtest=AdminRestaurantControllerTest | grep -E "FAILURE|ERROR" -A 5
   ```

3. **Lưu chỉ phần error:**
   ```bash
   mvn test -Dtest=AdminRestaurantControllerTest 2>&1 | grep -A 20 "Failures:" > errors-only.txt
   ```

---

**Error vs Failure:**
- **Failure:** Test chạy được nhưng assertion fail (business logic sai)
- **Error:** Test không chạy được (exception, NPE, dependency issues)

---

## 🛠️ Common Issues & Solutions

### Issue 1: "No tests were executed"
**Nguyên nhân:** Tên class hoặc method sai
**Solution:** 
- Check tên class: `AdminRestaurantControllerTest`
- Check tên nested class: `ApproveRestaurantTests`, `RejectRestaurantTests`, etc.
- Dùng `$` để access nested class

### Issue 2: "MockBean could not be injected"
**Nguyên nhân:** Service không được mock đúng
**Solution:**
- Check `@MockBean` annotation
- Check service constructor trong controller
- Restart IDE

### Issue 3: "403 Forbidden" khi test security
**Nguyên nhân:** CSRF token hoặc authentication thiếu
**Solution:**
- Đảm bảo có `.with(csrf())` trong POST requests
- Đảm bảo có `@WithMockUser(roles = "ADMIN")` cho admin tests

### Issue 4: Test chạy mãi không dừng
**Nguyên nhân:** Infinite loop hoặc blocking operation
**Solution:**
- Ctrl+C để stop
- Check code trong controller
- Check mock setup

---

## 📝 Tips cho việc maintain tests

### 1. Chạy tests trước khi commit
```bash
mvn test -Dtest=AdminRestaurantControllerTest
```

### 2. Chạy tests sau khi pull code mới
```bash
mvn clean test -Dtest=AdminRestaurantControllerTest
```

### 3. Khi thêm feature mới
- Thêm test vào đúng nested class
- Follow naming convention
- Chạy toàn bộ nhóm đó để đảm bảo không break existing tests

### 4. Khi refactor code
- Chạy toàn bộ tests
- Nếu có test fail, sửa test hoặc sửa code
- Đảm bảo all green trước khi commit

---

## 📞 Liên hệ & Support

Nếu có vấn đề với tests:
1. Đọc error message kỹ
2. Chạy lại với `-X` flag để xem chi tiết
3. Check file controller và service
4. Check documentation trong `AdminRestaurantController.md`

---

**Last Updated:** October 28, 2024  
**Version:** 1.0  
**Status:** ✅ Ready for use

