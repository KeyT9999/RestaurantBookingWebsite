# 🚀 Quick Start: JaCoCo Coverage Reports

## 📋 Cách Xem Reports

### Windows PowerShell

```powershell
# Generate reports
mvn test jacoco:report

# Mở Full Report
start target\site\jacoco\index.html

# Mở Tier 1 Report (18 lớp ưu tiên)
start target\site\jacoco-tier1\index.html
```

### Mac/Linux Terminal

```bash
# Generate reports
mvn test jacoco:report

# Mở Full Report
open target/site/jacoco/index.html

# Mở Tier 1 Report
open target/site/jacoco-tier1/index.html
```

---

## 🎯 Hai Loại Reports

### 1️⃣ Full Report → `target/site/jacoco/index.html`

- **Bao gồm**: Tất cả code trong project
- **Coverage**: Thấp (~8-9%) vì có nhiều code chưa test
- **Sử dụng**: Overview tổng quan

### 2️⃣ Tier 1 Report → `target/site/jacoco-tier1/index.html`

- **Bao gồm**: Chỉ 18 lớp quan trọng nhất
- **Coverage**: 42% instruction / **38% BRANCH**
- **Sử dụng**: **Focus vào các lớp này** ✅

---

## 📊 Tier 1 Classes (18 Lớp)

### ✅ Passed (≥80% BRANCH)

```
✅ BookingConflictService     79% ✅
✅ RestaurantSecurityService   70% ✅
✅ PaymentService              65% ⚠️
```

### 🔴 Needs More Tests

```
🔴 BookingService              41%
🔴 PayOsService                44%
🔴 WithdrawalService           38%
🔴 WaitlistService             37%
🔴 AdvancedRateLimitingService 23%
🔴 RefundService               29%
```

**Mục tiêu**: Đạt **≥80% BRANCH** cho tất cả classes

---

## 📈 Đọc Report

### Header Metrics

| Metric | Ý Nghĩa |
|--------|---------|
| **Cov. (Instructions)** | % dòng code đã chạy |
| **Cov. (Branches)** | % nhánh if/else đã test |
| **Missed Lines** | Số dòng chưa test |
| **Missed Methods** | Số method chưa test |

### Color Code

- 🟢 **Green**: Covered (>90%)
- 🟡 **Yellow**: Partially (50-90%)
- 🟠 **Orange**: Low (25-50%)
- 🔴 **Red**: Very low (<25%)

---

## 🔍 Chi Tiết Class

1. Click vào tên class trong bảng
2. Xem source code với màu sắc
3. **Red lines** = chưa covered → cần thêm test
4. **Green lines** = đã covered ✅

---

## ⚡ Quick Commands

```bash
# Generate + open Tier 1 report
mvn test jacoco:report && start target\site\jacoco-tier1\index.html

# Chạy test lại sau khi thêm test cases
mvn test jacoco:report

# Xem full coverage
start target\site\jacoco\index.html

# Xem Tier 1 coverage (recommended)
start target\site\jacoco-tier1\index.html
```

---

## 📖 Tài Liệu Chi Tiết

- **[HOW_TO_VIEW_JACOCO_REPORTS.md](HOW_TO_VIEW_JACOCO_REPORTS.md)** - Hướng dẫn chi tiết
- **[VIETQR_TEST_CONFIGURATION.md](VIETQR_TEST_CONFIGURATION.md)** - Cấu hình VietQR tests
- **[VIETQR_TEST_PATCH_SUMMARY.md](VIETQR_TEST_PATCH_SUMMARY.md)** - Summary VietQR patch

---

**Last Updated**: 2025-10-31  
**JaCoCo Version**: 0.8.12  
**Test Suite**: 711 tests (7 skipped)

