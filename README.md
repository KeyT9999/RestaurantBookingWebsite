# 🍽️ BookEat - Restaurant Booking Platform

**Hệ thống đặt bàn nhà hàng thông minh** với AI-powered recommendations, thanh toán trực tuyến, và quản lý toàn diện cho nhà hàng.

## 🚀 Tổng quan dự án

BookEat là một nền tảng đặt bàn nhà hàng hiện đại được xây dựng bằng Spring Boot 3.2.0, tích hợp AI, thanh toán trực tuyến, và các tính năng quản lý tiên tiến.

### ⭐ Điểm nổi bật

- 🤖 **AI-Powered Recommendations**: Gợi ý nhà hàng thông minh sử dụng OpenAI GPT-4
- 💳 **Đa phương thức thanh toán**: PayOS, MoMo
- 💬 **Real-time Chat**: WebSocket chat giữa khách hàng, nhà hàng và admin
- 🔒 **Bảo mật tiên tiến**: Rate limiting, IP blocking, audit logging
- 📊 **Dashboard phân tích**: Báo cáo chi tiết cho admin và chủ nhà hàng
- ☁️ **Cloud Integration**: Cloudinary image management
- 🎫 **Hệ thống voucher**: Quản lý và áp dụng mã giảm giá
- ⭐ **Review & Rating**: Đánh giá và phản hồi nhà hàng

## 🛠️ Công nghệ sử dụng

### 📊 Tech Stack Overview

| Category                 | Technologies                                  |
| ------------------------ | --------------------------------------------- |
| **Backend Framework**    | Spring Boot 3.2.0, Spring MVC, Spring WebFlux |
| **Programming Language** | Java 17+                                      |
| **Database**             | PostgreSQL 12+ (with pgvector extension)      |
| **ORM**                  | Spring Data JPA, Hibernate 6.x                |
| **Caching**              | Redis 6+, Caffeine Cache                      |
| **Security**             | Spring Security 6, OAuth2, JWT                |
| **Real-time**            | WebSocket (STOMP), SockJS                     |
| **Template Engine**      | Thymeleaf                                     |
| **CSS Framework**        | Bootstrap 5.3                                 |
| **Payment Gateways**     | PayOS, MoMo                                   |
| **AI/ML**                | OpenAI GPT-4o-mini, pgvector                  |
| **Cloud Services**       | Cloudinary (Images)                           |
| **Testing**              | JUnit 5, Mockito, Spring Boot Test            |
| **Build Tool**           | Maven 3.6+                                    |
| **Monitoring**           | Spring Actuator, Micrometer, Prometheus       |
| **Deployment**           | Docker, Docker Compose                        |

### 🔧 Core Dependencies & Versions

#### Backend Framework

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <version>3.2.0</version>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>
```

#### Database & Persistence

```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>com.pgvector</groupId>
    <artifactId>pgvector</artifactId>
    <version>0.1.4</version>
</dependency>
<dependency>
    <groupId>com.vladmihalcea</groupId>
    <artifactId>hibernate-types-60</artifactId>
    <version>2.21.1</version>
</dependency>
```

#### Caching & Performance

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>
<dependency>
    <groupId>com.github.ben-manes.caffeine</groupId>
    <artifactId>caffeine</artifactId>
</dependency>
```

#### Real-time Communication

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
```

#### Template & View

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
<dependency>
    <groupId>org.thymeleaf.extras</groupId>
    <artifactId>thymeleaf-extras-springsecurity6</artifactId>
</dependency>
```

#### Payment Integration

```xml
<!-- PayOS & MoMo -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.datatype</groupId>
    <artifactId>jackson-datatype-jsr310</artifactId>
</dependency>
```

#### AI & Machine Learning

```xml
<dependency>
    <groupId>com.theokanning.openai-gpt3-java</groupId>
    <artifactId>service</artifactId>
    <version>0.18.2</version>
</dependency>
```

#### Cloud Services

```xml
<dependency>
    <groupId>com.cloudinary</groupId>
    <artifactId>cloudinary-http44</artifactId>
    <version>1.36.0</version>
</dependency>
```

#### Security & Rate Limiting

```xml
<dependency>
    <groupId>com.github.vladimir-bukhtoyarov</groupId>
    <artifactId>bucket4j-core</artifactId>
    <version>7.6.0</version>
</dependency>
<dependency>
    <groupId>org.jsoup</groupId>
    <artifactId>jsoup</artifactId>
    <version>1.17.2</version>
</dependency>
```

#### Monitoring & Observability

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

#### Testing

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-test</artifactId>
    <scope>test</scope>
</dependency>
```

#### Development Tools

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
<dependency>
    <groupId>me.paulschwarz</groupId>
    <artifactId>spring-dotenv</artifactId>
    <version>3.0.0</version>
</dependency>
```

### 🎨 Frontend Technologies

| Technology       | Purpose                     | Version |
| ---------------- | --------------------------- | ------- |
| **Thymeleaf**    | Server-side template engine | Latest  |
| **Bootstrap 5**  | CSS framework               | 5.3+    |
| **jQuery**       | DOM manipulation            | 3.6+    |
| **SockJS**       | WebSocket fallback          | 1.5+    |
| **STOMP.js**     | WebSocket messaging         | 2.3+    |
| **Font Awesome** | Icons                       | 6.x     |
| **Chart.js**     | Data visualization          | 3.x     |
| **Moment.js**    | Date/time handling          | 2.29+   |

### 🗄️ Database Technologies

| Component            | Technology         | Version            |
| -------------------- | ------------------ | ------------------ |
| **RDBMS**            | PostgreSQL         | 12+                |
| **Vector Extension** | pgvector           | 0.1.4              |
| **Connection Pool**  | HikariCP           | Auto (Spring Boot) |
| **Migration**        | Manual SQL scripts | -                  |
| **Caching Layer**    | Redis              | 6+                 |

### 🔐 Security Technologies

| Feature                | Implementation           |
| ---------------------- | ------------------------ |
| **Authentication**     | Spring Security 6        |
| **OAuth2**             | Google Login             |
| **Password Hashing**   | BCrypt                   |
| **Session Management** | Redis-backed sessions    |
| **CSRF Protection**    | Spring Security built-in |
| **XSS Prevention**     | Jsoup HTML sanitization  |
| **Rate Limiting**      | Bucket4j + Caffeine      |
| **Audit Logging**      | Custom AOP aspects       |

### 💳 Payment Technologies

| Provider     | Integration Type | Features                      |
| ------------ | ---------------- | ----------------------------- |
| **PayOS**    | REST API         | QR code, Bank transfer, Cards |
| **MoMo**     | REST API + IPN   | E-wallet, QR payment          |
| **Security** | HMAC-SHA256      | Signature verification        |

### 🤖 AI & ML Technologies

| Component      | Technology             | Purpose                        |
| -------------- | ---------------------- | ------------------------------ |
| **AI Model**   | OpenAI GPT-4o-mini     | Restaurant recommendations     |
| **Vector DB**  | pgvector               | Semantic search                |
| **Embeddings** | text-embedding-ada-002 | Text vectorization             |
| **NLP**        | GPT-4                  | Natural language understanding |
| **Context**    | Custom algorithms      | User preferences, history      |

### 📊 Monitoring & DevOps

| Tool                | Purpose                       |
| ------------------- | ----------------------------- |
| **Spring Actuator** | Health checks, metrics        |
| **Micrometer**      | Metrics collection            |
| **Prometheus**      | Metrics storage               |
| **Logback**         | Application logging           |
| **Docker**          | Containerization              |
| **Docker Compose**  | Multi-container orchestration |
| **Maven**           | Build automation              |
| **JaCoCo**          | Code coverage                 |

### 🧪 Testing Technologies

| Framework                | Purpose             | Version |
| ------------------------ | ------------------- | ------- |
| **JUnit 5**              | Unit testing        | 5.10.0  |
| **Mockito**              | Mocking framework   | 5.5.0   |
| **AssertJ**              | Fluent assertions   | 3.24.2  |
| **Spring Boot Test**     | Integration testing | 3.2.0   |
| **Spring Security Test** | Security testing    | 6.x     |
| **JaCoCo**               | Code coverage       | 0.8.11  |

### 🌐 API & Communication

| Protocol/Format | Usage                        |
| --------------- | ---------------------------- |
| **REST API**    | Main API architecture        |
| **WebSocket**   | Real-time chat               |
| **STOMP**       | WebSocket messaging protocol |
| **JSON**        | Data exchange format         |
| **Thymeleaf**   | Server-side rendering        |
| **AJAX**        | Asynchronous requests        |

### 📦 Build & Dependency Management

```xml
<!-- Maven Configuration -->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.0</version>
</parent>

<properties>
    <java.version>17</java.version>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
</properties>

<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
            <version>3.2.0</version>
        </plugin>
        <plugin>
            <groupId>org.jacoco</groupId>
            <artifactId>jacoco-maven-plugin</artifactId>
            <version>0.8.11</version>
        </plugin>
    </plugins>
</build>
```

### 🔄 Additional Libraries

| Library         | Purpose                     | Version    |
| --------------- | --------------------------- | ---------- |
| **Commons IO**  | File operations             | 2.11.0     |
| **Jackson**     | JSON processing             | Auto       |
| **Lombok**      | Boilerplate reduction       | (optional) |
| **Spring AOP**  | Aspect-oriented programming | Auto       |
| **Spring Mail** | Email sending               | Auto       |
| **Ehcache**     | Cache implementation        | 3.10.8     |

### 🌍 Environment & Configuration

| Configuration                | Tool                        |
| ---------------------------- | --------------------------- |
| **Environment Variables**    | `.env` files                |
| **Configuration Management** | Spring Boot Properties/YAML |
| **Secrets Management**       | Environment variables       |
| **Feature Flags**            | Application properties      |

## 📋 Yêu cầu hệ thống

- **Java**: 17 hoặc cao hơn
- **Maven**: 3.6+
- **PostgreSQL**: 12+ (với pgvector extension)
- **Redis**: 6+ (optional, cho caching)
- **Node.js**: 16+ (cho frontend build tools, optional)

## 🚀 Cài đặt và chạy

### 1. Chuẩn bị Database

```bash
# Tạo database PostgreSQL
createdb bookeat_db

# Hoặc dùng SQL
psql -U postgres
CREATE DATABASE bookeat_db;

# Enable pgvector extension (cho AI features)
\c bookeat_db
CREATE EXTENSION IF NOT EXISTS vector;
```

### 2. Cấu hình môi trường

Sao chép file `.env.example` thành `.env` và cập nhật các giá trị:

```bash
cp env.example .env
```

Cấu hình tối thiểu:

```properties
# Database
JDBC_DATABASE_URL=jdbc:postgresql://localhost:5432/bookeat_db
DB_USERNAME=postgres
DB_PASSWORD=your_password

# Application
APP_BASE_URL=http://localhost:8080

# Payment (Optional - có thể dùng test credentials)
PAYOS_CLIENT_ID=your_client_id
PAYOS_API_KEY=your_api_key
PAYOS_CHECKSUM_KEY=your_checksum_key

# AI Features (Optional - bỏ trống để disable)
OPENAI_API_KEY=sk-your-openai-api-key
AI_ENABLED=true
```

### 3. Build và chạy

```bash
# Clone repository
git clone <repository-url>
cd RestaurantBookingWebsite

# Build project
mvn clean install

# Chạy ứng dụng
mvn spring-boot:run

# Hoặc chạy từ JAR
java -jar target/restaurant-booking-0.0.1-SNAPSHOT.jar
```

Ứng dụng sẽ chạy tại: **http://localhost:8080**

### 4. Docker Deployment (Optional)

```bash
# Build Docker image
docker build -t bookeat-app .

# Run with Docker Compose
docker-compose up -d
```

### 5. Đăng nhập hệ thống

Hệ thống sẽ tự động tạo tài khoản mặc định:

**Admin Account:**

- Username: `admin`
- Password: `admin123`
- Role: ADMIN

**Customer Demo Account:**

- Username: `customer`
- Password: `password`
- Role: CUSTOMER

**Restaurant Owner Demo:**

- Username: `owner`
- Password: `password`
- Role: RESTAURANT_OWNER

## 🎯 Tính năng chính

### 👥 Customer Features

#### 1. Đặt bàn (Booking)

- ✅ Tìm kiếm và lọc nhà hàng theo nhiều tiêu chí
- ✅ Xem thông tin chi tiết nhà hàng, menu, đánh giá
- ✅ Đặt bàn trực tuyến với real-time availability check
- ✅ Chọn món ăn và dịch vụ kèm theo
- ✅ Áp dụng voucher giảm giá
- ✅ Quản lý danh sách booking (xem, sửa, hủy)
- ✅ Waitlist nếu không có bàn trống

#### 2. Thanh toán

- ✅ PayOS integration (QR code, bank transfer, card)
- ✅ MoMo e-wallet payment
- ✅ Deposit payment với tỷ lệ linh hoạt
- ✅ Payment history và invoice
- ✅ Refund request handling

#### 3. AI Recommendations

- 🤖 Gợi ý nhà hàng thông minh dựa trên:
  - Lịch sử đặt bàn
  - Preferences và favorites
  - Context awareness (thời gian, sự kiện, thời tiết)
  - Đánh giá và review
- 🎯 Personalized search với natural language
- 📊 Diversity control để tránh bias

#### 4. Social Features

- ⭐ Review và rating nhà hàng
- 💬 Real-time chat với nhà hàng
- ❤️ Favorite restaurants
- 🎫 Voucher collection và redemption
- 🔔 Real-time notifications

### 🏪 Restaurant Owner Features

#### 1. Quản lý nhà hàng

- ✅ Profile management (thông tin, hình ảnh, menu)
- ✅ Table management (bàn ăn, capacity, layout)
- ✅ Service management (dịch vụ kèm theo)
- ✅ Availability calendar
- ✅ Business hours configuration

#### 2. Quản lý booking

- ✅ View và manage bookings
- ✅ Confirm/reject booking requests
- ✅ Waitlist management
- ✅ Table assignment
- ✅ Booking analytics và reports

#### 3. Marketing & Engagement

- ✅ Voucher creation và management
- ✅ Promotion campaigns
- ✅ Customer engagement tracking
- ✅ Review response management
- 💬 Real-time chat với customers

#### 4. Financial Management

- ✅ Revenue tracking và analytics
- ✅ Balance management
- ✅ Withdrawal requests
- ✅ Bank account integration
- ✅ Transaction history
- ✅ Commission tracking

### 👨‍💼 Admin Features

#### 1. Quản lý hệ thống

- ✅ User management (customers, owners, admins)
- ✅ Restaurant approval workflow
- ✅ Contract management
- ✅ System configuration

#### 2. Moderation & Support

- ✅ Review moderation
- ✅ Report handling
- ✅ Refund approval
- ✅ Withdrawal approval
- 💬 Admin chat support

#### 3. Security & Monitoring

- 🔒 Rate limiting management
- 🛡️ IP blocking và unblocking
- 📊 Security analytics
- 🔍 Audit logging
- 📈 System health monitoring

#### 4. Analytics & Reporting

- 📊 Platform statistics
- 💰 Revenue reports
- 👥 User analytics
- 🏪 Restaurant performance
- 🎫 Voucher analytics

## 🔒 Bảo mật & Security

### Authentication & Authorization

- ✅ **Spring Security 6**: Role-based access control (RBAC)
- ✅ **OAuth2**: Google Login integration
- ✅ **Password Encryption**: BCrypt hashing
- ✅ **Session Management**: Secure cookie configuration
- ✅ **CSRF Protection**: Production-ready

### Rate Limiting

- 🚦 **Multi-layer Rate Limiting**:
  - Login attempts: 5/15 minutes
  - API calls: Configurable per endpoint
  - AI requests: 50/user/hour
  - General requests: 100/IP/minute
- 📊 **Real-time Monitoring**: Dashboard cho admin
- 🛡️ **Automatic IP Blocking**: Temporary và permanent blocks

### Data Protection

- 🔐 **HTTPS Enforcement**: Secure transport in production
- 🛡️ **SQL Injection Prevention**: JPA/Hibernate parameterized queries
- 🧹 **XSS Protection**: Input sanitization với Jsoup
- 📝 **Audit Logging**: Comprehensive audit trail
- 🔍 **Suspicious Activity Detection**: Automated monitoring

### Payment Security

- ✅ **Signature Verification**: HMAC-SHA256 for all payment APIs
- ✅ **Webhook Validation**: Secure IPN handling
- ✅ **PCI Compliance**: No card data storage
- ✅ **Transaction Logging**: Complete payment audit trail

## 🗂️ Cấu trúc dự án

```
RestaurantBookingWebsite/
├── src/
│   ├── main/
│   │   ├── java/com/example/booking/
│   │   │   ├── annotation/          # Custom annotations
│   │   │   │   └── RateLimited.java
│   │   │   ├── aspect/              # AOP aspects
│   │   │   │   ├── AuditAspect.java
│   │   │   │   └── RateLimitingAspect.java
│   │   │   ├── audit/               # Audit system
│   │   │   │   ├── AuditAction.java
│   │   │   │   ├── AuditEvent.java
│   │   │   │   └── Auditable.java
│   │   │   ├── common/              # Common utilities
│   │   │   │   ├── api/            # API response wrappers
│   │   │   │   ├── base/           # Base entities
│   │   │   │   ├── constants/      # Constants
│   │   │   │   ├── enums/          # Enumerations
│   │   │   │   └── util/           # Utilities
│   │   │   ├── config/              # Configuration
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── WebSocketSecurityConfig.java
│   │   │   │   ├── CloudinaryConfig.java
│   │   │   │   ├── OpenAIConfiguration.java
│   │   │   │   ├── RateLimitingConfig.java
│   │   │   │   └── ...
│   │   │   ├── domain/              # JPA Entities (70+ entities)
│   │   │   │   ├── User.java
│   │   │   │   ├── Booking.java
│   │   │   │   ├── RestaurantProfile.java
│   │   │   │   ├── Payment.java
│   │   │   │   ├── Review.java
│   │   │   │   ├── Voucher.java
│   │   │   │   ├── AIRecommendation.java
│   │   │   │   └── ...
│   │   │   ├── dto/                 # Data Transfer Objects
│   │   │   │   └── ... (71 DTOs)
│   │   │   ├── exception/           # Custom exceptions
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── BookingException.java
│   │   │   │   └── ...
│   │   │   ├── repository/          # JPA Repositories
│   │   │   │   └── ... (43 repositories)
│   │   │   ├── service/             # Business logic
│   │   │   │   ├── BookingService.java
│   │   │   │   ├── PaymentService.java
│   │   │   │   ├── AIRecommendationService.java
│   │   │   │   ├── RestaurantService.java
│   │   │   │   ├── ReviewService.java
│   │   │   │   └── ... (62 services)
│   │   │   ├── web/                 # Controllers
│   │   │   │   └── controller/
│   │   │   │       ├── HomeController.java
│   │   │   │       ├── BookingController.java
│   │   │   │       ├── PaymentController.java
│   │   │   │       ├── AISearchController.java
│   │   │   │       ├── RestaurantOwnerController.java
│   │   │   │       ├── AdminDashboardController.java
│   │   │   │       ├── api/        # REST API controllers
│   │   │   │       │   └── ... (13 API controllers)
│   │   │   │       ├── admin/      # Admin controllers
│   │   │   │       │   └── ... (8 admin controllers)
│   │   │   │       └── restaurantowner/
│   │   │   │           └── ... (2 owner controllers)
│   │   │   ├── websocket/           # WebSocket handlers
│   │   │   ├── scheduler/           # Scheduled tasks
│   │   │   ├── validation/          # Custom validators
│   │   │   └── util/                # Utility classes
│   │   └── resources/
│   │       ├── templates/           # Thymeleaf templates
│   │       │   ├── public/         # Public pages
│   │       │   ├── customer/       # Customer pages
│   │       │   ├── restaurant/     # Owner pages
│   │       │   ├── admin/          # Admin pages
│   │       │   ├── booking/        # Booking pages
│   │       │   ├── payment/        # Payment pages
│   │       │   └── layout/         # Layout fragments
│   │       ├── static/              # Static resources
│   │       │   ├── css/
│   │       │   ├── js/
│   │       │   └── images/
│   │       ├── application.yml      # Main configuration
│   │       └── messages.properties  # i18n files
│   └── test/                        # Test files
│       └── java/                    # 332 test classes
├── docs/                            # Documentation
│   ├── TESTING_GUIDE.md
│   ├── MOMO_INTEGRATION.md
│   ├── COVERAGE_REPORT.md
│   └── ...
├── docker-compose.yml               # Docker configuration
├── Dockerfile
├── pom.xml                          # Maven dependencies
├── .env.example                     # Environment template
└── README.md
```

## 📊 Database Schema

### Core Tables (70+ tables)

#### User Management

- `users` - User accounts với multi-role support
- `user_preferences` - User preferences và settings
- `customer_favorites` - Favorite restaurants

#### Restaurant Management

- `restaurant_profiles` - Restaurant information
- `restaurant_tables` - Table management
- `restaurant_media` - Images và videos
- `restaurant_services` - Additional services
- `restaurant_availability` - Business hours
- `restaurant_contracts` - Contracts với platform
- `restaurant_balance` - Financial balances
- `restaurant_bank_accounts` - Payment information

#### Booking System

- `bookings` - Main booking records
- `booking_tables` - Table assignments
- `booking_dishes` - Ordered dishes
- `booking_services` - Additional services
- `waitlist` - Waiting list management
- `waitlist_tables` - Waitlist table assignments

#### Payment System

- `payments` - Payment records
- `withdrawal_requests` - Owner withdrawals
- `refund_requests` - Refund processing

#### Review & Rating

- `reviews` - Customer reviews
- `review_reports` - Review moderation
- `review_report_evidence` - Supporting evidence

#### Voucher System

- `vouchers` - Voucher definitions
- `customer_vouchers` - Issued vouchers
- `voucher_redemptions` - Usage history

#### AI & Recommendations

- `ai_recommendations` - AI-generated recommendations
- `ai_interactions` - AI conversation history
- `ai_recommendation_diversity` - Diversity tracking
- `external_context` - Context data (weather, events)

#### Communication

- `chat_rooms` - Chat room management
- `messages` - Chat messages
- `notifications` - System notifications
- `communication_history` - Communication log

#### Security & Monitoring

- `audit_logs` - Comprehensive audit trail
- `rate_limit_statistics` - Rate limiting metrics
- `rate_limit_blocks` - Blocked IPs
- `rate_limit_alerts` - Security alerts
- `blocked_ips` - Permanently blocked IPs
- `suspicious_activity` - Suspicious behavior log

### Key Relationships

```
User 1→N Booking
User 1→N Review
User 1→N CustomerFavorite
RestaurantProfile 1→N RestaurantTable
RestaurantProfile 1→N Review
RestaurantProfile 1→N Voucher
Booking 1→1 Payment
Booking 1→N BookingTable
Booking N→N RestaurantTable (through booking_tables)
Payment 1→1 RefundRequest
```

## 🌐 API Endpoints

### Public Endpoints

| Method | Endpoint                       | Description                            |
| ------ | ------------------------------ | -------------------------------------- |
| GET    | `/`                            | Home page với featured restaurants     |
| GET    | `/restaurants`                 | Restaurant listing với search & filter |
| GET    | `/restaurants/{id}`            | Restaurant detail page                 |
| GET    | `/restaurants/{id}/reviews`    | Restaurant reviews                     |
| POST   | `/auth/register`               | User registration                      |
| POST   | `/auth/login`                  | User login                             |
| GET    | `/oauth2/authorization/google` | Google OAuth login                     |

### Customer Endpoints

| Method | Endpoint                      | Description             |
| ------ | ----------------------------- | ----------------------- |
| GET    | `/booking/new`                | Create booking form     |
| POST   | `/booking`                    | Create new booking      |
| GET    | `/booking/my`                 | My bookings list        |
| GET    | `/booking/{id}/edit`          | Edit booking form       |
| POST   | `/booking/{id}`               | Update booking          |
| POST   | `/booking/{id}/cancel`        | Cancel booking          |
| GET    | `/payment/{bookingId}`        | Payment page            |
| POST   | `/payment/process`            | Process payment         |
| GET    | `/payment/result/{paymentId}` | Payment result          |
| GET    | `/favorites`                  | Favorite restaurants    |
| POST   | `/favorites/add`              | Add favorite            |
| GET    | `/vouchers/my`                | My vouchers             |
| POST   | `/reviews`                    | Submit review           |
| GET    | `/chat/customer`              | Customer chat interface |

### Restaurant Owner Endpoints

| Method | Endpoint                            | Description        |
| ------ | ----------------------------------- | ------------------ |
| GET    | `/restaurant/dashboard`             | Owner dashboard    |
| GET    | `/restaurant/profile`               | Manage profile     |
| POST   | `/restaurant/profile/update`        | Update profile     |
| GET    | `/restaurant/bookings`              | Manage bookings    |
| POST   | `/restaurant/bookings/{id}/confirm` | Confirm booking    |
| POST   | `/restaurant/bookings/{id}/reject`  | Reject booking     |
| GET    | `/restaurant/tables`                | Manage tables      |
| POST   | `/restaurant/tables`                | Add/update table   |
| GET    | `/restaurant/vouchers`              | Manage vouchers    |
| POST   | `/restaurant/vouchers`              | Create voucher     |
| GET    | `/restaurant/balance`               | View balance       |
| POST   | `/restaurant/withdrawal`            | Request withdrawal |
| GET    | `/restaurant/chat`                  | Restaurant chat    |

### Admin Endpoints

| Method | Endpoint                          | Description             |
| ------ | --------------------------------- | ----------------------- |
| GET    | `/admin/dashboard`                | Admin dashboard         |
| GET    | `/admin/users`                    | User management         |
| GET    | `/admin/restaurants`              | Restaurant approval     |
| POST   | `/admin/restaurants/{id}/approve` | Approve restaurant      |
| GET    | `/admin/reviews/reported`         | Review moderation       |
| GET    | `/admin/refunds`                  | Refund management       |
| POST   | `/admin/refunds/{id}/approve`     | Approve refund          |
| GET    | `/admin/withdrawals`              | Withdrawal approval     |
| GET    | `/admin/rate-limiting`            | Rate limiting dashboard |
| POST   | `/admin/rate-limiting/unblock`    | Unblock IP              |
| GET    | `/admin/analytics`                | Platform analytics      |
| GET    | `/admin/chat`                     | Admin support chat      |

### REST API Endpoints

| Method | Endpoint                            | Description              |
| ------ | ----------------------------------- | ------------------------ |
| GET    | `/api/restaurants/{id}/tables`      | Get tables by restaurant |
| GET    | `/api/tables/{id}/availability`     | Check table availability |
| POST   | `/api/bookings/{id}/conflict-check` | Check booking conflicts  |
| GET    | `/api/ai/recommendations`           | Get AI recommendations   |
| POST   | `/api/ai/search`                    | AI-powered search        |
| GET    | `/api/vouchers/available`           | Available vouchers       |
| POST   | `/api/vouchers/apply`               | Apply voucher code       |
| GET    | `/api/notifications/unread`         | Unread notifications     |
| POST   | `/api/notifications/{id}/read`      | Mark as read             |
| POST   | `/payment/api/payos/webhook`        | PayOS webhook            |
| POST   | `/payment/api/momo/ipn`             | MoMo IPN callback        |

### WebSocket Endpoints

| Endpoint               | Description            |
| ---------------------- | ---------------------- |
| `/ws`                  | WebSocket connection   |
| `/app/chat.send`       | Send chat message      |
| `/topic/public`        | Public chat topic      |
| `/topic/chat/{roomId}` | Chat room subscription |

## 🧪 Testing Suite

### Test Coverage

- **Total Test Classes**: 332+
- **Line Coverage**: ≥70%
- **Branch Coverage**: ≥65%
- **Framework**: JUnit 5 + Mockito + Spring Boot Test

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test package
mvn test -Dtest="com.example.booking.service.*"

# Run with coverage report
mvn clean test jacoco:report

# View coverage report
open target/site/jacoco/index.html

# Run tests in verbose mode
mvn test -X
```

### Test Categories

- ✅ **Unit Tests**: Service layer, utilities
- ✅ **Integration Tests**: Controller + Service + Repository
- ✅ **Security Tests**: Authentication, authorization
- ✅ **API Tests**: REST endpoints
- ✅ **WebSocket Tests**: Real-time communication

### Documentation

- 📘 [Testing Guide](docs/TESTING_GUIDE.md) - Comprehensive testing documentation
- 📊 [Test Results](docs/TEST_RESULTS.md) - Latest test execution results
- 📈 [Coverage Report](docs/COVERAGE_REPORT.md) - Code coverage analysis

## 🎨 UI/UX Features

### Design

- ✅ **Responsive Design**: Mobile-first Bootstrap 5 layout
- ✅ **Modern UI**: Clean, intuitive interface
- ✅ **Dark Mode Ready**: CSS variable based theming
- ✅ **Accessibility**: WCAG 2.1 AA compliant

### User Experience

- ✅ **Flash Messages**: Toast notifications cho user feedback
- ✅ **Form Validation**: Real-time validation với error messages
- ✅ **Loading States**: Skeleton screens và spinners
- ✅ **Confirmation Modals**: Safe destructive actions
- ✅ **Auto-save**: Draft support cho forms
- ✅ **Infinite Scroll**: Smooth content loading
- ✅ **Image Lazy Loading**: Optimized performance

### Internationalization

- 🇻🇳 **Vietnamese**: Primary language
- 🇬🇧 **English**: Secondary language
- ✅ **i18n Support**: Spring Messages integration

## 🚀 Deployment

### Environment Requirements

**Development:**

- Java 17+
- PostgreSQL 12+
- Maven 3.6+
- Redis (optional)

**Production:**

- Java 17+
- PostgreSQL 12+ with pgvector
- Redis 6+ (recommended)
- Nginx (reverse proxy)
- SSL Certificate

### Deployment Platforms

#### 1. Render.com (Recommended)

```bash
# 1. Create PostgreSQL database on Render
# 2. Create Web Service on Render
# 3. Set environment variables
# 4. Deploy from GitHub

# Environment variables on Render:
DATABASE_URL=postgresql://...
OPENAI_API_KEY=sk-...
PAYOS_CLIENT_ID=...
CLOUDINARY_CLOUD_NAME=...
```

#### 2. Heroku

```bash
# Add Heroku Postgres addon
heroku addons:create heroku-postgresql:hobby-dev

# Set environment variables
heroku config:set OPENAI_API_KEY=sk-...

# Deploy
git push heroku main
```

#### 3. Docker

```bash
# Build image
docker build -t bookeat-app .

# Run with docker-compose
docker-compose up -d

# View logs
docker-compose logs -f app
```

#### 4. Traditional VPS (Ubuntu)

```bash
# Install Java 17
sudo apt install openjdk-17-jdk

# Install PostgreSQL
sudo apt install postgresql-12

# Build application
mvn clean package -DskipTests

# Run as service
sudo systemctl enable bookeat
sudo systemctl start bookeat
```

### Production Configuration

```yaml
# application-prod.yml
spring:
  datasource:
    url: ${DATABASE_URL}
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
  jpa:
    show-sql: false
    hibernate:
      ddl-auto: validate

server:
  port: ${PORT:8080}

app:
  base-url: https://bookeat-app.onrender.com

# Enable security features
security:
  csrf:
    enabled: true
  session:
    secure: true
```

## 📈 Performance Optimization

### Caching Strategy

- ✅ **Redis Caching**: AI recommendations, restaurant data
- ✅ **Caffeine Cache**: Rate limiting, session data
- ✅ **HTTP Caching**: Static resources, images
- ✅ **Query Optimization**: JPA fetch strategies, indexes

### Database Optimization

- ✅ **Connection Pooling**: HikariCP configuration
- ✅ **Indexes**: Strategic indexing on query columns
- ✅ **Pagination**: Efficient large dataset handling
- ✅ **Query Optimization**: N+1 query prevention

### Frontend Optimization

- ✅ **Asset Minification**: CSS/JS compression
- ✅ **Image Optimization**: Cloudinary transformations
- ✅ **Lazy Loading**: Images và components
- ✅ **CDN**: Bootstrap, jQuery from CDN

## 🔧 Configuration & Customization

### Adding New Features

#### 1. Add New Entity

```java
@Entity
@Table(name = "your_table")
public class YourEntity extends BaseEntity {
    // fields, getters, setters
}
```

#### 2. Create Repository

```java
@Repository
public interface YourRepository extends JpaRepository<YourEntity, Long> {
    // custom queries
}
```

#### 3. Implement Service

```java
@Service
@Transactional
public class YourService {
    // business logic
}
```

#### 4. Create Controller

```java
@Controller
@RequestMapping("/your-path")
public class YourController {
    // endpoints
}
```

### Custom Validation

```java
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = YourValidator.class)
public @interface YourValidation {
    String message() default "Invalid value";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

## 🐛 Troubleshooting

### Common Issues

#### 1. Database Connection Error

```bash
# Error: Could not connect to PostgreSQL
# Solution:
- Check PostgreSQL is running: sudo systemctl status postgresql
- Verify database exists: psql -U postgres -l
- Check credentials in .env file
- Ensure pgvector extension is installed
```

#### 2. AI Features Not Working

```bash
# Error: OpenAI API error
# Solution:
- Verify OPENAI_API_KEY in .env
- Check API key is valid at platform.openai.com
- Ensure AI_ENABLED=true
- Check quota and billing on OpenAI account
```

#### 3. Payment Integration Issues

```bash
# Error: Payment signature mismatch
# Solution:
- Verify PayOS/MoMo credentials
- Check webhook URLs are publicly accessible
- Review signature generation algorithm
- Check server time synchronization
```

#### 4. WebSocket Connection Failed

```bash
# Error: WebSocket connection refused
# Solution:
- Check WebSocketSecurityConfig
- Verify CORS settings
- Ensure SockJS is properly configured
- Check firewall rules for WebSocket ports
```

#### 5. Rate Limiting Too Aggressive

```bash
# Error: Too many requests
# Solution:
- Adjust rate limits in RateLimitingConfig
- Check IP not in blocked list: /admin/rate-limiting
- Clear rate limit cache
- Whitelist your IP for testing
```

### Debug Mode

```yaml
# application-dev.yml
logging:
  level:
    com.example.booking: DEBUG
    org.springframework.security: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE

spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
```

### Health Check Endpoints

```bash
# Application health
curl http://localhost:8080/actuator/health

# Database health
curl http://localhost:8080/actuator/health/db

# Disk space
curl http://localhost:8080/actuator/health/diskSpace

# Metrics
curl http://localhost:8080/actuator/metrics
```

## 📚 Additional Documentation

### Technical Documentation

- 📘 [Testing Guide](docs/TESTING_GUIDE.md) - Comprehensive testing strategies
- 💳 [MoMo Integration](docs/MOMO_INTEGRATION.md) - MoMo payment setup
- 📊 [Coverage Report](docs/COVERAGE_REPORT.md) - Test coverage analysis
- 🏠 [Home Page Guide](docs/HOME_PAGE_FIX_SUMMARY.md) - Home page implementation
- 🎨 [Featured Restaurants](docs/FEATURED_RESTAURANTS_REDESIGN.md) - Featured section design

### API Documentation

- 🌐 Swagger UI: `http://localhost:8080/swagger-ui.html` (coming soon)
- 📖 API Docs: `http://localhost:8080/api-docs` (coming soon)

### UML Diagrams

- 🔐 [Authentication Class Diagram](docs/uml/Auth_Register_ClassDiagram.puml)
- 🏗️ [System Context Diagram](docs/uml/Restaurant_Booking_System_Context_Diagram.puml)

## 🎯 Roadmap & Future Features

### Phase 1 (Completed) ✅

- [x] Core booking system
- [x] User authentication & authorization
- [x] Payment integration (PayOS, MoMo)
- [x] AI recommendations
- [x] Real-time chat
- [x] Admin dashboard
- [x] Review system
- [x] Voucher system

### Phase 2 (In Progress) 🚧

- [ ] Mobile app (React Native)
- [ ] Push notifications
- [ ] Advanced analytics dashboard
- [ ] Restaurant analytics AI insights
- [ ] Multi-language support expansion
- [ ] QR code table ordering
- [ ] Loyalty program

### Phase 3 (Planned) 📋

- [ ] Restaurant POS integration
- [ ] Delivery integration
- [ ] Social media integration
- [ ] Advanced AI chatbot
- [ ] Predictive analytics
- [ ] Blockchain-based loyalty points

## 🤝 Contributing

### How to Contribute

1. **Fork the repository**
2. **Create your feature branch**
   ```bash
   git checkout -b feature/AmazingFeature
   ```
3. **Commit your changes**
   ```bash
   git commit -m 'Add some AmazingFeature'
   ```
4. **Push to the branch**
   ```bash
   git push origin feature/AmazingFeature
   ```
5. **Open a Pull Request**

### Code Style Guidelines

- Follow Java naming conventions
- Use meaningful variable and method names
- Add JavaDoc comments for public methods
- Write unit tests for new features
- Keep methods small and focused
- Follow SOLID principles

### Commit Message Format

```
<type>(<scope>): <subject>

<body>

<footer>
```

Types: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`

Example:

```
feat(booking): add waitlist functionality

- Implement waitlist queue system
- Add notification when table available
- Update booking service tests

Closes #123
```

## ⚠️ Known Issues

### Current Limitations

- [ ] AI recommendations require OpenAI API key (costs apply)
- [ ] Real-time chat limited to 100 concurrent connections
- [ ] Image upload limited to 20MB per file
- [ ] Rate limiting may be too strict for some use cases
- [ ] WebSocket doesn't support horizontal scaling yet (needs Redis adapter)

### Performance Notes

- First AI recommendation may be slow (~2-3s) due to cold start
- Large restaurant listings (>1000 items) may need pagination optimization
- Image optimization depends on Cloudinary configuration

## 🔐 Security Considerations

### Production Checklist

- [ ] Change all default passwords
- [ ] Enable HTTPS/SSL
- [ ] Configure CSRF protection
- [ ] Set secure session cookies
- [ ] Enable rate limiting
- [ ] Configure CORS properly
- [ ] Set up database backups
- [ ] Enable audit logging
- [ ] Review and limit API endpoints exposure
- [ ] Set up monitoring and alerts

### Sensitive Data

⚠️ **Never commit sensitive data to repository:**

- API keys (OpenAI, PayOS, MoMo, Cloudinary)
- Database passwords
- JWT secrets
- OAuth client secrets

Always use environment variables or secure secret management.

## 📊 Performance Metrics

### Target Metrics

- **Page Load Time**: < 2s
- **API Response Time**: < 500ms
- **Database Query Time**: < 100ms
- **Uptime**: > 99.5%
- **Error Rate**: < 1%

### Monitoring

- Application: Spring Boot Actuator + Micrometer
- Database: PostgreSQL slow query log
- Server: System metrics via Prometheus
- User Experience: Browser performance API

## 👥 Nhóm phát triển

**Team 7 - SWP391 Course**

| Thành viên             | Mã SV    | Role           | Responsibilities                               |
| ---------------------- | -------- | -------------- | ---------------------------------------------- |
| **Nguyễn Hồng Phúc**   | DE190234 | Team Lead      | Backend, AI Integration, System Architecture   |
| **Trần Kim Thắng**     | DE180020 | Backend Dev    | Payment Integration, Security, API Development |
| **Phan Thành Tài**     | DE190491 | Full-stack Dev | Frontend, UI/UX, Real-time Features            |
| **Đặng Văn Công Danh** | DE180814 | Backend Dev    | Database Design, Testing, Deployment           |

### Contact

- 📧 Email: [bookeat.team7@example.com](mailto:bookeat.team7@example.com)
- 🔗 Repository: [GitHub](https://github.com/your-org/RestaurantBookingWebsite)
- 📝 Documentation: [Wiki](https://github.com/your-org/RestaurantBookingWebsite/wiki)

## 📄 License

Dự án được phát triển trong khuôn khổ môn SWP391 - FPT University.

© 2024 Team 7 - Restaurant Booking Platform. All rights reserved.

---

## 🙏 Acknowledgments

- **FPT University** - Educational support and project guidance
- **OpenAI** - AI recommendation engine
- **PayOS & MoMo** - Payment gateway integration
- **Cloudinary** - Image management service
- **Spring Boot Community** - Excellent framework and documentation
- **Stack Overflow** - Community support

---

## 📞 Support

Nếu bạn gặp vấn đề hoặc có câu hỏi:

1. 📖 Xem [Documentation](docs/)
2. 🐛 Tạo [Issue](https://github.com/your-org/RestaurantBookingWebsite/issues)
3. 💬 Tham gia [Discussions](https://github.com/your-org/RestaurantBookingWebsite/discussions)
4. 📧 Email team qua bookeat.team7@example.com

---

<div align="center">

### 🍽️ BookEat - Đặt bàn thông minh, Ăn uống tiện lợi

**Made with ❤️ by Team 7**

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-12+-blue.svg)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-Academic-yellow.svg)](LICENSE)

[🏠 Homepage](http://localhost:8080) · [📚 Documentation](docs/) · [🐛 Report Bug](issues) · [✨ Request Feature](issues)

</div>

---

🚀 **Happy Coding!** Chúc bạn thành công với dự án Restaurant Booking Platform!
