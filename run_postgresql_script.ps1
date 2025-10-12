# Script PowerShell để chạy SQL PostgreSQL trên Windows

# Cấu hình
$DB_HOST = "localhost"
$DB_PORT = "5432"
$DB_NAME = "restaurant_booking"
$DB_USER = "postgres"
$DB_PASSWORD = "your_password"  # Thay đổi password của bạn

# Đường dẫn đến file SQL
$SQL_FILE = "database/rate_limiting_tables_postgresql.sql"

# Kiểm tra file SQL có tồn tại không
if (-not (Test-Path $SQL_FILE)) {
    Write-Host "❌ File SQL không tồn tại: $SQL_FILE" -ForegroundColor Red
    exit 1
}

Write-Host "🚀 Đang chạy SQL script cho PostgreSQL..." -ForegroundColor Green
Write-Host "📁 File: $SQL_FILE" -ForegroundColor Yellow
Write-Host "🗄️ Database: $DB_NAME" -ForegroundColor Yellow

# Cách 1: Sử dụng psql (nếu có trong PATH)
try {
    Write-Host "📝 Đang chạy với psql..." -ForegroundColor Blue
    
    # Set password environment variable
    $env:PGPASSWORD = $DB_PASSWORD
    
    # Chạy psql
    $result = & psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -f $SQL_FILE 2>&1
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Script chạy thành công!" -ForegroundColor Green
        Write-Host $result
    } else {
        Write-Host "❌ Lỗi khi chạy script:" -ForegroundColor Red
        Write-Host $result
    }
} catch {
    Write-Host "⚠️ psql không có trong PATH, thử cách khác..." -ForegroundColor Yellow
    
    # Cách 2: Sử dụng đường dẫn đầy đủ đến psql
    $PSQL_PATHS = @(
        "C:\Program Files\PostgreSQL\15\bin\psql.exe",
        "C:\Program Files\PostgreSQL\14\bin\psql.exe",
        "C:\Program Files\PostgreSQL\13\bin\psql.exe",
        "C:\Program Files\PostgreSQL\12\bin\psql.exe",
        "C:\Program Files\PostgreSQL\11\bin\psql.exe"
    )
    
    $psqlFound = $false
    
    foreach ($psqlPath in $PSQL_PATHS) {
        if (Test-Path $psqlPath) {
            Write-Host "📝 Tìm thấy psql tại: $psqlPath" -ForegroundColor Blue
            
            # Set password environment variable
            $env:PGPASSWORD = $DB_PASSWORD
            
            # Chạy psql với đường dẫn đầy đủ
            $result = & $psqlPath -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -f $SQL_FILE 2>&1
            
            if ($LASTEXITCODE -eq 0) {
                Write-Host "✅ Script chạy thành công!" -ForegroundColor Green
                Write-Host $result
                $psqlFound = $true
                break
            } else {
                Write-Host "❌ Lỗi khi chạy script:" -ForegroundColor Red
                Write-Host $result
            }
        }
    }
    
    if (-not $psqlFound) {
        Write-Host "❌ Không tìm thấy psql.exe" -ForegroundColor Red
        Write-Host "💡 Hướng dẫn thủ công:" -ForegroundColor Yellow
        Write-Host "1. Mở pgAdmin hoặc psql command line" -ForegroundColor White
        Write-Host "2. Kết nối đến database: $DB_NAME" -ForegroundColor White
        Write-Host "3. Chạy nội dung file: $SQL_FILE" -ForegroundColor White
        Write-Host "4. Hoặc copy-paste nội dung SQL vào pgAdmin Query Tool" -ForegroundColor White
    }
}

# Xóa password khỏi environment variable
Remove-Item Env:PGPASSWORD -ErrorAction SilentlyContinue

Write-Host "`n🎉 Hoàn thành! Bây giờ bạn có thể restart ứng dụng Spring Boot." -ForegroundColor Green
