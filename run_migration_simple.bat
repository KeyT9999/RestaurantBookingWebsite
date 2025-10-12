@echo off
echo Starting database migration...

REM Set your PostgreSQL password here
set PGPASSWORD=your_password_here

REM Run the migration
psql -h localhost -U postgres -d book_eat_db -f fix_restaurant_profile_migration.sql

REM Clear password
set PGPASSWORD=

echo Migration completed!
pause
