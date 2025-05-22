package org.example.analysis;

import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.example.DashboardView;
import org.example.User;
import org.example.UserManager;
import org.example.utils.LoadingUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 预算推荐视图 - 基于用户历史支出提供个性化预算分配建议
 */
public class BudgetRecommendationView extends BorderPane {

    private static final String TRANSACTION_FILE = "./transactions.csv";
    private final User currentUser;
    private final UserManager userManager;
    private final Map<String, Double> monthlyAverages = new HashMap<>();
    private Map<String, Double> recommendedBudgets = new HashMap<>();

    // UI组件
    private StackPane rootPane;
    private VBox contentContainer;
    private TableView<BudgetItem> budgetTable;
    private ComboBox<String> periodSelector;
    private CheckBox seasonalAdjustmentCheckbox;
    private Label totalBudgetLabel;
    private double totalBudget = 0.0;

    /**
     * 构造函数
     */
    public BudgetRecommendationView() {
        this.currentUser = DashboardView.getCurrentUser();
        this.userManager = new UserManager();

        setPadding(new Insets(20));

        // 创建UI布局
        setupUI();

        // 加载数据（移至后台线程）
        loadDataInBackground();
    }

    /**
     * 设置UI组件和布局
     */
    private void setupUI() {
        // 创建根容器为StackPane，用于显示加载指示器
        rootPane = new StackPane();
        rootPane.setPadding(new Insets(0));

        // 创建标题
        Label titleLabel = new Label("Intelligent Budget Recommendations");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.setPadding(new Insets(0, 0, 20, 0));

        // 创建控制面板
        HBox controlPanel = createControlPanel();

        // 创建内容容器
        contentContainer = new VBox(20);
        contentContainer.setAlignment(Pos.CENTER);
        contentContainer.setPadding(new Insets(10));

        // 创建总预算标签
        totalBudgetLabel = new Label();
        totalBudgetLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

        // 创建说明文本
        Label explanationLabel = new Label(
                "These budget recommendations are based on your historical spending patterns, "
                        + "adjusted for seasonal factors and optimized for your financial goals.");
        explanationLabel.setWrapText(true);
        explanationLabel.setMaxWidth(800);

        // 创建总体容器
        VBox mainContainer = new VBox(15);
        mainContainer.getChildren().addAll(titleLabel, controlPanel, contentContainer, totalBudgetLabel,
                explanationLabel);

        // 将主容器添加到根容器
        rootPane.getChildren().add(mainContainer);

        // 设置根容器为当前BorderPane的中心
        this.setCenter(rootPane);
    }

    /**
     * 在后台线程中加载数据
     */
    private void loadDataInBackground() {
        // 创建后台任务
        Task<Void> loadDataTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // 执行耗时操作
                analyzeHistoricalData();
                generateRecommendations();
                return null;
            }
        };

        // 显示加载指示器并执行任务
        LoadingUtils.showLoadingIndicator(rootPane, loadDataTask, result -> {
            // 任务完成后的操作（在UI线程中执行）
            displayRecommendations();
        });
    }

    /**
     * 分析历史消费数据
     */
    private void analyzeHistoricalData() {
        System.out.println("开始分析历史消费数据...");

        // 清空之前的数据
        monthlyAverages.clear();

        // 获取选定的历史数据周期
        String period = periodSelector.getValue();
        LocalDate startDate = calculateHistoricalStartDate(period);

        // 尝试多个可能的文件路径
        File transactionFile = new File(TRANSACTION_FILE);
        if (!transactionFile.exists()) {
            // 尝试项目根目录
            transactionFile = new File("transactions.csv");
            if (!transactionFile.exists()) {
                // 尝试从当前工作目录的根开始查找
                transactionFile = new File(System.getProperty("user.dir"), "transactions.csv");
            }
        }

        System.out.println("交易文件路径: " + transactionFile.getAbsolutePath());
        System.out.println("文件是否存在: " + transactionFile.exists());
        System.out.println("当前用户: " + (currentUser != null ? currentUser.getUsername() : "null"));
        System.out.println("分析数据的开始日期: " + startDate);
        System.out.println("选定的数据周期: " + period);

        try {
            BufferedReader reader = new BufferedReader(new FileReader(transactionFile));
            System.out.println("成功打开交易文件");

            // 跳过标题行
            String header = reader.readLine();
            System.out.println("CSV标题: " + header);

            // 按类别统计支出
            Map<String, List<Double>> categoryExpenses = new HashMap<>();

            // 日期格式化
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            int lineCount = 0;
            int processedCount = 0;
            int skippedCount = 0;
            int dateFilteredCount = 0;

            String line;
            while ((line = reader.readLine()) != null) {
                lineCount++;
                String[] parts = line.split(",");
                if (parts.length >= 6) {
                    try {
                        // 解析日期在索引2 (Date列)
                        LocalDate date = LocalDate.parse(parts[2], formatter);

                        // 只考虑选定时间范围内的数据
                        if (date.isBefore(startDate)) {
                            dateFilteredCount++;
                            continue;
                        }

                        // 解析金额和类别
                        // 金额在索引3 (Amount列)
                        double amount = Double.parseDouble(parts[3]);
                        // 类别在索引4 (Category列)
                        String category = parts[4].trim();

                        System.out
                                .println("处理第 " + lineCount + " 行: 日期=" + date + ", 金额=" + amount + ", 类别=" + category);

                        // 只考虑支出（负数金额）
                        if (amount < 0) {
                            amount = Math.abs(amount); // 转为正数用于计算
                            processedCount++;

                            // 添加到类别统计
                            if (!categoryExpenses.containsKey(category)) {
                                categoryExpenses.put(category, new ArrayList<>());
                            }
                            categoryExpenses.get(category).add(amount);
                            System.out.println("添加类别支出: " + category + " = " + amount);
                        } else {
                            System.out.println("跳过第 " + lineCount + " 行: 非支出交易 (金额不是负数)");
                            skippedCount++;
                        }
                    } catch (Exception e) {
                        System.out.println("解析第 " + lineCount + " 行出错: " + e.getMessage() + ", 原始数据: " + line);
                        skippedCount++;
                        // 忽略解析错误的记录
                        continue;
                    }
                } else {
                    System.out.println("跳过第 " + lineCount + " 行: 列数不足 (需要至少6列), 实际列数: " + parts.length);
                    skippedCount++;
                }
            }

            reader.close();

            System.out.println("处理完成。总行数: " + lineCount);
            System.out.println("有效支出记录数: " + processedCount);
            System.out.println("因日期范围被过滤的行数: " + dateFilteredCount);
            System.out.println("被跳过的行数: " + skippedCount);
            System.out.println("支出类别数: " + categoryExpenses.size());

            // 计算每个类别的月均支出
            for (Map.Entry<String, List<Double>> entry : categoryExpenses.entrySet()) {
                String category = entry.getKey();
                List<Double> expenses = entry.getValue();

                double total = 0.0;
                for (Double expense : expenses) {
                    total += expense;
                }

                // 计算月平均支出
                double monthlyAverage = total / getMonthsFromPeriod(period);
                monthlyAverages.put(category, monthlyAverage);
                System.out.println("月平均支出: " + category + " = " + monthlyAverage);
            }

        } catch (Exception e) {
            System.err.println("分析历史数据出错: " + e.getMessage());
            e.printStackTrace();
            // 不在后台线程弹出UI提示，只记录错误
        }
    }

    /**
     * 根据选定的历史数据周期计算开始日期
     */
    private LocalDate calculateHistoricalStartDate(String period) {
        LocalDate now = LocalDate.now();

        switch (period) {
            case "All Data":
                return LocalDate.of(2000, 1, 1); // 返回一个非常早的日期，包含所有数据
            case "Last Month":
                return now.minusMonths(1);
            case "Last 3 Months":
                return now.minusMonths(3);
            case "Last 6 Months":
                return now.minusMonths(6);
            case "This Year":
                return LocalDate.of(now.getYear(), 1, 1);
            default:
                return LocalDate.of(2000, 1, 1); // 默认包含所有数据
        }
    }

    /**
     * 从周期字符串获取月数
     */
    private int getMonthsFromPeriod(String period) {
        switch (period) {
            case "All Data":
                return 60; // 假设5年的跨度
            case "Last Month":
                return 1;
            case "Last 3 Months":
                return 3;
            case "Last 6 Months":
                return 6;
            case "This Year":
                return LocalDate.now().getMonthValue();
            default:
                return 60; // 默认为5年
        }
    }

    /**
     * 生成预算建议
     */
    private void generateRecommendations() {
        if (monthlyAverages.isEmpty()) {
            recommendedBudgets = new HashMap<>();
            return;
        }

        boolean applySeasonalAdjustments = seasonalAdjustmentCheckbox.isSelected();

        // 使用AI服务生成预算建议
        recommendedBudgets = AIModelService.getBudgetRecommendations(monthlyAverages, applySeasonalAdjustments);

        // 计算总预算
        totalBudget = 0.0;
        for (Double amount : recommendedBudgets.values()) {
            totalBudget += amount;
        }

        // 更新总预算显示
        updateTotalBudgetLabel();
    }

    /**
     * 显示预算建议
     */
    private void displayRecommendations() {
        if (monthlyAverages.isEmpty()) {
            showNoDataMessage();
            return;
        }

        // 更新预算表格
        createBudgetTable();

        // 添加预算分析文本
        addBudgetAnalysisText();
    }

    /**
     * 创建预算表格
     */
    private void createBudgetTable() {
        // 清空内容容器
        contentContainer.getChildren().clear();

        // 创建表格
        budgetTable = new TableView<>();
        budgetTable.setEditable(false);
        budgetTable.setMinHeight(250); // 进一步减小表格的最小高度
        budgetTable.setPrefHeight(250); // 设置首选高度
        budgetTable.setMaxHeight(250); // 限制最大高度

        // 创建列
        TableColumn<BudgetItem, String> categoryColumn = new TableColumn<>("Category");
        categoryColumn.setCellValueFactory(cellData -> cellData.getValue().categoryProperty());
        categoryColumn.setPrefWidth(150);

        TableColumn<BudgetItem, String> budgetColumn = new TableColumn<>("Recommended Budget");
        budgetColumn.setCellValueFactory(cellData -> cellData.getValue().budgetProperty());
        budgetColumn.setPrefWidth(200);
        budgetColumn.setStyle("-fx-alignment: CENTER-RIGHT;");

        TableColumn<BudgetItem, String> noteColumn = new TableColumn<>("Seasonal Note");
        noteColumn.setCellValueFactory(cellData -> cellData.getValue().noteProperty());
        noteColumn.setPrefWidth(300);

        // 添加列到表格
        budgetTable.getColumns().addAll(categoryColumn, budgetColumn, noteColumn);

        // 填充数据
        List<BudgetItem> items = new ArrayList<>();
        for (Map.Entry<String, Double> entry : recommendedBudgets.entrySet()) {
            String category = entry.getKey();
            double budget = entry.getValue();
            String note = "Based on typical seasonal patterns";

            items.add(new BudgetItem(category, String.format("¥%.2f", budget), note));
        }

        // 按预算金额排序（降序）
        items.sort((a, b) -> {
            double amountA = Double.parseDouble(a.getBudget().substring(1));
            double amountB = Double.parseDouble(b.getBudget().substring(1));
            return Double.compare(amountB, amountA);
        });

        // 设置数据到表格
        budgetTable.getItems().addAll(items);

        // 添加表格到容器
        Label tableTitle = new Label("Recommended Budget Allocation");
        tableTitle.setFont(Font.font("System", FontWeight.BOLD, 16));

        VBox tableBox = new VBox(10, tableTitle, budgetTable);
        tableBox.setPadding(new Insets(0, 0, 10, 0));
        contentContainer.getChildren().add(tableBox);
    }

    /**
     * 添加预算分析文本
     */
    private void addBudgetAnalysisText() {
        // 从AIModelService获取分析文本
        String analysisText = AIModelService.getLatestBudgetRecommendationAnalysis();

        if (analysisText != null && !analysisText.isEmpty()) {
            VBox analysisBox = new VBox(10);
            analysisBox.setPadding(new Insets(10, 0, 0, 0));
            analysisBox.setStyle(
                    "-fx-background-color: #f5f9ff; -fx-border-color: #b8d0e8; -fx-border-radius: 5px; -fx-padding: 15px;");

            Label analysisTitle = new Label("Budget Analysis and Recommendations:");
            analysisTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
            analysisTitle.setStyle("-fx-text-fill: #2c5777;");

            TextArea analysisArea = new TextArea(analysisText);
            analysisArea.setEditable(false);
            analysisArea.setWrapText(true);
            analysisArea.setMaxWidth(Double.MAX_VALUE);
            analysisArea.setPrefHeight(400); // 增加文本区域高度
            analysisArea.setMinHeight(350); // 设置最小高度
            analysisArea.setStyle(
                    "-fx-background-color: #f8f8f8; -fx-border-color: #e0e0e0; -fx-font-size: 14px; -fx-control-inner-background: #ffffff;");

            // 设置文本区域可以自由滚动
            ScrollPane scrollPane = new ScrollPane(analysisArea);
            scrollPane.setFitToWidth(true);
            scrollPane.setMinHeight(350);
            scrollPane.setPrefHeight(400);
            scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

            analysisBox.getChildren().addAll(analysisTitle, scrollPane);
            analysisBox.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(analysisBox, Priority.ALWAYS);

            contentContainer.getChildren().add(analysisBox);
        }
    }

    /**
     * 显示无数据提示
     */
    private void showNoDataMessage() {
        VBox messageBox = new VBox(15);
        messageBox.setAlignment(Pos.CENTER);
        messageBox.setPadding(new Insets(50));

        Label titleLabel = new Label("No Transaction Data Available");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 20));

        Label descLabel = new Label(
                "We don't have enough transaction data to generate personalized budget recommendations. " +
                        "Please add more transaction records and try again.");
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(500);

        messageBox.getChildren().addAll(titleLabel, descLabel);
        contentContainer.getChildren().add(messageBox);
    }

    /**
     * 显示提示对话框
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * 刷新数据
     */
    private void refreshData() {
        // 创建后台任务
        Task<Void> refreshTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                analyzeHistoricalData();
                generateRecommendations();
                return null;
            }
        };

        // 显示加载指示器并执行任务
        LoadingUtils.showLoadingIndicator(rootPane, refreshTask, result -> {
            displayRecommendations();
        });
    }

    /**
     * 应用推荐预算
     */
    private void applyRecommendedBudgets() {
        if (currentUser != null) {
            // 设置总预算
            currentUser.setMonthlyBudget(totalBudget);

            // 更新数据库中的用户预算
            boolean success = userManager.updateUserBudget(currentUser.getUsername(), totalBudget);

            // 根据结果显示反馈
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Budget Updated",
                        "Your monthly budget has been updated successfully.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Update Failed",
                        "Failed to update your monthly budget. Please try again later.");
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "User Error",
                    "No user is currently logged in. Please log in first.");
        }
    }

    /**
     * 创建控制面板
     */
    private HBox createControlPanel() {
        HBox controlBox = new HBox(15);
        controlBox.setAlignment(Pos.CENTER_LEFT);
        controlBox.setPadding(new Insets(10));

        // 创建历史数据周期选择器
        Label periodLabel = new Label("Analysis Period:");
        periodLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        // 初始化周期选择器
        periodSelector = new ComboBox<>();
        periodSelector.getItems().addAll("All Data", "Last Month", "Last 3 Months", "Last 6 Months", "This Year");
        periodSelector.setValue("All Data");
        periodSelector.setMinWidth(150);
        periodSelector.setOnAction(e -> refreshData());

        // 创建季节性调整选项
        seasonalAdjustmentCheckbox = new CheckBox("Apply Seasonal Adjustments");
        seasonalAdjustmentCheckbox.setSelected(true);
        seasonalAdjustmentCheckbox.setOnAction(e -> refreshData());

        // 刷新按钮
        Button refreshButton = new Button("Refresh Data");
        refreshButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        refreshButton.setOnAction(e -> refreshData());

        // 应用建议按钮
        Button applyButton = new Button("Apply Recommendations");
        applyButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        applyButton.setOnAction(e -> applyRecommendedBudgets());

        controlBox.getChildren().addAll(periodLabel, periodSelector, seasonalAdjustmentCheckbox, refreshButton,
                applyButton);
        return controlBox;
    }

    /**
     * 更新总预算标签
     */
    private void updateTotalBudgetLabel() {
        totalBudgetLabel.setText(String.format("Total Recommended Budget: ¥%.2f", totalBudget));
    }

    /**
     * 预算项目类
     */
    public static class BudgetItem {
        private final javafx.beans.property.SimpleStringProperty category;
        private final javafx.beans.property.SimpleStringProperty budget;
        private final javafx.beans.property.SimpleStringProperty note;

        public BudgetItem(String category, String budget, String note) {
            this.category = new javafx.beans.property.SimpleStringProperty(category);
            this.budget = new javafx.beans.property.SimpleStringProperty(budget);
            this.note = new javafx.beans.property.SimpleStringProperty(note);
        }

        public String getCategory() {
            return category.get();
        }

        public String getBudget() {
            return budget.get();
        }

        public String getNote() {
            return note.get();
        }

        public javafx.beans.property.StringProperty categoryProperty() {
            return category;
        }

        public javafx.beans.property.StringProperty budgetProperty() {
            return budget;
        }

        public javafx.beans.property.StringProperty noteProperty() {
            return note;
        }
    }
}