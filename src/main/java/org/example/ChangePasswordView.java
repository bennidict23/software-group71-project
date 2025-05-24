package org.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class ChangePasswordView extends Application {

    private final User currentUser;
    private final UserManager userManager;
    private Stage primaryStage; // 保存对主舞台的引用

    public ChangePasswordView(User currentUser, UserManager userManager, Stage primaryStage) {
        this.currentUser = currentUser;
        this.userManager = userManager;
        this.primaryStage = primaryStage;
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Change Password");

        // 创建密码修改表单布局
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20, 20, 20, 20));
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);

        // 用户名和密码标签
        Label userLabel = new Label("Username: " + currentUser.getUsername());
        Label passLabel = new Label("Current Password: " + currentUser.getPassword());

        // 新密码和确认密码输入框
        Label newPassLabel = new Label("New Password:");
        PasswordField newPassField = new PasswordField();

        Label confirmPassLabel = new Label("Confirm Password:");
        PasswordField confirmPassField = new PasswordField();

        // 提交按钮
        Button btnSubmit = new Button("Submit");

        // 登出按钮
        Button btnLogout = new Button("Logout");
        btnLogout.setOnAction(e -> logout());

        // 返回 Dashboard 按钮
        Button btnBackToDashboard = new Button("Back to Dashboard");
        btnBackToDashboard.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        btnBackToDashboard.setOnAction(e -> {
            primaryStage.close();
            showDashboard();
        });

        // 将组件添加到网格布局
        grid.add(userLabel, 0, 0);
        grid.add(passLabel, 0, 1);
        grid.add(newPassLabel, 0, 2);
        grid.add(newPassField, 1, 2);
        grid.add(confirmPassLabel, 0, 3);
        grid.add(confirmPassField, 1, 3);

        // 按钮区域
        HBox buttonBox = new HBox(10, btnSubmit, btnLogout);
        buttonBox.setAlignment(Pos.CENTER); // 设置按钮水平居中对齐

        // 将按钮区域添加到网格布局
        grid.add(buttonBox, 0, 4, 2, 1);

        // 添加返回 Dashboard 按钮
        grid.add(btnBackToDashboard, 1, 5);

        // 设置密码修改提交逻辑
        btnSubmit.setOnAction(e -> {
            String newPass = newPassField.getText();
            String confirmPass = confirmPassField.getText();
            if (newPass.isEmpty() || confirmPass.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "Fields cannot be empty.");
            } else if (!newPass.equals(confirmPass)) {
                showAlert(Alert.AlertType.ERROR, "Error", "Passwords do not match.");
            } else {
                boolean syncResult = userManager.updateUserPassword(currentUser.getUsername(), newPass);
                if (syncResult) {
                    currentUser.setPassword(newPass);
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Password updated successfully.");
                    primaryStage.close();
                    showLoginWindow();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to update password in file.");
                }
            }
        });

        Scene scene = new Scene(grid, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showLoginWindow() {
        LoginFrame loginFrame = new LoginFrame();
        Stage loginStage = new Stage();
        try {
            loginFrame.start(loginStage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void logout() {
        userManager.shutdownScheduler(); // 停止定时任务
        DashboardView.currentUser = null;
        primaryStage.close();
        LoginFrame loginFrame = new LoginFrame();
        Stage loginStage = new Stage();
        try {
            loginFrame.start(loginStage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void showDashboard() {
        // 重新启动 DashboardView
        DashboardView dashboard = new DashboardView();
        try {
            dashboard.start(primaryStage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}