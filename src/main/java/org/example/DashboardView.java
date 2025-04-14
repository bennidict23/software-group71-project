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
    // 创建 UserManager 实例，用于操作 CSV 文件（用于修改密码等操作）
    private UserManager userManager = new UserManager();

    // 默认本月预算、存钱目标（target）和当前存款金额（实际存款）
    private double monthlyBudget = 4000.0;
    private double savingsGoal = 5000.0;
    private double savedAmount = 1500.0;

    // 用于显示信息的控件
    private Label passwordLabel;
    private Label budgetLabel;
    private Label goalLabel;
    private ProgressBar progressBar;
    private Label progressLabel;

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

        // 显示本月预算
        budgetLabel = new Label("Monthly Budget: $" + monthlyBudget);
        // 显示存钱目标
        goalLabel = new Label("Savings Goal: $" + savingsGoal);
        // 显示存钱进度（progress = 当前存款 / 存钱目标）
        progressBar = new ProgressBar(savedAmount / savingsGoal);
        progressLabel = new Label("Savings Progress: " + (int)(savedAmount / savingsGoal * 100)
                + "% (" + savedAmount + " saved)");
        VBox budgetBox = new VBox(10, budgetLabel, goalLabel, progressBar, progressLabel);
        budgetBox.setPadding(new Insets(10));
        budgetBox.setStyle("-fx-border-color: gray; -fx-border-radius: 5px; -fx-padding: 10px;");

        // 显示用户个人信息：账户和密码
        Label accountLabel = new Label("Account: "
                + (currentUser != null ? currentUser.getUsername() : "N/A"));
        passwordLabel = new Label("Password: "
                + (currentUser != null ? currentUser.getPassword() : "N/A"));
        Button btnChangePassword = new Button("Change Password");
        btnChangePassword.setOnAction(e -> showChangePasswordDialog(primaryStage));
        VBox personalInfoBox = new VBox(10, accountLabel, passwordLabel, btnChangePassword);
        personalInfoBox.setPadding(new Insets(10));
        personalInfoBox.setStyle("-fx-border-color: gray; -fx-border-radius: 5px; -fx-padding: 10px;");

        // 添加设置预算和目标的按钮
        Button btnSetBudgetGoal = new Button("Set Budget & Goal");
        btnSetBudgetGoal.setOnAction(e -> showBudgetGoalDialog(primaryStage));

        // 主布局
        VBox mainLayout = new VBox(20, titleLabel, budgetBox, personalInfoBox, btnSetBudgetGoal);
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setPadding(new Insets(20));

        Scene scene = new Scene(mainLayout, 500, 500);
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
            if (newPass.isEmpty() || confirmPass.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "Fields cannot be empty.");
            } else if (!newPass.equals(confirmPass)) {
                showAlert(Alert.AlertType.ERROR, "Error", "Passwords do not match.");
            } else {
                // 调用 userManager 更新 CSV 文件中对应的用户密码
                boolean syncResult = userManager.updateUserPassword(currentUser.getUsername(), newPass);
                if (syncResult) {
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

    // 弹出设置预算和存钱目标的对话框
    private void showBudgetGoalDialog(Stage owner) {
        Stage dialog = new Stage();
        dialog.setTitle("Set Budget & Goal");
        dialog.initOwner(owner);

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);

        Label budgetInputLabel = new Label("Monthly Budget:");
        TextField budgetField = new TextField(String.valueOf(monthlyBudget));

        Label goalInputLabel = new Label("Savings Goal:");
        TextField goalField = new TextField(String.valueOf(savingsGoal));

        Button btnSubmit = new Button("Submit");

        grid.add(budgetInputLabel, 0, 0);
        grid.add(budgetField, 1, 0);
        grid.add(goalInputLabel, 0, 1);
        grid.add(goalField, 1, 1);
        grid.add(btnSubmit, 1, 2);

        btnSubmit.setOnAction(e -> {
            try {
                double newBudget = Double.parseDouble(budgetField.getText());
                double newGoal = Double.parseDouble(goalField.getText());
                if (newBudget <= 0 || newGoal <= 0) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Values must be positive.");
                    return;
                }
                // 更新变量
                monthlyBudget = newBudget;
                savingsGoal = newGoal;
                // 更新界面显示：预算、目标与存钱进度
                budgetLabel.setText("Monthly Budget: $" + monthlyBudget);
                goalLabel.setText("Savings Goal: $" + savingsGoal);
                progressBar.setProgress(savedAmount / savingsGoal);
                progressLabel.setText("Savings Progress: " + (int)(savedAmount / savingsGoal * 100)
                        + "% (" + savedAmount + " saved)");
                showAlert(Alert.AlertType.INFORMATION, "Success", "Budget and Goal updated.");
                dialog.close();
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Please enter valid numbers.");
            }
        });

        Scene scene = new Scene(grid, 300, 200);
        dialog.setScene(scene);
        dialog.show();
    }

    // 简单的弹窗方法
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}



