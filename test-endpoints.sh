#!/bin/bash

# Test Endpoints Script for Restaurant Booking App
# Usage: ./test-endpoints.sh [dev|prod]

PROFILE=${1:-dev}
BASE_URL="http://localhost:8080"

echo "🧪 Testing endpoints for profile: $PROFILE"
echo "Base URL: $BASE_URL"
echo "================================"

# Test Health Check
echo "1️⃣ Testing Health Check:"
curl -s "$BASE_URL/actuator/health" | jq . || echo "Health endpoint failed"
echo ""

# Test Home Page
echo "2️⃣ Testing Home Page:"
curl -s -I "$BASE_URL/" | head -n 1
echo ""

# Test Login Page
echo "3️⃣ Testing Login Page:"
curl -s -I "$BASE_URL/login" | head -n 1
echo ""

# Test Register Page
echo "4️⃣ Testing Register Page:"
curl -s -I "$BASE_URL/auth/register" | head -n 1
echo ""

# Test H2 Console (only for dev profile)
if [ "$PROFILE" = "dev" ]; then
    echo "5️⃣ Testing H2 Console (dev only):"
    curl -s -I "$BASE_URL/h2-console" | head -n 1
    echo ""
    echo "📝 H2 Console Access Info:"
    echo "   URL: $BASE_URL/h2-console"
    echo "   JDBC URL: jdbc:h2:mem:devdb"
    echo "   Username: sa"
    echo "   Password: (leave empty)"
fi

echo "✅ Test completed!"
echo ""
echo "🔗 Quick Links:"
echo "   App: $BASE_URL"
echo "   Login: $BASE_URL/login"
echo "   Register: $BASE_URL/auth/register"
echo "   Health: $BASE_URL/actuator/health"
if [ "$PROFILE" = "dev" ]; then
    echo "   H2 Console: $BASE_URL/h2-console"
fi 