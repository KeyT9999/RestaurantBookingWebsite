# PowerShell script để liệt kê tất cả các file test sẽ chạy khi dùng mvn test

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "DANH SÁCH CÁC FILE TEST SẼ CHẠY" -ForegroundColor Cyan
Write-Host "Khi dùng lệnh: mvn test" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Tìm tất cả file test theo pattern của Maven Surefire
$testFiles = Get-ChildItem -Path "src\test\java" -Recurse -Filter "*Test.java" | Sort-Object FullName

Write-Host "Tổng số file test: $($testFiles.Count)" -ForegroundColor Green
Write-Host ""

# Hiển thị danh sách file
Write-Host "Danh sách các file test:" -ForegroundColor Yellow
Write-Host ""

$counter = 1
foreach ($file in $testFiles) {
    $relativePath = $file.FullName.Replace((Get-Location).Path + "\", "")
    $packageName = $relativePath.Replace("src\test\java\", "").Replace("\", ".").Replace(".java", "")
    Write-Host "$counter. $packageName" -ForegroundColor White
    Write-Host "   Path: $relativePath" -ForegroundColor Gray
    $counter++
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Quy tắc Maven Surefire Plugin:" -ForegroundColor Cyan
Write-Host "- **/Test*.java" -ForegroundColor Yellow
Write-Host "- **/*Test.java  (Pattern chính)" -ForegroundColor Yellow
Write-Host "- **/*Tests.java" -ForegroundColor Yellow
Write-Host "- **/*TestCase.java" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan



