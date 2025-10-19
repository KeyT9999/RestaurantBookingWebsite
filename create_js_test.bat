@echo off
echo Testing JavaScript functionality...
echo.

echo Creating test HTML file...
echo ^<!DOCTYPE html^> > test_js.html
echo ^<html^> >> test_js.html
echo ^<head^> >> test_js.html
echo ^<title^>AI Search Test^</title^> >> test_js.html
echo ^</head^> >> test_js.html
echo ^<body^> >> test_js.html
echo ^<h1^>AI Search Test^</h1^> >> test_js.html
echo ^<input type="text" id="aiSearchQuery" placeholder="Enter your query"^> >> test_js.html
echo ^<button type="button" id="aiSearchBtn"^>Test Search^</button^> >> test_js.html
echo ^<div id="aiSearchResults"^>Results will appear here^</div^> >> test_js.html
echo ^<script^> >> test_js.html
echo console.log('Test script loaded'); >> test_js.html
echo document.getElementById('aiSearchBtn').addEventListener('click', function() { >> test_js.html
echo   console.log('Button clicked!'); >> test_js.html
echo   const query = document.getElementById('aiSearchQuery').value; >> test_js.html
echo   console.log('Query:', query); >> test_js.html
echo   document.getElementById('aiSearchResults').innerHTML = 'Button works! Query: ' + query; >> test_js.html
echo }); >> test_js.html
echo ^</script^> >> test_js.html
echo ^</body^> >> test_js.html
echo ^</html^> >> test_js.html

echo Test HTML file created: test_js.html
echo Open this file in browser to test JavaScript functionality
pause
