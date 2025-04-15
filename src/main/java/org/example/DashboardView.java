package org.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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

    // 下拉选择框，用于页面导航
    private ComboBox<String> pageSelector;

    // 缓存FormattedInput实例
    private FormattedInput formattedInput = null;

    // 登录时调用该方法设置当前用户
    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("User Dashboard");

        // 初始化FormattedInput实例
        if (formattedInput == null) {
            formattedInput = new FormattedInput();
        }

        // 创建页面导航下拉框，并放在顶部
        pageSelector = new ComboBox<>();
        pageSelector.getItems().addAll("Formatted Input");
        pageSelector.setPromptText("Select a page...");
        pageSelector.setOnAction(e -> {
            String selectedPage = pageSelector.getValue();
            if (selectedPage == null || selectedPage.isEmpty()) {
                return;  // 没有选择时直接返回
            }
            if ("Formatted Input".equals(selectedPage)) {
                // 调用FormattedInput的start方法
                if (formattedInput != null) {
                    try {
                        Stage formattedStage = new Stage();
                        formattedInput.start(formattedStage);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
            // 可根据需要重新设置下拉框为当前页面
            pageSelector.setValue("Formatted Input");
        });
        // 使用一个 HBox 来显示下拉框，并设置居中与内边距
        HBox navigationBox = new HBox(pageSelector);
        navigationBox.setAlignment(Pos.CENTER);
        navigationBox.setPadding(new Insets(10));

        // 顶部标题
        Label titleLabel = new Label("Dashboard");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // 显示本月预算
        budgetLabel = new Label("Monthly Budget: $" + monthlyBudget);
        // 显示存钱目标
        goalLabel = new Label("Savings Goal: $" + savingsGoal);
        // 显示存钱进度（progress = 当前存款 / 存钱目标）
        progressBar = new ProgressBar(savedAmount / savingsGoal);
        progressLabel = new Label("Savings Progress: " + (int) (savedAmount / savingsGoal * 100)
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

        // 预留图像位置
        ImageView imageView = new ImageView();
        imageView.setFitHeight(200);
        imageView.setFitWidth(200);
        imageView.setStyle("-fx-border-color: gray; -fx-border-radius: 5px;");

        // 添加标题
        Label imageTitleLabel = new Label("Consumer Trend");
        imageTitleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // 将图像放入一个 VBox，并用边框框起来
        VBox imageBox = new VBox(10,imageTitleLabel,imageView);
        imageBox.setPadding(new Insets(10));
        imageBox.setStyle("-fx-border-color: gray; -fx-border-radius: 5px; -fx-padding: 10px;");

        // 将用户信息和月预算存钱目标的两个框放在左侧，图像框放在右侧
        HBox mainBox = new HBox(20, personalInfoBox, budgetBox, imageBox);
        mainBox.setAlignment(Pos.CENTER);

        // 添加设置预算和目标的按钮
        Button btnSetBudgetGoal = new Button("Set Budget & Goal");
        btnSetBudgetGoal.setOnAction(e -> showBudgetGoalDialog(primaryStage));

        // 将设置预算与目标按钮放入一个 HBox，居中显示
        HBox optionsBox = new HBox(20, btnSetBudgetGoal);
        optionsBox.setAlignment(Pos.CENTER);

        // 主布局，将导航下拉框放在最顶端
        VBox mainLayout = new VBox(20, navigationBox, titleLabel, mainBox, optionsBox);
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setPadding(new Insets(20));

        Scene scene = new Scene(mainLayout, 800, 600);
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
                boolean syncResult = userManager.updateUserPassword(currentUser.getUsername(), newPass);
                if (syncResult) {
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
                monthlyBudget = newBudget;
                savingsGoal = newGoal;
                budgetLabel.setText("Monthly Budget: $" + monthlyBudget);
                goalLabel.setText("Savings Goal: $" + savingsGoal);
                progressBar.setProgress(savedAmount / savingsGoal);
                progressLabel.setText("Savings Progress: " + (int) (savedAmount / savingsGoal * 100)
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