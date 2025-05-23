package org.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.analysis.SpendingStructureChart;
import org.example.dataImport.DataImportController;
import org.example.list.TransactionViewer;
import org.example.analysis.AnalysisView;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
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

    /** 由外部（DataImport）调用，标记已导入数据 */
    public static void setImportDone(boolean done) {
        importDone = done;
    }

    // DataImport控制器对象，用于处理数据导入相关操作
    private DataImportController dataImportController = null;
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

        // 初始化页面选择下拉框，添加页面选项，并设置页面切换逻辑
        pageSelector = new ComboBox<>();
        pageSelector.getItems().addAll("Dashboard", "Data Import", "Transaction Viewer", "Analysis", "Set Goal", "Set Budget", "Change Password");
        pageSelector.setPromptText("Select a page...");
        pageSelector.setOnAction(e -> {
            String selectedPage = pageSelector.getValue();
            if (selectedPage == null || selectedPage.isEmpty()) {
                return;
            }
            if ("Dashboard".equals(selectedPage)) {
                showDashboard(primaryStage);
            } else if ("Data Import".equals(selectedPage)) {
                try {
                    // 创建新的DataImportController实例并显示
                    String username = (currentUser != null) ? currentUser.getUsername() : "default_user";
                    dataImportController = new DataImportController(primaryStage, username);
                    dataImportController.show();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to open Data Import page: " + ex.getMessage());
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
        // 减少导航栏内边距
        navigationBox.setPadding(new Insets(5));

        // 创建标题标签
        Label titleLabel = new Label("Dashboard");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // 创建新的框
        VBox newBox = new VBox();
        newBox.setAlignment(Pos.CENTER);
        newBox.setPadding(new Insets(10));
        newBox.setStyle("-fx-border-color: gray; -fx-border-radius: 5px; -fx-padding: 10px;");
        // 增大高度
        newBox.setPrefHeight(250);
        HBox.setHgrow(newBox, Priority.ALWAYS);

        // 创建储蓄金额和剩余预算标签
        Label savedAmountLabel = new Label("Saved Amount: $" + (currentUser != null ? currentUser.getSavedAmount() : "N/A"));
        Label remainingBudgetLabel = new Label("Remaining Budget: $" + (currentUser != null ? calculateRemainingBudget(currentUser) : "N/A"));

        // 设置标签样式
        savedAmountLabel.setStyle("-fx-font-size: 16px; -fx-padding: 10px;");
        remainingBudgetLabel.setStyle("-fx-font-size: 16px; -fx-padding: 10px;");

        // 将标签添加到 newBox 中
        newBox.getChildren().addAll(savedAmountLabel, remainingBudgetLabel);

        // 创建另一个 VBox
        VBox anotherBox = new VBox();
        anotherBox.setAlignment(Pos.CENTER);
        anotherBox.setPadding(new Insets(10));
        anotherBox.setStyle("-fx-border-color: gray; -fx-border-radius: 5px; -fx-padding: 10px;");
        // 增大高度
        anotherBox.setPrefHeight(250);
        HBox.setHgrow(anotherBox, Priority.ALWAYS);

        // 创建饼状图
        PieChart pieChart = createPieChart();
        anotherBox.getChildren().add(pieChart);

        // 将 newBox 和 anotherBox 组合到一个 HBox 中
        HBox sideBySideBox = new HBox(10, newBox, anotherBox);
        sideBySideBox.setAlignment(Pos.CENTER);
        sideBySideBox.setMaxWidth(Double.MAX_VALUE);

        // 创建 LineChart
        lineChart = new ConsumerTrendChart(currentUser).createChart();

        // 创建 StackPane 并添加 LineChart
        StackPane chartPane = new StackPane();
        chartPane.getChildren().add(lineChart);
        chartPane.setStyle("-fx-border-color: gray; -fx-border-radius: 5px; -fx-padding: 10px;");

        // 检查是否有交易记录，如果有则显示图表
        String transactionFile = currentUser.getUsername() + "_transactions.csv";
        try (BufferedReader br = new BufferedReader(new FileReader(transactionFile))) {
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
        imageBox.setPrefSize(620, 300);

        // 将各个区域组合到主布局中
        VBox mainLayout = new VBox(15, titleLabel, navigationBox, sideBySideBox, imageBox); // 缩小间距
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setPadding(new Insets(20));

        // 创建场景并设置到主舞台
        Scene scene = new Scene(mainLayout, 1000, 800);
        primaryStage.setScene(scene);
    }

    private PieChart createPieChart() {
        // 创建 SpendingStructureChart 实例以获取数据
        SpendingStructureChart spendingStructureChart = new SpendingStructureChart(currentUser);
        // 加载数据
        spendingStructureChart.loadTransactionData();

        // 获取数据
        Map<String, Double> categoryTotals = spendingStructureChart.getCategoryTotals();
        double totalSpending = spendingStructureChart.getTotalSpending();

        // 创建饼图数据集
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        // 添加数据
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            String category = entry.getKey();
            double amount = entry.getValue();
            pieChartData.add(new PieChart.Data(category, amount));
        }

        // 创建饼图
        PieChart pieChart = new PieChart(pieChartData);
        pieChart.setTitle("Spending by Category in Last 3 Weeks");
        pieChart.setTitleSide(javafx.geometry.Side.TOP);
        pieChart.setLegendSide(javafx.geometry.Side.BOTTOM);
        pieChart.setLabelsVisible(true);
        pieChart.setLabelLineLength(20);
        pieChart.setStartAngle(90);
        pieChart.setAnimated(true);
        pieChart.setMinSize(200, 200);
        pieChart.setClockwise(false);

        return pieChart;
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