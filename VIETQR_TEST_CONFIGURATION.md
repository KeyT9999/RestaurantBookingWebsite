# VietQR Test Configuration

## 📋 Tổng Quan

Các test VietQR được cấu hình để **bỏ qua mặc định** trong môi trường PayOS. Điều này giúp:
- Tránh chạy các test không cần thiết khi sử dụng PayOS là payment provider chính
- Giảm thời gian chạy test
- Cho phép bật VietQR tests khi cần thiết bằng một flag đơn giản

## 🔧 Cấu Hình

### 1. pom.xml - Maven Surefire Plugin

```xml
<!-- Maven Surefire Plugin -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.1.2</version>
    <configuration>
        <!-- Default to PayOS provider, skip VietQR tests -->
        <systemPropertyVariables>
            <payment.provider>payos</payment.provider>
        </systemPropertyVariables>
    </configuration>
</plugin>
```

### 2. Test Class - VietQRServiceTest.java

```java
@ExtendWith(MockitoExtension.class)
@DisplayName("VietQRService Tests")
@EnabledIfSystemProperty(named = "payment.provider", matches = "vietqr")
class VietQRServiceTest {
    // Test methods...
}
```

## 🚀 Cách Sử Dụng

### Chạy test mặc định (PayOS provider - VietQR tests bị skip)

```bash
mvn test
```

**Kết quả**: Tất cả các test VietQR sẽ bị skip

### Chạy test VietQR

```bash
mvn test -Dpayment.provider=vietqr
```

hoặc

```bash
mvn -Dpayment.provider=vietqr test
```

**Kết quả**: Tất cả các test VietQR sẽ chạy

### Chạy cụ thể một test class VietQR

```bash
mvn test -Dtest=VietQRServiceTest -Dpayment.provider=vietqr
```

## 📊 Ví Dụ Kết Quả

### Khi payment.provider=payos (mặc định):

```
[WARNING] Tests run: 5, Failures: 0, Errors: 0, Skipped: 5, Time elapsed: 0 s
  -- in com.example.booking.service.VietQRServiceTest
```

**5 tests bị skip** ✅

### Khi payment.provider=vietqr:

```
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
  -- in com.example.booking.service.VietQRServiceTest
```

**10 tests chạy thành công** ✅

## 🎯 Lợi Ích

1. **Hiệu suất**: Giảm thời gian chạy test khi sử dụng PayOS
2. **Linh hoạt**: Dễ dàng bật/tắt VietQR tests
3. **Rõ ràng**: Dễ hiểu payment provider nào đang được sử dụng
4. **CI/CD**: Có thể cấu hình riêng cho các môi trường khác nhau

## 📝 Notes

- **VietQRService** không phải là payment gateway, mà là service để:
  - Lấy danh sách ngân hàng
  - Verify số tài khoản
- Service này vẫn hoạt động bình thường trong production
- Tests chỉ được skip khi chạy test suite mặc định
- Có thể bật VietQR tests bất kỳ lúc nào khi cần

