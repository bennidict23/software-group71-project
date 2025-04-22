package org.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.list.TransactionViewer;


import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DashboardView extends Application {

    // 当前登录用户，静态变量，方便在类的其他方法中访问
    private static User currentUser;
    // 用户管理对象，用于处理用户相关操作
    private UserManager userManager = new UserManager();

    // 界面中的一些组件，用于展示和操作用户信息
    private Label passwordLabel;
    private Label budgetLabel;
    private Label goalLabel;
    private ProgressBar progressBar;
    private Label progressLabel;

    // 页面选择下拉框，用于切换不同的功能页面
    private ComboBox<String> pageSelector;

    // 标记是否已导入过数据
    private static boolean importDone = false;

    /** 由外部（FormattedInput.importCSV）调用，标记已导入数据 */
    public static void setImportDone(boolean done) {
        importDone = done;
    }

    // 格式化输入页面对象，用于处理格式化输入相关操作
    private FormattedInput formattedInput = null;

    // 定时任务，用于定期更新 savedAmount 和 annualSavedAmount
    private ScheduledExecutorService scheduler;

    /**
     * 设置当前用户，静态方法，方便从外部设置当前登录用户。
     * 
     * @param user 当前登录用户
     */
    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    /**
     * 获取当前用户，静态方法，方便在类的其他方法中获取当前登录用户。
     * 
     * @return 当前登录用户
     */
    public static User getCurrentUser() {
        return currentUser;
    }

    /**
     * 启动仪表盘界面。
     * 
     * @param primaryStage 主舞台
     * @throws Exception 异常
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        // 如果当前用户为空，说明未登录，跳转到登录界面
        if (currentUser == null) {
            LoginFrame loginFrame = new LoginFrame();
            Stage loginStage = new Stage();
            try {
                loginFrame.start(loginStage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            primaryStage.close();
            return;
        }

        // 检查并重置月储蓄目标和月预算，确保数据的准确性
        userManager.checkAndResetMonthlySettings(currentUser);

        // 检查本月消费情况，为后续的预算和目标展示提供数据支持
        //userManager.checkMonthlyExpenses(currentUser);

        primaryStage.setTitle("User Dashboard");

        // 如果 formattedInput 对象为空，进行初始化
        if (formattedInput == null) {
            formattedInput = new FormattedInput();
        }

        // 初始化页面选择下拉框，添加页面选项，并设置页面切换逻辑
        pageSelector = new ComboBox<>();
        pageSelector.getItems().addAll("Formatted Input", "Transaction Viewer", "Analysis");
        pageSelector.setPromptText("Select a page...");
        pageSelector.setOnAction(e -> {
            String selectedPage = pageSelector.getValue();
            if (selectedPage == null || selectedPage.isEmpty()) {
                return;
            }
            if ("Formatted Input".equals(selectedPage)) {
                if (formattedInput != null) {
                    try {
                        // 这里不关闭 primaryStage，而是在新的窗口中打开 FormattedInput
                        Stage formattedStage = new Stage();
                        formattedInput.start(formattedStage);
                        primaryStage.close();
                        // 不关闭主舞台
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            } else if ("Transaction Viewer".equals(selectedPage)) {
                try {
                    // 这里不关闭 primaryStage，而是在新的窗口中打开 TransactionViewer
                    Stage transactionStage = new Stage();
                    TransactionViewer transactionViewer = new TransactionViewer();
                    transactionStage.setTitle("交易记录查看器");
                    transactionViewer.start(transactionStage);
                    primaryStage.close();
                    // 不关闭主舞台
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else if ("Analysis".equals(selectedPage)) {
                try {
                    // 打开Analysis页面
                    Stage analysisStage = new Stage();
                    analysisStage.setTitle("Data Analysis");
                    // 使用AnalysisView
                    new org.example.analysis.AnalysisView().start(analysisStage);
                    primaryStage.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to open Analysis page: " + ex.getMessage());
                }
            }
            pageSelector.setValue(selectedPage);
        });
        // 创建导航栏布局，包含页面选择下拉框
        HBox navigationBox = new HBox(pageSelector);
        navigationBox.setAlignment(Pos.TOP_CENTER);
        navigationBox.setPadding(new Insets(10));

        // 创建标题标签
        Label titleLabel = new Label("Dashboard");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // 创建个人信息区域，展示账户信息，并提供密码修改和登出功能
        Label accountLabel = new Label("Account: " + (currentUser != null ? currentUser.getUsername() : "N/A"));
        passwordLabel = new Label("Password: " + (currentUser != null ? currentUser.getPassword() : "N/A"));
        Button btnChangePassword = new Button("Change Password");
        btnChangePassword.setOnAction(e -> showChangePasswordDialog(primaryStage));
        Button btnLogout = new Button("Logout");
        btnLogout.setOnAction(e -> logout(primaryStage));
        VBox personalInfoBox = new VBox(10, accountLabel, passwordLabel, btnChangePassword, btnLogout);
        personalInfoBox.setPadding(new Insets(10));
        personalInfoBox.setStyle("-fx-border-color: gray; -fx-border-radius: 5px; -fx-padding: 10px;");
        personalInfoBox.setPrefSize(300, 100);

        // 创建预算和目标区域，展示月预算、月储蓄目标、储蓄进度等信息，并提供目标设置和预算设置功能
        budgetLabel = new Label("Monthly Budget: $" + currentUser.getMonthlyBudget());
        goalLabel = new Label("Monthly Savings Goal: $" + currentUser.getMonthlyTarget());
        progressBar = new ProgressBar(currentUser.getSavedAmount() / currentUser.getMonthlyTarget());
        progressLabel = new Label(
                "Savings Progress: " + (int) (currentUser.getSavedAmount() / currentUser.getMonthlyTarget() * 100)
                        + "% (" + currentUser.getSavedAmount() + " saved)");
        Button btnSetGoal = new Button("Set Goal");
        btnSetGoal.setOnAction(e -> showGoalDialog(primaryStage));
        Button btnSetBudget = new Button("Set Budget");
        btnSetBudget.setOnAction(e -> showBudgetDialog(primaryStage));
        HBox buttonsBox = new HBox(10, btnSetGoal, btnSetBudget);
        buttonsBox.setAlignment(Pos.CENTER);
        VBox budgetBox = new VBox(10, budgetLabel, goalLabel, progressBar, progressLabel, buttonsBox);
        budgetBox.setPadding(new Insets(10));
        budgetBox.setStyle("-fx-border-color: gray; -fx-border-radius: 5px; -fx-padding: 10px;");
        budgetBox.setPrefSize(300, 250);

        // 创建消费趋势区域，预留位置用于展示消费趋势图表等信息
        HBox topBox = new HBox(20, personalInfoBox, budgetBox);
        topBox.setAlignment(Pos.TOP_CENTER);
        topBox.setPadding(new Insets(10));
        // 创建 LineChart
        LineChart<String, Number> lineChart = new ConsumerTrendChart(currentUser).createChart();

        // 创建 StackPane 并添加 LineChart
        StackPane chartPane = new StackPane();
        chartPane.getChildren().add(lineChart);
        chartPane.setStyle("-fx-border-color: gray; -fx-border-radius: 5px; -fx-padding: 10px;");

        chartPane.setVisible(importDone);
        chartPane.managedProperty().bind(chartPane.visibleProperty());

        // 将图表添加到消费趋势区域
        VBox imageBox = new VBox(10, new Label("Consumer Trend"), chartPane);
        imageBox.setAlignment(Pos.TOP_CENTER);
        imageBox.setPadding(new Insets(10));
        imageBox.setStyle("-fx-border-color: gray; -fx-border-radius: 5px; -fx-padding: 10px;");
        imageBox.setPrefSize(300, 300);

        // 将各个区域组合到主布局中
        VBox mainLayout = new VBox(20, navigationBox, titleLabel, topBox, imageBox);
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setPadding(new Insets(20));

        // 创建场景并设置到主舞台
        Scene scene = new Scene(mainLayout, 1000, 800);
        primaryStage.setScene(scene);

        // 设置关闭窗口时的事件处理器
        primaryStage.setOnCloseRequest(event -> {
            try {
                stop(); // 调用 stop 方法来停止程序
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        primaryStage.show();

        // 启动定时任务，每10秒更新一次 savedAmount 和 annualSavedAmount
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::updateSavedAmounts, 0, 10, TimeUnit.SECONDS);
    }

    /**
     * 更新 savedAmount 和 annualSavedAmount，并刷新界面显示。
     */
    private void updateSavedAmounts() {
        // 调用 UserManager 的 checkTransactionsFile 方法更新 savedAmount 和 annualSavedAmount
        userManager.checkTransactionsFile();

        // 更新界面中的显示内容
        Platform.runLater(() -> {
            if (currentUser != null) {
                budgetLabel.setText("Monthly Budget: $" + currentUser.getMonthlyBudget());
                goalLabel.setText("Monthly Savings Goal: $" + currentUser.getMonthlyTarget());
                progressBar.setProgress(currentUser.getSavedAmount() / currentUser.getMonthlyTarget());
                progressLabel.setText("Savings Progress: "
                        + (int) (currentUser.getSavedAmount() / currentUser.getMonthlyTarget() * 100) + "% ("
                        + currentUser.getSavedAmount() + " saved)");
            }
        });
    }

    /**
     * 显示密码修改对话框。
     * 
     * @param owner 父级舞台
     */
    private void showChangePasswordDialog(Stage owner) {
        Stage dialog = new Stage();
        dialog.setTitle("Change Password");
        dialog.initOwner(owner);

        // 创建密码修改表单布局
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

    /**
     * 显示目标设置对话框。
     * 
     * @param owner 父级舞台
     */
    private void showGoalDialog(Stage owner) {
        Stage dialog = new Stage();
        dialog.setTitle("Set Goal");
        dialog.initOwner(owner);

        // 创建年度储蓄进度展示区域
        ProgressBar annualProgressBar = new ProgressBar(
                currentUser.getAnnualSavedAmount() / currentUser.getAnnualTarget());
        annualProgressBar.setPrefWidth(550);
        int initPercent = (int) (currentUser.getAnnualSavedAmount() / currentUser.getAnnualTarget() * 100);
        Label annualProgressLabel = new Label(
                "Annual Savings Progress: " + initPercent + "% (" + currentUser.getAnnualSavedAmount() + " saved)");
        VBox progressBox = new VBox(5, annualProgressBar, annualProgressLabel);
        progressBox.setAlignment(Pos.CENTER);
        progressBox.setPadding(new Insets(10));

        // 创建年度目标设置区域
        Label annualLabel = new Label("Annual Target:");
        TextField annualField = new TextField(String.valueOf(currentUser.getAnnualTarget()));
        Label annualRemarkLabel = new Label("Remark:");
        TextField annualRemarkField = new TextField();
        Button annualSetButton = new Button("SET");
        annualSetButton.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-pref-height: 40px;");

        VBox annualBox = new VBox(10, annualLabel, annualField, annualRemarkLabel, annualRemarkField, annualSetButton);
        annualBox.setPadding(new Insets(10));
        annualBox.setStyle("-fx-border-color: gray; -fx-border-radius: 5px; -fx-padding: 10px;");

        // 创建月目标设置区域
        Label monthlyLabel = new Label("Monthly Target:");
        TextField monthlyField = new TextField(String.valueOf(currentUser.getMonthlyTarget()));
        Label monthlyRemarkLabel = new Label("Remark:");
        TextField monthlyRemarkField = new TextField();
        Button monthlySetButton = new Button("SET");
        monthlySetButton.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-pref-height: 40px;");

        VBox monthlyBox = new VBox(10, monthlyLabel, monthlyField, monthlyRemarkLabel, monthlyRemarkField,
                monthlySetButton);
        monthlyBox.setPadding(new Insets(10));
        monthlyBox.setStyle("-fx-border-color: gray; -fx-border-radius: 5px; -fx-padding: 10px;");

        // 将年度和月目标设置区域组合到一起
        HBox inputRow = new HBox(20, annualBox, monthlyBox);
        inputRow.setAlignment(Pos.CENTER);
        inputRow.setPadding(new Insets(10));

        VBox container = new VBox(10, progressBox, inputRow);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(10));

        Scene scene = new Scene(container, 600, 400);
        dialog.setScene(scene);
        dialog.show();

        // 设置年度目标设置按钮的点击逻辑
        annualSetButton.setOnAction(e -> {
            try {
                double newAnnual = Double.parseDouble(annualField.getText());
                if (newAnnual <= 0) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Annual value must be positive.");
                    return;
                }
                currentUser.setAnnualTarget(newAnnual);
                double ratio = currentUser.getAnnualSavedAmount() / newAnnual;
                annualProgressBar.setProgress(ratio);
                int percent = (int) (ratio * 100);
                annualProgressLabel.setText(
                        "Annual Savings Progress: " + percent + "% (" + currentUser.getAnnualSavedAmount() + " saved)");
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
                goalLabel.setText("Monthly Saving Goal: $" + newMonthly);
                progressBar.setProgress(currentUser.getSavedAmount() / newMonthly);
                progressLabel.setText("Savings Progress: " + (int) (currentUser.getSavedAmount() / newMonthly * 100)
                        + "% (" + currentUser.getSavedAmount() + " saved)");
                showAlert(Alert.AlertType.INFORMATION, "Success", "Monthly target updated to: " + newMonthly);
                userManager.saveUserSettings(currentUser);
                dialog.close();
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Please enter a valid monthly target number.");
            }
        });
    }

    /**
     * 显示预算设置对话框。
     * 
     * @param owner 父级舞台
     */
    private void showBudgetDialog(Stage owner) {
        Stage dialog = new Stage();
        dialog.setTitle("Set Budget");
        dialog.initOwner(owner);

        // 创建预算设置表单布局
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setAlignment(Pos.CENTER);

        // 创建购物预算设置区域
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

        shoppingBox.getChildren().addAll(shoppingLabel, shoppingField, shoppingRemarkLabel, shoppingRemarkField,
                shoppingSetButton);

        // 创建交通预算设置区域
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

        transportBox.getChildren().addAll(transportLabel, transportField, transportRemarkLabel, transportRemarkField,
                transportSetButton);

        // 创建饮食预算设置区域
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

        // 创建娱乐预算设置区域
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

        amusementBox.getChildren().addAll(amusementLabel, amusementField, amusementRemarkLabel, amusementRemarkField,
                amusementSetButton);

        // 将各个预算设置区域组合到表单布局中
        grid.add(shoppingBox, 0, 0);
        grid.add(transportBox, 1, 0);
        grid.add(dietBox, 0, 1);
        grid.add(amusementBox, 1, 1);

        Scene scene = new Scene(grid, 600, 600);
        dialog.setScene(scene);
        dialog.show();

        // 设置购物预算设置按钮的点击逻辑
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
                                currentUser.getAmusementBudget());
                budgetLabel.setText("Monthly Budget: $" + currentUser.getMonthlyBudget());
                showAlert(Alert.AlertType.INFORMATION, "Success", "Shopping budget updated to: " + newBudget);
                userManager.saveUserSettings(currentUser);
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Please enter a valid budget number.");
            }
        });

        // 设置交通预算设置按钮的点击逻辑
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
                                currentUser.getAmusementBudget());
                budgetLabel.setText("Monthly Budget: $" + currentUser.getMonthlyBudget());
                showAlert(Alert.AlertType.INFORMATION, "Success", "Transport budget updated to: " + newBudget);
                userManager.saveUserSettings(currentUser);
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Please enter a valid budget number.");
            }
        });

        // 设置饮食预算设置按钮的点击逻辑
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
                                currentUser.getAmusementBudget());
                budgetLabel.setText("Monthly Budget: $" + currentUser.getMonthlyBudget());
                showAlert(Alert.AlertType.INFORMATION, "Success", "Diet budget updated to: " + newBudget);
                userManager.saveUserSettings(currentUser);
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Please enter a valid budget number.");
            }
        });

        // 设置娱乐预算设置按钮的点击逻辑
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
                                currentUser.getAmusementBudget());
                budgetLabel.setText("Monthly Budget: $" + currentUser.getMonthlyBudget());
                showAlert(Alert.AlertType.INFORMATION, "Success", "Amusement budget updated to: " + newBudget);
                userManager.saveUserSettings(currentUser);
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Please enter a valid budget number.");
            }
        });
    }

    /**
     * 显示提示对话框。
     * 
     * @param type    对话框类型
     * @param title   对话框标题
     * @param message 对话框内容
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * 用户登出逻辑。
     * 
     * @param primaryStage 主舞台
     */
    private void logout(Stage primaryStage) {
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

    /**
     * 在应用程序关闭时调用，确保定时任务能够优雅地停止。
     */
    @Override
    public void stop() throws Exception {
        super.stop();
        userManager.shutdownScheduler(); // 关闭 UserManager 的定时任务
        if (scheduler != null) {
            scheduler.shutdown(); // 关闭 DashboardView 的定时任务
        }
        Platform.exit(); // 退出 JavaFX 应用程序
        System.exit(0); // 停止执行程序
    }
}