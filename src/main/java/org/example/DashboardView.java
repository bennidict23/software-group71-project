package org.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
    // 新增：年目标，默认值可以自行设定
    private double annualTarget = 20000.0;

    private double savedAmount = 1500.0;

    private double annualSavedAmount = 6000.0;

    // 用于显示信息的控件
    private Label passwordLabel;
    private Label budgetLabel;
    private Label goalLabel;
    private ProgressBar progressBar;
    private Label progressLabel;

    // 下拉选择框，用于页面导航
    private ComboBox<String> pageSelector;

    // 登录时调用该方法设置当前用户
    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("User Dashboard");

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
                // 打开 FormattedInput 页面（假设已实现 FormattedInput 类）
                FormattedInput formattedInput = new FormattedInput();
                Stage formattedStage = new Stage();
                try {
                    formattedInput.start(formattedStage);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            // 可根据需要重新设置下拉框为当前页面
            pageSelector.setValue("Formatted Input");
        });
        HBox navigationBox = new HBox(pageSelector);
        navigationBox.setAlignment(Pos.TOP_CENTER);
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
        // 创建设置预算和目标的按钮
        Button btnSetBudgetGoal = new Button("Set Budget & Goal");
        btnSetBudgetGoal.setOnAction(e -> showBudgetGoalDialog(primaryStage));

        // 将预算、目标、进度和按钮放在一起
        VBox budgetBox = new VBox(10, budgetLabel, goalLabel, progressBar, progressLabel, btnSetBudgetGoal);
        budgetBox.setPadding(new Insets(10));
        budgetBox.setStyle("-fx-border-color: gray; -fx-border-radius: 5px; -fx-padding: 10px;");
        // 设置预算框的固定尺寸
        budgetBox.setPrefSize(300, 250);

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
        // 设置个人信息框的固定尺寸
        personalInfoBox.setPrefSize(300, 150);

        // 预留图像位置
        ImageView imageView = new ImageView();
        imageView.setFitHeight(200);
        imageView.setFitWidth(200);
        imageView.setStyle("-fx-border-color: gray; -fx-border-radius: 5px;");
        Label imageTitleLabel = new Label("Consumer Trend");
        imageTitleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        VBox imageBox = new VBox(10, imageTitleLabel, imageView);
        imageBox.setAlignment(Pos.TOP_CENTER);
        imageBox.setPadding(new Insets(10));
        imageBox.setStyle("-fx-border-color: gray; -fx-border-radius: 5px; -fx-padding: 10px;");
        // 设置图像框的固定尺寸
        imageBox.setPrefSize(300, 300);

        // 将个人信息和预算框放在左侧，垂直排列
        VBox leftColumn = new VBox(20, personalInfoBox, budgetBox);
        leftColumn.setAlignment(Pos.TOP_LEFT);
        leftColumn.setPadding(new Insets(10));

        // 将左侧和右侧图像框放在一个 HBox 内
        HBox mainBox = new HBox(20, leftColumn, imageBox);
        mainBox.setAlignment(Pos.CENTER);

        // 主布局，将导航下拉框放在最顶端
        VBox mainLayout = new VBox(20, navigationBox, titleLabel, mainBox);
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

        // ----------------- 新增部分：年度目标进度条 -----------------
        ProgressBar annualProgressBar = new ProgressBar(0);
        annualProgressBar.setPrefWidth(550);
        Label annualProgressLabel = new Label("Annual Savings Progress: 0% (0 saved)");
        VBox progressBox = new VBox(5, annualProgressBar, annualProgressLabel);
        progressBox.setAlignment(Pos.CENTER);
        progressBox.setPadding(new Insets(10));

        // ----------------- 原有输入区域 -----------------
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);

        // 第一部分：Annual Target（年目标）
        Label annualLabel = new Label("Annual Target:");
        TextField annualField = new TextField(String.valueOf(annualTarget));

        Label annualRemarkLabel = new Label("remark");
        TextField annualRemarkField = new TextField();

        Button annualSetButton = new Button("SET");
        annualSetButton.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-pref-height: 40px;");

        grid.add(annualLabel, 0, 0);
        grid.add(annualField, 1, 0);
        grid.add(annualRemarkLabel, 0, 1);
        grid.add(annualRemarkField, 1, 1);
        grid.add(annualSetButton, 1, 2);

        // 第二部分：Monthly Target（仅用于更新月目标，在Dashboard中显示）
        Label monthlyLabel = new Label("Monthly Target:");
        TextField monthlyField = new TextField(String.valueOf(savingsGoal));

        Label monthlyRemarkLabel = new Label("remark");
        TextField monthlyRemarkField = new TextField();

        Button monthlySetButton = new Button("SET");
        monthlySetButton.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-pref-height: 40px;");

        grid.add(monthlyLabel, 0, 3);
        grid.add(monthlyField, 1, 3);
        grid.add(monthlyRemarkLabel, 0, 4);
        grid.add(monthlyRemarkField, 1, 4);
        grid.add(monthlySetButton, 1, 5);

        // ----------------- 组合布局 -----------------
        // 将进度条部分和输入区域组合在一起
        VBox container = new VBox(10, progressBox, grid);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(10));

        // ----------------- 事件处理 -----------------
        // 年目标 SET 按钮逻辑：更新 annualTarget 和年度进度条
        annualSetButton.setOnAction(e -> {
            try {
                double newAnnual = Double.parseDouble(annualField.getText());
                if (newAnnual <= 0) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Annual value must be positive.");
                    return;
                }
                annualTarget = newAnnual;
                // 计算年度目标进度
                double ratio = annualSavedAmount / newAnnual;
                annualProgressBar.setProgress(ratio);
                int percent = (int)(ratio * 100);
                annualProgressLabel.setText("Annual Savings Progress: " + percent + "% (" + annualSavedAmount + " saved)");
                showAlert(Alert.AlertType.INFORMATION, "Success", "Annual target updated to: " + annualTarget);
                // 可选择在设置完成后不关闭对话框，让用户同时设置月目标
                // dialog.close();
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Please enter a valid annual target number.");
            }
        });

        // 月目标 SET 按钮逻辑：更新 savingsGoal 并刷新 Dashboard 中显示的月目标和进度
        monthlySetButton.setOnAction(e -> {
            try {
                double newMonthly = Double.parseDouble(monthlyField.getText());
                if (newMonthly <= 0) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Monthly value must be positive.");
                    return;
                }
                savingsGoal = newMonthly;
                // 假设 Dashboard 中 goalLabel 显示的是月目标，这里更新它
                goalLabel.setText("Monthly Saving Goal: $" + savingsGoal);
                progressBar.setProgress(savedAmount / savingsGoal);
                progressLabel.setText("Savings Progress: "
                        + (int)(savedAmount / savingsGoal * 100)
                        + "% (" + savedAmount + " saved)");
                showAlert(Alert.AlertType.INFORMATION, "Success", "Monthly target updated to: " + savingsGoal);
                dialog.close();
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Please enter a valid monthly target number.");
            }
        });

        Scene scene = new Scene(container, 600, 400);
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

