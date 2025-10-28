# 🚨 Quick Debug Guide - Khi Test Fail

## ⚡ TL;DR - Quá dài không đọc?

### Khi test fail, CHỈ CẦN 3 thứ này:

```
1️⃣ [ERROR] Tests run: 8, Failures: 1    ← Bao nhiêu test fail?
2️⃣ testApproveRestaurant...line:102     ← Test nào fail, dòng nào?
3️⃣ Expected: X but was: Y               ← Lỗi gì?
```

**Tổng cộng: ~10 dòng thay vì 500+ dòng!** ✅

---

## 📊 Visual Guide: Log Structure

```
┌─────────────────────────────────────────────────────────┐
│  [INFO] -----------------------------------------------  │
│  [INFO]  T E S T S                                      │  ← SKIP (không cần)
│  [INFO] -----------------------------------------------  │
│  [INFO] Running AdminRestaurantControllerTest...        │
│  ... (300+ dòng Spring Boot startup)                    │  ← SKIP (không cần)
│  ... (200+ dòng bean initialization)                    │  ← SKIP (không cần)
│  ... (100+ dòng DEBUG logs)                             │  ← SKIP (không cần)
├─────────────────────────────────────────────────────────┤
│                                                          │
│  [ERROR] Tests run: 8, Failures: 1, Errors: 0  ⭐⭐⭐   │  ← ✅ CẦN (quan trọng!)
│  [ERROR] Failures:                              ⭐⭐⭐   │
│  [ERROR]   testApproveRestaurant...:102        ⭐⭐⭐   │  ← ✅ CẦN (test nào?)
│  Expected: redirect to "/admin/..."            ⭐⭐⭐   │  ← ✅ CẦN (lỗi gì?)
│       but: was redirect to "/"                 ⭐⭐⭐   │
│                                                          │
│  at RestaurantApprovalService.java:145         ⭐⭐     │  ← 🆗 HỮU ÍCH (nếu cần)
│  at AdminRestaurantController.java:178         ⭐⭐     │
│                                                          │
│  at sun.reflect.NativeMethod...                         │  ← SKIP (framework code)
│  at java.lang.reflect.Method...                         │  ← SKIP (framework code)
│  ... (40+ dòng framework stacktrace)                    │  ← SKIP (không cần)
│                                                          │
│  [INFO] BUILD FAILURE                                   │
└─────────────────────────────────────────────────────────┘
         ↑                                    ↑
    Scroll xuống đây!              Phần quan trọng ở đây!
```

---

## 🎯 Workflow 3 Bước

### Bước 1: Chạy test
```bash
mvn test -Dtest=AdminRestaurantControllerTest$ApproveRestaurantTests
```

### Bước 2: Scroll xuống CUỐI CÙNG của log
- Đừng đọc từ trên xuống (quá dài!)
- **Scroll thẳng xuống cuối** ⬇️
- Tìm dòng `[ERROR]`

### Bước 3: Copy 3 phần này thôi
```
[ERROR] Tests run: 8, Failures: 1        ← Line này
[ERROR] testApproveRestaurant...:102     ← Line này
Expected: X but was: Y                    ← Line này
```

**Done!** Bạn có đủ thông tin để debug! ✅

---

## 📝 Template Copy/Paste

```
🐛 Bug Report

Command:
mvn test -Dtest=AdminRestaurantControllerTest$ApproveRestaurantTests

Result:
[ERROR] Tests run: 8, Failures: 1

Test failed:
testApproveRestaurant_WithPendingStatus_ShouldApproveSuccessfully:102

Error:
Expected: redirect to "/admin/restaurant/requests/1"
     but: was redirect to "/"

Files to check:
- AdminRestaurantController.java
- RestaurantApprovalService.java
```

---

## 💡 Pro Tip: Lưu chỉ phần error

### Windows PowerShell:
```powershell
mvn test -Dtest=AdminRestaurantControllerTest$ApproveRestaurantTests 2>&1 | Tee-Object test.log
Get-Content test.log -Tail 50
```

### Linux/Mac:
```bash
mvn test -Dtest=AdminRestaurantControllerTest$ApproveRestaurantTests 2>&1 | tee test.log
tail -50 test.log
```

→ Chỉ xem 50 dòng cuối (chứa error)! ✅

---

## 🎨 Color Guide (nếu terminal có màu)

| Màu | Ý nghĩa | Cần đọc? |
|-----|---------|----------|
| 🟢 **[INFO]** | Thông tin chung | ❌ Không |
| 🟡 **[WARNING]** | Cảnh báo nhẹ | 🆗 Nếu có thời gian |
| 🔴 **[ERROR]** | **Lỗi test** | ✅ **Phải đọc!** |

---

## ❓ FAQ

### Q: Tôi phải chụp màn hình log không?
**A:** **KHÔNG!** Chỉ cần copy/paste 3 dòng error message.

### Q: Log dài 500+ dòng, tôi phải đọc hết không?
**A:** **KHÔNG!** Scroll xuống cuối, chỉ đọc 10-20 dòng cuối.

### Q: Làm sao biết test nào fail?
**A:** Xem dòng có chữ `Failures:` hoặc `Errors:` phía dưới.

### Q: Có cần copy toàn bộ stacktrace không?
**A:** **KHÔNG!** Chỉ cần 5-10 dòng đầu của stacktrace (nếu cần).

### Q: Framework logs có quan trọng không?
**A:** **KHÔNG!** Skip tất cả `sun.reflect.*`, `java.lang.reflect.*`, Spring framework code.

---

## 🆘 Còn bí? 

### Đọc thêm:
- 📖 File đầy đủ: `AdminRestaurantController_TestCommands_Guide.md`
- 📋 Chi tiết tests: `AdminRestaurantController.md`
- 🚀 Commands: `run_admin_restaurant_controller_tests.md`

### Key takeaway:
```
Log dài 500 dòng ≠ Phải đọc 500 dòng

Chỉ cần:
✅ Summary line (1 dòng)
✅ Test name (1 dòng)  
✅ Error message (2-3 dòng)

= Tổng cộng ~5 dòng! 🎉
```

---

**Last Updated:** October 28, 2024  
**Status:** ✅ Ready to use  
**Xem trong:** < 1 phút ⚡


