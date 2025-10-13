@echo off
echo 🚀 Testing Refund API with cURL commands
echo.

echo ⏳ Waiting for server to start...
timeout /t 10 /nobreak > nul

echo.
echo ============================================
echo TEST 1: Check Refund Eligibility
echo ============================================
curl -X GET "http://localhost:8080/payment/1/refund/check" ^
  -H "Content-Type: application/json" ^
  -w "\nHTTP Status: %%{http_code}\n"

echo.
echo ============================================
echo TEST 2: Get Refundable Payments
echo ============================================
curl -X GET "http://localhost:8080/payment/refundable" ^
  -H "Content-Type: application/json" ^
  -w "\nHTTP Status: %%{http_code}\n"

echo.
echo ============================================
echo TEST 3: Process Full Refund
echo ============================================
curl -X POST "http://localhost:8080/payment/1/refund?reason=Test%20full%20refund" ^
  -H "Content-Type: application/json" ^
  -w "\nHTTP Status: %%{http_code}\n"

echo.
echo ============================================
echo TEST 4: Process Partial Refund
echo ============================================
curl -X POST "http://localhost:8080/payment/1/refund/partial?amount=50000&reason=Test%20partial%20refund" ^
  -H "Content-Type: application/json" ^
  -w "\nHTTP Status: %%{http_code}\n"

echo.
echo ============================================
echo TEST 5: Get Refund Info
echo ============================================
curl -X GET "http://localhost:8080/payment/1/refund/info" ^
  -H "Content-Type: application/json" ^
  -w "\nHTTP Status: %%{http_code}\n"

echo.
echo 🎉 All tests completed!
echo.
echo 📝 Notes:
echo    - If you get 404, check if payment ID 1 exists
echo    - If you get 401, you may need authentication
echo    - Check server logs for detailed error messages
pause
