@echo off
echo Testing AI Restaurant Search...
echo.

echo Sending AI search request...
powershell -Command "try { $body = '{\"query\": \"Tôi muốn ăn sushi gần đây với 2 người\"}'; $response = Invoke-RestMethod -Uri 'http://localhost:8080/ai/restaurants/search' -Method Post -Body $body -ContentType 'application/json'; Write-Host 'SUCCESS!'; Write-Host 'Total Found:' $response.totalFound; Write-Host 'Explanation:' $response.explanation } catch { Write-Host 'ERROR:' $_.Exception.Message }"

echo.
echo Test completed!
pause
