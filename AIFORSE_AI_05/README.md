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
| Nhóm | Biến | Giá trị mẫu | Ghi chú |
|------|------|-------------|---------|
| Database | `JDBC_DATABASE_URL` | `jdbc:postgresql://localhost:5432/bookeat_db` | Ẩn trong `application.yml` nếu chưa set |
| Database | `DB_USERNAME` / `DB_PASSWORD` | `postgres` / `password` | Dùng cho datasource và migration |
| SMTP | `MAIL_USERNAME` / `MAIL_PASSWORD` | `your-email@gmail.com` / `app-password` | Gmail App Password / provider khác |
| OAuth2 | `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET` | `<google-client>` | Bắt buộc khi bật login Google |
| PayOS | `PAYOS_CLIENT_ID`, `PAYOS_API_KEY`, `PAYOS_CHECKSUM_KEY` | từ PayOS dashboard | Dùng cho payment link & webhook |
| AI | `OPENAI_API_KEY` hoặc `AI_SERVER_URL` | `sk-...` / `http://localhost:8000` | Chọn direct OpenAI hoặc thông qua AI server |
| AI Sync | `AI_SYNC_URL`, `AI_SYNC_SECRET`, `AI_SYNC_API_KEY` | URL service nội bộ | Đồng bộ dữ liệu nhà hàng vào AI |
| Cloudinary | `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET` | tuỳ chọn | Bật nếu muốn upload CDN |
| Misc | `APP_BASE_URL` | `http://localhost:8080` | Render/Prod cần set chuẩn để webhook redirect |

> **Security note:** `application.yml` hiện chứa credential PayOS mẫu. Đặt biến môi trường khi deploy để tránh lộ thông tin thật và commit `.env` vào `.gitignore`.

## Tài khoản demo
| Vai trò | Tài khoản | Mật khẩu | Nguồn |
|---------|-----------|----------|-------|
| Admin | `admin` / `admin@bookeat.vn` | `admin123` | `config/AdminUserInitializer` |
| Restaurant Owner 1 | `owner1@example.com` / user `owner1` | `password123` | `config/DataSeeder` |
| Restaurant Owner 2 | `owner2@example.com` / user `owner2` | `password123` | `config/DataSeeder` |

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
### Lệnh test Maven
- Toàn bộ test: `mvn clean test`
- Giữ nguyên build + test + checkstyle (nếu bật): `mvn clean verify`
- Chạy một class: `mvn -Dtest=BookingServiceTest test`
- Chạy theo pattern: `mvn -Dtest=*ControllerTest test`

Test suite đã bao phủ 590 test case (JUnit 5, Mockito, AssertJ) với các lớp trong thư mục `src/test/java/com/example/booking`:
- Service: booking, voucher, notification, AI, payment, waitlist, conflict detection.
- Controller: REST + MVC controllers (AdminDashboard, RestaurantRegistration, PayOS callback).
- PayOS integration: tạo link, xử lý webhook, refund.
- AI: RecommendationServiceTest, OpenAIServiceTest với mock OpenAI client.

### Coverage (JaCoCo)
`pom.xml` đã cấu hình `jacoco-maven-plugin` ở pha `verify`. Để tạo báo cáo coverage:
1. Chạy `mvn clean verify` (bao gồm test + jacoco report).  
   *Hoặc chạy thủ công:* `mvn jacoco:prepare-agent test jacoco:report`.
2. Mở file `target/site/jacoco/index.html` trên trình duyệt để xem tổng quan coverage, package breakdown và top thiếu sót.
3. Báo cáo dòng chưa được cover nằm trong `target/site/jacoco/com.example.booking/index.html`.

Nếu muốn export XML cho CI, thêm `-Djacoco.skip=false` và dùng `target/site/jacoco/jacoco.xml`.

## Troubleshooting nhanh
- **Không kết nối được DB**: kiểm tra `JDBC_DATABASE_URL`, firewall PostgreSQL, hoặc bật `spring.jpa.show-sql=true` để debug.
- **PayOS webhook không chạy**: chắc chắn `APP_BASE_URL` public, PayOS whitelist IP webhook, xem log `PaymentController`.
- **AI Search timeout**: xem log `RecommendationService` (`DEBUG`), kiểm tra API key OpenAI hoặc tắt AI (`AI_ENABLED=false`) để fallback.

## Tài liệu & liên kết
- Slide overview: [Canva Deck](https://www.canva.com/design/DAG2vdA6nfo/gPC2aeLD_FmPMMdJ0rwaTw/edit?ui=e30)
- Kết quả test AI: `AI_RECOMMEND_TEST_RESULTS.md`
- Test suites tiêu biểu: `src/test/java/com/example/booking/service/BookingServiceTest.java`, `PayOsServiceTest.java`, `RestaurantRegistrationControllerTest.java`
- Đọc thêm: `src/main/java/com/example/booking/service/ai/RecommendationService.java`, `OpenAIService.java`

