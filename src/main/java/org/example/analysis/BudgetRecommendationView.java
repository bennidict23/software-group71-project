package org.example.analysis;

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

        // 分析历史数据
        analyzeHistoricalData();

        // 生成预算建议
        generateRecommendations();

        // 显示预算建议
        displayRecommendations();
    }

    /**
     * 设置UI组件和布局
     */
    private void setupUI() {
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
        this.setCenter(mainContainer);
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
        System.out.println("开始日期: " + startDate);

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

            String line;
            while ((line = reader.readLine()) != null) {
                lineCount++;
                String[] parts = line.split(",");
                if (parts.length >= 7) {
                    try {
                        // 解析日期在索引3 (Date列)
                        LocalDate date = LocalDate.parse(parts[3], formatter);

                        // 只考虑选定时间范围内的数据
                        if (date.isBefore(startDate)) {
                            continue;
                        }

                        // 解析金额和类别
                        // 金额在索引4 (Amount列)
                        double amount = Double.parseDouble(parts[4]);
                        // 类别在索引5 (Category列)
                        String category = parts[5].trim();

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
                        }
                    } catch (Exception e) {
                        System.out.println("解析第 " + lineCount + " 行出错: " + e.getMessage());
                        // 忽略解析错误的记录
                        continue;
                    }
                }
            }

            reader.close();

            System.out.println("处理完成。总共处理 " + processedCount + " 条支出记录");
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
            showAlert(Alert.AlertType.ERROR, "Data Analysis Error",
                    "Could not analyze historical data: " + e.getMessage());
        }
    }

    /**
     * 根据选定的历史数据周期计算开始日期
     */
    private LocalDate calculateHistoricalStartDate(String period) {
        LocalDate now = LocalDate.now();

        switch (period) {
            case "Last Month":
                return now.minusMonths(1);
            case "Last 3 Months":
                return now.minusMonths(3);
            case "Last 6 Months":
                return now.minusMonths(6);
            case "This Year":
                return LocalDate.of(now.getYear(), 1, 1);
            default:
                return now.minusMonths(6); // 默认为最近6个月
        }
    }

    /**
     * 从周期字符串获取月数
     */
    private int getMonthsFromPeriod(String period) {
        switch (period) {
            case "Last Month":
                return 1;
            case "Last 3 Months":
                return 3;
            case "Last 6 Months":
                return 6;
            case "This Year":
                return LocalDate.now().getMonthValue();
            default:
                return 6;
        }
    }

    /**
     * 生成预算建议
     */
    private void generateRecommendations() {
        if (monthlyAverages.isEmpty()) {
            return;
        }

        boolean applySeasonalAdjustments = seasonalAdjustmentCheckbox.isSelected();

        // 使用AI服务生成预算建议
        recommendedBudgets = AIModelService.getBudgetRecommendations(
                monthlyAverages, applySeasonalAdjustments);

        // 计算总预算
        totalBudget = 0.0;
        for (Double amount : recommendedBudgets.values()) {
            totalBudget += amount;
        }
    }

    /**
     * 显示预算建议
     */
    private void displayRecommendations() {
        // 清空内容容器
        contentContainer.getChildren().clear();

        // 如果没有数据，显示提示信息
        if (monthlyAverages.isEmpty()) {
            showNoDataMessage();
            return;
        }

        // 创建预算表格
        createBudgetTable();

        // 更新总预算标签
        totalBudgetLabel.setText(String.format("Total Recommended Budget: ¥%.2f", totalBudget));
    }

    /**
     * 创建预算表格
     */
    private void createBudgetTable() {
        // 创建表格
        budgetTable = new TableView<>();
        budgetTable.setEditable(false);
        budgetTable.setMinHeight(400);

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
        contentContainer.getChildren().add(budgetTable);
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
        analyzeHistoricalData();
        generateRecommendations();
        displayRecommendations();
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
        // 创建基于历史数据周期的选择器
        Label basedOnLabel = new Label("Based on spending from:");
        basedOnLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        periodSelector = new ComboBox<>();
        periodSelector.getItems().addAll("Last Month", "Last 3 Months", "Last 6 Months", "This Year");
        periodSelector.setValue("Last 6 Months");
        periodSelector.setMinWidth(150);
        periodSelector.setOnAction(e -> refreshData());

        // 季节性调整复选框
        seasonalAdjustmentCheckbox = new CheckBox("Apply Seasonal Adjustments");
        seasonalAdjustmentCheckbox.setSelected(true);
        seasonalAdjustmentCheckbox.setOnAction(e -> refreshData());

        // 刷新按钮
        Button refreshButton = new Button("Refresh");
        refreshButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        refreshButton.setOnAction(e -> refreshData());

        // 应用推荐预算按钮
        Button applyBudgetButton = new Button("Apply Recommended Budget");
        applyBudgetButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        applyBudgetButton.setOnAction(e -> applyRecommendedBudgets());

        // 创建控制面板容器
        HBox controlPanel = new HBox(15);
        controlPanel.setAlignment(Pos.CENTER_LEFT);
        controlPanel.getChildren().addAll(basedOnLabel, periodSelector, seasonalAdjustmentCheckbox, refreshButton,
                applyBudgetButton);

        return controlPanel;
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