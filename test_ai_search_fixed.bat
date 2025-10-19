@echo off
echo Testing AI Search API...
echo.

echo Sending request to: http://localhost:8080/ai/restaurants/search
echo Query: Tôi muốn ăn sushi gần đây
echo.

powershell -Command "$body = @{query='Tôi muốn ăn sushi gần đây'} | ConvertTo-Json; try { $response = Invoke-RestMethod -Uri 'http://localhost:8080/ai/restaurants/search' -Method Post -Body $body -ContentType 'application/json'; Write-Host 'SUCCESS!'; Write-Host 'Total Found:' $response.totalFound; Write-Host 'Explanation:' $response.explanation; if ($response.recommendations) { Write-Host 'Recommendations:' $response.recommendations.Count } } catch { Write-Host 'ERROR:' $_.Exception.Message; Write-Host 'Status:' $_.Exception.Response.StatusCode }"

echo.
echo Test completed!
pause
