# 🚀 CÁCH CHẠY JACOCO REPORT CHO 18 FILES

## ⚠️ Vấn đề: Build fail nhưng vẫn muốn xem report

Nếu build fail do test errors (như trong hình), bạn vẫn có thể generate JaCoCo report bằng các cách sau:

---

## ✅ CÁCH 1: Chạy với flag ignore test failures (KHUYẾN NGHỊ)

```bash
mvn clean test -Dmaven.test.failure.ignore=true
```

**Ưu điểm:**
- Vẫn chạy tất cả tests
- Generate JaCoCo report ngay cả khi có test fail
- Xem được coverage cho cả tests pass và fail

**Sau khi chạy:**
```bash
# Mở report
start target/site/jacoco/index.html
```

---

## ✅ CÁCH 2: Chạy riêng JaCoCo report (nếu đã có jacoco.exec)

Nếu bạn đã chạy tests trước đó và có file `target/jacoco.exec`:

```bash
# Chỉ generate report từ file exec đã có
mvn jacoco:report
```

**Sau khi chạy:**
```bash
# Mở report
start target/site/jacoco/index.html
```

---

## ✅ CÁCH 3: Chạy tests cụ thể (chỉ 18 file cần thiết)

Nếu bạn chỉ muốn chạy tests cho 18 file này:

```bash
# Chạy tests cho các Service
mvn test -Dtest="BookingServiceTest,PaymentServiceTest,PayOsServiceTest,RestaurantManagementServiceTest,RestaurantDashboardServiceTest,RestaurantOwnerServiceTest,AdvancedRateLimitingServiceTest,RestaurantSecurityServiceTest,RefundServiceTest,WithdrawalServiceTest,WaitlistServiceTest,CustomerServiceTest,BookingConflictServiceTest,NotificationServiceImplTest"

# Chạy tests cho các Controller
mvn test -Dtest="BookingControllerTest,RestaurantRegistrationControllerTest,AdminRestaurantControllerTest,AdminDashboardControllerTest"
```

**Sau khi chạy:**
```bash
mvn jacoco:report
start target/site/jacoco/index.html
```

---

## ✅ CÁCH 4: Sửa cấu hình Maven để luôn generate report

Đã cập nhật `pom.xml` với:
- `maven-surefire-plugin` để xử lý test failures
- JaCoCo với wildcard patterns cho 18 file

**Chạy:**
```bash
mvn clean test -Dmaven.test.failure.ignore=true
```

---

## 📊 Kiểm tra Report

Sau khi generate report, mở file:
```
target/site/jacoco/index.html
```

**Kiểm tra:**
- ✅ Chỉ thấy 18 file trong danh sách
- ✅ Không thấy các file khác (domain, dto, repository, etc.)
- ✅ Coverage % chỉ tính cho 18 file này

---

## 🔍 Troubleshooting

### 1. Report không được generate:
```bash
# Kiểm tra file exec có tồn tại không
ls target/jacoco.exec

# Nếu không có, chạy lại tests
mvn clean test -Dmaven.test.failure.ignore=true
```

### 2. Report không chỉ hiện 18 file:
- Kiểm tra lại cấu hình trong `pom.xml`
- Đảm bảo includes/excludes đúng format

### 3. Không mở được file HTML:
```bash
# Dùng lệnh khác
cd target/site/jacoco
python -m http.server 8000
# Truy cập: http://localhost:8000
```

---

## 📝 Lưu ý

1. **Test failures vẫn hiển thị**: Flag `-Dmaven.test.failure.ignore=true` chỉ cho phép build tiếp tục, không sửa test failures

2. **Report có thể không chính xác**: Nếu tests fail, coverage có thể không reflect đúng code thực tế

3. **Nên fix tests**: Tốt nhất là fix test failures trước khi xem coverage report

---

**Tạo bởi:** Auto (AI Assistant)  
**Ngày:** $(date)

