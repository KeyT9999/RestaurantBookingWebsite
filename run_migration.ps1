# PowerShell script to run database migration
# This script will execute the migration to add missing columns to restaurant_profile table

Write-Host "Starting database migration..." -ForegroundColor Green

# Database connection parameters
$DB_HOST = "localhost"
$DB_PORT = "5432"
$DB_NAME = "book_eat_db"
$DB_USER = "postgres"

# Migration file path
$MIGRATION_FILE = "fix_restaurant_profile_migration.sql"

# Check if psql is available
try {
    $psqlVersion = psql --version 2>$null
    Write-Host "PostgreSQL client found: $psqlVersion" -ForegroundColor Green
} catch {
    Write-Host "PostgreSQL client (psql) not found. Please install PostgreSQL client tools." -ForegroundColor Red
    Write-Host "You can download from: https://www.postgresql.org/download/" -ForegroundColor Yellow
    exit 1
}

# Check if migration file exists
if (-not (Test-Path $MIGRATION_FILE)) {
    Write-Host "Migration file not found: $MIGRATION_FILE" -ForegroundColor Red
    exit 1
}

Write-Host "Executing migration: $MIGRATION_FILE" -ForegroundColor Yellow

# Run the migration
try {
    # Set PGPASSWORD environment variable (you may need to change this)
    $env:PGPASSWORD = "your_password_here"  # Replace with your actual password
    
    # Execute the migration
    $result = psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -f $MIGRATION_FILE 2>&1
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Migration completed successfully!" -ForegroundColor Green
        Write-Host $result -ForegroundColor White
    } else {
        Write-Host "Migration failed with exit code: $LASTEXITCODE" -ForegroundColor Red
        Write-Host $result -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "Error executing migration: $_" -ForegroundColor Red
    exit 1
} finally {
    # Clear password from environment
    Remove-Item Env:PGPASSWORD -ErrorAction SilentlyContinue
}

Write-Host "Migration script completed." -ForegroundColor Green
