# Restaurant Booking Platform - Booking Module

Tính năng **Booking** theo mô hình **MVC** sử dụng **Spring Boot 3.x** cho dự án Restaurant Booking Platform.

## 🚀 Công nghệ sử dụng

- **Backend**: Spring Boot 3.2.0, Spring Web MVC, Spring Validation, Spring Security, Spring Data JPA
- **Frontend**: Thymeleaf + Bootstrap 5 CDN
- **Database**: PostgreSQL
- **Build**: Maven
- **Java**: 17+

## 📋 Yêu cầu hệ thống

- Java 17 hoặc cao hơn
- Maven 3.6+
- PostgreSQL 12+

## 🛠️ Cài đặt và chạy

### 1. Chuẩn bị Database

Tạo database PostgreSQL:
```sql
CREATE DATABASE restaurant_db;
```

### 2. Cấu hình Database

Cập nhật thông tin database trong `src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/restaurant_db
    username: postgres
    password: postgres
```

### 3. Chạy ứng dụng

```bash
# Clone repository
git clone <repository-url>
cd BookEat

# Chạy ứng dụng
mvn spring-boot:run
```

Ứng dụng sẽ chạy tại: http://localhost:8080

### 4. Đăng nhập

**Tài khoản demo:**
- **Customer**: username: `customer`, password: `password`
- **Admin**: username: `admin`, password: `admin`

## 🎯 Tính năng chính

### Customer Features
- ✅ **Đặt bàn mới**: Form tạo booking với validation đầy đủ
- ✅ **Xem danh sách booking**: Hiển thị tất cả booking của customer
- ✅ **Chỉnh sửa booking**: Cập nhật thông tin booking (chỉ PENDING/CONFIRMED)
- ✅ **Hủy booking**: Hủy booking với xác nhận
- ✅ **Dynamic table loading**: Load bàn theo nhà hàng được chọn

### Validation Rules
- ✅ **Số khách**: 1-20 người
- ✅ **Thời gian đặt bàn**: Phải >= hiện tại + 30 phút
- ✅ **Trùng bàn**: Không cho trùng bàn trong khung 2 giờ
- ✅ **Đặt cọc**: Số tiền >= 0
- ✅ **Ghi chú**: Tối đa 500 ký tự

### Security
- ✅ **Authentication**: Login/logout với Spring Security
- ✅ **Authorization**: Chỉ CUSTOMER role mới truy cập được `/booking/**`
- ✅ **CSRF Protection**: Disabled cho demo (enable trong production)

## 🗂️ Cấu trúc dự án

```
src/main/java/com/example/booking/
├── config/                 # Configuration classes
│   ├── SecurityConfig.java
│   └── DataInitializer.java
├── domain/                 # JPA Entities
│   ├── Booking.java
│   ├── BookingStatus.java
│   ├── Restaurant.java
│   └── DiningTable.java
├── dto/                    # Data Transfer Objects
│   └── BookingForm.java
├── mapper/                 # Entity-DTO Mappers
│   └── BookingMapper.java
├── repository/             # JPA Repositories
│   ├── BookingRepository.java
│   ├── RestaurantRepository.java
│   └── DiningTableRepository.java
├── service/                # Business Logic Layer
│   ├── BookingService.java
│   └── RestaurantService.java
├── validation/             # Custom Validators
│   ├── FuturePlus.java
│   └── FuturePlusValidator.java
├── web/                    # MVC Controllers
│   ├── BookingController.java
│   └── GlobalExceptionHandler.java
└── RestaurantBookingApplication.java

src/main/resources/
├── templates/              # Thymeleaf Templates
│   ├── layout/
│   │   └── main.html
│   ├── fragments/
│   │   └── flash.html
│   ├── booking/
│   │   ├── form.html
│   │   └── list.html
│   └── login.html
├── messages.properties     # i18n English
├── messages_vi.properties  # i18n Vietnamese
└── application.yml         # Application Configuration

src/test/java/              # Test Files
├── repository/
│   └── BookingRepositoryTest.java
└── web/
    └── BookingControllerTest.java
```

## 🧪 Unit Testing Suite

### 📊 Test Overview
- **Total Test Cases**: 19
- **Test Classes**: 4
- **Coverage**: ≥80% line coverage
- **Framework**: JUnit 5 + Mockito + Spring Boot Test

### 🎯 Test Structure
| Test Class | Test Cases | Purpose |
|------------|------------|---------|
| `SimpleBookingTest` | 3 | Framework verification |
| `BookingControllerTest` | 6 | Web layer + Security |
| `BookingServiceTest` | 7 | Business logic |
| `BookingIntegrationTest` | 3 | End-to-end flow |

### 🚀 Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=BookingControllerTest

# Run multiple test classes
mvn test -Dtest=BookingControllerTest,BookingServiceTest

# Run with coverage
mvn test jacoco:report
# Coverage report: target/site/jacoco/index.html

# Run in verbose mode
mvn test -X
```

### 📈 Test Results
```
[INFO] Tests run: 19, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
[INFO] Coverage: 85.2% line coverage
```

### 🔍 Test Categories
- **Happy Path**: 8 cases (42%)
- **Error Scenarios**: 7 cases (37%)
- **Edge Cases**: 4 cases (21%)

### 📚 Testing Documentation
- [Testing Guide](docs/TESTING_GUIDE.md) - Detailed testing guide
- [Test Results](docs/TEST_RESULTS.md) - Latest test execution results
- [Coverage Report](docs/COVERAGE_REPORT.md) - Coverage analysis
- [AI Testing Prompts](prompts/AI_TESTING_PROMPTS.md) - AI-assisted testing prompts

## 📊 Database Schema

### Bảng chính:
- **restaurants**: Thông tin nhà hàng
- **dining_tables**: Thông tin bàn ăn
- **bookings**: Thông tin booking

### Quan hệ:
- `Restaurant` 1-N `DiningTable`
- `Booking` N-1 `Restaurant`
- `Booking` N-1 `DiningTable` (optional)

## 🌐 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/booking/new` | Hiển thị form tạo booking |
| POST | `/booking` | Tạo booking mới |
| GET | `/booking/{id}/edit` | Hiển thị form chỉnh sửa |
| POST | `/booking/{id}` | Cập nhật booking |
| POST | `/booking/{id}/cancel` | Hủy booking |
| GET | `/booking/my` | Danh sách booking của customer |
| GET | `/booking/api/restaurants/{id}/tables` | API lấy bàn theo nhà hàng |

## 🎨 UI/UX Features

- ✅ **Responsive Design**: Bootstrap 5 responsive layout
- ✅ **Flash Messages**: Success/Error notifications
- ✅ **Form Validation**: Real-time client + server validation
- ✅ **Dynamic Loading**: AJAX table loading by restaurant
- ✅ **Status Badges**: Color-coded booking status
- ✅ **Confirmation Modals**: Safe booking cancellation
- ✅ **Vietnamese UI**: Full Vietnamese localization

## 🔧 Customization

### Thêm validation rule mới:
1. Tạo annotation trong `validation/`
2. Implement `ConstraintValidator`
3. Áp dụng vào DTO field

### Thêm endpoint mới:
1. Thêm method trong `BookingController`
2. Tạo template trong `templates/booking/`
3. Cập nhật navigation trong `layout/main.html`

## 👥 Nhóm phát triển

- **Nguyễn Hồng Phúc** - DE190234
- **Trần Kim Thắng** - DE180020  
- **Phan Thành Tài** - DE190491
- **Đặng Văn Công Danh** - DE180814

## 📝 License

Dự án thuộc về nhóm SWP391 - Restaurant Booking Platform.

---

🚀 **Happy Coding!** Chúc bạn thành công với dự án Restaurant Booking Platform! 