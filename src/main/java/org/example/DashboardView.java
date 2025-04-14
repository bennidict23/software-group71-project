package org.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class DashboardView extends Application {

    // 静态变量存储当前用户的信息，由登录页面设置
    private static User currentUser;
    // 创建 UserManager 实例，用于操作 CSV 文件
    private UserManager userManager = new UserManager();

    // 假设本月预算与已存金额（可根据实际数据动态获取）
    private double monthlyBudget = 4000.0;
    private double savedAmount = 1500.0;

    private Label passwordLabel;

    // 登录时调用该方法设置当前用户
    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("User Dashboard");

        // 顶部标题
        Label titleLabel = new Label("Dashboard");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // 显示本月预算和存钱进度
        Label budgetLabel = new Label("Monthly Budget: $" + monthlyBudget);
        double progress = savedAmount / monthlyBudget;
        ProgressBar progressBar = new ProgressBar(progress);
        Label progressLabel = new Label("Savings Progress: " + (int)(progress * 100) + "% (" + savedAmount + " saved)");
        VBox budgetBox = new VBox(10, budgetLabel, progressBar, progressLabel);
        budgetBox.setPadding(new Insets(10));
        budgetBox.setStyle("-fx-border-color: gray; -fx-border-radius: 5px; -fx-padding: 10px;");

        // 显示用户个人信息：账户和密码
        Label accountLabel = new Label("Account: " + (currentUser != null ? currentUser.getUsername() : "N/A"));
        passwordLabel = new Label("Password: " + (currentUser != null ? currentUser.getPassword() : "N/A"));
        Button btnChangePassword = new Button("Change Password");
        btnChangePassword.setOnAction(e -> showChangePasswordDialog(primaryStage));
        VBox personalInfoBox = new VBox(10, accountLabel, passwordLabel, btnChangePassword);
        personalInfoBox.setPadding(new Insets(10));
        personalInfoBox.setStyle("-fx-border-color: gray; -fx-border-radius: 5px; -fx-padding: 10px;");

        // 主布局
        VBox mainLayout = new VBox(20, titleLabel, budgetBox, personalInfoBox);
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setPadding(new Insets(20));

        Scene scene = new Scene(mainLayout, 500, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // 弹出修改密码的对话框，并同步更新 CSV 文件
    private void showChangePasswordDialog(Stage owner) {
        Stage dialog = new Stage();
        dialog.setTitle("Change Password");
        dialog.initOwner(owner);

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);

        Label newPassLabel = new Label("New Password:");
        PasswordField newPassField = new PasswordField();

        Label confirmPassLabel = new Label("Confirm Password:");
        PasswordField confirmPassField = new PasswordField();

        Button btnSubmit = new Button("Submit");

        grid.add(newPassLabel, 0, 0);
        grid.add(newPassField, 1, 0);
        grid.add(confirmPassLabel, 0, 1);
        grid.add(confirmPassField, 1, 1);
        grid.add(btnSubmit, 1, 2);

        btnSubmit.setOnAction(e -> {
            String newPass = newPassField.getText();
            String confirmPass = confirmPassField.getText();
            if(newPass.isEmpty() || confirmPass.isEmpty()){
                showAlert(Alert.AlertType.ERROR, "Error", "Fields cannot be empty.");
            } else if(!newPass.equals(confirmPass)){
                showAlert(Alert.AlertType.ERROR, "Error", "Passwords do not match.");
            } else {
                // 调用 userManager 更新 CSV 文件中对应的用户密码
                boolean syncResult = userManager.updateUserPassword(currentUser.getUsername(), newPass);
                if(syncResult) {
                    // 同步成功后更新当前用户对象和界面显示
                    currentUser.setPassword(newPass);
                    passwordLabel.setText("Password: " + newPass);
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Password updated successfully.");
                    dialog.close();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to update password in file.");
                }
            }
        });

        Scene scene = new Scene(grid, 300, 200);
        dialog.setScene(scene);
        dialog.show();
    }

    // 简单的弹窗方法
    private void showAlert(Alert.AlertType type, String title, String message){
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}


