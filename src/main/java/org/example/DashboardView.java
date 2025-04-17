package org.example;

import org.example.list.TransactionViewer;

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

    // 用户设置的字段
    //    private double savingsGoal = 5000.0; // 这里可能是多余的变量，后续可以移除
    //    private double annualTarget = 20000.0; // 这里可能是多余的变量，后续可以移除
    //    private double savedAmount = 1500.0; // 这里可能是多余的变量，后续可以移除
    //    private double annualSavedAmount = 6000.0; // 这里可能是多余的变量，后续可以移除

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

    /** 让其它类可以拿到当前用户 */
    public static User getCurrentUser() {
        return currentUser;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 检查是否有用户登录
        if (currentUser == null) {
            // 如果没有用户登录，跳转到登录界面
            LoginFrame loginFrame = new LoginFrame();
            Stage loginStage = new Stage();
            try {
                loginFrame.start(loginStage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            primaryStage.close(); // 关闭当前 Dashboard 窗口
            return; // 退出方法
        }

        // 加载用户设置
        userManager.loadUserSettings(currentUser);

        primaryStage.setTitle("User Dashboard");

        // 初始化FormattedInput实例
        if (formattedInput == null) {
            formattedInput = new FormattedInput();
        }

        // 创建页面导航下拉框，并放在顶部
        pageSelector = new ComboBox<>();
        pageSelector.getItems().addAll("Formatted Input");
        pageSelector.getItems().addAll("Transaction Viewer");
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
                        primaryStage.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
            // 新增交易查看器页面逻辑
            else if ("Transaction Viewer".equals(selectedPage)) { 
                try {
                    // 创建新窗口
                    Stage transactionStage = new Stage();
                    // 初始化交易查看器
                    TransactionViewer transactionViewer = new TransactionViewer();
                    // 设置窗口标题
                    transactionStage.setTitle("交易记录查看器");
                    // 启动页面
                    transactionViewer.start(transactionStage);
                    primaryStage.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            // 可根据需要重新设置下拉框为当前页面
            pageSelector.setValue(selectedPage);
        });
        HBox navigationBox = new HBox(pageSelector);
        navigationBox.setAlignment(Pos.TOP_CENTER);
        navigationBox.setPadding(new Insets(10));

        // 顶部标题
        Label titleLabel = new Label("Dashboard");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // 显示用户个人信息：账户和密码
        Label accountLabel = new Label("Account: "
                + (currentUser != null ? currentUser.getUsername() : "N/A"));
        passwordLabel = new Label("Password: "
                + (currentUser != null ? currentUser.getPassword() : "N/A"));
        Button btnChangePassword = new Button("Change Password");
        btnChangePassword.setOnAction(e -> showChangePasswordDialog(primaryStage));
        Button btnLogout = new Button("Logout");
        btnLogout.setOnAction(e -> logout(primaryStage));
        VBox personalInfoBox = new VBox(10, accountLabel, passwordLabel, btnChangePassword, btnLogout);
        personalInfoBox.setPadding(new Insets(10));
        personalInfoBox.setStyle("-fx-border-color: gray; -fx-border-radius: 5px; -fx-padding: 10px;");
        // 设置个人信息框的固定尺寸
        personalInfoBox.setPrefSize(300, 100);

        // 显示本月预算
        budgetLabel = new Label("Monthly Budget: $" + currentUser.getMonthlyBudget());
        // 显示存钱目标
        goalLabel = new Label("Monthly Savings Goal: $" + currentUser.getMonthlyTarget());
        // 显示存钱进度（progress = 当前存款 / 存钱目标）
        progressBar = new ProgressBar(currentUser.getSavedAmount() / currentUser.getMonthlyTarget());
        progressLabel = new Label("Savings Progress: " + (int) (currentUser.getSavedAmount() / currentUser.getMonthlyTarget() * 100)
                + "% (" + currentUser.getSavedAmount() + " saved)");
        // 创建设置预算和目标的按钮
        Button btnSetGoal = new Button("Set Goal");
        btnSetGoal.setOnAction(e -> showGoalDialog(primaryStage));

        // 创建设置预算的按钮
        Button btnSetBudget = new Button("Set Budget");
        btnSetBudget.setOnAction(e -> showBudgetDialog(primaryStage));

        // 将预算、目标、进度和按钮放在一起
        HBox buttonsBox = new HBox(10, btnSetGoal, btnSetBudget);
        buttonsBox.setAlignment(Pos.CENTER);

        VBox budgetBox = new VBox(10, budgetLabel, goalLabel, progressBar, progressLabel, buttonsBox);
        budgetBox.setPadding(new Insets(10));
        budgetBox.setStyle("-fx-border-color: gray; -fx-border-radius: 5px; -fx-padding: 10px;");
        // 设置预算框的固定尺寸
        budgetBox.setPrefSize(300, 250);

        // 将个人信息和预算框放在左侧，垂直排列
        HBox topBox = new HBox(20, personalInfoBox, budgetBox);
        topBox.setAlignment(Pos.TOP_CENTER);
        topBox.setPadding(new Insets(10));

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

        // 将顶部和图像框放在一个 VBox 内
        VBox mainLayout = new VBox(20, navigationBox, titleLabel, topBox, imageBox);
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setPadding(new Insets(20));

        Scene scene = new Scene(mainLayout, 1000, 800);
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
    private void showGoalDialog(Stage owner) {
        Stage dialog = new Stage();
        dialog.setTitle("Set Goal");
        dialog.initOwner(owner);

        // ----------------- 顶部：年度进度条 -----------------
        ProgressBar annualProgressBar = new ProgressBar(currentUser.getAnnualSavedAmount() / currentUser.getAnnualTarget());
        annualProgressBar.setPrefWidth(550);
        int initPercent = (int)(currentUser.getAnnualSavedAmount() / currentUser.getAnnualTarget() * 100);
        Label annualProgressLabel = new Label("Annual Savings Progress: " + initPercent + "% (" + currentUser.getAnnualSavedAmount() + " saved)");
        VBox progressBox = new VBox(5, annualProgressBar, annualProgressLabel);
        progressBox.setAlignment(Pos.CENTER);
        progressBox.setPadding(new Insets(10));

        // ----------------- 年目标部分：年目标值及备注 -----------------
        Label annualLabel = new Label("Annual Target:");
        TextField annualField = new TextField(String.valueOf(currentUser.getAnnualTarget()));
        Label annualRemarkLabel = new Label("Remark:");
        TextField annualRemarkField = new TextField();
        Button annualSetButton = new Button("SET");
        annualSetButton.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-pref-height: 40px;");

        // 年目标部分垂直排列
        VBox annualBox = new VBox(10, annualLabel, annualField, annualRemarkLabel, annualRemarkField, annualSetButton);
        annualBox.setPadding(new Insets(10));
        annualBox.setStyle("-fx-border-color: gray; -fx-border-radius: 5px; -fx-padding: 10px;");

        // ----------------- 月目标部分：月目标值及备注 -----------------
        Label monthlyLabel = new Label("Monthly Target:");
        TextField monthlyField = new TextField(String.valueOf(currentUser.getMonthlyTarget()));
        Label monthlyRemarkLabel = new Label("Remark:");
        TextField monthlyRemarkField = new TextField();
        Button monthlySetButton = new Button("SET");
        monthlySetButton.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-pref-height: 40px;");

        // 月目标部分垂直排列
        VBox monthlyBox = new VBox(10, monthlyLabel, monthlyField, monthlyRemarkLabel, monthlyRemarkField, monthlySetButton);
        monthlyBox.setPadding(new Insets(10));
        monthlyBox.setStyle("-fx-border-color: gray; -fx-border-radius: 5px; -fx-padding: 10px;");

        // ----------------- 将年目标和月目标部分水平排列 -----------------
        HBox inputRow = new HBox(20, annualBox, monthlyBox);
        inputRow.setAlignment(Pos.CENTER);
        inputRow.setPadding(new Insets(10));

        // ----------------- 组合整体布局 -----------------
        VBox container = new VBox(10, progressBox, inputRow);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(10));

        Scene scene = new Scene(container, 600, 400);
        dialog.setScene(scene);
        dialog.show();

        // ----------------- 事件处理 -----------------
        // 年目标 SET 按钮逻辑：更新 annualTarget 并更新年度进度条
        annualSetButton.setOnAction(e -> {
            try {
                double newAnnual = Double.parseDouble(annualField.getText());
                if (newAnnual <= 0) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Annual value must be positive.");
                    return;
                }
                currentUser.setAnnualTarget(newAnnual);
                // 计算年度目标进度
                double ratio = currentUser.getAnnualSavedAmount() / newAnnual;
                annualProgressBar.setProgress(ratio);
                int percent = (int)(ratio * 100);
                annualProgressLabel.setText("Annual Savings Progress: " + percent + "% (" + currentUser.getAnnualSavedAmount() + " saved)");
                showAlert(Alert.AlertType.INFORMATION, "Success", "Annual target updated to: " + newAnnual);
                userManager.saveUserSettings(currentUser);
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
                currentUser.setMonthlyTarget(newMonthly);
                // 假设 Dashboard 中 goalLabel 显示的是月目标，这里更新它
                goalLabel.setText("Monthly Saving Goal: $" + newMonthly);
                progressBar.setProgress(currentUser.getSavedAmount() / newMonthly);
                progressLabel.setText("Savings Progress: "
                        + (int)(currentUser.getSavedAmount() / newMonthly * 100)
                        + "% (" + currentUser.getSavedAmount() + " saved)");
                showAlert(Alert.AlertType.INFORMATION, "Success", "Monthly target updated to: " + newMonthly);
                userManager.saveUserSettings(currentUser);
                dialog.close();
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Please enter a valid monthly target number.");
            }
        });
    }

    private void showBudgetDialog(Stage owner) {
        Stage dialog = new Stage();
        dialog.setTitle("Set Budget");
        dialog.initOwner(owner);

        // 使用GridPane以2行2列排列四个预算区域
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setAlignment(Pos.CENTER);

        // ------ 购物预算区域（左上） ------
        VBox shoppingBox = new VBox(10);
        shoppingBox.setAlignment(Pos.CENTER_LEFT);
        shoppingBox.setPadding(new Insets(10));
        shoppingBox.setStyle("-fx-border-color: gray; -fx-border-radius: 5px; -fx-padding: 10px;");

        Label shoppingLabel = new Label("Shopping Budget:");
        TextField shoppingField = new TextField(String.valueOf(currentUser.getShoppingBudget()));
        Label shoppingRemarkLabel = new Label("remark:");
        TextField shoppingRemarkField = new TextField();
        Button shoppingSetButton = new Button("SET");
        shoppingSetButton.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-pref-height: 40px;");

        shoppingBox.getChildren().addAll(shoppingLabel, shoppingField, shoppingRemarkLabel, shoppingRemarkField, shoppingSetButton);

        // ------ 交通预算区域（右上） ------
        VBox transportBox = new VBox(10);
        transportBox.setAlignment(Pos.CENTER_LEFT);
        transportBox.setPadding(new Insets(10));
        transportBox.setStyle("-fx-border-color: gray; -fx-border-radius: 5px; -fx-padding: 10px;");

        Label transportLabel = new Label("Transport Budget:");
        TextField transportField = new TextField(String.valueOf(currentUser.getTransportBudget()));
        Label transportRemarkLabel = new Label("remark:");
        TextField transportRemarkField = new TextField();
        Button transportSetButton = new Button("SET");
        transportSetButton.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-pref-height: 40px;");

        transportBox.getChildren().addAll(transportLabel, transportField, transportRemarkLabel, transportRemarkField, transportSetButton);

        // ------ 饮食预算区域（左下） ------
        VBox dietBox = new VBox(10);
        dietBox.setAlignment(Pos.CENTER_LEFT);
        dietBox.setPadding(new Insets(10));
        dietBox.setStyle("-fx-border-color: gray; -fx-border-radius: 5px; -fx-padding: 10px;");

        Label dietLabel = new Label("Diet Budget:");
        TextField dietField = new TextField(String.valueOf(currentUser.getDietBudget()));
        Label dietRemarkLabel = new Label("remark:");
        TextField dietRemarkField = new TextField();
        Button dietSetButton = new Button("SET");
        dietSetButton.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-pref-height: 40px;");

        dietBox.getChildren().addAll(dietLabel, dietField, dietRemarkLabel, dietRemarkField, dietSetButton);

        // ------ 娱乐预算区域（右下） ------
        VBox amusementBox = new VBox(10);
        amusementBox.setAlignment(Pos.CENTER_LEFT);
        amusementBox.setPadding(new Insets(10));
        amusementBox.setStyle("-fx-border-color: gray; -fx-border-radius: 5px; -fx-padding: 10px;");

        Label amusementLabel = new Label("Amusement Budget:");
        TextField amusementField = new TextField(String.valueOf(currentUser.getAmusementBudget()));
        Label amusementRemarkLabel = new Label("remark:");
        TextField amusementRemarkField = new TextField();
        Button amusementSetButton = new Button("SET");
        amusementSetButton.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-pref-height: 40px;");

        amusementBox.getChildren().addAll(amusementLabel, amusementField, amusementRemarkLabel, amusementRemarkField, amusementSetButton);

        // 将四个区域分别放入GridPane：购物（0,0），交通（1,0），饮食（0,1），娱乐（1,1）
        grid.add(shoppingBox, 0, 0);
        grid.add(transportBox, 1, 0);
        grid.add(dietBox, 0, 1);
        grid.add(amusementBox, 1, 1);

        Scene scene = new Scene(grid, 600, 600);
        dialog.setScene(scene);
        dialog.show();

        // ------------- 事件处理 -------------
        // 购物预算SET按钮：更新购物预算并刷新月预算总和
        shoppingSetButton.setOnAction(e -> {
            try {
                double newBudget = Double.parseDouble(shoppingField.getText());
                if (newBudget < 0) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Budget value must be non-negative.");
                    return;
                }
                currentUser.setShoppingBudget(newBudget);
                currentUser.setMonthlyBudget(
                        currentUser.getShoppingBudget() +
                                currentUser.getTransportBudget() +
                                currentUser.getDietBudget() +
                                currentUser.getAmusementBudget()
                );
                budgetLabel.setText("Monthly Budget: $" + currentUser.getMonthlyBudget());
                showAlert(Alert.AlertType.INFORMATION, "Success", "Shopping budget updated to: " + newBudget);
                userManager.saveUserSettings(currentUser);
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Please enter a valid budget number.");
            }
        });

        // 交通预算SET按钮
        transportSetButton.setOnAction(e -> {
            try {
                double newBudget = Double.parseDouble(transportField.getText());
                if (newBudget < 0) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Budget value must be non-negative.");
                    return;
                }
                currentUser.setTransportBudget(newBudget);
                currentUser.setMonthlyBudget(
                        currentUser.getShoppingBudget() +
                                currentUser.getTransportBudget() +
                                currentUser.getDietBudget() +
                                currentUser.getAmusementBudget()
                );
                budgetLabel.setText("Monthly Budget: $" + currentUser.getMonthlyBudget());
                showAlert(Alert.AlertType.INFORMATION, "Success", "Transport budget updated to: " + newBudget);
                userManager.saveUserSettings(currentUser);
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Please enter a valid budget number.");
            }
        });

        // 饮食预算SET按钮
        dietSetButton.setOnAction(e -> {
            try {
                double newBudget = Double.parseDouble(dietField.getText());
                if (newBudget < 0) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Budget value must be non-negative.");
                    return;
                }
                currentUser.setDietBudget(newBudget);
                currentUser.setMonthlyBudget(
                        currentUser.getShoppingBudget() +
                                currentUser.getTransportBudget() +
                                currentUser.getDietBudget() +
                                currentUser.getAmusementBudget()
                );
                budgetLabel.setText("Monthly Budget: $" + currentUser.getMonthlyBudget());
                showAlert(Alert.AlertType.INFORMATION, "Success", "Diet budget updated to: " + newBudget);
                userManager.saveUserSettings(currentUser);
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Please enter a valid budget number.");
            }
        });

        // 娱乐预算SET按钮
        amusementSetButton.setOnAction(e -> {
            try {
                double newBudget = Double.parseDouble(amusementField.getText());
                if (newBudget < 0) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Budget value must be non-negative.");
                    return;
                }
                currentUser.setAmusementBudget(newBudget);
                currentUser.setMonthlyBudget(
                        currentUser.getShoppingBudget() +
                                currentUser.getTransportBudget() +
                                currentUser.getDietBudget() +
                                currentUser.getAmusementBudget()
                );
                budgetLabel.setText("Monthly Budget: $" + currentUser.getMonthlyBudget());
                showAlert(Alert.AlertType.INFORMATION, "Success", "Amusement budget updated to: " + newBudget);
                userManager.saveUserSettings(currentUser);
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Please enter a valid budget number.");
            }
        });
    }

    // 简单的弹窗方法
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // 退出登录方法
    private void logout(Stage primaryStage) {
        DashboardView.currentUser = null; // 清除当前用户信息
        primaryStage.close(); // 关闭Dashboard界面
        // 重新启动登录界面
        LoginFrame loginFrame = new LoginFrame();
        Stage loginStage = new Stage();
        try {
            loginFrame.start(loginStage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}