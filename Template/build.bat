@echo off

echo Building extension frontend...
cd frontend
call npm run build:all
if %errorlevel% neq 0 exit /b %errorlevel%

echo Building extension backend...
cd ..
call mvn clean package
if %errorlevel% neq 0 exit /b %errorlevel%

echo Build completed, check "target" folder.
pause