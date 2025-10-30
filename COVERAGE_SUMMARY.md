# 📊 Coverage Report - Quick Summary

## Overall Metrics

```
┌─────────────────────────────────────────────────────────────┐
│                    COVERAGE METRICS                          │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  📊 Line Coverage:        21.53%  [████░░░░░░] 4,789/22,246 │
│  🌿 Branch Coverage:      15.05%  [███░░░░░░░]   901/5,987  │
│  🔧 Method Coverage:      22.94%  [████░░░░░░] 1,222/5,328  │
│  📝 Instruction Coverage: 21.59%  [████░░░░░░]              │
│                                                              │
│  📦 Total Classes:        423                                │
│  ✅ Tests Passed:         578/590                            │
│  ❌ Tests Failed:         12                                 │
│  ⏭️  Tests Skipped:        2                                 │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

## Top 5 Best Covered Packages

| Rank | Package | Line Coverage |
|------|---------|---------------|
| 🥇 | `annotation` | **100.00%** |
| 🥈 | `mapper` | **100.00%** |
| 🥉 | `service.ai` | **85.68%** |
| 4️⃣ | `audit` | **72.87%** |
| 5️⃣ | `common.enums` | **67.78%** |

## Top 5 Critical Files Needing Tests

| Rank | File | Coverage | Priority |
|------|------|----------|----------|
| ⚠️ | `RestaurantOwnerController.java` | 3.59% | 🔴 CRITICAL |
| ⚠️ | `PaymentController.java` | 0.31% | 🔴 CRITICAL |
| ⚠️ | `ChatService.java` | 0.37% | 🔴 CRITICAL |
| ⚠️ | `RestaurantOwnerService.java` | 3.65% | 🔴 CRITICAL |
| ⚠️ | `ChatMessageController.java` | 0.39% | 🟠 HIGH |

## Failed Tests

**All 12 failures in:** `BookingConflictServiceTest`

**Issue:** Restaurant operating hours validation
```
Error: Nhà hàng đóng cửa: Nhà hàng chỉ hoạt động từ 10:00 đến 22:00
```

**Fix:** Update test data to use booking times within operating hours (10:00-22:00)

## Coverage by Layer

```
Service Layer:     24.71% ████░░░░░░░░░░░░░░░░
Controller Layer:  11.48% ██░░░░░░░░░░░░░░░░░░
Domain/Entity:     38.44% ███████░░░░░░░░░░░░░
Config:            43.02% ████████░░░░░░░░░░░░
DTO:               3.54%  ░░░░░░░░░░░░░░░░░░░░
```

## Next Steps

### This Week
- [ ] Fix 12 failing tests
- [ ] Add payment flow tests
- [ ] Add concurrent booking tests

### Next Sprint
- [ ] Increase payment coverage to 60%
- [ ] Add restaurant owner tests
- [ ] Add integration tests

### Goal
- **Target:** 35% line coverage by end of next sprint
- **Long-term:** 80% line coverage

## Quick Commands

```bash
# Run tests with coverage
mvn clean test

# Generate report
mvn jacoco:report

# Open HTML report
start target/site/jacoco/index.html   # Windows
open target/site/jacoco/index.html    # Mac/Linux
```

## Report Files

- 📄 Detailed Report: [COVERAGE_REPORT_2024.md](./COVERAGE_REPORT_2024.md)
- 🌐 HTML Report: [target/site/jacoco/index.html](./target/site/jacoco/index.html)
- 📊 CSV Data: [target/site/jacoco/jacoco.csv](./target/site/jacoco/jacoco.csv)
- 📋 XML Report: [target/site/jacoco/jacoco.xml](./target/site/jacoco/jacoco.xml)

---

**Generated:** October 30, 2024  
**Tool:** JaCoCo 0.8.11  
**Next Review:** November 13, 2024


