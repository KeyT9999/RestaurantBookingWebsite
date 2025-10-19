@echo off
echo ========================================
echo    OpenAI API Key Test Script
echo ========================================
echo.

REM Check if .env file exists
if not exist .env (
    echo ❌ .env file not found!
    echo Please create .env file with your OpenAI API key:
    echo OPENAI_API_KEY=sk-your-openai-api-key-here
    echo AI_ENABLED=true
    echo AI_SEARCH_ENABLED=true
    pause
    exit /b 1
)

REM Check if API key is set
findstr /C:"OPENAI_API_KEY=" .env >nul
if errorlevel 1 (
    echo ❌ OPENAI_API_KEY not found in .env file!
    echo Please add: OPENAI_API_KEY=sk-your-openai-api-key-here
    pause
    exit /b 1
)

echo ✅ .env file found
echo.

REM Start the application
echo 🚀 Starting Spring Boot application...
echo.
echo Once the application starts, you can test with:
echo.
echo 1. Test OpenAI API Key:
echo    curl http://localhost:8080/test/openai
echo.
echo 2. Test Intent Parsing:
echo    curl http://localhost:8080/test/openai/intent
echo.
echo 3. Test Custom Query:
echo    curl -X POST http://localhost:8080/test/openai/query ^
echo         -H "Content-Type: application/json" ^
echo         -d "{\"query\": \"Tôi muốn ăn sushi\"}"
echo.
echo 4. Test AI Search:
echo    curl -X POST http://localhost:8080/ai/search ^
echo         -H "Content-Type: application/json" ^
echo         -d "{\"query\": \"Tôi muốn ăn sushi\", \"maxResults\": 3}"
echo.

mvn spring-boot:run
