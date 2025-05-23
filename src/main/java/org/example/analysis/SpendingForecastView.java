package org.example.analysis;

import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.example.DashboardView;
import org.example.User;
import org.example.utils.LoadingUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 支出预测视图
 * 分析用户历史支出数据并提供未来支出预测
 */
public class SpendingForecastView extends BorderPane {

    private static final String TRANSACTION_FILE_PATTERN = "%s_transactions.csv";
    private final User currentUser;

    // 历史数据
    private final Map<YearMonth, Double> monthlySpending = new TreeMap<>();

    // 预测数据
    private Map<String, Double> forecastSpending = new LinkedHashMap<>();

    // UI组件
    private StackPane rootPane;
    private VBox contentContainer;
    private ComboBox<String> historicalPeriodSelector;
    private ComboBox<String> forecastPeriodSelector;
    private CheckBox useAIPredictionCheckbox;
    private LineChart<Number, Number> lineChart;

    /**
     * 构造函数
     * 
     * @param currentUser 当前用户
     */
    public SpendingForecastView(User currentUser) {
        this.currentUser = currentUser;
        this.setPadding(new Insets(20));

        // 创建UI布局
        setupUI();

        // 后台加载数据
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
        Label titleLabel = new Label("Spending Forecast Analysis");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.setPadding(new Insets(0, 0, 20, 0));

        // 创建控制面板
        GridPane controlPanel = createControlPanel();

        // 创建内容容器
        contentContainer = new VBox(20);
        contentContainer.setAlignment(Pos.CENTER);

        // 创建说明文本
        Text explanationText = new Text(
                "This chart shows your historical spending patterns and forecasts future spending based on trends. "
                        + "When AI prediction is enabled, the forecast considers seasonal factors and Chinese holidays.");
        explanationText.setWrappingWidth(800);

        // 创建总体容器
        VBox mainContainer = new VBox(15);
        mainContainer.getChildren().addAll(titleLabel, controlPanel, contentContainer, explanationText);

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
                loadHistoricalData();
                generateForecast();
                return null;
            }
        };

        // 显示加载指示器并执行任务
        LoadingUtils.showLoadingIndicator(rootPane, loadDataTask, result -> {
            // 任务完成后的操作（在UI线程中执行）
            createChart();
        });
    }

    /**
     * 创建控制面板
     */
    private GridPane createControlPanel() {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        grid.setAlignment(Pos.CENTER_LEFT);

        // 历史数据周期选择
        Label historicalLabel = new Label("Historical Data:");
        historicalLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        historicalPeriodSelector = new ComboBox<>();
        historicalPeriodSelector.getItems().addAll("All Available Data", "Last 3 Months", "Last 6 Months", "Last Year");
        historicalPeriodSelector.setValue("All Available Data");
        historicalPeriodSelector.setMinWidth(150);
        historicalPeriodSelector.setOnAction(e -> refreshData());

        // 预测周期选择
        Label forecastLabel = new Label("Forecast Period:");
        forecastLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        forecastPeriodSelector = new ComboBox<>();
        forecastPeriodSelector.getItems().addAll("Next 3 Months", "Next 6 Months", "Next Year");
        forecastPeriodSelector.setValue("Next 3 Months");
        forecastPeriodSelector.setMinWidth(150);
        forecastPeriodSelector.setOnAction(e -> refreshData());

        // AI预测选项
        useAIPredictionCheckbox = new CheckBox("Use AI Prediction");
        useAIPredictionCheckbox.setSelected(true);
        useAIPredictionCheckbox.setOnAction(e -> refreshData());

        // 刷新按钮
        Button refreshButton = new Button("Refresh Data");
        refreshButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        refreshButton.setOnAction(e -> refreshData());

        // 添加控件到网格
        grid.add(historicalLabel, 0, 0);
        grid.add(historicalPeriodSelector, 1, 0);
        grid.add(forecastLabel, 2, 0);
        grid.add(forecastPeriodSelector, 3, 0);
        grid.add(useAIPredictionCheckbox, 0, 1);
        grid.add(refreshButton, 3, 1);

        return grid;
    }

    /**
     * 加载历史消费数据
     */
    private void loadHistoricalData() {
        System.out.println("开始加载历史消费数据...");

        // 清空之前的数据
        monthlySpending.clear();

        // 获取选定的历史数据周期
        String period = historicalPeriodSelector.getValue();
        LocalDate startDate = calculateHistoricalStartDate(period);

        // 使用基于用户名的文件名
        String transactionFileName = String.format(TRANSACTION_FILE_PATTERN, currentUser.getUsername());
        File transactionFile = new File(transactionFileName);

        System.out.println("交易文件路径: " + transactionFile.getAbsolutePath());
        System.out.println("文件是否存在: " + transactionFile.exists());
        System.out.println("当前用户: " + (currentUser != null ? currentUser.getUsername() : "null"));
        System.out.println("分析数据的开始日期: " + startDate);
        System.out.println("选定的数据周期: " + period);

        try (BufferedReader reader = new BufferedReader(new FileReader(transactionFile))) {
            // 跳过标题行
            String header = reader.readLine();
            System.out.println("CSV标题: " + header);
            System.out.println("成功打开交易文件");

            String line;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            int lineCount = 0;
            int processedCount = 0;
            int skippedCount = 0;
            int dateFilteredCount = 0;

            while ((line = reader.readLine()) != null) {
                lineCount++;
                String[] parts = line.split(",");

                // 验证数据格式
                if (parts.length < 6) {
                    System.out
                            .println("跳过第 " + lineCount + " 行: 列数不足 (需要至少6列), 实际列数: " + parts.length + ", 数据: " + line);
                    skippedCount++;
                    continue;
                }

                try {
                    // 解析日期在索引3 (Date列)
                    LocalDate date = LocalDate.parse(parts[3], formatter);

                    // 检查日期是否在选定的时间范围内
                    if (date.isBefore(startDate)) {
                        dateFilteredCount++;
                        continue;
                    }

                    // 金额在索引4 (Amount列)
                    double amount = Double.parseDouble(parts[4]);

                    System.out.println("处理第 " + lineCount + " 行: 日期=" + date + ", 金额=" + amount);

                    // 仅考虑支出（正数金额）
                    if (amount > 0) {
                        amount = Math.abs(amount); // 转为正数以便计算
                        YearMonth month = YearMonth.from(date);
                        processedCount++;

                        // 累加到月度总计
                        monthlySpending.put(month, monthlySpending.getOrDefault(month, 0.0) + amount);
                        System.out.println("添加月度支出: " + month + " = " + amount);
                    } else {
                        System.out.println("跳过第 " + lineCount + " 行: 非支出交易 (金额不是正数)");
                        skippedCount++;
                    }
                } catch (Exception e) {
                    System.out.println("解析第 " + lineCount + " 行出错: " + e.getMessage() + ", 原始数据: " + line);
                    skippedCount++;
                    continue;
                }
            }

            System.out.println("处理完成。总行数: " + lineCount);
            System.out.println("有效支出记录数: " + processedCount);
            System.out.println("因日期范围被过滤的行数: " + dateFilteredCount);
            System.out.println("被跳过的行数: " + skippedCount);
            System.out.println("月度数据总数: " + monthlySpending.size());

        } catch (IOException e) {
            System.err.println("读取交易文件出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 根据选定的历史数据周期计算开始日期
     */
    private LocalDate calculateHistoricalStartDate(String period) {
        LocalDate now = LocalDate.now();

        switch (period) {
            case "Last 3 Months":
                return now.minusMonths(3);
            case "Last 6 Months":
                return now.minusMonths(6);
            case "Last Year":
                return now.minusYears(1);
            case "All Available Data":
                return LocalDate.of(2000, 1, 1); // 非常早的日期，包含所有数据
            default:
                return now.minusMonths(6); // 默认为最近6个月
        }
    }

    /**
     * 生成支出预测
     */
    private void generateForecast() {
        // 如果历史数据不足，无法进行预测
        if (monthlySpending.size() < 2) {
            return;
        }

        // 获取预测周期
        String forecastPeriod = forecastPeriodSelector.getValue();
        int months = getForecastMonths(forecastPeriod);

        // 决定是否使用AI预测
        boolean useAI = useAIPredictionCheckbox.isSelected();

        // 将月度支出转换为格式适合AI模型的数据
        Map<String, Double> historicalData = new LinkedHashMap<>();
        for (Map.Entry<YearMonth, Double> entry : monthlySpending.entrySet()) {
            historicalData.put(entry.getKey().toString(), entry.getValue());
        }

        // 使用AI服务生成预测
        forecastSpending = AIModelService.getForecastSpending(historicalData, months);
    }

    /**
     * 获取预测月数
     */
    private int getForecastMonths(String period) {
        switch (period) {
            case "Next 3 Months":
                return 3;
            case "Next 6 Months":
                return 6;
            case "Next Year":
                return 12;
            default:
                return 3;
        }
    }

    /**
     * 创建预测图表
     */
    private void createChart() {
        // 清空之前的内容
        contentContainer.getChildren().clear();

        // 如果没有足够的历史数据，显示提示信息
        if (monthlySpending.size() < 2) {
            showInsufficientDataMessage();
            return;
        }

        // 如果AI预测开启，显示信息
        if (useAIPredictionCheckbox.isSelected()) {
            Label aiInfoLabel = new Label(
                    "AI prediction is enabled. Forecasts include seasonal patterns and Chinese holiday factors.");
            aiInfoLabel.setTextFill(Color.GREEN);
            aiInfoLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
            contentContainer.getChildren().add(aiInfoLabel);
        }

        // 创建坐标轴
        NumberAxis xAxis = new NumberAxis(1, monthlySpending.size() + forecastSpending.size(), 1);
        xAxis.setLabel("Month");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Spending Amount ($)");

        // 创建线图
        lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Spending Forecast");
        lineChart.setCreateSymbols(true);
        lineChart.setAnimated(true);
        lineChart.setLegendVisible(true);
        lineChart.setPrefHeight(400); // 减小图表高度，给分析区域留更多空间

        // 历史数据系列
        XYChart.Series<Number, Number> historicalSeries = new XYChart.Series<>();
        historicalSeries.setName("Historical Spending");

        // 预测数据系列
        XYChart.Series<Number, Number> forecastSeries = new XYChart.Series<>();
        forecastSeries.setName("AI Forecasted Spending");

        // 填充历史数据
        int index = 1;
        Map<Integer, String> monthLabels = new HashMap<>();

        for (Map.Entry<YearMonth, Double> entry : monthlySpending.entrySet()) {
            historicalSeries.getData().add(new XYChart.Data<>(index, entry.getValue()));
            monthLabels.put(index, entry.getKey().getMonth().toString() + " " + entry.getKey().getYear());
            index++;
        }

        // 填充预测数据
        YearMonth lastHistoricalMonth = null;
        if (!monthlySpending.isEmpty()) {
            lastHistoricalMonth = monthlySpending.keySet().stream().reduce((first, second) -> second).orElse(null);
        }

        if (lastHistoricalMonth == null) {
            lastHistoricalMonth = YearMonth.now();
        }

        int i = 0;
        for (Map.Entry<String, Double> entry : forecastSpending.entrySet()) {
            forecastSeries.getData().add(new XYChart.Data<>(index, entry.getValue()));

            // 计算正确的年月
            YearMonth forecastMonth = lastHistoricalMonth.plusMonths(i + 1);
            monthLabels.put(index, forecastMonth.getMonth().toString() + " " + forecastMonth.getYear());

            index++;
            i++;
        }

        // 自定义X轴标签
        xAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(xAxis) {
            @Override
            public String toString(Number object) {
                int index = object.intValue();
                if (monthLabels.containsKey(index)) {
                    return monthLabels.get(index);
                }
                return "";
            }
        });

        // 添加数据系列到图表
        lineChart.getData().addAll(historicalSeries, forecastSeries);

        // 为预测系列设置不同的样式
        for (XYChart.Data<Number, Number> data : forecastSeries.getData()) {
            data.getNode().setStyle("-fx-stroke: orange; -fx-stroke-width: 2px;");
        }

        // 添加到容器
        contentContainer.getChildren().add(lineChart);

        // 添加AI预测说明文本区域
        String explanationText = AIModelService.getLatestForecastExplanation();
        if (explanationText != null && !explanationText.isEmpty()) {
            VBox explanationBox = new VBox(10);
            explanationBox.setPadding(new Insets(20, 0, 0, 0));

            Label explainTitle = new Label("AI Forecast Analysis:");
            explainTitle.setFont(Font.font("System", FontWeight.BOLD, 16));

            TextArea explainArea = new TextArea(explanationText);
            explainArea.setEditable(false);
            explainArea.setWrapText(true);
            explainArea.setMaxWidth(Double.MAX_VALUE); // 使用最大宽度
            explainArea.setPrefHeight(250); // 增加文本区域高度
            explainArea.setStyle("-fx-background-color: #f8f8f8; -fx-border-color: #e0e0e0; -fx-font-size: 14px;");

            explanationBox.getChildren().addAll(explainTitle, explainArea);
            explanationBox.setMaxWidth(Double.MAX_VALUE); // 让解释区域框占满可用宽度
            HBox.setHgrow(explanationBox, Priority.ALWAYS); // 允许水平增长

            contentContainer.getChildren().add(explanationBox);
        }
    }

    /**
     * 显示数据不足提示
     */
    private void showInsufficientDataMessage() {
        VBox messageBox = new VBox(15);
        messageBox.setAlignment(Pos.CENTER);
        messageBox.setPadding(new Insets(50));

        Label titleLabel = new Label("Insufficient Data");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));

        Label messageLabel = new Label("At least 2 months of spending data are required for forecasting.");
        messageLabel.setTextFill(Color.GRAY);

        Label actionLabel = new Label("Please add more transaction records and try again.");
        actionLabel.setTextFill(Color.GRAY);

        messageBox.getChildren().addAll(titleLabel, messageLabel, actionLabel);
        contentContainer.getChildren().add(messageBox);
    }

    /**
     * 刷新数据
     */
    private void refreshData() {
        // 创建后台任务
        Task<Void> refreshTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                loadHistoricalData();
                generateForecast();
                return null;
            }
        };

        // 显示加载指示器并执行任务
        LoadingUtils.showLoadingIndicator(rootPane, refreshTask, result -> {
            // 清除现有图表
            contentContainer.getChildren().clear();
            // 重新创建图表
            createChart();
        });
    }
}