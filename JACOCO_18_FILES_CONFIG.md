# 📊 Cấu hình JaCoCo chỉ report 18 file được chỉ định

## ✅ Đã cấu hình xong

File `pom.xml` đã được cập nhật để JaCoCo chỉ report coverage cho **18 file** được chỉ định.

### 📋 Danh sách 18 file được include:

#### Service Layer (14 files):
1. `BookingService`
2. `PaymentService`
3. `PayOsService`
4. `RestaurantManagementService`
5. `RestaurantDashboardService`
6. `RestaurantOwnerService`
7. `AdvancedRateLimitingService`
8. `RestaurantSecurityService`
9. `RefundService`
10. `WithdrawalService`
11. `WaitlistService`
12. `CustomerService`
13. `BookingConflictService`
14. `NotificationService` + `NotificationServiceImpl`

#### Controller Layer (4 files):
15. `BookingController`
16. `RestaurantRegistrationController`
17. `AdminRestaurantController`
18. `AdminDashboardController`

---

## 🚀 Cách sử dụng

### 1. Chạy tests và generate JaCoCo report:

**Cách 1: Chạy bình thường (build sẽ fail nếu có test lỗi):**
```bash
mvn clean test
```

**Cách 2: Chạy và vẫn generate report dù có test fail:**
```bash
mvn clean test -Dmaven.test.failure.ignore=true
```

**Cách 3: Chỉ chạy JaCoCo report (nếu đã có jacoco.exec):**
```bash
mvn jacoco:report
```

Sau khi chạy xong, JaCoCo sẽ tự động generate report tại:
```
target/site/jacoco/index.html
```

### ⚠️ Nếu build fail do test errors:

Nếu bạn thấy lỗi như:
```
Tests run: 829, Failures: 19, Errors: 65
BUILD FAILURE
```

Vẫn có thể generate report bằng cách:
```bash
# Bỏ qua test failures và vẫn generate report
mvn clean test -Dmaven.test.failure.ignore=true

# Hoặc chỉ generate report từ file exec đã có
mvn jacoco:report
```

### 2. Xem report:

```bash
# Mở file HTML trong browser
target/site/jacoco/index.html
```

Hoặc chạy server đơn giản:
```bash
cd target/site/jacoco
python -m http.server 8000
# Truy cập: http://localhost:8000
```

---

## 📝 Cấu hình trong pom.xml

Cấu hình JaCoCo đã được thêm vào `pom.xml`:

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <configuration>
        <!-- Chỉ report coverage cho 18 file được chỉ định -->
        <includes>
            <!-- Service Layer (14 files) -->
            <include>com/example/booking/service/BookingService.class</include>
            <include>com/example/booking/service/PaymentService.class</include>
            <!-- ... các file khác ... -->
            <!-- Controller Layer (4 files) -->
            <include>com/example/booking/web/controller/BookingController.class</include>
            <!-- ... -->
        </includes>
    </configuration>
    <!-- ... executions ... -->
</plugin>
```

---

## ⚠️ Lưu ý

1. **Format includes**: Phải theo format `com/example/booking/package/ClassName.class` (dùng `/` không phải `.`)

2. **Sau khi sửa pom.xml**: Cần chạy lại:
   ```bash
   mvn clean test
   ```

3. **Nếu không thấy các file trong report**: 
   - Kiểm tra xem các class có được compile không (`target/classes/`)
   - Kiểm tra format includes có đúng không
   - Kiểm tra xem các class có tồn tại không

4. **Thêm file mới vào report**: 
   - Thêm dòng `<include>` mới vào `<includes>` section trong pom.xml

---

## 🔍 Kiểm tra

Sau khi chạy `mvn test`, mở file `target/site/jacoco/index.html` và kiểm tra:

- ✅ Chỉ thấy 18 file trong danh sách
- ✅ Không thấy các file khác (repository, domain, dto, etc.)
- ✅ Coverage % chỉ tính cho 18 file này

---

## 📊 Test lại cấu hình

```bash
# Clean và test lại
mvn clean test

# Xem report
start target/site/jacoco/index.html  # Windows
open target/site/jacoco/index.html   # Mac
xdg-open target/site/jacoco/index.html  # Linux
```

---

**Tạo bởi:** Auto (AI Assistant)  
**Ngày:** $(date)

