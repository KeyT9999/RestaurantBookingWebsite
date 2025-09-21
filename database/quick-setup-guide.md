# 🚀 Quick Setup Guide - Restaurant Booking Database

## Bước 1: Mở Query Tool trên `restaurant_db`

* Trong **pgAdmin**: chuột phải **Databases → restaurant_db → Query Tool**
* Hoặc sử dụng **DBeaver**: Connect → New SQL Script

## Bước 2: Chạy Script Database

**Copy toàn bộ nội dung file `spring_boot_compatible.sql` và Run một lần:**

Hoặc sử dụng command line:
```bash
psql -U restaurant_user -h localhost -d restaurant_db -f database/spring_boot_compatible.sql
```

**Sau khi chạy xong:** 
- **Refresh** `Schemas → public → Tables` để thấy các bảng
- Kiểm tra có 3 tables: `restaurants`, `dining_tables`, `bookings`

## Bước 3: Xác nhận cấu hình Spring Boot

**File: `src/main/resources/application.yml`**
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/restaurant_db
    username: restaurant_user   # user bạn đã tạo
    password: 123456            # mật khẩu bạn đặt
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: validate        # kiểm tra schema (không tự tạo)
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.PostgreSQLDialect
    show-sql: true
  
  thymeleaf:
    cache: false
    encoding: UTF-8
    mode: HTML
    prefix: classpath:/templates/
    suffix: .html
  
  messages:
    basename: messages
    encoding: UTF-8
    cache-duration: 3600

server:
  port: 8080

logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

**Dependency trong `pom.xml` (đã có):**
```xml
<dependency>
  <groupId>org.postgresql</groupId>
  <artifactId>postgresql</artifactId>
</dependency>
```

## Bước 4: Chạy ứng dụng

```bash
mvn spring-boot:run
```

## ✅ Kỳ vọng kết quả:

**Console sẽ hiển thị:**
```
✅ Database already contains sample data!
📍 Found 3 restaurants
🪑 Found 12 dining tables
HikariPool-1 - Starting...
HikariPool-1 - Start completed.
```

**Các SQL queries sẽ xuất hiện:**
```sql
select r1_0.id,r1_0.address,r1_0.created_at,r1_0.description,r1_0.name,r1_0.phone,r1_0.updated_at 
from restaurants r1_0

select d1_0.id,d1_0.capacity,d1_0.created_at,d1_0.description,d1_0.name,d1_0.restaurant_id,d1_0.updated_at 
from dining_tables d1_0
```

## 🌐 Truy cập ứng dụng:

1. **Mở browser:** http://localhost:8080
2. **Login page:** username: `customer`, password: `password`
3. **Đặt bàn mới:** http://localhost:8080/booking/new
4. **Xem booking:** http://localhost:8080/booking/my

## 🔧 Troubleshooting:

### Lỗi "table không tồn tại":
```sql
-- Kiểm tra tables đã tạo chưa
SELECT tablename FROM pg_tables WHERE schemaname = 'public';
```

### Lỗi "relation does not exist":
- Chạy lại script `spring_boot_compatible.sql`
- Kiểm tra `ddl-auto: validate` trong application.yml

### Lỗi connection:
- Kiểm tra PostgreSQL service đang chạy
- Kiểm tra user `restaurant_user` có quyền truy cập

## 🎯 Test nhanh:

**Kiểm tra data trong database:**
```sql
-- Check restaurants
SELECT id, name FROM restaurants;

-- Check tables
SELECT r.name as restaurant, t.name as table_name, t.capacity 
FROM restaurants r 
JOIN dining_tables t ON r.id = t.restaurant_id 
ORDER BY r.name, t.name;

-- Check if ready for bookings
SELECT COUNT(*) as total_tables FROM dining_tables;
```

**Expected output:**
- 3 restaurants
- 12 dining tables  
- 0 bookings (initially)

---

🎉 **Hoàn thành!** App sẵn sàng để demo và test các tính năng booking! 