package org.example.analysis;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.example.User;
import org.example.utils.LoadingUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import javafx.concurrent.Task;

/**
 * 支出结构饼图类
 * 展示用户各类别支出的占比和金额
 */
public class SpendingStructureChart extends BorderPane {

    private static final String TRANSACTION_FILE = "./transactions.csv";
    private final User currentUser;
    private final Map<String, Double> categoryTotals = new HashMap<>();
    private double totalSpending = 0.0;

    private StackPane rootPane;
    private ComboBox<String> periodSelector;
    private PieChart chart;
    private VBox chartContainer;
    private Label summaryLabel;

    /**
     * 构造函数
     * 
     * @param currentUser 当前用户
     */
    public SpendingStructureChart(User currentUser) {
        this.currentUser = currentUser;
        this.setPadding(new Insets(20));

        setupLayout();

        // 后台加载数据
        loadDataInBackground();
    }

    /**
     * 设置布局结构
     */
    private void setupLayout() {
        // 创建根容器为StackPane，用于显示加载指示器
        rootPane = new StackPane();
        rootPane.setPadding(new Insets(0));

        // 创建标题
        Label titleLabel = new Label("Spending Structure Analysis");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.setPadding(new Insets(0, 0, 20, 0));

        // 创建控制面板
        HBox controlPanel = createControlPanel();

        // 创建图表容器
        chartContainer = new VBox(15);
        chartContainer.setAlignment(Pos.CENTER);
        chartContainer.setPadding(new Insets(20, 0, 20, 0));

        // 创建说明文本
        Text explanationText = new Text(
                "This chart shows your spending breakdown by category. Use it to identify areas where you might reduce expenses.");
        explanationText.setWrappingWidth(800);

        // 创建总体容器
        VBox mainContainer = new VBox(15);
        mainContainer.getChildren().addAll(titleLabel, controlPanel, chartContainer, explanationText);

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
                loadTransactionData();
                return null;
            }
        };

        // 显示加载指示器并执行任务
        LoadingUtils.showLoadingIndicator(rootPane, loadDataTask, result -> {
            // 任务完成后的操作（在UI线程中执行）
            setupChart();
        });
    }

    /**
     * 创建控制面板
     */
    private HBox createControlPanel() {
        // 创建时间周期选择器
        Label periodLabel = new Label("Time Period:");
        periodLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        periodSelector = new ComboBox<>();
        periodSelector.getItems().addAll("Last Month", "Last 3 Months", "Last 6 Months", "This Year");
        periodSelector.setValue("Last 3 Months");
        periodSelector.setMinWidth(150);
        periodSelector.setOnAction(e -> refreshData());

        // 创建刷新按钮
        Button refreshButton = new Button("Refresh Data");
        refreshButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        refreshButton.setOnAction(e -> refreshData());

        HBox controlPanel = new HBox(15);
        controlPanel.setAlignment(Pos.CENTER_LEFT);
        controlPanel.getChildren().addAll(periodLabel, periodSelector, refreshButton);

        return controlPanel;
    }

    /**
     * 从transactions.csv加载交易数据
     */
    private void loadTransactionData() {
        System.out.println("开始加载交易数据...");

        // 清空之前的数据
        categoryTotals.clear();
        totalSpending = 0.0;

        // 获取选定的时间周期
        String period = periodSelector.getValue();
        LocalDate startDate = calculateHistoricalStartDate(period);

        // 尝试多个可能的文件路径
        File transactionFile = new File(TRANSACTION_FILE);
        if (!transactionFile.exists()) {
            // 尝试项目根目录
            transactionFile = new File("transactions.csv");
            if (!transactionFile.exists()) {
                // 尝试categorized版本
                transactionFile = new File("transactions_categorized.csv");
                if (!transactionFile.exists()) {
                    // 尝试从当前工作目录的根开始查找
                    transactionFile = new File(System.getProperty("user.dir"), "transactions.csv");
                }
            }
        }

        System.out.println("交易文件路径: " + transactionFile.getAbsolutePath());
        System.out.println("文件是否存在: " + transactionFile.exists());
        System.out.println("当前用户: " + (currentUser != null ? currentUser.getUsername() : "null"));
        System.out.println("开始日期: " + startDate);

        try (BufferedReader reader = new BufferedReader(new FileReader(transactionFile))) {
            // 读取标题行
            String header = reader.readLine();
            System.out.println("CSV标题: " + header);
            System.out.println("成功打开交易文件");

            // 解析标题获取列索引
            String[] headers = header.split(",");
            int userIdx = -1, dateIdx = -1, amountIdx = -1, categoryIdx = -1, descriptionIdx = -1;

            // 查找列索引
            for (int i = 0; i < headers.length; i++) {
                String col = headers[i].trim().toLowerCase();
                if (col.equals("user"))
                    userIdx = i;
                else if (col.equals("date"))
                    dateIdx = i;
                else if (col.equals("amount"))
                    amountIdx = i;
                else if (col.equals("category"))
                    categoryIdx = i;
                else if (col.equals("description"))
                    descriptionIdx = i;
            }

            // 验证必要的列是否存在
            if (dateIdx == -1 || amountIdx == -1) {
                System.err.println("CSV文件缺少必要的Date或Amount列");
                return;
            }

            // 记录列索引情况
            System.out.println("CSV列索引: User=" + userIdx +
                    ", Date=" + dateIdx +
                    ", Amount=" + amountIdx +
                    ", Category=" + categoryIdx +
                    ", Description=" + descriptionIdx);

            String line;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            int lineCount = 0;
            int processedCount = 0;

            while ((line = reader.readLine()) != null) {
                lineCount++;
                String[] parts = line.split(",");

                // 检查数据格式
                if (parts.length <= Math.max(dateIdx, amountIdx)) {
                    System.out.println("第 " + lineCount + " 行数据不足: " + line);
                    continue;
                }

                try {
                    // 解析日期
                    LocalDate date = LocalDate.parse(parts[dateIdx], formatter);

                    // 检查日期是否在选定的时间范围内
                    if (date.isBefore(startDate)) {
                        continue;
                    }

                    // 解析金额
                    double amount;
                    try {
                        amount = Double.parseDouble(parts[amountIdx]);
                    } catch (NumberFormatException e) {
                        // 尝试去除货币符号和逗号
                        String cleanAmount = parts[amountIdx].replaceAll("[^\\d.-]", "");
                        amount = Double.parseDouble(cleanAmount);
                    }

                    // 获取类别（如果存在）
                    String category;
                    if (categoryIdx != -1 && categoryIdx < parts.length) {
                        category = parts[categoryIdx].trim();
                        // 如果类别为空，使用"Uncategorized"
                        if (category.isEmpty()) {
                            category = "Uncategorized";
                        }
                    } else {
                        category = "Uncategorized";
                    }

                    System.out.println("发现交易: 日期=" + date + ", 类别=" + category + ", 金额=" + amount);

                    // 仅考虑支出（负数金额）
                    if (amount < 0) {
                        amount = Math.abs(amount); // 转为正数以便计算
                        processedCount++;

                        // 累加到类别总计
                        categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) + amount);
                        totalSpending += amount;
                        System.out.println("添加支出: " + category + " = " + amount);
                    }
                } catch (NumberFormatException e) {
                    System.out.println("解析第 " + lineCount + " 行金额出错: " + e.getMessage());
                    continue;
                } catch (Exception e) {
                    System.out.println("处理第 " + lineCount + " 行出错: " + e.getMessage());
                    e.printStackTrace();
                    continue;
                }
            }

            System.out.println("处理完成。总共处理 " + processedCount + " 条支出记录");
            System.out.println("类别总数: " + categoryTotals.size());
            System.out.println("总支出: " + totalSpending);

        } catch (IOException e) {
            System.err.println("读取交易文件出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 根据选定的时间周期计算开始日期
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
     * 设置饼图
     */
    private void setupChart() {
        // 清空图表容器
        chartContainer.getChildren().clear();

        // 如果没有数据，显示空状态
        if (categoryTotals.isEmpty() || totalSpending == 0) {
            showNoDataMessage();
            return;
        }

        // 创建饼图数据集
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        // 添加数据
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            String category = entry.getKey();
            double amount = entry.getValue();
            pieChartData.add(new PieChart.Data(category, amount));
        }

        // 创建饼图
        chart = new PieChart(pieChartData);
        chart.setTitle("Spending by Category");
        chart.setTitleSide(Side.TOP);
        chart.setLegendSide(Side.BOTTOM);
        chart.setLabelsVisible(true);
        chart.setLabelLineLength(20);
        chart.setStartAngle(90);
        chart.setAnimated(true);
        chart.setMinSize(600, 400);
        chart.setClockwise(false);

        // 存储原始类别名
        Map<String, String> originalCategories = new HashMap<>();

        // 添加数据标签提示
        for (PieChart.Data data : chart.getData()) {
            String category = data.getName();
            originalCategories.put(category, category); // 保存原始类别名

            double amount = data.getPieValue();
            double percentage = (amount / totalSpending) * 100;

            // 修改标签显示格式，包含类别名称和金额，使用人民币符号
            data.setName(String.format("%s: ¥%.2f", category, amount));

            // 创建详细提示信息，使用人民币符号
            String tooltipText = String.format("%s: ¥%.2f (%.1f%%)", category, amount, percentage);
            Tooltip tooltip = new Tooltip(tooltipText);
            Tooltip.install(data.getNode(), tooltip);

            // 鼠标悬停效果
            data.getNode().setOnMouseEntered(e -> {
                data.getNode().setScaleX(1.1);
                data.getNode().setScaleY(1.1);
            });

            data.getNode().setOnMouseExited(e -> {
                data.getNode().setScaleX(1);
                data.getNode().setScaleY(1);
            });
        }

        // 创建摘要标签，使用人民币符号
        summaryLabel = new Label(String.format("Total Expenditure: ¥%.2f", totalSpending));
        summaryLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

        // 使用内置图例，但恢复原始类别名
        // 通过反射获取图例中的标签并修改它们的文本
        try {
            for (Node legendItem : chart.lookupAll(".chart-legend-item")) {
                if (legendItem instanceof Label) {
                    Label label = (Label) legendItem;
                    String currentText = label.getText();
                    if (currentText.contains(":")) {
                        // 只保留冒号前的类别名
                        String category = currentText.split(":")[0].trim();
                        label.setText(category);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 设置布局
        chartContainer.getChildren().addAll(chart, summaryLabel);
    }

    /**
     * 显示无数据信息
     */
    private void showNoDataMessage() {
        VBox messageBox = new VBox(15);
        messageBox.setAlignment(Pos.CENTER);
        messageBox.setPadding(new Insets(50));

        Label noDataLabel = new Label("No spending data available");
        noDataLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        noDataLabel.setTextFill(Color.GRAY);

        Label infoLabel = new Label("Add transactions with negative amounts to see your spending structure");
        infoLabel.setTextFill(Color.GRAY);
        infoLabel.setFont(Font.font("System", 16));

        messageBox.getChildren().addAll(noDataLabel, infoLabel);
        chartContainer.getChildren().add(messageBox);
    }

    /**
     * 刷新数据
     */
    private void refreshData() {
        // 创建后台任务
        Task<Void> refreshTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                loadTransactionData();
                return null;
            }
        };

        // 显示加载指示器并执行任务
        LoadingUtils.showLoadingIndicator(rootPane, refreshTask, result -> {
            // 清除现有图表容器内容
            chartContainer.getChildren().clear();
            // 重新创建图表
            setupChart();
        });
    }
}