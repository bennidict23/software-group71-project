@echo off
chcp 65001 >nul
echo ======================================
echo GitHub自动提交脚本 - 财务分析模块
echo ======================================

set /p COMMIT_MESSAGE="请输入提交信息: "

echo.
echo 正在添加所有更改...
git add .

echo.
echo 正在提交更改...
git commit -m "%COMMIT_MESSAGE%"

echo.
echo 正在推送到GitHub (分支:ChubinZhang)...
git push origin ChubinZhang

echo.
echo 提交完成!
echo ======================================
pause 