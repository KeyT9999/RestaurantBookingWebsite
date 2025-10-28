# PowerShell Maven Command - Lỗi và Cách Sửa

## 🔴 Lỗi gặp phải

```
[ERROR] Unknown lifecycle phase ".profiles.active=test". You must specify a valid lifecycle phase or a goal...
```

---

## 🔍 Nguyên nhân

**PowerShell** xử lý dấu chấm `.` trong parameter một cách đặc biệt, khiến Maven nhận sai parameter.

### Command gây lỗi:
```powershell
mvn test -Dtest=WithdrawalServiceTest -Dspring.profiles.active=test
```

**PowerShell parse thành:**
- `-Dtest=WithdrawalServiceTest` ✅
- `-Dspring` ❌ (thiếu value)
- `.profiles.active=test` ❌ (Maven hiểu đây là lifecycle phase)

---

## ✅ Giải pháp

### Cách 1: Dùng Dấu Ngoặc Kép (Recommended cho PowerShell)

```powershell
mvn test "-Dtest=WithdrawalServiceTest" "-Dspring.profiles.active=test"
```

### Cách 2: Dùng Batch File (Easiest)

Tạo file `run_tests.bat`:
```batch
@echo off
mvn test "-Dtest=WithdrawalServiceTest" "-Dspring.profiles.active=test"
pause
```

Chạy:
```powershell
.\run_tests.bat
```

### Cách 3: Chuyển sang CMD

```cmd
cmd /c "mvn test -Dtest=WithdrawalServiceTest -Dspring.profiles.active=test"
```

### Cách 4: Dùng Git Bash

```bash
mvn test -Dtest=WithdrawalServiceTest -Dspring.profiles.active=test
```

---

## 📝 So sánh Shell

| Shell | Command | Cần Quotes? |
|-------|---------|-------------|
| **PowerShell** | `mvn test "-Dtest=..." "-Dspring.profiles.active=test"` | ✅ Yes |
| **CMD** | `mvn test -Dtest=... -Dspring.profiles.active=test` | ❌ No |
| **Git Bash** | `mvn test -Dtest=... -Dspring.profiles.active=test` | ❌ No |
| **Linux/Mac** | `mvn test -Dtest=... -Dspring.profiles.active=test` | ❌ No |

---

## 🎯 Best Practices

### 1. Luôn dùng Quotes trong PowerShell cho `-D` parameters

```powershell
# ❌ SAI
mvn test -Dspring.profiles.active=test

# ✅ ĐÚNG
mvn test "-Dspring.profiles.active=test"
```

### 2. Escape ký tự đặc biệt trong PowerShell

```powershell
# Nested tests ($ cần escape)
mvn test "-Dtest=WithdrawalServiceTest`$RequestWithdrawalTests"
```

### 3. Tạo Batch Scripts cho commands phức tạp

Thay vì gõ lại command dài, tạo `.bat` files:

**run_withdrawal_service_tests.bat:**
```batch
@echo off
echo Running WithdrawalService Tests...
mvn test "-Dtest=WithdrawalServiceTest" "-Dspring.profiles.active=test"
if %errorlevel% neq 0 (
    echo Tests FAILED!
    pause
    exit /b %errorlevel%
) else (
    echo All tests PASSED!
)
pause
```

---

## 🚀 WithdrawalService Commands - Fixed

### Run All Tests (PowerShell)
```powershell
mvn test "-Dtest=WithdrawalServiceTest" "-Dspring.profiles.active=test"
```

### Run Specific Test Classes (PowerShell)
```powershell
# Request tests
mvn test "-Dtest=WithdrawalServiceTest`$RequestWithdrawalTests" "-Dspring.profiles.active=test"

# Process tests
mvn test "-Dtest=WithdrawalServiceTest`$ProcessWithdrawalTests" "-Dspring.profiles.active=test"

# Reject tests
mvn test "-Dtest=WithdrawalServiceTest`$RejectWithdrawalTests" "-Dspring.profiles.active=test"

# History tests
mvn test "-Dtest=WithdrawalServiceTest`$GetWithdrawalHistoryTests" "-Dspring.profiles.active=test"
```

### Run with Coverage (PowerShell)
```powershell
mvn clean test jacoco:report "-Dtest=WithdrawalServiceTest" "-Dspring.profiles.active=test"
```

---

## 🎓 Tại sao PowerShell khác?

PowerShell là một **object-based shell**, không phải text-based như CMD hay Bash.

### PowerShell parsing rules:
1. **Dấu chấm `.`** = property access
2. **Dấu `$`** = variable
3. **Khoảng trắng** = argument separator

Vì vậy `-Dspring.profiles.active=test` được hiểu là:
- `-Dspring` (parameter name)
- `.profiles` (property access)
- `.active=test` (another property)

**Quotes `"..."` bảo PowerShell treat toàn bộ string as-is.**

---

## ✅ Kết luận

**Recommended approach cho Windows:**
1. ✅ **Dùng Batch Scripts** - Đơn giản, không lỗi
2. ✅ **Quotes trong PowerShell** - Khi cần gõ trực tiếp
3. ❌ **Avoid PowerShell without quotes** - Sẽ lỗi với `-D` parameters

---

**Date:** 28/10/2025  
**Issue:** PowerShell Maven parameter parsing  
**Status:** ✅ Resolved

