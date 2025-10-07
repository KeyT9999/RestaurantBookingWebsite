# Hướng Dẫn Kiểm Tra Giá Trị Đơn Hàng và Thanh Toán Qua PayOS

## 📋 Tổng Quan

Dự án Restaurant Booking Platform đã tích hợp đầy đủ tính năng tính toán tổng tiền đơn hàng và thanh toán qua PayOS. Tài liệu này sẽ giải thích chi tiết cách hệ thống hoạt động.

---

## 💰 1. Cách Tính Tổng Tiền Đơn Hàng

### 1.1. Công Thức Tính Tổng

Tổng tiền đơn hàng (booking) được tính bằng công thức:

```
TỔNG TIỀN = Tiền Đặt Cọc + Tổng Tiền Món Ăn + Tổng Tiền Dịch Vụ
```

### 1.2. Chi Tiết Implementation

File: `src/main/java/com/example/booking/service/BookingService.java`

```java
/**
 * Method tính tổng tiền cho booking
 */
public BigDecimal calculateTotalAmount(Booking booking) {
    BigDecimal total = BigDecimal.ZERO;
    
    // 1. Cộng tiền đặt cọc (deposit)
    total = total.add(booking.getDepositAmount());
    
    // 2. Cộng tổng tiền các món ăn đã đặt
    List<BookingDish> bookingDishes = bookingDishRepository.findByBooking(booking);
    if (!bookingDishes.isEmpty()) {
        for (BookingDish bookingDish : bookingDishes) {
            // Mỗi món: giá * số lượng
            total = total.add(bookingDish.getTotalPrice());
        }
    }
    
    // 3. Cộng tổng tiền các dịch vụ đã chọn
    List<BookingService> bookingServices = bookingServiceRepository.findByBooking(booking);
    if (!bookingServices.isEmpty()) {
        for (BookingService bookingService : bookingServices) {
            // Mỗi dịch vụ: giá * số lượng
            total = total.add(bookingService.getTotalPrice());
        }
    }
    
    return total;
}
```

### 1.3. Ví Dụ Tính Toán

```
Booking ID: 123
├── Tiền đặt cọc: 100,000 VNĐ
├── Món ăn:
│   ├── Phở Bò x 2 = 80,000 VNĐ
│   └── Cơm Gà x 1 = 50,000 VNĐ
└── Dịch vụ:
    └── Trang trí sinh nhật x 1 = 200,000 VNĐ

➜ TỔNG TIỀN = 100,000 + 130,000 + 200,000 = 430,000 VNĐ
```

---

## 🔐 2. Cấu Trúc Database

### 2.1. Bảng Booking

```sql
CREATE TABLE booking (
    booking_id        INTEGER PRIMARY KEY,
    customer_id       UUID NOT NULL,
    restaurant_id     INTEGER NOT NULL,
    booking_time      TIMESTAMPTZ NOT NULL,
    number_of_guests  INTEGER NOT NULL,
    status            VARCHAR(20) DEFAULT 'pending',
    deposit_amount    NUMERIC(18,2) DEFAULT 0,  -- Tiền đặt cọc
    note              TEXT,
    created_at        TIMESTAMPTZ DEFAULT now(),
    updated_at        TIMESTAMPTZ DEFAULT now()
);
```

### 2.2. Bảng Payment

```sql
CREATE TABLE payment (
    payment_id              INTEGER PRIMARY KEY,
    customer_id             UUID NOT NULL,
    booking_id              INTEGER NOT NULL,
    amount                  NUMERIC(18,2) NOT NULL,  -- Số tiền thanh toán
    payment_method          VARCHAR(20),              -- PAYOS, CASH, MOMO, etc.
    status                  VARCHAR(20) DEFAULT 'PENDING',
    payment_type            VARCHAR(20) DEFAULT 'DEPOSIT',
    order_code              BIGINT UNIQUE,           -- Mã đơn hàng cho PayOS
    payos_payment_link_id   VARCHAR(255),
    payos_checkout_url      TEXT,
    paid_at                 TIMESTAMPTZ DEFAULT now()
);
```

### 2.3. Bảng BookingDish (Món ăn trong đơn)

```sql
CREATE TABLE booking_dish (
    booking_dish_id  INTEGER PRIMARY KEY,
    booking_id       INTEGER NOT NULL,
    dish_id          INTEGER NOT NULL,
    quantity         INTEGER NOT NULL,
    total_price      NUMERIC(18,2) NOT NULL  -- = dish.price * quantity
);
```

### 2.4. Bảng BookingService (Dịch vụ trong đơn)

```sql
CREATE TABLE booking_service (
    booking_service_id  INTEGER PRIMARY KEY,
    booking_id          INTEGER NOT NULL,
    service_id          INTEGER NOT NULL,
    quantity            INTEGER NOT NULL,
    total_price         NUMERIC(18,2) NOT NULL  -- = service.price * quantity
);
```

---

## 🚀 3. Luồng Thanh Toán Qua PayOS

### 3.1. Sơ Đồ Luồng Thanh Toán

```
1. Khách hàng tạo booking
   ↓
2. Hệ thống tính tổng tiền (calculateTotalAmount)
   ↓
3. Khách chọn phương thức thanh toán: PayOS
   ↓
4. Hệ thống tạo Payment record (PaymentService.createPayment)
   ↓
5. Gọi PayOS API tạo payment link (PayOsService.createPaymentLink)
   ↓
6. PayOS trả về checkout URL
   ↓
7. Redirect khách hàng đến trang thanh toán PayOS
   ↓
8. Khách thanh toán trên PayOS
   ↓
9. PayOS gửi webhook về server
   ↓
10. Hệ thống xử lý webhook (PaymentService.handlePayOsWebhook)
    ↓
11. Cập nhật trạng thái Payment: COMPLETED
    ↓
12. Cập nhật trạng thái Booking: CONFIRMED
```

### 3.2. Chi Tiết Implementation

#### Bước 1: Tạo Payment Record

File: `src/main/java/com/example/booking/service/PaymentService.java`

```java
public Payment createPayment(Integer bookingId, UUID customerId, 
                            PaymentMethod paymentMethod, 
                            PaymentType paymentType, 
                            String voucherCode) {
    
    // 1. Lấy thông tin booking
    Booking booking = bookingRepository.findById(bookingId)
        .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
    
    // 2. Tính tổng tiền
    BigDecimal totalAmount = calculateTotalAmount(booking, paymentType, voucherCode);
    
    // 3. Tạo Payment record
    Payment payment = new Payment();
    payment.setCustomer(customer);
    payment.setBooking(booking);
    payment.setAmount(totalAmount);
    payment.setPaymentMethod(paymentMethod);
    payment.setPaymentType(paymentType);
    payment.setStatus(PaymentStatus.PENDING);
    
    // 4. Generate orderCode duy nhất cho PayOS
    Long orderCode = generateUniqueOrderCode(bookingId);
    payment.setOrderCode(orderCode);
    
    // 5. Lưu vào database
    return paymentRepository.save(payment);
}
```

#### Bước 2: Tạo PayOS Payment Link

File: `src/main/java/com/example/booking/service/PayOsService.java`

```java
public CreateLinkResponse createPaymentLink(long orderCode, 
                                           long amount, 
                                           String description) {
    // 1. Tạo signature HMAC-SHA256
    String signature = signCreate(amount, cancelUrl, description, orderCode, returnUrl);
    
    // 2. Chuẩn bị payload
    Map<String, Object> payload = new HashMap<>();
    payload.put("orderCode", orderCode);
    payload.put("amount", amount);                    // Số tiền (VNĐ)
    payload.put("description", description);          // Mô tả giao dịch
    payload.put("cancelUrl", cancelUrl);             // URL khi hủy
    payload.put("returnUrl", returnUrl);             // URL khi thành công
    payload.put("expiredAt", Instant.now().plusSeconds(15 * 60).getEpochSecond()); // Hết hạn sau 15 phút
    payload.put("signature", signature);
    
    // 3. Gọi PayOS API
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.add("x-client-idx-api-key", clientId + apiKey);
    
    String url = endpoint + "/v2/payment-requests";
    ResponseEntity<CreateLinkResponse> resp = restTemplate.exchange(
        URI.create(url), HttpMethod.POST, new HttpEntity<>(payload, headers), 
        CreateLinkResponse.class
    );
    
    return resp.getBody();
}
```

#### Bước 3: Xử Lý Webhook Từ PayOS

File: `src/main/java/com/example/booking/service/PaymentService.java`

```java
public boolean handlePayOsWebhook(String rawBody) {
    try {
        // 1. Parse JSON webhook
        ObjectMapper mapper = new ObjectMapper();
        WebhookRequest webhookRequest = mapper.readValue(rawBody, WebhookRequest.class);
        WebhookData data = webhookRequest.getData();
        
        // 2. Tìm Payment theo orderCode
        Integer orderCode = data.getOrderCode();
        Payment payment = paymentRepository.findByOrderCode(orderCode.longValue())
            .orElseThrow(() -> new IllegalArgumentException("Payment not found"));
        
        // 3. Kiểm tra trạng thái thanh toán
        boolean success = Boolean.TRUE.equals(webhookRequest.getSuccess()) 
                       && "00".equals(data.getCode());
        
        if (success) {
            // ✅ Thanh toán thành công
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setPaidAt(LocalDateTime.now());
            
            // Confirm booking
            bookingService.confirmBooking(payment.getBooking().getBookingId());
        } else {
            // ❌ Thanh toán thất bại
            payment.setStatus(PaymentStatus.FAILED);
        }
        
        // 4. Lưu lại raw webhook data
        payment.setIpnRaw(rawBody);
        paymentRepository.save(payment);
        
        return true;
    } catch (Exception e) {
        logger.error("Error processing PayOS webhook", e);
        return false;
    }
}
```

---

## ⚙️ 4. Cấu Hình PayOS

### 4.1. File Cấu Hình

File: `src/main/resources/application.yml`

```yaml
payment:
  payos:
    client-id: ${PAYOS_CLIENT_ID}           # Client ID từ PayOS
    api-key: ${PAYOS_API_KEY}               # API Key từ PayOS
    checksum-key: ${PAYOS_CHECKSUM_KEY}     # Checksum Key để verify signature
    endpoint: https://api-merchant.payos.vn  # PayOS API endpoint
    return-url: ${PAYOS_RETURN_URL:http://localhost:8081/payment/payos/return}
    cancel-url: ${PAYOS_CANCEL_URL:http://localhost:8081/payment/payos/cancel}
```

### 4.2. Biến Môi Trường

Tạo file `.env` hoặc set environment variables:

```bash
# PayOS Configuration
PAYOS_CLIENT_ID=your-client-id-here
PAYOS_API_KEY=your-api-key-here
PAYOS_CHECKSUM_KEY=your-checksum-key-here
PAYOS_RETURN_URL=http://localhost:8081/payment/payos/return
PAYOS_CANCEL_URL=http://localhost:8081/payment/payos/cancel
```

### 4.3. Lấy Thông Tin Từ PayOS

1. Đăng ký tài khoản tại: https://payos.vn
2. Vào **Dashboard** → **Cài đặt** → **API Keys**
3. Copy các thông tin:
   - Client ID
   - API Key
   - Checksum Key
4. Cấu hình Webhook URL trong PayOS Dashboard:
   ```
   http://your-domain.com/api/payment/payos/webhook
   ```

---

## 🧪 5. Test API Endpoints

### 5.1. Tạo Payment và Lấy Link PayOS

**Endpoint:** `POST /payment/create`

**Request Body:**
```json
{
  "bookingId": 123,
  "paymentMethod": "PAYOS",
  "paymentType": "DEPOSIT",
  "voucherCode": ""
}
```

**Response:**
```json
{
  "success": true,
  "paymentId": 456,
  "amount": 430000,
  "orderCode": 20250107123456,
  "checkoutUrl": "https://pay.payos.vn/web/abc123xyz"
}
```

### 5.2. Kiểm Tra Trạng Thái Payment

**Endpoint:** `GET /payment/{paymentId}`

**Response:**
```json
{
  "paymentId": 456,
  "bookingId": 123,
  "amount": 430000,
  "status": "COMPLETED",
  "paymentMethod": "PAYOS",
  "orderCode": 20250107123456,
  "paidAt": "2025-01-07T10:30:00"
}
```

### 5.3. Webhook từ PayOS (Auto)

**Endpoint:** `POST /api/payment/payos/webhook`

PayOS sẽ tự động gọi endpoint này khi thanh toán thành công:

```json
{
  "code": "00",
  "desc": "Success",
  "success": true,
  "data": {
    "orderCode": 20250107123456,
    "amount": 430000,
    "description": "Thanh toan dat ban #123",
    "accountNumber": "12345678",
    "reference": "FT25010712345678",
    "transactionDateTime": "2025-01-07T10:30:00",
    "paymentLinkId": "abc123xyz"
  }
}
```

---

## 📱 6. Flow Người Dùng (Frontend)

### 6.1. Trang Thanh Toán

File: `src/main/resources/templates/payment/create.html`

```html
<!-- Hiển thị tổng tiền -->
<div class="total-amount">
    <h4>Tổng tiền: <span th:text="${#numbers.formatDecimal(totalAmount, 0, 'COMMA', 0, 'POINT')}"></span> VNĐ</h4>
</div>

<!-- Chọn phương thức thanh toán -->
<form method="post" th:action="@{/payment/create}">
    <input type="hidden" name="bookingId" th:value="${booking.bookingId}">
    
    <div class="payment-methods">
        <label>
            <input type="radio" name="paymentMethod" value="PAYOS" required>
            <img src="/images/payos-logo.png" alt="PayOS">
            Thanh toán qua PayOS
        </label>
        
        <label>
            <input type="radio" name="paymentMethod" value="CASH">
            Thanh toán tiền mặt
        </label>
    </div>
    
    <button type="submit" class="btn btn-primary">Thanh toán</button>
</form>
```

### 6.2. Trang Kết Quả Thanh Toán

**URL Return Success:** `/payment/payos/return?orderCode=xxx&status=PAID`

```html
<div class="payment-success">
    <h2>✅ Thanh toán thành công!</h2>
    <p>Mã đơn hàng: <span th:text="${orderCode}"></span></p>
    <p>Booking của bạn đã được xác nhận.</p>
    <a th:href="@{/booking/my}" class="btn btn-primary">Xem booking của tôi</a>
</div>
```

**URL Cancel:** `/payment/payos/cancel`

```html
<div class="payment-cancelled">
    <h2>❌ Thanh toán đã bị hủy</h2>
    <p>Bạn có thể thử lại hoặc chọn phương thức thanh toán khác.</p>
    <a th:href="@{/payment/{id}(id=${bookingId})}" class="btn btn-secondary">Thử lại</a>
</div>
```

---

## 🐛 7. Debugging và Logging

### 7.1. Xem Log Tính Toán Tổng Tiền

File: `src/main/java/com/example/booking/service/BookingService.java`

```java
public BigDecimal calculateTotalAmount(Booking booking) {
    System.out.println("💰 Deposit amount: " + booking.getDepositAmount());
    
    // Log từng món ăn
    for (BookingDish dish : bookingDishes) {
        System.out.println("🍽️ Dish: " + dish.getDish().getName() + 
                         " x" + dish.getQuantity() + 
                         " = " + dish.getTotalPrice());
    }
    
    // Log từng dịch vụ
    for (BookingService service : bookingServices) {
        System.out.println("🔧 Service: " + service.getService().getName() + 
                         " x" + service.getQuantity() + 
                         " = " + service.getTotalPrice());
    }
    
    System.out.println("💰 TOTAL AMOUNT: " + total);
    return total;
}
```

### 7.2. Test Locally với ngrok

Vì PayOS cần gọi webhook về server, bạn cần expose localhost:

```bash
# Install ngrok
npm install -g ngrok

# Expose port 8081
ngrok http 8081

# Copy URL và cấu hình trong PayOS Dashboard
# Ví dụ: https://abc123.ngrok.io/api/payment/payos/webhook
```

---

## 📊 8. Flow Chart Tổng Thể

```
┌─────────────────┐
│  Khách hàng     │
│  tạo booking    │
└────────┬────────┘
         │
         ▼
┌─────────────────────────────┐
│ calculateTotalAmount()      │
│ = Deposit + Dishes + Svcs   │
└────────┬────────────────────┘
         │
         ▼
┌─────────────────────────────┐
│ Chọn phương thức: PayOS     │
└────────┬────────────────────┘
         │
         ▼
┌─────────────────────────────┐
│ PaymentService              │
│ .createPayment()            │
│ - Generate orderCode        │
│ - Save to DB                │
└────────┬────────────────────┘
         │
         ▼
┌─────────────────────────────┐
│ PayOsService                │
│ .createPaymentLink()        │
│ - Call PayOS API            │
│ - Get checkout URL          │
└────────┬────────────────────┘
         │
         ▼
┌─────────────────────────────┐
│ Redirect khách đến PayOS    │
│ Khách thanh toán            │
└────────┬────────────────────┘
         │
         ▼
┌─────────────────────────────┐
│ PayOS gửi webhook           │
│ POST /api/payment/webhook   │
└────────┬────────────────────┘
         │
         ▼
┌─────────────────────────────┐
│ handlePayOsWebhook()        │
│ - Update Payment: COMPLETED │
│ - Confirm Booking           │
└─────────────────────────────┘
```

---

## 🔒 9. Security Best Practices

### 9.1. Verify Webhook Signature

```java
public boolean verifyWebhook(String body, String signature) {
    try {
        String expected = hmacSHA256(body, checksumKey);
        return expected.equals(signature);
    } catch (Exception e) {
        logger.error("Verify webhook signature failed", e);
        return false;
    }
}
```

### 9.2. Validate Order Code

```java
// Đảm bảo orderCode là duy nhất
private Long generateUniqueOrderCode(Integer bookingId) {
    long timestamp = System.currentTimeMillis();
    long orderCode = timestamp * 1000 + bookingId;
    
    // Check if exists
    while (paymentRepository.existsByOrderCode(orderCode)) {
        orderCode++;
    }
    
    return orderCode;
}
```

---

## 📞 10. Support & Resources

### 10.1. PayOS Documentation
- API Docs: https://payos.vn/docs/
- Webhook Guide: https://payos.vn/docs/webhook
- Testing: https://payos.vn/docs/testing

### 10.2. Project Files
- PayOS Service: `src/main/java/com/example/booking/service/PayOsService.java`
- Payment Service: `src/main/java/com/example/booking/service/PaymentService.java`
- Booking Service: `src/main/java/com/example/booking/service/BookingService.java`
- Payment Controller: `src/main/java/com/example/booking/web/controller/PaymentController.java`

### 10.3. Liên Hệ
- Nhóm phát triển: SWP391 Restaurant Booking Team
- Email support: support@bookeat.vn (example)

---

## ✅ Checklist Triển Khai

- [ ] Đăng ký tài khoản PayOS
- [ ] Lấy Client ID, API Key, Checksum Key
- [ ] Cấu hình biến môi trường (.env)
- [ ] Deploy server lên production
- [ ] Cấu hình Webhook URL trong PayOS Dashboard
- [ ] Test thanh toán sandbox
- [ ] Test thanh toán production
- [ ] Xác nhận webhook hoạt động
- [ ] Monitor logs và errors
- [ ] Setup alerting cho payment failures

---

**Chúc bạn thành công với tích hợp PayOS!** 🚀

