# Restaurant Booking Platform (BookEAT)

Nền tảng đặt bàn nhà hàng built on Spring Boot 3, hỗ trợ đầy đủ quy trình quản lý nhà hàng, thanh toán PayOS, thông báo đa kênh và trải nghiệm tìm kiếm thông minh dựa trên AI.

## Mục lục

- [Giới thiệu](#giới-thiệu)
- [Tính năng chính](#tính-năng-chính)
- [Kiến trúc hệ thống](#kiến-trúc-hệ-thống)
- [Cấu trúc thư mục](#cấu-trúc-thư-mục)
- [Công nghệ & tích hợp](#công-nghệ--tích-hợp)
- [Chuẩn bị môi trường](#chuẩn-bị-môi-trường)
- [Chạy dự án (local dev)](#chạy-dự-án-local-dev)
- [Thiết lập biến môi trường](#thiết-lập-biến-môi-trường)
- [Tài khoản demo](#tài-khoản-demo)
- [AI features](#ai-features)
- [Kiểm thử & coverage](#kiểm-thử--coverage)
  - [Tổng quan JUnit Testing](#tổng-quan-junit-testing)
  - [Test Coverage Statistics](#test-coverage-statistics)
  - [Cấu trúc Test Project](#cấu-trúc-test-project)
  - [Testing Frameworks & Tools](#testing-frameworks--tools)
  - [Test Execution Commands](#test-execution-commands)
  - [Coverage Reporting (JaCoCo)](#coverage-reporting-jacoco)
  - [AI-Assisted Testing Workflow](#ai-assisted-testing-workflow)
  - [Testing Methodology & Best Practices](#testing-methodology--best-practices)
- [Troubleshooting nhanh](#troubleshooting-nhanh)
- [Tài liệu & liên kết](#tài-liệu--liên-kết)

## Giới thiệu

BookEAT hỗ trợ nhà hàng và khách hàng trong toàn bộ vòng đời đặt bàn:

- Khách hàng khám phá và đặt bàn theo thời gian thực, tối ưu theo nhu cầu.
- Nhà hàng nhận, quản lý, xác nhận/huỷ đặt bàn, cấu hình menu, dịch vụ và khuyến mại.
- Tích hợp thanh toán PayOS, gửi thông báo qua email và dashboard nội bộ.
- Công cụ AI cho phép tìm kiếm tự nhiên, gợi ý nhà hàng phù hợp và giải thích đề xuất.

## Tính năng chính

- **Đặt bàn đa kênh**: web form, API REST, đồng bộ trạng thái bàn và đơn booking.
- **Quản trị nhà hàng**: quản lý hồ sơ nhà hàng, lịch hoạt động, bàn, menu, dịch vụ mở rộng.
- **Quản lý thanh toán**: tạo, xác thực và refund giao dịch PayOS; theo dõi lịch sử thanh toán.
- **Thông báo & lịch sử**: gửi email, lưu notification trong hệ thống cho admin/nhà hàng/khách.
- **Bảo mật & phân quyền**: Spring Security, Google OAuth2, rate limiting, bảo vệ brute force.
- **Realtime**: WebSocket chat giữa admin, nhà hàng và khách hàng.
- **AI Search**: xử lý ngôn ngữ tự nhiên, matching theo vị trí/giá/khẩu vị, giải thích đề xuất.

## Kiến trúc hệ thống

BookEAT được thiết kế theo layered architecture nhằm tách biệt rõ UI, business logic và persistence.

```
Client (Web, Mobile, REST)
    │
    │  HTTP / WebSocket
    ▼
Spring MVC + WebSocket Controllers (`web.controller`, `websocket`)
    │
    │ Service Calls
    ▼
Service Layer (`service.impl`, `service.ai`, `scheduler`, `aspect`)
    │
    │ Repository Abstraction
    ▼
Persistence Layer (`repository`, `entity`, `domain`) → PostgreSQL / Cache
    │
    ├─ External Integrations: PayOS, Cloudinary, OpenAI
    └─ Support Services: Redis/Ehcache cache, Rate limiting filters, Email (SMTP)
```

- **Web layer**: REST + Thymeleaf controllers, API endpoints cho admin, customer, restaurant owner, ngoại lệ tập trung (`GlobalControllerAdvice`).
- **Service layer**: xử lý nghiệp vụ đặt bàn, thanh toán, voucher, notification, recommendation (AI), đồng bộ AI (`AiSyncConfig`).
- **Domain/Persistence**: Entity JPA, repository interface, audit, transaction, caching.
- **Infrastructure**: cấu hình bảo mật (`SecurityConfig`, rate limiting, OAuth2), lịch chạy (`scheduler`), websocket, AI caching.

## Cấu trúc thư mục

```
src/
├── main/java/com/example/booking
│   ├── web/                 # Controllers (admin, api, customer, restaurant owner, websocket)
│   ├── service/             # Service layer (impl, ai/OpenAIService, RecommendationService)
│   ├── domain | entity      # Domain models & JPA entities
│   ├── repository           # Spring Data repositories
│   ├── dto                  # Data transfer objects & request/response models
│   ├── config               # Security, rate limit, cache, PayOS, OpenAI, command line runners
│   ├── util/common          # Geo utils, validators, helpers
│   ├── aspect/annotation    # Cross-cutting concerns (rate limit, audit)
│   └── scheduler/websocket  # Background jobs, realtime messaging
└── test/java/com/example/booking
    ├── service/...          # Unit tests service layer
    ├── web/controller/...   # Controller tests (MockMvc)
    ├── booking/test/base    # Test base, data factories
    └── resources            # application-test.yml, test fixtures
```

## Công nghệ & tích hợp

- **Backend**: Spring Boot 3.2, Spring MVC, Spring Data JPA, Spring Security, Thymeleaf.
- **Database**: PostgreSQL (prod/dev), H2 (test), Hibernate types (JSONB).
- **Caching & rate limiting**: Ehcache, Bucket4j, Caffeine, tùy chọn Redis.
- **Thanh toán**: PayOS SDK + webhook handler, refund/withdrawal workflow.
- **Media**: Cloudinary integration, local FS fallback.
- **Email/OAuth2**: Gmail SMTP, Google OAuth2 login.
- **AI/ML**: OpenAI GPT, Recommendation engine, AI sync API server (Node/Python).
- **Observability**: Micrometer + Prometheus registry, audit trail.

## Chuẩn bị môi trường

1. **Java 17** (JDK >= 17), **Maven 3.9+**.
2. **PostgreSQL 14+** với database `bookeat_db` (hoặc tuỳ chỉnh qua biến môi trường).
3. (Optional) **Redis** nếu muốn kích hoạt cache phân tán cho AI/notification.
4. Tài khoản PayOS (client-id/api-key), OpenAI API key (hoặc server nội bộ), Cloudinary (tuỳ chọn).
5. SMTP (Gmail App Password) để gửi email.

## Chạy dự án (local dev)

1. Clone repository và mở trong IDE.
2. Khởi chạy PostgreSQL và tạo database: `CREATE DATABASE bookeat_db;` (user `postgres`/`password` mặc định).
3. Sao chép `src/main/resources/application.yml` (hoặc sử dụng `application-dev.yml`) để cập nhật credential nếu cần.
4. Xuất các biến môi trường tối thiểu (tham khảo bảng bên dưới) hoặc chỉnh trực tiếp trong `application.yml` (không khuyến khích).
5. Cài dependencies & build: `mvn clean package`.
6. Chạy ứng dụng:
   - `mvn spring-boot:run` (profile dev mặc định).
   - hoặc `java -jar target/restaurant-booking-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod`.
7. Truy cập `http://localhost:8080`, đăng nhập với tài khoản demo.

## Thiết lập biến môi trường

| Nhóm       | Biến                                                                   | Giá trị mẫu                                   | Ghi chú                                       |
| ---------- | ---------------------------------------------------------------------- | --------------------------------------------- | --------------------------------------------- |
| Database   | `JDBC_DATABASE_URL`                                                    | `jdbc:postgresql://localhost:5432/bookeat_db` | Ẩn trong `application.yml` nếu chưa set       |
| Database   | `DB_USERNAME` / `DB_PASSWORD`                                          | `postgres` / `password`                       | Dùng cho datasource và migration              |
| SMTP       | `MAIL_USERNAME` / `MAIL_PASSWORD`                                      | `your-email@gmail.com` / `app-password`       | Gmail App Password / provider khác            |
| OAuth2     | `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET`                            | `<google-client>`                             | Bắt buộc khi bật login Google                 |
| PayOS      | `PAYOS_CLIENT_ID`, `PAYOS_API_KEY`, `PAYOS_CHECKSUM_KEY`               | từ PayOS dashboard                            | Dùng cho payment link & webhook               |
| AI         | `OPENAI_API_KEY` hoặc `AI_SERVER_URL`                                  | `sk-...` / `http://localhost:8000`            | Chọn direct OpenAI hoặc thông qua AI server   |
| AI Sync    | `AI_SYNC_URL`, `AI_SYNC_SECRET`, `AI_SYNC_API_KEY`                     | URL service nội bộ                            | Đồng bộ dữ liệu nhà hàng vào AI               |
| Cloudinary | `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET` | tuỳ chọn                                      | Bật nếu muốn upload CDN                       |
| Misc       | `APP_BASE_URL`                                                         | `http://localhost:8080`                       | Render/Prod cần set chuẩn để webhook redirect |

> **Security note:** `application.yml` hiện chứa credential PayOS mẫu. Đặt biến môi trường khi deploy để tránh lộ thông tin thật và commit `.env` vào `.gitignore`.

## Tài khoản demo

| Vai trò            | Tài khoản                            | Mật khẩu      | Nguồn                         |
| ------------------ | ------------------------------------ | ------------- | ----------------------------- |
| Admin              | `admin` / `admin@bookeat.vn`         | `admin123`    | `config/AdminUserInitializer` |
| Restaurant Owner 1 | `owner1@example.com` / user `owner1` | `password123` | `config/DataSeeder`           |
| Restaurant Owner 2 | `owner2@example.com` / user `owner2` | `password123` | `config/DataSeeder`           |

- Khi chạy lần đầu ở môi trường sạch, `DataSeeder` tự động tạo nhà hàng mẫu (Phở Bò ABC, Pizza Italia) kèm bàn, món, dịch vụ.
- Người dùng cuối (customer) có thể tự đăng ký qua UI; admin có thể kích hoạt tài khoản trong dashboard.

## AI features

AI được bật mặc định (`AI_ENABLED=true`). Luồng xử lý chính:

1. **Natural Language Parsing** – `OpenAIService.parseIntent()` dùng GPT model (mặc định `gpt-4o-mini`) để trích xuất cuisine, party size, ngân sách, locality, dietary requirement. Timeout 800ms, fallback nếu API lỗi.
2. **Recommendation Pipeline** – `RecommendationService.search()` kết hợp intent, dữ liệu nhà hàng (toạ độ, tag, menu), heuristics (giá, khoảng cách, stop words) để trả về danh sách xếp hạng.
3. **Explanation** – `OpenAIService.explainRestaurants()` sinh lý do ngắn gọn cho từng gợi ý; fallback sang lời giải thích chuẩn nếu quá thời gian.
4. **AI Sync** – `AiSyncConfig` push sự kiện (cập nhật nhà hàng, booking) tới AI server qua REST, retry với backoff.
5. **Caching & Rate limiting** – `AiCacheConfig`, `RateLimitingConfig`, Bucket4j bảo vệ endpoint AI search.

Để tắt AI, đặt `AI_ENABLED=false` hoặc `AI_SEARCH_ENABLED=false`. Có thể chuyển sang server nội bộ bằng cách set `AI_SERVER_URL`.

## Kiểm thử & coverage

### Tổng quan JUnit Testing

Dự án sử dụng **JUnit 5** làm framework kiểm thử chính với hệ thống test suite toàn diện đạt **73% code coverage** và hơn **590 test cases** bao phủ tất cả các layer của ứng dụng.

### Test Coverage Statistics

**Coverage tổng thể: 73%**

| Module            | Coverage | Test Cases | Status        |
| ----------------- | -------- | ---------- | ------------- |
| Service Layer     | ~75%     | ~200 cases | ✅ Excellent  |
| Controller Layer  | ~70%     | ~150 cases | ✅ Good       |
| Repository Layer  | ~75%     | ~50 cases  | ✅ Good       |
| Domain/Entity     | ~65%     | ~80 cases  | ✅ Acceptable |
| DTO/Config        | ~70%     | ~60 cases  | ✅ Good       |
| Integration Tests | ~80%     | ~20 cases  | ✅ Excellent  |
| **Tổng cộng**     | **73%**  | **590+**   | ✅ **Good**   |

### Cấu trúc Test Project

Cấu trúc thư mục test được tổ chức theo mô hình layered architecture, mirror với cấu trúc source code chính:

```
src/test/java/com/example/booking/
├── service/                        # Service Layer Tests (~66 files)
│   ├── BookingServiceTest.java     # Core booking logic (88 tests)
│   ├── PayOsServiceTest.java       # Payment integration
│   ├── VoucherServiceImplTest.java # Voucher management
│   ├── WaitlistServiceTest.java    # Waitlist functionality
│   ├── ChatServiceTest.java        # Chat/messaging
│   ├── EmailServiceTest.java       # Email notifications
│   ├── RestaurantManagementServiceTest.java
│   └── ... (60+ service test files)
│
├── web/controller/                 # Controller Layer Tests (~99 files)
│   ├── admin/                      # Admin controllers
│   │   ├── AdminUserControllerTest.java
│   │   ├── AdminRefundControllerTest.java
│   │   ├── AdminVoucherControllerTest.java
│   │   └── ...
│   ├── api/                        # REST API controllers
│   │   ├── BookingApiControllerTest.java
│   │   ├── ChatApiControllerTest.java
│   │   ├── VoucherApiControllerTest.java
│   │   └── ...
│   ├── restaurantowner/            # Restaurant owner controllers
│   │   ├── RestaurantVoucherControllerTest.java
│   │   └── ...
│   ├── PaymentControllerTest.java # PayOS integration
│   ├── RestaurantRegistrationControllerTest.java
│   └── ... (90+ controller test files)
│
├── repository/                     # Repository Layer Tests (~13 files)
│   ├── BookingRepositoryTest.java
│   ├── CustomerRepositoryTest.java
│   ├── PaymentRepositoryTest.java
│   ├── VoucherRepositoryTest.java
│   └── ... (9+ repository test files)
│
├── domain/                         # Domain/Entity Tests (~50+ files)
│   ├── BookingTest.java
│   ├── PaymentTest.java
│   ├── RestaurantProfileTest.java
│   ├── CustomerTest.java
│   ├── VoucherTest.java
│   └── ... (45+ entity test files)
│
├── dto/                            # DTO Tests (~45 files)
│   ├── ai/
│   │   ├── AISearchRequestTest.java
│   │   ├── AISearchResponseTest.java
│   │   └── RestaurantRecommendationTest.java
│   └── ... (42+ DTO test files)
│
├── config/                         # Configuration Tests (~25 files)
│   ├── SecurityConfigTest.java
│   ├── RateLimitingConfigTest.java
│   ├── AiSyncConfigTest.java
│   ├── PayOSBootCheckTest.java
│   └── ... (21+ config test files)
│
├── integration/                    # Integration Tests (~2 files)
│   ├── BookingIntegrationTest.java
│   └── BookingEndToEndIntegrationTest.java
│
├── websocket/                      # WebSocket Tests (~3 files)
│   ├── ChatMessageControllerTest.java
│   └── WebSocketDTOTest.java
│
├── aspect/                         # AOP Tests (~2 files)
│   ├── AuditAspectTest.java
│   └── RateLimitingAspectTest.java
│
├── mapper/                         # Mapper Tests (~1 file)
│   └── BookingMapperTest.java
│
└── test/                           # Test Utilities (~9 files)
    └── ... (test helpers, factories)
```

### Testing Frameworks & Tools

#### Core Testing Stack

| Framework/Tool           | Version | Purpose                  | Usage                                                             |
| ------------------------ | ------- | ------------------------ | ----------------------------------------------------------------- |
| **JUnit 5**              | 5.10.1  | Test framework           | Primary testing framework, `@Test`, `@BeforeEach`, `@DisplayName` |
| **Mockito**              | 5.5.0   | Mocking framework        | `@Mock`, `@InjectMocks`, `when().thenReturn()`, `verify()`        |
| **AssertJ**              | Latest  | Fluent assertions        | `assertThat().isEqualTo()`, chained assertions                    |
| **JaCoCo**               | 0.8.11  | Code coverage            | Maven plugin for coverage reporting                               |
| **Spring Boot Test**     | 3.2.0   | Spring testing utilities | `@SpringBootTest`, `@WebMvcTest`, `@DataJpaTest`                  |
| **H2 Database**          | Latest  | In-memory DB             | Test database for repository/integration tests                    |
| **Spring Security Test** | Latest  | Security testing         | `@WithMockUser`, `@WithUserDetails`                               |

#### JUnit 5 Annotations & Patterns

```java
// Service Layer Tests
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BookingServiceTest {
    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        // Setup test data
    }

    @Test
    @DisplayName("Should create booking when valid data provided")
    void shouldCreateBooking_WhenValidData() {
        // Given-When-Then pattern
    }
}

// Controller Layer Tests
@WebMvcTest(BookingController.class)
class BookingControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void shouldReturnBooking_WhenAuthorized() throws Exception {
        mockMvc.perform(get("/api/bookings/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }
}

// Repository Layer Tests
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class BookingRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    void shouldFindByStatus_WhenStatusProvided() {
        // Test repository methods
    }
}
```

### Test Execution Commands

#### Basic Commands

```bash
# Chạy tất cả tests
mvn clean test

# Chạy với coverage report
mvn clean test jacoco:report

# Chạy test cụ thể
mvn test -Dtest=BookingServiceTest

# Chạy nhiều test classes
mvn test -Dtest=BookingServiceTest,PayOsServiceTest

# Chạy theo pattern
mvn test -Dtest=*ServiceTest      # Tất cả service tests
mvn test -Dtest=*ControllerTest    # Tất cả controller tests
mvn test -Dtest=*RepositoryTest    # Tất cả repository tests
```

#### Advanced Commands

```bash
# Chạy với profile cụ thể
mvn test -Dspring.profiles.active=test

# Chạy với verbose output
mvn test -X

# Bỏ qua test failures (để xem tất cả kết quả)
mvn test -Dmaven.test.failure.ignore=true

# Chạy integration tests riêng
mvn test -Dtest=*IntegrationTest

# Generate coverage report
mvn clean verify jacoco:report

# Chỉ chạy tests thay đổi (nếu có plugin)
mvn test -DfailIfNoTests=false
```

### Coverage Reporting (JaCoCo)

JaCoCo được cấu hình trong `pom.xml` với plugin version **0.8.11**:

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

#### Xem Coverage Report

1. **Generate report:**

   ```bash
   mvn clean test jacoco:report
   ```

2. **Mở report:**

   - Mở file: `target/site/jacoco/index.html` trong trình duyệt
   - Xem package breakdown: `target/site/jacoco/com.example.booking/index.html`
   - Export XML cho CI: `target/site/jacoco/jacoco.xml`

3. **Coverage Metrics:**
   - **Line Coverage**: % dòng code được execute
   - **Branch Coverage**: % nhánh if/else được test
   - **Method Coverage**: % methods được gọi
   - **Class Coverage**: % classes có test

### AI-Assisted Testing Workflow

Dự án sử dụng hệ thống **5 bước prompt-based testing workflow** để tạo và maintain test suite một cách có hệ thống:

#### Overview: Testing Workflow Pipeline

```
Prompt 1: Feature Inventory
    ↓
Prompt 2: Method Decomposition
    ↓
Prompt 3: Test Case Generation
    ↓
Prompt 4: JUnit Code Generation
    ↓
Prompt 5: Bug Fix & Optimization
```

---

#### Prompt 1: Feature Inventory & Mapping

**Mục đích:** Liệt kê toàn bộ features trong dự án để thiết lập testing scope.

**Prompt Template:**

```
🎯 Role: Senior QA Analyst mapping functional scope for BookEAT Restaurant Booking Platform.

🎯 Objective: Enumerate every user-facing and backend feature in the project to establish a comprehensive testing scope.

📦 Context:
- Project documentation: README.md, AIFORSE_AI_05/README.md
- Source directories: src/main/java/com/example/booking/ (controllers, services, repositories, config, ai, payment, security)
- Existing test artifacts: src/test/java/
- Tech stack: Spring Boot 3, Thymeleaf, Spring Security, PayOS integration, OpenAI-based recommendation

🧱 Requirements:
- Review the provided docs and source tree to identify functional features, grouping them by business domain (Booking, Payments, AI Recommendation, Restaurant Management, Security, Notification, Analytics, Waitlist, etc.).
- For each feature, capture:
  - Feature Name
  - User Roles involved (Customer, Restaurant Owner, Admin, Guest)
  - Core components/classes (controller/service/config)
  - External dependencies (PayOS, OpenAI, Email, OAuth)
  - Current test coverage status (Existing tests? Y/N/Unknown)
- Highlight cross-cutting concerns (security, validation, rate limiting) as separate feature entries.

🎨 Output Format:
- Markdown table with columns: Feature, Description, Roles, Key Classes, External Dependencies, Existing Tests
- Follow with a short "Key Observations" bullet list summarizing coverage gaps or high-risk modules.

💡 Code Quality Standards:
- Use concise, accurate descriptions (≤2 sentences per cell).
- When coverage info is missing, mark as Unknown; do not speculate.
- Ensure class names and paths are accurate (copy exact names from source).
- Maintain consistent terminology with documentation (e.g., "Booking Service", "PayOS Integration").
```

**Output Example:**
| Feature | Description | Roles | Key Classes | External Dependencies | Existing Tests |
|---------|-------------|-------|-------------|----------------------|----------------|
| Booking Creation & Management | Create, update, cancel bookings with table assignment | Customer, Restaurant Owner | `BookingService`, `BookingController` | PayOS, Email | ✅ Yes |
| AI Restaurant Recommendation | Natural language search with GPT-based intent parsing | Guest, Customer | `RecommendationService`, `OpenAIService` | OpenAI API | ✅ Yes |

**Rationale:** Prompt này map toàn bộ functional surface để QA biết cần test gì. Bằng cách group features, roles, classes, dependencies, nó expose các modules có risk cao (Booking, Payment, AI) và highlight nơi có test hay thiếu.

---

#### Prompt 2: Method-Level Decomposition

**Mục đích:** Phân rã từng method trong feature để chuẩn bị test plan chi tiết.

**Prompt Template:**

```
🎯 Role: Senior QA Engineer specializing in decomposing the Booking Creation & Management feature.

🎯 Objective: Enumerate all public/protected methods tied to creating and managing bookings—service, controller, repository, validator—to prepare the testing plan.

📦 Context:
- Core classes: src/main/java/com/example/booking/service/BookingService.java, BookingConflictService, VoucherService, RefundService
- Controllers: src/main/java/com/example/booking/web/controller/BookingController.java
- Repositories/entities: BookingRepository, BookingTableRepository, RestaurantTableRepository, BookingDishRepository, BookingServiceRepository
- Test utilities: BookingTestBase, TestDataFactory (if present)

🧱 Requirements:
- Inspect every public/protected method in classes handling booking creation, update, cancellation, total calculation, table assignment, voucher handling, and refunds.
- For each method, list:
  - Method name + declaring class
  - Core purpose (≤1 sentence)
  - Input parameters & types
  - Return value
  - Key exceptions thrown
  - Side effects (e.g., which repositories/services are invoked, booking state changes)
  - Dependencies to mock in unit tests
  - Current test status (Covered / Missing / Unknown)
- Group results by class (BookingService, BookingController, BookingConflictService, etc.).
- Exclude private/helper methods; if a private method drives critical logic, note it in the side-effect column.

🎨 Output Format:
- Markdown section per class; each section contains a table with columns {Method, Purpose, Input, Output, Exceptions, Side Effects, Dependencies, Test Status}.
- Add a closing "Notes" list capturing special considerations (async calls, transactional requirements, security checks).

💡 Code Quality Standards:
- Keep each cell ≤2 sentences; use consistent domain terminology (PENDING, CONFIRMED, BookingConflictException, etc.).
- If information is unclear, use "Unknown" instead of guessing.
- Ensure class/method names match code exactly (case-sensitive).
```

**Rationale:** Sau khi biết features nào quan trọng, prompt này drill vào từng public/protected method handling booking creation & management. Capture inputs, outputs, exceptions, side effects, dependencies giúp xác định chính xác cần mock gì và assert gì.

---

#### Prompt 3: Test Case Generation (Given-When-Then)

**Mục đích:** Tạo test cases Given-When-Then cho mỗi function để drive JUnit implementation.

**Prompt Template:**

```
🎯 Role: Senior Test Case Designer crafting comprehensive scenario coverage for Booking Creation & Management.

🎯 Objective: Based on the method inventory generated in Prompt 2, produce detailed Given–When–Then test cases for every listed function to drive JUnit implementation.

📦 Context:
- Method tables per class from Prompt 2 (BookingService, BookingController, BookingConflictService, VoucherService, RefundService…)
- Business rules documented in README.md, AIFORSE_AI_05/README.md
- Domain entities & repositories in src/main/java/com/example/booking/
- Existing unit tests (for reference) under src/test/java/

🧱 Requirements:
- For each function captured in Prompt 2, create at least two test cases (covering distinct categories).
- Use the format Given–When–Then laid out in a Markdown table with columns: TC ID, Function, Category (Happy Path / Edge Case / Error Scenario / State Verification), Given, When, Then, Priority.
- Highlight preconditions such as mocked dependencies, data fixtures, authentication context, or repository states in the "Given" column.
- Ensure coverage of:
  - Success paths
  - Boundary inputs (dates, guest counts, price limits)
  - Exceptional branches (missing entities, conflicts, validation failures, external service errors)
  - Side-effect validation (repository saves, notifications, refund triggers)
- Reference domain-specific terminology consistently (BookingStatus.PENDING, BookingConflictException, PayOsService failure, etc.).
- Include at least one scenario per function that verifies interactions with external dependencies or side effects.
- Keep each cell ≤3 lines; use concise sentences.

🎨 Output Format:
- One combined Markdown table, grouped logically (e.g., subsections per class with headings).
- After the table, add a short summary noting any functions that require additional clarification before test design.

💡 Code Quality Standards:
- Use consistent TC ID naming (e.g., BS-001, BC-002, …) tied to class prefixes.
- Avoid speculative behavior; if behavior is unclear, mark "Unknown – requires clarification" in the Then column.
- Ensure categories are balanced—minimum 5 Error Scenarios and 5 State Verification cases overall.
```

**Rationale:** Transform method list thành Given–When–Then scenarios tạo executable blueprint. Categorizing cases (Happy/Edge/Error/State) đảm bảo balanced coverage và highlight corner cases như conflicts hoặc voucher failures.

---

#### Prompt 4: JUnit Test Code Generation

**Mục đích:** Convert Given-When-Then scenarios thành executable JUnit 5 code.

**Prompt Template:**

```
🎯 Role: Senior Java Developer in Test responsible for translating test cases into executable JUnit 5 code.

🎯 Objective: Convert the Given–When–Then scenarios produced in Prompt 3 into fully implemented JUnit 5 test classes for the Booking Creation & Management feature.

📦 Context:
- Test case matrix from Prompt 3 (covering BookingService, BookingController, BookingConflictService, VoucherService, RefundService, etc.).
- Source classes under src/main/java/com/example/booking/.
- Existing fixtures/utilities (e.g., BookingTestBase, planned TestDataFactory, TestSecurityUtils).
- Project stack: Spring Boot 3, JUnit 5, Mockito, @WebMvcTest, @DataJpaTest, H2.

🧱 Requirements:
- For each function in the Prompt 3 matrix, implement corresponding test methods inside appropriate test classes (BookingServiceTest, BookingControllerTest, BookingConflictServiceTest, VoucherServiceTest, RefundServiceTest, …).
- Follow Given–When–Then structure inside the code (comments or logical blocks) and mirror scenario IDs (e.g., method names like shouldCreateBooking_WhenValidData).
- Use the correct testing style:
  - Service tests: @ExtendWith(MockitoExtension.class) with mocks and verifies.
  - Controller tests: @WebMvcTest, MockMvc, security utilities.
  - Repository/integration tests: @DataJpaTest, H2 setup, transactional assertions.
- Ensure every scenario from Prompt 3 is implemented; multiple scenarios per class if required.
- Include @BeforeEach for fixture setup (leveraging TestDataFactory/BookingTestBase when available).
- Mock external dependencies (OpenAI, PayOS, RestTemplate, NotificationRepository, etc.) to keep tests isolated.
- Cover assertions for both return values and side effects (repository saves, status updates, exception messages).
- Document TODOs if any scenario needs additional data or clarification.

🎨 Output Format:
- Provide separate Markdown code blocks (java … ) for each test class file, including package declarations, imports, class annotations, fields, setup, and test methods.

💡 Code Quality Standards:
- JUnit 5 annotations (@Test, @BeforeEach), Mockito when/thenReturn, verify, ArgumentCaptor where relevant.
- Use descriptive test method names (should...).
- No unused imports; consistent formatting, indentation, and naming.
- Leverage constants/test data for readability; avoid magic numbers except where they reflect business rules.
- Ensure tests are deterministic and independent (no reliance on execution order).
```

**Rationale:** Với test cases đã định nghĩa, prompt này convert chúng thành JUnit 5 classes, specify annotations, mocks, và structure. Enforce project conventions (Mockito, MockMvc, DataJpaTest) để generated code drop thẳng vào repo.

---

#### Prompt 5: Bug Fix & Optimization

**Mục đích:** Chạy test suite, collect failures, và tạo follow-up prompts để fix bugs.

**Prompt Template:**

```
🎯 Role: Senior QA Engineer conducting regression validation and driving bug-fix automation for the generated JUnit suite.

🎯 Objective: Execute the full Maven test pipeline, collect any failing/unstable results, and craft follow-up prompts that guide AI-assisted debugging and optimization.

📦 Context:
- Full test code generated from previous prompts now resides under src/test/java.
- Build tool: Maven (Spring Boot project).
- Coverage tooling: JaCoCo (configured in pom.xml).
- Logs: Maven console output, target/surefire-reports, target/failsafe-reports, and target/site/jacoco/.

🧱 Requirements:
- Run regression commands:
  - mvn clean test
  - mvn -Dtest=BookingServiceTest,BookingControllerTest,PayOsServiceTest,RecommendationServiceTest test
  - mvn -Dtest=BookingIntegrationTest test
  - mvn jacoco:report
- Parse outputs to identify:
  - Failing tests (names, stack traces).
  - Flaky/intermittent behaviour (if any).
  - Compilation or dependency issues.
  - Lines/classes with coverage <80% (via Jacoco HTML).
- Summarize findings in Markdown: {Test Class, Test Method, Failure Reason, Stack Trace snippet/Message, Suspected Root Cause} and {Class, Coverage %, Required Fix}.
- Based on failures/coverage gaps, compose targeted "Fix Bug" prompts that:
  - Provide the failing test details (Given-When-Then if useful).
  - Include relevant code snippets (production & test).
  - Ask for optimized code/test updates ("Refine this test…" or "Adjust service logic…").
  - Include any additional steps (e.g., rerun after fixes, add missing mocks, adjust fixtures).

🎨 Output Format:
- Section 1: Commands & results (code block + bullet summary).
- Section 2: Failure table (Markdown).
- Section 3: Coverage gaps table.
- Section 4: "Fix Bug Prompt" templates—one per issue.
- Section 5: Next actions checklist (re-run, add tests, review).

💡 Code Quality Standards:
- Use exact test names and concise stack trace snippets (one line).
- No speculation without evidence—mark unresolved causes as "Needs investigation".
- Prompts should be self-contained (code snippet + question + expected behaviour).
```

**Rationale:** Prompt 5 đóng vòng lặp bằng cách move từ generated tests sang actual validation và bug resolution. Chạy full Maven pipeline surface real failures, flaky behavior, hoặc coverage gaps mà earlier prompts không thể anticipate. Capture output, cluster failures, và craft targeted bug-fix prompts biến raw test results thành actionable follow-ups, đảm bảo test suite trở nên reliable và regression-ready thay vì chỉ theoretical.

---

### Workflow Best Practices

**Luồng chuẩn cho từng module:**

1. **Prompt 1** → Chạy đầu tiên để có bức tranh tổng quan các features

   - Kết quả: Bảng feature inventory dùng làm QA documentation chính

2. **Prompt 2** → Chọn một feature ưu tiên (ví dụ Booking) và phân rã methods

   - Lặp lại prompt nếu cần phân tích thêm features khác

3. **Prompt 3** → Dựa trên bảng method, nhập prompt để sinh test case matrix Given–When–Then

   - Kiểm tra mỗi method xuất hiện tối thiểu hai test cases

4. **Prompt 4** → Cung cấp test case matrix để AI sinh JUnit code hoàn chỉnh

   - Sao chép từng code block vào `src/test/java` tương ứng
   - Commit khi kiểm tra xong

5. **Prompt 5** → Sau khi thêm tests, chạy Maven pipeline
   - Copy kết quả fail/coverage vào prompt để nhận regression report + bug-fix prompts

**Lưu ý:** Lặp bước 2-5 cho từng module quan trọng để đảm bảo coverage đầy đủ.

---

### Testing Methodology & Best Practices

#### Test Pyramid

```
        /\
       /  \     E2E Tests (~2%)
      /____\
     /      \   Integration Tests (~8%)
    /________\
   /          \ Unit Tests (~90%)
  /____________\
```

- **Unit Tests (90%)**: Service, repository, domain logic với mocks
- **Integration Tests (8%)**: Controller với MockMvc, repository với H2
- **E2E Tests (2%)**: Full workflow tests với test database

#### Test Categories

| Category           | Percentage | Examples                                        |
| ------------------ | ---------- | ----------------------------------------------- |
| Happy Path         | ~45%       | Valid booking creation, successful payment      |
| Error Scenarios    | ~30%       | Invalid data, exceptions, missing entities      |
| Edge Cases         | ~15%       | Boundary values, null checks, empty collections |
| State Verification | ~10%       | Status transitions, side effects validation     |

#### Code Quality Standards

- ✅ **Descriptive test names**: `shouldCreateBooking_WhenValidData_ThenReturnsBooking`
- ✅ **Given-When-Then structure**: Clear comments/logical blocks
- ✅ **Independent tests**: No execution order dependency
- ✅ **Deterministic**: Same input → same output
- ✅ **Fast execution**: Mock external dependencies
- ✅ **Comprehensive assertions**: Verify both return values and side effects
- ✅ **Proper cleanup**: @AfterEach for resource cleanup if needed

---

#### Ghi chú chi tiết cho BookingConflictServiceTest

- **Số lượng test**: 58 test cases, tổ chức thành 7 nhóm `@Nested`:
  - `validateBookingConflicts`
  - `validateBookingUpdateConflicts`
  - `validateBookingTime`
  - `validateRestaurantHours`
  - `validateTableStatus`
  - `validateTableConflicts`
  - `getAvailableTimeSlots`
- **Quy tắc được kiểm thử**: buffer 30 phút, thời lượng 2 giờ, và giờ mở cửa của nhà hàng
- **Bug fix**: Lỗi NullPointerException do thiếu mock `BookingTableRepository`/`RestaurantTableRepository` đã được khắc phục trong test bằng cách mock đầy đủ các repository liên quan

## Troubleshooting nhanh

- **Không kết nối được DB**: kiểm tra `JDBC_DATABASE_URL`, firewall PostgreSQL, hoặc bật `spring.jpa.show-sql=true` để debug.
- **PayOS webhook không chạy**: chắc chắn `APP_BASE_URL` public, PayOS whitelist IP webhook, xem log `PaymentController`.
- **AI Search timeout**: xem log `RecommendationService` (`DEBUG`), kiểm tra API key OpenAI hoặc tắt AI (`AI_ENABLED=false`) để fallback.

## Tài liệu & liên kết

- Slide overview: [Canva Deck](https://www.canva.com/design/DAG2vdA6nfo/gPC2aeLD_FmPMMdJ0rwaTw/edit?ui=e30)
- Kết quả test AI: `AI_RECOMMEND_TEST_RESULTS.md`
- Test suites tiêu biểu: `src/test/java/com/example/booking/service/BookingServiceTest.java`, `PayOsServiceTest.java`, `RestaurantRegistrationControllerTest.java`
- Đọc thêm: `src/main/java/com/example/booking/service/ai/RecommendationService.java`, `OpenAIService.java`
