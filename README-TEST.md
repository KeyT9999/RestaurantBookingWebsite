# Hướng dẫn chạy tất cả tests và coverage

## Chạy tất cả tests và JaCoCo coverage

### Trên Windows:
```batch
.\run-all-tests.bat
```

Hoặc chạy trực tiếp:
```batch
mvn clean test jacoco:report
```

### Trên Linux/Mac:
```bash
chmod +x run-all-tests.sh
./run-all-tests.sh
```

Hoặc chạy trực tiếp:
```bash
mvn clean test jacoco:report
```

## Kết quả

### Test results:
- Tất cả test results được lưu tại: `target/surefire-reports/`
- Tổng số tests chạy sẽ hiển thị trong console

### JaCoCo coverage report:
- Report được tạo tại: `target/site/jacoco/index.html`
- Mở file này trong browser để xem chi tiết coverage

## Coverage bao gồm:

### 1. Global Exception Handling (GE-001 → GE-006)
- `GlobalExceptionHandlerTest.java`
- `ApiResponseAdviceTest.java`

### 2. Realtime Chat (RC-001 → RC-036)
- `ChatMessageControllerTest.java`
- `ChatServiceTest.java`
- `ChatApiControllerTest.java`

### 3. AI Actions API (AI-001 → AI-017)
- `AIActionsControllerTest.java`
- `AIServiceTest.java`
- `AIIntentDispatcherServiceTest.java`
- `AIResponseProcessorServiceTest.java`
- `AIActionRequestTest.java`
- `AIActionResponseTest.java`

### 4. AI Sync (AS-001 → AS-018)
- `AiSyncEventPublisherTest.java`
- `AiSyncConfigTest.java`
- `AiSyncPropertiesTest.java`

### 5. AI Caching (AC-001 → AC-003)
- `AiCacheConfigTest.java`

### 6. Các tests khác trong project
- `HomeControllerTest.java`
- `FavoriteControllerTest.java`
- `AuditServiceTest.java`
- `AuditAspectTest.java`
- `AsyncConfigTest.java`
- `RestTemplateConfigTest.java`
- `RestaurantBookingApplicationTest.java`
- `BankAccountServiceTest.java`
- `VietQRServiceTest.java`
- `BankAccountApiControllerTest.java`
- `PayOSReconciliationSchedulerTest.java`
- Và nhiều tests khác...

## Tổng số test cases: ~150+ tests

## Coverage targets:

| Module | Current Coverage | Target |
|--------|-----------------|--------|
| Controllers | ~15% | ≥70% |
| Services | ~24% | ≥80% |
| Repositories | ~7% | ≥70% |
| Config | ~46% | ≥80% |

## Lưu ý

- Đảm bảo database đang chạy (hoặc sử dụng H2 embedded cho tests)
- Một số tests yêu cầu mock external services (PayOS, AI, VietQR)
- Test execution time: ~2-3 phút
- JaCoCo report generation: ~10-15 giây

## Troubleshooting

### Nếu tests fail:
```bash
mvn clean test -DfailIfNoTests=false
```

### Xem test output chi tiết:
```bash
mvn test -X
```

### Chạy only một test class:
```bash
mvn test -Dtest=GlobalExceptionHandlerTest
```

### Chỉ generate JaCoCo report (không chạy lại tests):
```bash
mvn jacoco:report
```

