package org.example.analysis;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.example.User;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 支出预测视图
 * 分析用户历史支出数据并提供未来支出预测
 */
public class SpendingForecastView extends BorderPane {

    private static final String TRANSACTION_FILE = "transactions.csv";
    private final User currentUser;

    // 历史数据
    private final Map<YearMonth, Double> monthlySpending = new TreeMap<>();

    // 预测数据（未来3个月）
    private Map<YearMonth, Double> forecastSpending = new HashMap<>();

    // 中国传统节日数据（示例）
    private static final Map<String, Double> SEASONAL_FACTORS = new HashMap<>();

    static {
        // 初始化季节性因素（节日可能增加支出的因素）
        // 例如：春节期间支出可能增加50%
        SEASONAL_FACTORS.put("01", 1.5); // 1月 - 春节
        SEASONAL_FACTORS.put("05", 1.2); // 5月 - 劳动节
        SEASONAL_FACTORS.put("10", 1.3); // 10月 - 国庆节
    }

    /**
     * 构造函数
     * 
     * @param currentUser 当前用户
     */
    public SpendingForecastView(User currentUser) {
        this.currentUser = currentUser;
        this.setPadding(new Insets(15));

        // 加载历史数据
        loadHistoricalData();

        // 生成预测
        generateForecast();

        // 设置UI
        setupUI();
    }

    /**
     * 加载历史消费数据
     */
    private void loadHistoricalData() {
        try (BufferedReader reader = new BufferedReader(new FileReader(TRANSACTION_FILE))) {
            // 跳过标题行
            reader.readLine();

            String line;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");

                // 验证数据格式和用户匹配
                if (parts.length < 6)
                    continue;
                if (!parts[0].equals(currentUser.getUsername()))
                    continue;

                try {
                    // 解析日期和金额
                    LocalDate date = LocalDate.parse(parts[2], formatter);
                    double amount = Double.parseDouble(parts[3]);

                    // 仅考虑支出（负数金额）
                    if (amount < 0) {
                        amount = Math.abs(amount); // 转为正数以便计算
                        YearMonth month = YearMonth.from(date);

                        // 累加到月度总计
                        monthlySpending.put(month, monthlySpending.getOrDefault(month, 0.0) + amount);
                    }
                } catch (Exception e) {
                    // 忽略格式错误的数据
                    continue;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 生成支出预测
     */
    private void generateForecast() {
        // 如果历史数据不足，无法进行预测
        if (monthlySpending.size() < 3) {
            return;
        }

        // 获取历史月份和支出数据
        List<YearMonth> months = new ArrayList<>(monthlySpending.keySet());
        List<Double> spendings = new ArrayList<>(monthlySpending.values());

        // 获取最近一个月
        YearMonth lastMonth = months.get(months.size() - 1);

        // 简单移动平均预测方法
        double avg3Month = 0;
        for (int i = Math.max(0, spendings.size() - 3); i < spendings.size(); i++) {
            avg3Month += spendings.get(i);
        }
        avg3Month /= Math.min(3, spendings.size());

        // 预测未来3个月
        for (int i = 1; i <= 3; i++) {
            YearMonth futureMonth = lastMonth.plusMonths(i);

            // 基础预测（移动平均）
            double basicForecast = avg3Month;

            // 应用季节性因素
            String monthStr = String.format("%02d", futureMonth.getMonthValue());
            double seasonalFactor = SEASONAL_FACTORS.getOrDefault(monthStr, 1.0);

            // 最终预测值
            double finalForecast = basicForecast * seasonalFactor;

            // 存储预测结果
            forecastSpending.put(futureMonth, finalForecast);
        }
    }

    /**
     * 设置UI组件
     */
    private void setupUI() {
        // 如果没有足够数据，显示提示信息
        if (monthlySpending.size() < 3) {
            showInsufficientDataMessage();
            return;
        }

        // 创建UI组件
        VBox container = new VBox(20);
        container.setAlignment(Pos.CENTER);

        // 1. 标题
        Label titleLabel = new Label("AI Spending Forecast");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 20));

        // 2. 预测图表
        LineChart<Number, Number> lineChart = createForecastChart();

        // 3. 预测解释
        TextFlow explanationFlow = createExplanationText();

        // 添加组件到容器
        container.getChildren().addAll(titleLabel, lineChart, explanationFlow);
        this.setCenter(container);
    }

    /**
     * 创建预测图表
     */
    private LineChart<Number, Number> createForecastChart() {
        // 创建坐标轴
        NumberAxis xAxis = new NumberAxis(1, monthlySpending.size() + 3, 1);
        xAxis.setLabel("Months");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Spending Amount ($)");

        // 创建图表
        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Historical and Forecast Spending");

        // 历史数据系列
        XYChart.Series<Number, Number> historicalSeries = new XYChart.Series<>();
        historicalSeries.setName("Historical Spending");

        // 预测数据系列
        XYChart.Series<Number, Number> forecastSeries = new XYChart.Series<>();
        forecastSeries.setName("Forecast Spending");

        // 填充历史数据
        int monthIndex = 1;
        for (Map.Entry<YearMonth, Double> entry : monthlySpending.entrySet()) {
            historicalSeries.getData().add(new XYChart.Data<>(monthIndex++, entry.getValue()));
        }

        // 填充预测数据
        for (Map.Entry<YearMonth, Double> entry : forecastSpending.entrySet()) {
            forecastSeries.getData().add(new XYChart.Data<>(monthIndex++, entry.getValue()));
        }

        // 添加数据系列到图表
        lineChart.getData().addAll(historicalSeries, forecastSeries);

        // 为预测系列设置不同的样式
        for (XYChart.Data<Number, Number> data : forecastSeries.getData()) {
            data.getNode().setStyle("-fx-stroke-dash-array: 5 5;"); // 虚线
        }

        return lineChart;
    }

    /**
     * 创建预测解释文本
     */
    private TextFlow createExplanationText() {
        TextFlow textFlow = new TextFlow();
        textFlow.setPadding(new Insets(10));
        textFlow.setMaxWidth(600);

        // 标题
        Text titleText = new Text("Forecast Analysis\n\n");
        titleText.setFont(Font.font("System", FontWeight.BOLD, 16));

        // 方法说明
        Text methodText = new Text("Method: ");
        methodText.setFont(Font.font("System", FontWeight.BOLD, 14));

        Text methodDescText = new Text(
                "The forecast uses a 3-month moving average model with seasonal adjustments for Chinese holidays.\n\n");

        // 季节性因素
        Text seasonalText = new Text("Seasonal Factors: ");
        seasonalText.setFont(Font.font("System", FontWeight.BOLD, 14));

        StringBuilder seasonalFactors = new StringBuilder();
        for (Map.Entry<String, Double> entry : SEASONAL_FACTORS.entrySet()) {
            String month = "";
            switch (entry.getKey()) {
                case "01":
                    month = "January (Chinese New Year)";
                    break;
                case "05":
                    month = "May (Labor Day)";
                    break;
                case "10":
                    month = "October (National Day)";
                    break;
            }
            seasonalFactors.append("• ").append(month).append(": +").append((int) ((entry.getValue() - 1) * 100))
                    .append("%\n");
        }
        seasonalFactors.append("\n");
        Text seasonalDescText = new Text(seasonalFactors.toString());

        // 预测结果
        Text forecastText = new Text("Forecast Results: ");
        forecastText.setFont(Font.font("System", FontWeight.BOLD, 14));

        StringBuilder results = new StringBuilder();
        int i = 1;
        YearMonth lastHistoricalMonth = monthlySpending.keySet().stream().max(YearMonth::compareTo)
                .orElse(YearMonth.now());

        for (Map.Entry<YearMonth, Double> entry : forecastSpending.entrySet()) {
            YearMonth month = entry.getKey();
            double amount = entry.getValue();

            // 判断是否受季节性因素影响
            String monthStr = String.format("%02d", month.getMonthValue());
            boolean isSeasonal = SEASONAL_FACTORS.containsKey(monthStr);

            results.append("• ").append(month.getMonth()).append(" ").append(month.getYear())
                    .append(": $").append(String.format("%.2f", amount));

            if (isSeasonal) {
                results.append(" (Includes seasonal adjustment for ")
                        .append(getSeasonalFactorDesc(monthStr)).append(")");
            }

            results.append("\n");
        }

        Text resultsDescText = new Text(results.toString());

        // 将所有文本添加到TextFlow
        textFlow.getChildren().addAll(
                titleText, methodText, methodDescText,
                seasonalText, seasonalDescText,
                forecastText, resultsDescText);

        return textFlow;
    }

    /**
     * 获取季节性因素的描述
     */
    private String getSeasonalFactorDesc(String month) {
        switch (month) {
            case "01":
                return "Chinese New Year";
            case "05":
                return "Labor Day";
            case "10":
                return "National Day";
            default:
                return "";
        }
    }

    /**
     * 显示数据不足提示
     */
    private void showInsufficientDataMessage() {
        VBox messageBox = new VBox(10);
        messageBox.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Insufficient Data");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));

        Label messageLabel = new Label("At least 3 months of spending data are required for forecasting.");
        messageLabel.setTextFill(Color.GRAY);

        Label actionLabel = new Label("Please add more transaction records and try again.");
        actionLabel.setTextFill(Color.GRAY);

        messageBox.getChildren().addAll(titleLabel, messageLabel, actionLabel);
        this.setCenter(messageBox);
    }
}