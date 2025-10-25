#!/bin/bash

echo "========================================"
echo "   CHẠY 3 TEST CASES TỐT NHẤT"
echo "   BOOKING SERVICE DEMO"
echo "========================================"
echo

echo "[1/3] Happy Path Test - Luồng tạo booking thành công"
echo "========================================"
mvn test -Dtest=BookingServiceTest#testCreateBooking_WithValidData_ShouldSuccess
echo

echo "[2/3] Error Handling Test - Customer không tồn tại"
echo "========================================"
mvn test -Dtest=BookingServiceTest#testCreateBooking_WithCustomerNotFound_ShouldThrowException
echo

echo "[3/3] Business Logic Test - Tính toán tổng tiền"
echo "========================================"
mvn test -Dtest=BookingServiceTest#testCalculateTotalAmount_WithOnlyDeposit_ShouldReturnDepositAmount
echo

echo "========================================"
echo "   HOÀN THÀNH DEMO 3 TEST CASES"
echo "========================================"
