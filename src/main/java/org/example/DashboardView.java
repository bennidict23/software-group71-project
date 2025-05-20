package org.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.list.TransactionViewer;
import org.example.analysis.AnalysisView;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DashboardView extends Application {

    // 当前登录用户，静态变量，方便在类的其他方法中访问
    public static User currentUser;
    // 用户管理对象，用于处理用户相关操作
    private UserManager userManager = new UserManager();

    // 界面中的一些组件，用于展示和操作用户信息
    private Label passwordLabel;
    private Label budgetLabel;
    private Label goalLabel;

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
    // 用于显示消费趋势的折线图
    private LineChart<String, Number> lineChart;
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

        primaryStage.setTitle("User Dashboard");

        // 如果 formattedInput 对象为空，进行初始化
        if (formattedInput == null) {
            formattedInput = new FormattedInput();
        }

        // 初始化页面选择下拉框，添加页面选项，并设置页面切换逻辑
        pageSelector = new ComboBox<>();
        pageSelector.getItems().addAll("Dashboard", "Formatted Input", "Transaction Viewer", "Analysis", "Set Goal", "Set Budget", "Change Password");
        pageSelector.setPromptText("Select a page...");
        pageSelector.setOnAction(e -> {
            String selectedPage = pageSelector.getValue();
            if (selectedPage == null || selectedPage.isEmpty()) {
                return;
            }
            if ("Dashboard".equals(selectedPage)) {
                showDashboard(primaryStage);
            } else if ("Formatted Input".equals(selectedPage)) {
                try {
                    formattedInput.start(primaryStage);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else if ("Transaction Viewer".equals(selectedPage)) {
                try {
                    TransactionViewer transactionViewer = new TransactionViewer();
                    transactionViewer.start(primaryStage);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else if ("Analysis".equals(selectedPage)) {
                try {
                    new AnalysisView().start(primaryStage);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to open Analysis page: " + ex.getMessage());
                }
            } else if ("Set Goal".equals(selectedPage)) {
                GoalSettingsView goalSettingsView = new GoalSettingsView(currentUser, userManager);
                goalSettingsView.showGoalSettings(primaryStage);
            } else if ("Set Budget".equals(selectedPage)) {
                BudgetSettingsView budgetSettingsView = new BudgetSettingsView(currentUser, userManager);
                budgetSettingsView.showBudgetSettings(primaryStage);
            } else if ("Change Password".equals(selectedPage)) {
                ChangePasswordView changePasswordView = new ChangePasswordView(currentUser, userManager, primaryStage);
                changePasswordView.start(new Stage()); // 创建并显示 ChangePasswordView
            }
            pageSelector.setValue(selectedPage);
        });

        // 默认显示 Dashboard 页面
        showDashboard(primaryStage);

        primaryStage.show();

        // 启动定时任务，每5秒更新一次 savedAmount 和 annualSavedAmount
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::updateSavedAmounts, 0, 5, TimeUnit.SECONDS);
        // 启动定时任务，每5秒更新一次图表
        scheduler.scheduleAtFixedRate(this::updateChart, 0, 5, TimeUnit.SECONDS);
    }

    private void showDashboard(Stage primaryStage) {
        // 创建导航栏布局，包含页面选择下拉框
        HBox navigationBox = new HBox(pageSelector);
        navigationBox.setAlignment(Pos.TOP_CENTER);
        navigationBox.setPadding(new Insets(10));

        // 创建标题标签
        Label titleLabel = new Label("Dashboard");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // 创建个人信息区域，展示账户信息，并提供登出功能
        Label savedAmountLabel = new Label("Saved Amount: $" + (currentUser != null ? currentUser.getSavedAmount() : "N/A"));
        Label remainingBudgetLabel = new Label("Remaining Budget: $" + (currentUser != null ? calculateRemainingBudget(currentUser) : "N/A"));

        VBox personalInfoBox = new VBox(10, savedAmountLabel, remainingBudgetLabel);
        personalInfoBox.setPadding(new Insets(10));
        personalInfoBox.setStyle("-fx-border-color: gray; -fx-border-radius: 5px; -fx-padding: 10px;");
        personalInfoBox.setPrefSize(300, 100);


        VBox budgetBox = new VBox(10);
        budgetBox.setPadding(new Insets(10));
        budgetBox.setStyle("-fx-border-color: gray; -fx-border-radius: 5px; -fx-padding: 10px;");
        budgetBox.setPrefSize(300, 250);

        // 创建消费趋势区域，预留位置用于展示消费趋势图表等信息
        HBox topBox = new HBox(20, personalInfoBox, budgetBox);
        topBox.setAlignment(Pos.TOP_CENTER);
        topBox.setPadding(new Insets(10));
        // 创建 LineChart
        lineChart = new ConsumerTrendChart(currentUser).createChart();

        // 创建 StackPane 并添加 LineChart
        StackPane chartPane = new StackPane();
        chartPane.getChildren().add(lineChart);
        chartPane.setStyle("-fx-border-color: gray; -fx-border-radius: 5px; -fx-padding: 10px;");

        // 检查是否有交易记录，如果有则显示图表
        try (BufferedReader br = new BufferedReader(new FileReader("transactions.csv"))) {
            // 跳过表头
            String line = br.readLine();
            if ((line = br.readLine()) != null) {
                importDone = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        chartPane.setVisible(importDone);
        chartPane.managedProperty().bind(chartPane.visibleProperty());

        // 将图表添加到消费趋势区域
        VBox imageBox = new VBox(10, new Label("Consumer Trend"), chartPane);
        imageBox.setAlignment(Pos.TOP_CENTER);
        imageBox.setPadding(new Insets(10));
        imageBox.setStyle("-fx-border-color: gray; -fx-border-radius: 5px; -fx-padding: 10px;");
        imageBox.setPrefSize(300, 300);

        // 将各个区域组合到主布局中
        VBox mainLayout = new VBox(20, titleLabel, navigationBox, topBox, imageBox);
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setPadding(new Insets(20));

        // 创建场景并设置到主舞台
        Scene scene = new Scene(mainLayout, 1000, 800);
        primaryStage.setScene(scene);
    }

    private void updateChart() {
        // 调用 UserManager 的 checkTransactionsFile 方法检测文件变化
        userManager.checkTransactionsFile();

        // 更新图表数据
        Platform.runLater(() -> {
            // 清空旧数据
            lineChart.getData().clear();

            // 重新计算并添加新数据
            LineChart<String, Number> newChart = new ConsumerTrendChart(currentUser).createChart();
            lineChart.getData().addAll(newChart.getData());
        });
    }

    /**
     * 更新 savedAmount 和 annualSavedAmount，并刷新界面显示。
     */
    private void updateSavedAmounts() {
        // 调用 UserManager 的 checkTransactionsFile 方法更新 savedAmount 和 annualSavedAmount
        userManager.checkTransactionsFile();

    }

    private double calculateRemainingBudget(User user) {
        if (user == null) {
            return 0.0;
        }
        // 获取本月总预算
        double monthlyBudget = user.getMonthlyBudget();
        // 获取本月净支出
        double monthlyExpenses = userManager.getMonthlyTotalExpenses(user);
        // 剩余预算 = 本月总预算 - 本月净支出
        return monthlyBudget - monthlyExpenses;
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
        // 停止定时任务
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
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