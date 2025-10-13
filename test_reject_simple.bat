@echo off
echo Testing restaurant rejection...

echo Step 1: Check current status
psql -h localhost -U postgres -d bookeat_db -c "SELECT restaurant_id, restaurant_name, approval_status FROM restaurant_profile WHERE restaurant_id = 36;"

echo.
echo Step 2: Update to REJECTED status
psql -h localhost -U postgres -d bookeat_db -c "UPDATE restaurant_profile SET approval_status = 'REJECTED', rejection_reason = 'Test rejection', approved_by = 'admin', approved_at = NOW() WHERE restaurant_id = 36;"

echo.
echo Step 3: Check result
psql -h localhost -U postgres -d bookeat_db -c "SELECT restaurant_id, restaurant_name, approval_status, rejection_reason FROM restaurant_profile WHERE restaurant_id = 36;"

pause
