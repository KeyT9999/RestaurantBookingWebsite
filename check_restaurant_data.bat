@echo off
echo Checking restaurant data in database...
echo.

powershell -Command "try { $response = Invoke-RestMethod -Uri 'http://localhost:8080/test/openai' -Method Get; Write-Host 'OpenAI Test:' $response.status } catch { Write-Host 'OpenAI Test Error:' $_.Exception.Message }"

echo.
echo Testing restaurant count...
powershell -Command "try { $response = Invoke-RestMethod -Uri 'http://localhost:8080/api/booking/restaurants' -Method Get; Write-Host 'Restaurants API Status:' $response.status; if ($response.data) { Write-Host 'Restaurants Count:' $response.data.Count } } catch { Write-Host 'Restaurants API Error:' $_.Exception.Message }"

echo.
echo Test completed!
pause
