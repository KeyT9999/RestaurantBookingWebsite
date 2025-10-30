# Hướng Dẫn Xem JaCoCo Coverage Reports

## 📊 Tổng Quan

Project có **3 loại JaCoCo reports** khác nhau để theo dõi coverage:

1. **Full Report** - Bao quát toàn bộ project
2. **Tier 1 Report** - Chỉ 18 lớp ưu tiên cao nhất
3. **Package Reports** - Báo cáo chi tiết theo từng package

---

## 🎯 Access Reports

### 1. Full Coverage Report (Toàn Bộ Project)

**Path**: `target/site/jacoco/index.html`

**Command để generate**:
```bash
mvn test jacoco:report
```

**Bao gồm**: Tất cả các lớp trong project

**Sử dụng khi**: 
- Cần overview tổng quan về coverage toàn project
- Kiểm tra coverage tổng thể của tất cả packages

---

### 2. Tier 1 Report (18 Lớp Ưu Tiên)

**Path**: `target/site/jacoco-tier1/index.html`

**Command để generate**:
```bash
mvn test jacoco:report
```
*(Tự động generate cùng lúc với full report)*

**Bao gồm** chỉ 18 lớp Tier 1:
- ✅ BookingService
- ✅ BookingController
- ✅ PaymentService
- ✅ PayOsService
- ✅ RestaurantManagementService
- ✅ RestaurantDashboardService
- ✅ RestaurantOwnerService
- ✅ RestaurantRegistrationController
- ✅ AdminRestaurantController
- ✅ AdminDashboardController
- ✅ AdvancedRateLimitingService
- ✅ RestaurantSecurityService
- ✅ RefundService
- ✅ WithdrawalService
- ✅ WaitlistService
- ✅ CustomerService
- ✅ BookingConflictService
- ✅ NotificationService

**Sử dụng khi**:
- Cần focus vào các lớp quan trọng nhất
- Review coverage cho Tier 1 classes
- Tracking tiến độ coverage ≥80% BRANCH

---

### 3. Package-Specific Reports

**Path**: `target/site/jacoco/<package-name>/index.html`

**Ví dụ**:
- `target/site/jacoco/com.example.booking.service/index.html`
- `target/site/jacoco/com.example.booking.web.controller/index.html`

**Sử dụng khi**:
- Cần chi tiết coverage của một package cụ thể
- Review từng service/controller riêng biệt

---

## 🔍 Cách Đọc Report

### Header Metrics (Tổng Quan)

| Metric | Ý Nghĩa |
|--------|---------|
| **Missed Instructions** | Số lệnh chưa được test chạy qua |
| **Cov. (Instructions)** | % dòng code đã chạy |
| **Missed Branches** | Số nhánh if/else chưa test |
| **Cov. (Branches)** | % nhánh logic đã test |
| **Missed Lines** | Số dòng code chưa test |
| **Missed Methods** | Số method chưa test |
| **Missed Classes** | Số class chưa test |

### Mục Tiêu Coverage

| Tier | BRANCH Coverage Target |
|------|------------------------|
| **Tier 1** | ≥ **80%** |
| **Tier 2** | ≥ **80%** (sau khi Tier 1 đạt) |
| **Tier 3** | ≥ **60%** |

---

## 📈 Ví Dụ Kết Quả

### Full Report (Hiện Tại)

**Tổng Coverage**: ~8-9%
- Rất thấp vì bao gồm tất cả code
- Nhiều lớp không có test

**Sử dụng**: Overview tổng quan

### Tier 1 Report (Hiện Tại)

**Coverage**: 42% instruction, **38% BRANCH**

**Top Performers**:
- ✅ BookingConflictService: **79% BRANCH**
- ✅ PaymentService: **65% BRANCH**
- ✅ RestaurantSecurityService: **70% BRANCH**

**Cần Cải Thiện**:
- ⚠️ BookingService: 41% BRANCH (thiếu 39%)
- ⚠️ PayOsService: 44% BRANCH (thiếu 36%)
- ⚠️ WithdrawalService: 38% BRANCH (thiếu 42%)
- ⚠️ WaitlistService: 37% BRANCH (thiếu 43%)
- ⚠️ AdvancedRateLimitingService: 23% BRANCH (thiếu 57%)

---

## 🚀 Quick Start

### 1. Generate Reports

```bash
# Chạy test + generate reports
mvn test jacoco:report

# Chỉ generate report (không chạy test lại)
mvn jacoco:report
```

### 2. Mở Reports

#### Windows
```powershell
# Full report
start target\site\jacoco\index.html

# Tier 1 report
start target\site\jacoco-tier1\index.html
```

#### Mac/Linux
```bash
# Full report
open target/site/jacoco/index.html

# Tier 1 report
open target/site/jacoco-tier1/index.html
```

---

## 📋 Workflow Khuyến Nghị

### Bước 1: Check Tier 1 Report

```bash
# Generate report
mvn test jacoco:report

# Open Tier 1 report
start target\site\jacoco-tier1\index.html
```

### Bước 2: Xác định lớp cần test

- Xem bảng coverage
- Tìm lớp có BRANCH < 80%
- Click vào tên lớp để xem chi tiết

### Bước 3: Review Source Code

- Click vào file HTML của lớp
- Xem source code với màu sắc
- **Green**: Đã covered
- **Red**: Chưa covered
- **Yellow**: Partially covered

### Bước 4: Viết Test Cases

- Xem các nhánh chưa covered (red lines)
- Viết test cho các nhánh đó
- Chạy lại test + report

### Bước 5: Verify

```bash
mvn test jacoco:report
start target\site\jacoco-tier1\index.html
```

---

## 🎨 Color Code

| Màu | Ý Nghĩa |
|-----|---------|
| 🟢 **Green** | Hoàn toàn covered (> 90%) |
| 🟡 **Yellow** | Partially covered (50-90%) |
| 🟠 **Orange** | Low coverage (25-50%) |
| 🔴 **Red** | Very low coverage (< 25%) |

---

## 📦 Files Generated

```
target/site/
├── jacoco/                    # Full report
│   ├── index.html            # ← Full report entry
│   ├── com.example.booking/
│   │   ├── service/          # Service package report
│   │   └── web.controller/   # Controller package report
│   └── jacoco-resources/     # CSS, JS, images
│
└── jacoco-tier1/             # Tier 1 report
    ├── index.html            # ← Tier 1 report entry
    ├── com.example.booking/
    │   ├── service/          # Tier 1 services only
    │   └── web.controller/   # Tier 1 controllers only
    └── jacoco-resources/     # CSS, JS, images
```

---

## 🔍 Advanced: Package-Level Deep Dive

### Xem Chi Tiết Service Package

**Path**: `target/site/jacoco/com.example.booking.service/index.html`

**Shows**:
- Coverage của từng service class
- Sorted theo coverage (highest first)
- Direct links đến class reports

### Xem Chi Tiết Một Class

**Path**: `target/site/jacoco/com.example.booking.service/BookingService.html`

**Shows**:
- Source code với line-by-line coverage
- Green/Red highlighting
- Branch coverage indicators
- Click vào line number để xem details

---

## 💡 Tips

1. **Always check Tier 1 first** - Đó là priority
2. **Focus on BRANCH coverage** - Quan trọng hơn instruction
3. **Use source view** - Click vào class để xem code chưa covered
4. **Compare before/after** - Track progress sau mỗi batch tests
5. **Check skipped tests** - 7 tests bị skip (VietQR), không ảnh hưởng

---

## ⚙️ Configuration

### Current JaCoCo Settings

**File**: `pom.xml`

**Version**: `0.8.12`

**Reports Generated**:
- ✅ Full report (tất cả code)
- ✅ Tier 1 report (18 lớp priority)
- ✅ Package reports (auto-generated)

**Thresholds**:
- **LINE**: ≥ 60%
- **BRANCH**: ≥ 50% (overall)
- **Tier 1 Target**: ≥ 80% BRANCH

---

## 📊 Current Status

### Tier 1 Summary (Latest)

```
Total Coverage:       42% instruction / 38% BRANCH
Tier 1 Classes:       18 classes

Passing (≥80% BRANCH):
✅ BookingConflictService     79% ✅
✅ RestaurantSecurityService   70% ⚠️ (thiếu 10%)
⚠️ PaymentService              65% ⚠️ (thiếu 15%)

Need More Tests:
🔴 BookingService              41%
🔴 PayOsService                44%
🔴 WithdrawalService           38%
🔴 WaitlistService             37%
🔴 AdvancedRateLimitingService 23%
🔴 RefundService               29%
```

**Action Required**: Expand tests cho 6 classes trên để đạt ≥80% BRANCH

---

## 🎯 Next Steps

1. ✅ View Tier 1 report: `target/site/jacoco-tier1/index.html`
2. ⏭️ Identify low coverage classes
3. ⏭️ Write comprehensive tests
4. ⏭️ Verify coverage improvement
5. ⏭️ Repeat until ≥80% BRANCH

---

**Last Updated**: 2025-10-31  
**JaCoCo Version**: 0.8.12  
**Test Framework**: JUnit 5 + Mockito

