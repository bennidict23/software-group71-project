@echo off
echo Building the project...
call mvn clean package

echo Starting the Finance Tracker application...

:: 设置JavaFX的路径 - 使用Maven仓库
set JAVAFX_PATH=%USERPROFILE%\.m2\repository\org\openjfx

:: 运行主应用程序（添加调试信息）
echo Running with JavaFX path: %JAVAFX_PATH%
java -Djavafx.verbose=true --module-path "%JAVAFX_PATH%\javafx-controls\21.0.2;%JAVAFX_PATH%\javafx-base\21.0.2;%JAVAFX_PATH%\javafx-graphics\21.0.2;%JAVAFX_PATH%\javafx-fxml\21.0.2" --add-modules javafx.controls,javafx.fxml -cp target\classes com.financetracker.FinanceTrackerLauncher

echo Application closed. 