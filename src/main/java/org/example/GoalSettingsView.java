package org.example;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import org.example.DashboardView;
import org.example.User;
import org.example.UserManager;

public class GoalSettingsView {

    private final User currentUser;
    private final UserManager userManager;

    public GoalSettingsView(User currentUser, UserManager userManager) {
        this.currentUser = currentUser;
        this.userManager = userManager;
    }

    public void showGoalSettings(Stage primaryStage) {
        // 创建年度储蓄进度展示区域
        ProgressBar annualProgressBar = new ProgressBar();
        annualProgressBar.setPrefWidth(550);
        Label annualProgressLabel = new Label();
        VBox annualProgressBox = new VBox(5, annualProgressBar, annualProgressLabel);
        annualProgressBox.setAlignment(Pos.CENTER);
        annualProgressBox.setPadding(new Insets(10));

        // 创建月度储蓄进度展示区域
        ProgressBar monthlyProgressBar = new ProgressBar();
        monthlyProgressBar.setPrefWidth(550);
        Label monthlyProgressLabel = new Label();
        VBox monthlyProgressBox = new VBox(5, monthlyProgressBar, monthlyProgressLabel);
        monthlyProgressBox.setAlignment(Pos.CENTER);
        monthlyProgressBox.setPadding(new Insets(10));

        // 创建年度目标设置区域
        Label annualLabel = new Label("Annual Target");
        TextField annualField = new TextField(String.valueOf(currentUser.getAnnualTarget()));
        Button annualSetButton = new Button("SET");
        annualSetButton.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-pref-height: 40px;");

        // 设置输入框和按钮的宽度
        double inputWidth = 200;
        annualField.setPrefWidth(inputWidth);
        annualSetButton.setPrefWidth(inputWidth);

        VBox annualBox = new VBox(10, annualLabel, annualField, annualSetButton);
        annualBox.setPadding(new Insets(20));
        annualBox.setStyle("-fx-border-color: gray; -fx-border-width: 2px; -fx-border-radius: 5px; -fx-padding: 20px;");
        annualBox.setAlignment(Pos.CENTER);

        // 设置标签居中
        annualLabel.setAlignment(Pos.BASELINE_LEFT);

        // 创建月目标设置区域
        Label monthlyLabel = new Label("Monthly Target");
        TextField monthlyField = new TextField(String.valueOf(currentUser.getMonthlyTarget()));
        Button monthlySetButton = new Button("SET");
        monthlySetButton.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-pref-height: 40px;");

        // 设置输入框和按钮的宽度
        monthlyField.setPrefWidth(inputWidth);
        monthlySetButton.setPrefWidth(inputWidth);

        VBox monthlyBox = new VBox(10, monthlyLabel, monthlyField, monthlySetButton);
        monthlyBox.setPadding(new Insets(20));
        monthlyBox.setStyle("-fx-border-color: gray; -fx-border-width: 2px; -fx-border-radius: 5px; -fx-padding: 20px;");
        monthlyBox.setAlignment(Pos.CENTER);

        // 设置标签居中
        monthlyLabel.setAlignment(Pos.BASELINE_LEFT);

        // 将年度和月目标设置区域组合到一起
        HBox inputRow = new HBox(20, annualBox, monthlyBox);
        inputRow.setAlignment(Pos.CENTER);
        inputRow.setPadding(new Insets(10));

        // 创建返回仪表盘按钮
        Button backToDashboardButton = new Button("Back to Dashboard");
        backToDashboardButton.setOnAction(e -> {
            DashboardView dashboardView = new DashboardView();
            try {
                dashboardView.start(primaryStage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        VBox container = new VBox(10, annualProgressBox, monthlyProgressBox, inputRow, backToDashboardButton);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(10));

        Scene scene = new Scene(container, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.show();

        // 初始化进度条
        updateProgressBar(annualProgressBar, annualProgressLabel,
                currentUser.getAnnualSavedAmount(),
                currentUser.getAnnualTarget(),
                "Annual");

        updateProgressBar(monthlyProgressBar, monthlyProgressLabel,
                currentUser.getSavedAmount(),
                currentUser.getMonthlyTarget(),
                "Monthly");

        // 设置年度目标设置按钮的点击逻辑
        annualSetButton.setOnAction(e -> {
            try {
                double newAnnual = Double.parseDouble(annualField.getText());
                if (newAnnual <= 0) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Annual value must be positive.");
                    return;
                }
                currentUser.setAnnualTarget(newAnnual);
                updateProgressBar(annualProgressBar, annualProgressLabel,
                        currentUser.getAnnualSavedAmount(),
                        newAnnual,
                        "Annual");
                showAlert(Alert.AlertType.INFORMATION, "Success", "Annual target updated to: " + newAnnual);
                userManager.saveUserSettings(currentUser);
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Please enter a valid annual target number.");
            }
        });

        // 设置月目标设置按钮的点击逻辑
        monthlySetButton.setOnAction(e -> {
            try {
                double newMonthly = Double.parseDouble(monthlyField.getText());
                if (newMonthly <= 0) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Monthly value must be positive.");
                    return;
                }
                currentUser.setMonthlyTarget(newMonthly);
                updateProgressBar(monthlyProgressBar, monthlyProgressLabel,
                        currentUser.getSavedAmount(),
                        newMonthly,
                        "Monthly");
                showAlert(Alert.AlertType.INFORMATION, "Success", "Monthly target updated to: " + newMonthly);
                userManager.saveUserSettings(currentUser);
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Please enter a valid monthly target number.");
            }
        });
    }

    private void updateProgressBar(ProgressBar progressBar, Label progressLabel,
                                   double savedAmount, double target, String period) {
        double ratio = target == 0 ? 0.0 : savedAmount / target; // 避免除数为0
        double displayRatio = Math.min(ratio, 1.0); // 限制最大进度为100%
        int percent = (int) (displayRatio * 100);

        if (ratio >= 1.0) {
            // 达标时显示已攒金额和达标提示
            progressLabel.setText(period + " Savings Goal: " + percent + "% (" + savedAmount + " saved - Goal Reached!)");
            progressLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
        } else {
            // 未达标时显示进度和已攒金额
            progressLabel.setText(period + " Savings Progress: " + percent + "% (" + savedAmount + " saved)");
            progressLabel.setStyle("-fx-text-fill: black;");
        }
        progressBar.setProgress(displayRatio);
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}