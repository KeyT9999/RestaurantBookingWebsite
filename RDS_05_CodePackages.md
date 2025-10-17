# REQUIREMENT & DESIGN SPECIFICATION (RDS)
## HỆ THỐNG ĐẶT BÀN NHÀ HÀNG - RESTAURANT BOOKING PLATFORM

---

## 5. CODE PACKAGES (GÓI MÃ NGUỒN)

### 5.1 Tổng quan kiến trúc

**Kiến trúc**: Layered Architecture (MVC Pattern)  
**Framework**: Spring Boot 3.2.0  
**Build Tool**: Maven  
**Java Version**: 17+

### 5.2 Cấu trúc thư mục

```
src/main/java/com/example/booking/
├── config/                 # Configuration classes
├── domain/                 # JPA Entities & Enums
├── dto/                    # Data Transfer Objects
├── mapper/                 # Entity-DTO Mappers
├── repository/             # JPA Repositories
├── service/                # Business Logic Layer
├── web/                    # MVC Controllers
├── websocket/              # WebSocket Handlers
├── common/                 # Common utilities & constants
├── exception/              # Custom exceptions
├── validation/             # Custom validators
├── aspect/                 # AOP aspects
├── audit/                  # Audit logging
├── annotation/             # Custom annotations
├── util/                   # Utility classes
└── scheduler/              # Scheduled tasks
```

### 5.3 Chi tiết các Package

#### 5.3.1 Package: `com.example.booking.config`

**Mục đích**: Chứa các class cấu hình cho Spring Boot application.

**Trách nhiệm**:
- Cấu hình Spring Security
- Cấu hình JPA và Database
- Cấu hình WebSocket
- Cấu hình Rate Limiting
- Cấu hình Cloudinary
- Cấu hình PayOS
- Cấu hình Email
- Cấu hình OAuth2 (Google)

**Classes chính**:
- `SecurityConfig.java`: Cấu hình bảo mật và phân quyền
- `JpaConfig.java`: Cấu hình JPA và Hibernate
- `WebSocketSecurityConfig.java`: Cấu hình bảo mật WebSocket
- `RateLimitingConfig.java`: Cấu hình rate limiting
- `CloudinaryConfig.java`: Cấu hình Cloudinary service
- `PayoutConfiguration.java`: Cấu hình PayOS payout
- `DataInitializer.java`: Khởi tạo dữ liệu mẫu
- `WebConfig.java`: Cấu hình web MVC

#### 5.3.2 Package: `com.example.booking.domain`

**Mục đích**: Chứa các JPA Entity classes và Enums.

**Trách nhiệm**:
- Định nghĩa cấu trúc dữ liệu
- Mapping với database tables
- Định nghĩa relationships giữa entities
- Validation constraints
- Business logic cơ bản

**Classes chính**:
- `User.java`: Entity người dùng
- `Customer.java`: Entity khách hàng
- `RestaurantOwner.java`: Entity chủ nhà hàng
- `RestaurantProfile.java`: Entity nhà hàng
- `RestaurantTable.java`: Entity bàn nhà hàng
- `Booking.java`: Entity đặt bàn
- `Payment.java`: Entity thanh toán
- `ChatRoom.java`: Entity phòng chat
- `Message.java`: Entity tin nhắn
- `Review.java`: Entity đánh giá
- `Voucher.java`: Entity voucher
- `Waitlist.java`: Entity danh sách chờ

**Enums**:
- `UserRole.java`: Vai trò người dùng
- `BookingStatus.java`: Trạng thái booking
- `PaymentStatus.java`: Trạng thái thanh toán
- `TableStatus.java`: Trạng thái bàn
- `MessageType.java`: Loại tin nhắn
- `WaitlistStatus.java`: Trạng thái waitlist

#### 5.3.3 Package: `com.example.booking.dto`

**Mục đích**: Chứa các Data Transfer Objects để truyền dữ liệu giữa các layer.

**Trách nhiệm**:
- Đóng gói dữ liệu cho API requests/responses
- Validation dữ liệu đầu vào
- Mapping với entities
- Giảm coupling giữa các layer

**Classes chính**:
- `BookingForm.java`: Form đặt bàn
- `RestaurantDto.java`: DTO nhà hàng
- `RestaurantTableDto.java`: DTO bàn nhà hàng
- `BookingDetailsDto.java`: DTO chi tiết booking
- `ChatMessageDto.java`: DTO tin nhắn chat
- `ReviewForm.java`: Form đánh giá
- `VoucherDto.java`: DTO voucher
- `WaitlistDetailDto.java`: DTO chi tiết waitlist

**Sub-packages**:
- `admin/`: DTOs cho admin features
- `customer/`: DTOs cho customer features
- `payout/`: DTOs cho payout system
- `notification/`: DTOs cho notification system

#### 5.3.4 Package: `com.example.booking.repository`

**Mục đích**: Chứa các JPA Repository interfaces.

**Trách nhiệm**:
- Truy cập dữ liệu từ database
- Định nghĩa custom queries
- Xử lý pagination và sorting
- Optimize database queries

**Classes chính**:
- `UserRepository.java`: Repository cho User
- `CustomerRepository.java`: Repository cho Customer
- `RestaurantOwnerRepository.java`: Repository cho RestaurantOwner
- `RestaurantProfileRepository.java`: Repository cho RestaurantProfile
- `RestaurantTableRepository.java`: Repository cho RestaurantTable
- `BookingRepository.java`: Repository cho Booking
- `PaymentRepository.java`: Repository cho Payment
- `ChatRoomRepository.java`: Repository cho ChatRoom
- `MessageRepository.java`: Repository cho Message
- `ReviewRepository.java`: Repository cho Review
- `VoucherRepository.java`: Repository cho Voucher

#### 5.3.5 Package: `com.example.booking.service`

**Mục đích**: Chứa các Service classes xử lý business logic.

**Trách nhiệm**:
- Implement business rules
- Orchestrate multiple repositories
- Handle transactions
- Integration với external services
- Cache management
- Error handling

**Classes chính**:
- `BookingService.java`: Service xử lý booking logic
- `RestaurantManagementService.java`: Service quản lý nhà hàng
- `PaymentService.java`: Service xử lý thanh toán
- `ChatService.java`: Service xử lý chat
- `ReviewService.java`: Service xử lý đánh giá
- `VoucherService.java`: Service xử lý voucher
- `WaitlistService.java`: Service xử lý waitlist
- `SmartWaitlistService.java`: Service waitlist thông minh
- `RestaurantOwnerService.java`: Service cho restaurant owner
- `CustomerService.java`: Service cho customer
- `SimpleUserService.java`: Service quản lý user cơ bản
- `ImageUploadService.java`: Service upload hình ảnh
- `EmailService.java`: Service gửi email
- `RestaurantBalanceService.java`: Service quản lý số dư
- `WithdrawalService.java`: Service xử lý rút tiền
- `PayosPayoutService.java`: Service tích hợp PayOS payout

#### 5.3.6 Package: `com.example.booking.web`

**Mục đích**: Chứa các MVC Controller classes.

**Trách nhiệm**:
- Handle HTTP requests
- Validate input data
- Call appropriate services
- Return responses (HTML/JSON)
- Handle exceptions
- Authentication & authorization

**Classes chính**:
- `HomeController.java`: Controller trang chủ
- `AuthController.java`: Controller authentication
- `BookingController.java`: Controller đặt bàn
- `RestaurantOwnerController.java`: Controller restaurant owner
- `AdminDashboardController.java`: Controller admin dashboard
- `PaymentController.java`: Controller thanh toán
- `ReviewController.java`: Controller đánh giá
- `ChatController.java`: Controller chat

**Sub-packages**:
- `controller/`: Main controllers
- `controller/admin/`: Admin-specific controllers
- `controller/customer/`: Customer-specific controllers
- `controller/restaurantowner/`: Restaurant owner controllers
- `controller/api/`: REST API controllers
- `advice/`: Global exception handlers
- `config/`: Web-specific configurations

#### 5.3.7 Package: `com.example.booking.websocket`

**Mục đích**: Chứa các WebSocket handler classes.

**Trách nhiệm**:
- Handle real-time chat
- Manage WebSocket connections
- Broadcast messages
- Handle connection lifecycle

**Classes chính**:
- `ChatWebSocketHandler.java`: Handler cho chat WebSocket

#### 5.3.8 Package: `com.example.booking.common`

**Mục đích**: Chứa các utilities và constants dùng chung.

**Trách nhiệm**:
- Định nghĩa constants
- Utility functions
- Base classes
- Common enums

**Sub-packages**:
- `constants/`: Application constants
- `enums/`: Common enums
- `util/`: Utility classes
- `base/`: Base entity classes
- `api/`: Common API response classes

#### 5.3.9 Package: `com.example.booking.exception`

**Mục đích**: Chứa các custom exception classes.

**Trách nhiệm**:
- Định nghĩa business exceptions
- Handle specific error cases
- Provide meaningful error messages

**Classes chính**:
- `BookingConflictException.java`: Exception khi booking bị conflict
- `RestaurantNotFoundException.java`: Exception khi không tìm thấy nhà hàng
- `PaymentException.java`: Exception khi thanh toán lỗi
- `ValidationException.java`: Exception khi validation lỗi

#### 5.3.10 Package: `com.example.booking.validation`

**Mục đích**: Chứa các custom validator classes.

**Trách nhiệm**:
- Custom validation logic
- Business rule validation
- Cross-field validation

**Classes chính**:
- `FuturePlus.java`: Annotation cho validation thời gian tương lai
- `FuturePlusValidator.java`: Validator implementation

#### 5.3.11 Package: `com.example.booking.aspect`

**Mục đích**: Chứa các AOP aspect classes.

**Trách nhiệm**:
- Cross-cutting concerns
- Audit logging
- Rate limiting
- Performance monitoring

**Classes chính**:
- `AuditAspect.java`: Aspect cho audit logging
- `RateLimitingAspect.java`: Aspect cho rate limiting

#### 5.3.12 Package: `com.example.booking.audit`

**Mục đích**: Chứa các classes liên quan đến audit logging.

**Trách nhiệm**:
- Track changes to entities
- Log user actions
- Compliance requirements

**Classes chính**:
- `Auditable.java`: Annotation cho auditable entities
- `AuditEvent.java`: Event class cho audit
- `AuditLog.java`: Entity cho audit log

#### 5.3.13 Package: `com.example.booking.annotation`

**Mục đích**: Chứa các custom annotation classes.

**Trách nhiệm**:
- Custom annotations
- Metadata for processing
- Code generation hints

**Classes chính**:
- `RateLimited.java`: Annotation cho rate limiting

#### 5.3.14 Package: `com.example.booking.util`

**Mục đích**: Chứa các utility classes.

**Trách nhiệm**:
- Helper functions
- Data transformation
- Format utilities

**Classes chính**:
- `InputSanitizer.java`: Sanitize user input
- `PayOSSignatureGenerator.java`: Generate PayOS signatures
- `DatabaseFixer.java`: Database maintenance utilities

#### 5.3.15 Package: `com.example.booking.scheduler`

**Mục đích**: Chứa các scheduled task classes.

**Trách nhiệm**:
- Scheduled jobs
- Background processing
- Maintenance tasks

**Classes chính**:
- `PayOSReconciliationScheduler.java`: Đồng bộ dữ liệu PayOS
- `VoucherScheduler.java`: Xử lý voucher hết hạn

### 5.4 Mapper Package

#### 5.3.16 Package: `com.example.booking.mapper`

**Mục đích**: Chứa các mapper classes để convert giữa Entity và DTO.

**Trách nhiệm**:
- Entity to DTO conversion
- DTO to Entity conversion
- Handle complex mappings
- Reduce boilerplate code

**Classes chính**:
- `BookingMapper.java`: Mapper cho Booking entity/DTO

### 5.5 Dependencies và Integration

#### 5.5.1 External Dependencies
- **Spring Boot Starter Web**: Web MVC framework
- **Spring Boot Starter Security**: Security framework
- **Spring Boot Starter Data JPA**: JPA và Hibernate
- **Spring Boot Starter WebSocket**: WebSocket support
- **Spring Boot Starter Mail**: Email service
- **Spring Boot Starter OAuth2 Client**: OAuth2 integration
- **PostgreSQL Driver**: Database connectivity
- **Cloudinary**: Image management
- **Bucket4j**: Rate limiting
- **Jsoup**: HTML sanitization

#### 5.5.2 Internal Dependencies
- `web` → `service` → `repository` → `domain`
- `service` → `util`, `exception`, `validation`
- `web` → `dto`, `mapper`
- `config` → `service`, `repository`

### 5.6 Design Patterns

#### 5.6.1 MVC Pattern
- **Model**: Domain entities
- **View**: Thymeleaf templates
- **Controller**: Web controllers

#### 5.6.2 Repository Pattern
- Abstract data access layer
- Consistent data access interface
- Easy testing and mocking

#### 5.6.3 Service Layer Pattern
- Business logic encapsulation
- Transaction management
- Service orchestration

#### 5.6.4 DTO Pattern
- Data transfer optimization
- API contract stability
- Layer decoupling

### 5.7 Configuration Management

#### 5.7.1 Application Properties
- `application.yml`: Main configuration
- `application-dev.yml`: Development environment
- `application-prod.yml`: Production environment

#### 5.7.2 Environment Variables
- Database connection
- External service credentials
- Feature flags

### 5.8 Testing Strategy

#### 5.8.1 Test Packages
- `src/test/java/com/example/booking/`
- Unit tests cho services
- Integration tests cho repositories
- Web tests cho controllers

#### 5.8.2 Test Dependencies
- **Spring Boot Test Starter**: Testing framework
- **Mockito**: Mocking framework
- **Spring Security Test**: Security testing
- **Testcontainers**: Database testing

---

*Phần Code Packages này mô tả chi tiết cấu trúc mã nguồn, trách nhiệm của từng package và mối quan hệ giữa chúng trong kiến trúc layered architecture.*
