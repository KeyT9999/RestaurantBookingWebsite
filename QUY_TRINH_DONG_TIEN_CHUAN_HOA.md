# 💸 QUY TRÌNH DÒNG TIỀN CHUẨN HÓA - RESTAURANT BOOKING SYSTEM

## 📋 TỔNG QUAN

Tài liệu này chuẩn hóa quy trình dòng tiền giữa **Customer - Admin - Restaurant** trong hệ thống đặt chỗ nhà hàng, làm chuẩn cho toàn bộ nghiệp vụ Payment, Balance, và Withdrawal.

---

## 🏗️ KIẾN TRÚC DÒNG TIỀN

### 💡 Nguyên tắc cốt lõi
- **Admin PayOS Account** = Ví trung gian (escrow wallet) 
- **Customer** → **Admin PayOS** → **Restaurant Bank Account**
- **Admin** giữ phần commission, **Restaurant** nhận phần net revenue

---

## 🔄 QUY TRÌNH CHI TIẾT

### 1️⃣ GIAI ĐOẠN ĐẶT BÀN (Booking/Deposit Flow)

#### 📝 Quy trình:
1. **Customer** đặt bàn qua website/app
2. **Hệ thống** tính toán deposit amount:
   - Nếu tổng > 500k VNĐ → deposit = 10% tổng
   - Nếu tổng ≤ 500k VNĐ → deposit = 10k VNĐ (minimum)
3. **Customer** chuyển khoản deposit qua **PayOS Payment Gateway**
4. **Toàn bộ tiền** được chuyển vào **Admin PayOS Account** (Merchant)

#### 💻 Code Implementation:
```java
// PaymentService.java - calculateTotalAmount()
if (paymentType == PaymentType.DEPOSIT) {
    BigDecimal threshold = new BigDecimal("500000");
    BigDecimal minimumDeposit = new BigDecimal("10000");
    
    if (fullTotal.compareTo(threshold) > 0) {
        paymentAmount = fullTotal.multiply(new BigDecimal("0.1")); // 10%
    } else {
        paymentAmount = minimumDeposit; // 10k minimum
    }
}
```

#### 🗃️ Database Tables:
- `payment` - Lưu thông tin giao dịch deposit
- `booking` - Trạng thái đặt bàn (PENDING → CONFIRMED)

---

### 2️⃣ QUYỀN SỞ HỮU TIỀN

#### 🧩 Phân quyền:
- **Sở hữu vật lý**: Admin PayOS Account
- **Sở hữu nghiệp vụ**: Restaurant (doanh thu từ đơn hàng)
- **Admin** chỉ tạm giữ tiền (escrow) để:
  - Đảm bảo đơn hoàn tất hợp lệ
  - Tính và trừ phí hoa hồng (commission)
  - Chi trả (payout) lại cho Restaurant

---

### 3️⃣ SAU KHI ĐƠN HOÀN TẤT (Booking = COMPLETED)

#### 📊 Cập nhật doanh thu:
```java
// RestaurantBalance.java - addRevenue()
public void addRevenue(BigDecimal amount) {
    this.totalRevenue = this.totalRevenue.add(amount);
    this.totalBookingsCompleted++;
    recalculateAvailableBalance();
}
```

#### 🧮 Công thức tính toán:
- `deposit_amount` = số tiền khách cọc
- `commission_rate` = 7.5% (mặc định)
- `net_amount` = deposit_amount - commission
- `available_balance` = total_revenue - total_commission - total_withdrawn - pending_withdrawal

#### 🗃️ Database Updates:
```sql
-- restaurant_balance table
UPDATE restaurant_balance SET 
    total_revenue = total_revenue + deposit_amount,
    total_bookings_completed = total_bookings_completed + 1,
    available_balance = total_revenue - total_commission - total_withdrawn - pending_withdrawal
WHERE restaurant_id = ?;
```

---

### 4️⃣ NHÀ HÀNG RÚT DOANH THU (Withdrawal Flow)

#### 📋 Quy trình:
1. **Restaurant** mở dashboard → chọn "Withdraw Revenue"
2. **Yêu cầu rút** được lưu tại `withdrawal_request` (status = PENDING)
3. **Admin** duyệt lệnh rút → hệ thống gọi **PayOS Payout API**
4. **Chuyển tiền** từ Admin PayOS → Restaurant Bank Account
5. **Lưu chi tiết** tại `payout_transaction`

#### 💻 Code Implementation:
```java
// WithdrawalService.java - approveWithdrawal()
@Transactional
public WithdrawalRequestDto approveWithdrawal(Integer requestId, UUID adminUserId, String notes) {
    // Step 1: Lock withdrawal request (FOR UPDATE)
    WithdrawalRequest request = withdrawalRepository.findByIdForUpdate(requestId);
    
    // Step 2: Lock restaurant balance (FOR UPDATE)
    RestaurantBalance balance = balanceRepository.findByRestaurantIdForUpdate(restaurantId);
    
    // Step 3: Lock pending withdrawal
    balance.setPendingWithdrawal(balance.getPendingWithdrawal().add(request.getAmount()));
    
    // Step 4: Create payout transaction via PayOS
    createPayoutTransaction(request);
}
```

#### 🗃️ Database Tables:
- `withdrawal_request` - Yêu cầu rút tiền
- `payout_transaction` - Chi tiết giao dịch PayOS
- `restaurant_bank_account` - Thông tin tài khoản nhận tiền

---

## 🔁 DÒNG TIỀN THỰC TẾ

| Bước | Dòng tiền | Mô tả |
|------|-----------|-------|
| 1 | **Customer** → **Admin PayOS** | Đặt cọc qua PayOS khi booking |
| 2 | **Admin PayOS** → **Restaurant Bank** | Rút doanh thu qua PayOS Payout |
| 3 | **Admin** giữ lại phần hoa hồng | Lợi nhuận hệ thống (7.5%) |
| 4 | **Admin** → **Customer** (nếu cần) | Refund nếu đơn bị hủy |

---

## 🧮 CÔNG THỨC TÍNH KHẢ DỤNG

```java
// RestaurantBalance.java - recalculateAvailableBalance()
public void recalculateAvailableBalance() {
    this.totalCommission = calculateCommission();
    this.availableBalance = this.totalRevenue
        .subtract(this.totalCommission)      // Trừ hoa hồng
        .subtract(this.totalWithdrawn)        // Trừ đã rút
        .subtract(this.pendingWithdrawal);    // Trừ đang chờ rút
}
```

### 📊 Chi tiết tính toán:
- `total_revenue` = Tổng doanh thu từ các booking completed
- `total_commission` = Tổng hoa hồng (7.5% × total_revenue)
- `total_withdrawn` = Tổng đã rút thành công
- `pending_withdrawal` = Tổng đang chờ rút (đã approve)
- `available_balance` = Số dư có thể rút

---

## ✅ CHUẨN HÓA CHO CODE

### 🗃️ Database Schema:

| Thành phần | Bảng dữ liệu | Mục đích |
|------------|---------------|----------|
| **Restaurant Bank Info** | `restaurant_bank_account` | Lưu thông tin tài khoản nhận tiền |
| **Doanh thu** | `restaurant_balance` | Theo dõi tổng revenue, commission, available |
| **Lệnh rút** | `withdrawal_request` | Lưu yêu cầu rút, admin duyệt |
| **Giao dịch chi** | `payout_transaction` | Lưu trạng thái PayOS payout |
| **Log hoạt động** | `payout_audit_log` | Ghi lịch sử thao tác, debug |
| **Ngân hàng** | `bank_directory` | Cache danh sách ngân hàng (BIN) |

### 🔧 Service Classes:

| Service | Chức năng |
|---------|-----------|
| `PaymentService` | Xử lý payment từ Customer |
| `RestaurantBalanceService` | Quản lý số dư nhà hàng |
| `WithdrawalService` | Xử lý yêu cầu rút tiền |
| `PayosPayoutService` | Tích hợp PayOS Payout API |
| `WithdrawalNotificationService` | Gửi thông báo |

---

## 🎯 TÓM TẮT QUY TRÌNH NGẮN GỌN

### 📝 Flow Summary:
1. **Customer** đặt bàn → tiền vào ví **PayOS của Admin**
2. Sau khi đơn hoàn tất → **Restaurant** có thể rút doanh thu (đã trừ hoa hồng)
3. Hệ thống gọi **PayOS Payout API** → **Admin** chuyển tiền sang tài khoản ngân hàng của **Restaurant**

### 💡 Tóm tắt 1 dòng cho dev:
> **Customer → PayOS (Admin ví) → Admin (PayOS) → Restaurant Bank** = Luồng tiền chuẩn. Admin giữ phần commission, nhà hàng rút phần net revenue qua PayOS Payout.

---

## 🔒 BẢO MẬT VÀ KIỂM SOÁT

### 🛡️ Pessimistic Locking:
```java
// Ngăn chặn race condition khi approve withdrawal
@Transactional
public WithdrawalRequestDto approveWithdrawal(Integer requestId, UUID adminUserId, String notes) {
    // Lock withdrawal request (FOR UPDATE)
    WithdrawalRequest request = withdrawalRepository.findByIdForUpdate(requestId);
    
    // Lock restaurant balance (FOR UPDATE)  
    RestaurantBalance balance = balanceRepository.findByRestaurantIdForUpdate(restaurantId);
}
```

### 📊 Audit Trail:
- Tất cả thao tác được ghi log trong `payout_audit_log`
- Tracking đầy đủ từ tạo request → approve → payout → success/failed

### ⚠️ Validation Rules:
- Minimum withdrawal: 100k VNĐ
- Maximum withdrawals per day: 3 lần
- Balance validation trước khi approve

---

## 🚀 IMPLEMENTATION STATUS

### ✅ Đã hoàn thành:
- [x] Payment flow với PayOS
- [x] Restaurant balance calculation
- [x] Withdrawal request system
- [x] PayOS Payout integration
- [x] Notification system
- [x] Audit logging

### 🔄 Cần cải thiện:
- [ ] Webhook handling cho PayOS Payout status
- [ ] Automated commission calculation per booking
- [ ] Refund handling
- [ ] Multi-currency support

---

*Tài liệu này được tạo dựa trên phân tích codebase hiện tại và sẽ được cập nhật khi có thay đổi trong hệ thống.*
