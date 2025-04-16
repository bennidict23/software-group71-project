@echo off
echo Building Finance Tracker application...
call mvn clean package
echo Starting Finance Tracker application...

:: 设置JavaFX的路径 - 尝试从Maven仓库获取
set JAVAFX_PATH=%USERPROFILE%\.m2\repository\org\openjfx

:: 运行应用程序并添加JavaFX模块
java --module-path "%JAVAFX_PATH%\javafx-controls\21.0.2;%JAVAFX_PATH%\javafx-fxml\21.0.2;%JAVAFX_PATH%\javafx-base\21.0.2;%JAVAFX_PATH%\javafx-graphics\21.0.2;%JAVAFX_PATH%\javafx-web\21.0.2;%JAVAFX_PATH%\javafx-swing\21.0.2" --add-modules javafx.controls,javafx.fxml,javafx.web,javafx.swing -jar target\finance-tracker-1.0-SNAPSHOT-jar-with-dependencies.jar

echo Application closed. 