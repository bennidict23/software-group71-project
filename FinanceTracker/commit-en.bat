@echo off
echo ======================================
echo GitHub Auto Commit - Finance Analysis Module
echo ======================================

set /p COMMIT_MESSAGE="Enter commit message: "

echo.
echo Adding all changes...
git add .

echo.
echo Committing changes...
git commit -m "%COMMIT_MESSAGE%"

echo.
echo Pushing to GitHub (branch: ChubinZhang)...
git push origin ChubinZhang

echo.
echo Commit completed!
echo ======================================
pause 