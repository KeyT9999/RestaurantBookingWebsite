# Test script cho Refund API
# Chạy script này để test các tính năng refund và reconciliation

Write-Host "🚀 Starting Refund API Tests..." -ForegroundColor Green

# Configuration
$BASE_URL = "http://localhost:8080"
$TEST_PAYMENT_ID = 1  # Thay đổi ID này thành payment ID thực tế trong database

Write-Host "📋 Test Configuration:" -ForegroundColor Yellow
Write-Host "   Base URL: $BASE_URL"
Write-Host "   Test Payment ID: $TEST_PAYMENT_ID"
Write-Host ""

# Function để test API endpoint
function Test-RefundAPI {
    param(
        [string]$Method,
        [string]$Endpoint,
        [string]$Description,
        [hashtable]$Body = $null,
        [hashtable]$Headers = @{}
    )
    
    Write-Host "🧪 Testing: $Description" -ForegroundColor Cyan
    Write-Host "   $Method $Endpoint"
    
    try {
        $params = @{
            Uri = "$BASE_URL$Endpoint"
            Method = $Method
            Headers = $Headers
            ContentType = "application/json"
        }
        
        if ($Body) {
            $params.Body = ($Body | ConvertTo-Json)
        }
        
        $response = Invoke-RestMethod @params
        Write-Host "   ✅ Success: $($response | ConvertTo-Json -Compress)" -ForegroundColor Green
        return $response
    }
    catch {
        Write-Host "   ❌ Error: $($_.Exception.Message)" -ForegroundColor Red
        if ($_.Exception.Response) {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $responseBody = $reader.ReadToEnd()
            Write-Host "   Response: $responseBody" -ForegroundColor Red
        }
        return $null
    }
    Write-Host ""
}

# Test 1: Check refund eligibility
Write-Host "=" * 60 -ForegroundColor Magenta
Write-Host "TEST 1: Check Refund Eligibility" -ForegroundColor Magenta
Write-Host "=" * 60 -ForegroundColor Magenta

Test-RefundAPI -Method "GET" -Endpoint "/payment/$TEST_PAYMENT_ID/refund/check" -Description "Check if payment can be refunded"

# Test 2: Get refundable payments
Write-Host "=" * 60 -ForegroundColor Magenta
Write-Host "TEST 2: Get Refundable Payments" -ForegroundColor Magenta
Write-Host "=" * 60 -ForegroundColor Magenta

Test-RefundAPI -Method "GET" -Endpoint "/payment/refundable" -Description "Get list of refundable payments"

# Test 3: Process full refund
Write-Host "=" * 60 -ForegroundColor Magenta
Write-Host "TEST 3: Process Full Refund" -ForegroundColor Magenta
Write-Host "=" * 60 -ForegroundColor Magenta

Test-RefundAPI -Method "POST" -Endpoint "/payment/$TEST_PAYMENT_ID/refund?reason=Test%20full%20refund" -Description "Process full refund"

# Test 4: Process partial refund
Write-Host "=" * 60 -ForegroundColor Magenta
Write-Host "TEST 4: Process Partial Refund" -ForegroundColor Magenta
Write-Host "=" * 60 -ForegroundColor Magenta

Test-RefundAPI -Method "POST" -Endpoint "/payment/$TEST_PAYMENT_ID/refund/partial?amount=50000&reason=Test%20partial%20refund" -Description "Process partial refund"

# Test 5: Get refund info
Write-Host "=" * 60 -ForegroundColor Magenta
Write-Host "TEST 5: Get Refund Info" -ForegroundColor Magenta
Write-Host "=" * 60 -ForegroundColor Magenta

Test-RefundAPI -Method "GET" -Endpoint "/payment/$TEST_PAYMENT_ID/refund/info" -Description "Get refund information"

Write-Host "🎉 All tests completed!" -ForegroundColor Green
Write-Host ""
Write-Host "📝 Notes:" -ForegroundColor Yellow
Write-Host "   - Đảm bảo server đang chạy trên port 8080"
Write-Host "   - Thay đổi TEST_PAYMENT_ID thành ID thực tế"
Write-Host "   - Cần authentication token nếu có security"
Write-Host "   - Check logs để xem chi tiết reconciliation"
