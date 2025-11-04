# 📊 PHÂN TÍCH COVERAGE THIẾU - DANH SÁCH ĐẦY ĐỦ

**Ngày**: 2025-01-31  
**Tổng số file Java**: 361  
**Tổng số file Test**: 96  
**Tỷ lệ**: ~26.6% (96/361)  
**Số file thiếu test**: 285 files

---

## 📋 DANH SÁCH CÁC CLASS CHƯA CÓ TEST

### 🔴 SERVICE LAYER (Services chưa có test)

#### DANH SÁCH CHÍNH XÁC - 26 SERVICES CHƯA CÓ TEST:

1. **AuthRateLimitingService.java** ❌
2. **CloudinaryService.java** ❌ - Image upload to Cloudinary - **CRITICAL**
3. **DatabaseRateLimitingService.java** ❌
4. **EmailService.java** ❌ - Email notifications - **CRITICAL**
5. **EndpointRateLimitingService.java** ❌
6. **FavoriteService.java** ❌ (Có FavoriteServiceImpl nhưng cần test interface)
7. **FileUploadService.java** ❌ - File upload handling - **HIGH PRIORITY**
8. **FOHManagementService.java** ❌ - Front-of-house management
9. **GeneralRateLimitingService.java** ❌
10. **ImageUploadService.java** ❌ - Image processing - **HIGH PRIORITY**
11. **LoginRateLimitingService.java** ❌
12. **NotificationService.java** ❌ (Có NotificationServiceImpl nhưng cần test interface)
13. **OAuth2UserService.java** ❌ - OAuth authentication - **CRITICAL**
14. **PaymentLedgerService.java** ❌ - Payment tracking - **HIGH PRIORITY**
15. **RateLimitingMonitoringService.java** ❌
16. **RateLimitingService.java** ❌
17. **RestaurantBalanceService.java** ❌ - Financial tracking - **CRITICAL**
18. **RestaurantBankAccountService.java** ❌ - Bank account management - **CRITICAL**
19. **RestaurantDashboardService.java** ❌ - Dashboard analytics
20. **RestaurantNotificationService.java** ❌ - Restaurant notifications
21. **ReviewReportService.java** ❌ (Có ReviewReportServiceImpl nhưng cần test interface)
22. **ReviewService.java** ❌ - Review management - **HIGH PRIORITY**
23. **SmartWaitlistService.java** ❌ - Advanced waitlist logic
24. **TableStatusManagementService.java** ❌ - Table status management
25. **WithdrawalNotificationService.java** ❌ - Withdrawal notifications
26. **EnhancedRefundService.java** ❌ (Có thể có test nhưng cần check)

**Tổng**: 26 services chưa có test (từ 49 services, đã có ~23 có test)

---

### 🔴 CONTROLLER LAYER (Controllers chưa có test)

#### DANH SÁCH CHÍNH XÁC - ~28 CONTROLLERS CHƯA CÓ TEST:

#### Web Controllers:
1. **AdminSetupController.java** ❌
2. **AdminUserController.java** ❌
3. **CloudinaryTestController.java** ❌ (Test controller, có thể không cần test)
4. **CustomerChatController.java** ❌
5. **DebugController.java** ❌ (Debug controller, có thể không cần test)
6. **DemoController.java** ❌ (Demo controller, có thể không cần test)
7. **EnvTestController.java** ❌ (Test controller, có thể không cần test)
8. **NotificationController.java** ❌
9. **PaymentController.java** ❌ (Có PaymentControllerWebMvcTest nhưng cần check)
10. **RestaurantFileUploadController.java** ❌
11. **RestaurantOwnerChatController.java** ❌
12. **RestaurantReviewController.java** ❌
13. **RestaurantWithdrawalViewController.java** ❌

#### Admin Controllers:
14. **AdminFavoriteController.java** ❌
15. **AdminModerationController.java** ❌
16. **AdminNotificationController.java** ❌
17. **AdminVoucherAnalyticsController.java** ❌
18. **AdminVoucherController.java** ❌
19. **RateLimitingAdminController.java** ❌
20. **SimpleRateLimitingController.java** ❌
21. **UltraSimpleController.java** ❌

#### API Controllers:
22. **RateLimitingApiController.java** ❌
23. **RestaurantWithdrawalApiController.java** ❌
24. **SmartWaitlistApiController.java** ❌
25. **TableStatusApiController.java** ❌
26. **UserApiController.java** ❌
27. **VoucherApiController.java** ❌

#### Restaurant Owner Controllers:
28. **RestaurantFavoriteController.java** ❌
29. **RestaurantVoucherController.java** ❌

**Tổng**: ~28-30 controllers chưa có test (tùy theo loại controller debug/test)

---

### 🔴 REPOSITORY LAYER (Repositories chưa có test)

**43 Repositories tổng cộng, chỉ có 2 có test:**

✅ Đã có test:
1. InternalNoteRepositoryTest.java
2. CommunicationHistoryRepositoryTest.java

❌ **DANH SÁCH CHÍNH XÁC - 40 REPOSITORIES CHƯA CÓ TEST**:

1. **AIRecommendationRepository.java** ❌
2. **AuditLogRepository.java** ❌
3. **BankDirectoryRepository.java** ❌
4. **BlockedIpRepository.java** ❌
5. **BookingDishRepository.java** ❌
6. **BookingRepository.java** ❌ - **CRITICAL**
7. **BookingServiceRepository.java** ❌
8. **BookingTableRepository.java** ❌
9. **ChatRoomRepository.java** ❌
10. **CustomerFavoriteRepository.java** ❌
11. **CustomerRepository.java** ❌ - **CRITICAL**
12. **CustomerVoucherRepository.java** ❌
13. **DiningTableRepository.java** ❌
14. **DishRepository.java** ❌
15. **MessageRepository.java** ❌
16. **NotificationRepository.java** ❌
17. **PaymentRepository.java** ❌ - **CRITICAL**
18. **RateLimitAlertRepository.java** ❌
19. **RateLimitBlockRepository.java** ❌
20. **RateLimitStatisticsRepository.java** ❌
21. **RefundRequestRepository.java** ❌
22. **RestaurantBalanceRepository.java** ❌
23. **RestaurantBankAccountRepository.java** ❌
24. **RestaurantContractRepository.java** ❌
25. **RestaurantMediaRepository.java** ❌
26. **RestaurantOwnerRepository.java** ❌
27. **RestaurantProfileRepository.java** ❌ - **CRITICAL**
28. **RestaurantRepository.java** ❌
29. **RestaurantServiceRepository.java** ❌
30. **RestaurantTableRepository.java** ❌
31. **ReviewReportRepository.java** ❌
32. **ReviewRepository.java** ❌ - **CRITICAL**
33. **UserPreferencesRepository.java** ❌
34. **UserRepository.java** ❌ - **CRITICAL**
35. **VoucherRedemptionRepository.java** ❌
36. **VoucherRepository.java** ❌
37. **WaitlistDishRepository.java** ❌
38. **WaitlistRepository.java** ❌
39. **WaitlistServiceRepository.java** ❌
40. **WaitlistTableRepository.java** ❌
41. **WithdrawalRequestRepository.java** ❌

**Tổng**: 40 repositories chưa có test (từ 43 repositories, chỉ có 2-3 có test)

---

### 🔴 CONFIG LAYER (Config classes chưa có test)

1. **SecurityConfig.java** ❌ - Critical
2. **WebSocketSecurityConfig.java** ❌
3. **CustomAuthenticationSuccessHandler.java** ❌
4. **CustomAuthenticationFailureHandler.java** ❌
5. **CloudinaryConfig.java** ❌
6. **JpaConfig.java** ❌
7. **OpenAIConfiguration.java** ❌
8. **PayoutConfiguration.java** ❌
9. **WebConfig.java** ❌
10. **GlobalControllerAdvice.java** ❌
11. **RateLimitingConfig.java** ❌
12. **RateLimitingInterceptor.java** ❌
13. **AdvancedRateLimitingInterceptor.java** ❌
14. **AuthRateLimitFilter.java** ❌
15. **LoginRateLimitFilter.java** ❌
16. **GeneralRateLimitFilter.java** ❌
17. **PermanentlyBlockedIpFilter.java** ❌
18. Các config classes khác...

---

### 🔴 UTILITY LAYER (Utilities chưa có test)

1. **CityGeoResolver.java** ❌
2. **DatabaseFixer.java** ❌
3. **GeoUtils.java** ❌
4. **PayOSSignatureGenerator.java** ❌
5. Các utility classes khác...

---

### 🔴 DOMAIN LAYER (Domain entities chưa có test)

#### Entities (Nhiều entities chưa có test):
1. **Booking.java** ❌ (Có thể có test trong service layer)
2. **Customer.java** ❌
3. **RestaurantProfile.java** ❌
4. **Payment.java** ❌
5. **Review.java** ❌
6. **Voucher.java** ❌
7. **Waitlist.java** ❌
8. **Dish.java** ❌
9. **RestaurantTable.java** ❌
10. **ChatRoom.java** ❌
11. **Message.java** ❌
12. **Notification.java** ❌
13. Và nhiều entities khác...

#### Converters:
14. **UserRoleAttributeConverter.java** ❌

#### Enums:
- Một số enums đã có test nhưng chưa đầy đủ

---

### 🔴 ASPECT/AOP LAYER

1. **RateLimitingAspect.java** ❌
2. **AuditAspect.java** ✅ (Có test nhưng đang lỗi - cần fix)

---

### 🔴 EXCEPTION HANDLERS

1. **GlobalExceptionHandler.java** ✅ (Có test nhưng có lỗi)
2. Các exception classes khác...

---

### 🔴 DTO LAYER (DTOs chưa có test)

66 DTO files, chỉ có 3 có test:
- DtosTest.java (test một số DTOs)
- AIActionRequestTest.java
- AIActionResponseTest.java

❌ Còn ~60 DTOs chưa có test

---

## 📊 PHÂN TÍCH THEO PRIORITY

### 🔴 **PRIORITY 1 - CRITICAL** (Phải test ngay):

1. **EmailService** - Email notifications critical
2. **CloudinaryService** - Image upload critical  
3. **PaymentLedgerService** - Payment tracking critical
4. **OAuth2UserService** - Authentication critical
5. **ReviewService** - Business logic critical
6. **RestaurantBalanceService** - Finance critical
7. **RestaurantBankAccountService** - Payments critical
8. **SecurityConfig** - Security critical

**Tác động**: Ảnh hưởng trực tiếp đến core functionality

---

### 🟡 **PRIORITY 2 - HIGH** (Nên test sớm):

1. FileUploadService
2. ImageUploadService
3. ReviewService
4. RestaurantDashboardService
5. TableStatusManagementService
6. SmartWaitlistService
7. FOHManagementService

**Tác động**: Ảnh hưởng đến user experience và business features

---

### 🟢 **PRIORITY 3 - MEDIUM** (Có thể test sau):

1. Rate limiting services (nhiều services)
2. Notification services
3. Dashboard services
4. Utility classes
5. DTO classes

**Tác động**: Hỗ trợ features, ít ảnh hưởng trực tiếp

---

### ⚪ **PRIORITY 4 - LOW** (Có thể bỏ qua):

1. Một số config classes đơn giản
2. Một số DTOs đơn giản
3. Enum classes (đã có một số test)

---

## 🎯 KẾ HOẠCH TĂNG COVERAGE

### Bước 1: Fix các test đang lỗi
- AuditAspectTest (3 failures)
- UserRoleStringConverterTest (2 failures)
- Các test khác đang lỗi

### Bước 2: Viết test cho Priority 1 (Critical)
- 8 services quan trọng nhất
- **Mục tiêu**: +15% coverage

### Bước 3: Viết test cho Priority 2 (High)
- 7 services quan trọng
- **Mục tiêu**: +10% coverage

### Bước 4: Viết test cho Controllers
- Customer controllers
- Restaurant owner controllers  
- API controllers
- **Mục tiêu**: +10% coverage

### Bước 5: Viết test cho Repositories
- Test custom queries
- Test CRUD operations
- **Mục tiêu**: +15% coverage

### Bước 6: Viết test cho Config và Utilities
- Security configs
- Rate limiting configs
- Utility classes
- **Mục tiêu**: +5% coverage

**Tổng mục tiêu**: Từ 28% lên **75-80% coverage**

---

## 📈 METRICS DỰ KIẾN

| Layer | Current | Target | Gap |
|-------|---------|--------|-----|
| Services | ~55% | 85% | +30% |
| Controllers | ~37% | 75% | +38% |
| Repositories | ~5% | 70% | +65% |
| Config | ~27% | 60% | +33% |
| Utilities | ~20% | 60% | +40% |
| Domain | ~17% | 50% | +33% |
| **Overall** | **~28%** | **75%** | **+47%** |

---

**Tác giả**: Auto (AI Assistant)  
**Cập nhật**: $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")

