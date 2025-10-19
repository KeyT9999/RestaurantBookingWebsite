@echo off
echo Testing AI with simple question...
echo.

echo Sending request to: http://localhost:8080/test/openai/query
echo Query: Hello, are you working?
echo.

powershell -Command "try { $body = '{\"query\": \"Hello, are you working?\"}'; $response = Invoke-RestMethod -Uri 'http://localhost:8080/test/openai/query' -Method Post -Body $body -ContentType 'application/json'; Write-Host 'SUCCESS!'; Write-Host 'Status:' $response.status; Write-Host 'Response:' $response.response } catch { Write-Host 'ERROR:' $_.Exception.Message }"

echo.
echo Test completed!
pause