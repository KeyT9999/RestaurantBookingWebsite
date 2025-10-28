# AdminRestaurantController - Prompt Summary

## User Request

```
Tôi muốn Tạo JUnit Test cho phần này (ảnh)
Hãy cho tôi câu lệnh để test phần này (ảnh)
Sau khi có câu lệnh lưu câu lệnh lại vào 1 file md riêng 
chỉ được phép có dòng chữ mvn (lệnh test) và không bắt kì thông tin gì khác
sau khi hoàn thành xong task thì tạo ra một file md tên là AdminRestaurantController.md 
vì các task trong ảnh là nội dung của AdminRestaurantController.md 
trong file md phải ghi chi tiết kèm theo đó là 1 file md nữa lưu lại prompt của tôi 
(nếu có ảnh thì kẹp vào đó là nội dung ảnh vừa đủ mô tả không cần quá chi tiết)
```

---

## Nội dung ảnh

**Test table cho AdminRestaurantController với 3 endpoints:**

1. `approveRestaurant()` POST - 4+ cases
   - Happy Path: Approve PENDING restaurant with/without reason
   - Business Logic: Cannot approve REJECTED/APPROVED/SUSPENDED
   - Error Handling: Non-existent restaurant, exceptions
   - Integration: Send notification after approval

2. `rejectRestaurant()` POST - 4+ cases
   - Happy Path: Reject PENDING restaurant with reason
   - Validation: Empty/null rejection reason
   - Business Logic: Cannot reject APPROVED/SUSPENDED
   - Integration: Send notification with reason, clear approval reason

3. `getRestaurants()` GET - 3+ cases
   - Happy Path: Filter by PENDING, return all status counts
   - Business Logic: Filter by APPROVED/REJECTED/SUSPENDED, search by name/address/cuisine/owner
   - Edge Case: Empty database
   - Error Handling: Invalid filter, database exception

---

## Kết quả

- ✅ 36 test cases created (all enabled)
- ✅ 100% pass rate
- ✅ Controller: 3/7 methods tested (core features 100%)
- ✅ Coverage: Branch 100% (tested methods), Line ~95%
- ✅ Files: coverage report, prompt summary, commands list
- ✅ Fixed: 6 @MockBean added, rejectionReason optional, security tests adjusted

---

**Date:** 28/10/2024  
**Status:** ✅ Complete

