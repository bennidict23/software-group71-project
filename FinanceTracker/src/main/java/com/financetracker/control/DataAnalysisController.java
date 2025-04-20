package com.financetracker.control;

import com.financetracker.entity.CategoryExpense;
import com.financetracker.entity.Transaction;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller class for handling data analysis operations.
 * This is a control class in the MVC pattern.
 */
public class DataAnalysisController {

    private final DataAccessInterface dataAccess;
    private final DeepSeekForecastClient forecastClient;
    private boolean useAiForecasting = true;

    private static DataAnalysisController instance;

    // 存储不同时间段的类别开支数据
    private final Map<Integer, List<CategoryExpense>> periodExpenses;

    /**
     * Constructs a DataAnalysisController with the specified data access interface
     * 
     * @param dataAccess The data access interface to use
     */
    public DataAnalysisController(DataAccessInterface dataAccess) {
        this.dataAccess = dataAccess;
        this.forecastClient = new DeepSeekForecastClient();
        periodExpenses = new HashMap<>();
        periodExpenses.put(3, new ArrayList<>());
        periodExpenses.put(6, new ArrayList<>());
        periodExpenses.put(12, new ArrayList<>());
    }

    /**
     * Gets a default instance of the controller with mock data
     * 
     * @return A DataAnalysisController using mock data
     */
    public static DataAnalysisController getDefaultInstance() {
        if (instance == null) {
            instance = new DataAnalysisController(new MockDataAccess());
        }
        return instance;
    }

    /**
     * Gets spending data by category for the specified time period
     * 
     * @param months Number of months to analyze
     * @return List of CategoryExpense objects with category names and expenses
     */
    public List<CategoryExpense> getSpendingByCategory(int months) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minus(months, ChronoUnit.MONTHS);

        Map<String, Double> spendingMap = dataAccess.getSpendingByCategory(startDate, endDate);

        // Convert map to list of CategoryExpense objects
        return spendingMap.entrySet().stream()
                .map(entry -> new CategoryExpense(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * Gets historical spending data for the specified time period
     * 
     * @param months Number of months of historical data to retrieve
     * @return Map with month names as keys and total spending as values
     */
    public Map<String, Double> getHistoricalSpending(int months) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minus(months, ChronoUnit.MONTHS);

        return dataAccess.getSpendingByMonth(startDate, endDate);
    }

    /**
     * Analyzes spending trends and provides a forecast for future spending.
     * If AI forecasting is enabled, uses DeepSeek API, otherwise uses simple trend
     * analysis.
     * 
     * @param historicalMonths Number of months of historical data to use
     * @param forecastMonths   Number of months to forecast
     * @return Map with month names as keys and forecasted spending as values
     */
    public Map<String, Double> getForecastedSpending(int historicalMonths, int forecastMonths) {
        // Get historical data
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minus(historicalMonths, ChronoUnit.MONTHS);
        List<Transaction> transactions = dataAccess.getTransactions(startDate, endDate);

        // 如果启用AI预测并且有交易数据，则使用DeepSeek API
        if (useAiForecasting && !transactions.isEmpty()) {
            try {
                // 将月份转换为天数
                int forecastDays = forecastMonths * 30;

                // 获取AI预测
                Map<String, Double> dailyForecast = forecastClient.getForecast(transactions, forecastDays);

                // 转换为月度预测
                return convertDailyToMonthlyForecast(dailyForecast);
            } catch (Exception e) {
                System.err.println("AI forecasting failed, falling back to trend-based forecast: " + e.getMessage());
                // 出错时回退到传统预测
                return getTrendBasedForecast(historicalMonths, forecastMonths);
            }
        } else {
            // 使用传统的趋势分析预测
            return getTrendBasedForecast(historicalMonths, forecastMonths);
        }
    }

    /**
     * Converts daily forecast to monthly forecast
     */
    private Map<String, Double> convertDailyToMonthlyForecast(Map<String, Double> dailyForecast) {
        Map<String, Double> monthlyForecast = new HashMap<>();
        DateTimeFormatter monthYearFormatter = DateTimeFormatter.ofPattern("MMM yyyy");

        // 按月分组并计算总和
        for (Map.Entry<String, Double> entry : dailyForecast.entrySet()) {
            LocalDate date = LocalDate.parse(entry.getKey());
            String monthYear = date.format(monthYearFormatter);

            // 累加同一月份的金额
            monthlyForecast.put(
                    monthYear,
                    monthlyForecast.getOrDefault(monthYear, 0.0) + entry.getValue());
        }

        return monthlyForecast;
    }

    /**
     * Gets a trend-based forecast using simple statistical methods
     */
    private Map<String, Double> getTrendBasedForecast(int historicalMonths, int forecastMonths) {
        // Get historical data first
        Map<String, Double> historical = getHistoricalSpending(historicalMonths);
        Map<String, Double> forecast = new HashMap<>();

        // Calculate the average monthly spending
        double total = 0;
        for (Double amount : historical.values()) {
            total += amount;
        }
        double avgMonthly = total / historical.size();

        // 检测是否有季节性模式
        boolean hasSeasonalPattern = detectSeasonalPattern(historical);

        // Generate forecast data
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");

        for (int i = 0; i < forecastMonths; i++) {
            LocalDate date = today.plus(i + 1, ChronoUnit.MONTHS);
            String monthKey = date.format(formatter);

            // 基本趋势：每月增长3%
            double trendFactor = 1.0 + (i * 0.03);

            // 季节性因素
            double seasonalFactor = 1.0;
            if (hasSeasonalPattern) {
                // 简单的季节性模型：节假日月份支出更高
                int month = date.getMonthValue();
                // 中国主要节日月份：1(春节)、5(五一)、10(国庆)
                if (month == 1 || month == 5 || month == 10) {
                    seasonalFactor = 1.2; // 节日月份支出增加20%
                } else if (month == 2 || month == 6 || month == 11) {
                    seasonalFactor = 1.1; // 节日后的月份支出略高
                } else if (month == 7 || month == 8) {
                    seasonalFactor = 1.15; // 暑假期间支出高
                }
            }

            // 随机波动因素
            double randomFactor = 0.95 + Math.random() * 0.1; // 0.95-1.05之间

            // 计算预测值
            forecast.put(monthKey, avgMonthly * trendFactor * seasonalFactor * randomFactor);
        }

        return forecast;
    }

    /**
     * Detects if there is a seasonal pattern in the historical data
     */
    private boolean detectSeasonalPattern(Map<String, Double> historicalData) {
        // 简单的季节性检测：检查是否有明显的月份间差异
        if (historicalData.size() < 6) {
            return false; // 数据不足以检测季节性
        }

        // 按月份分组
        Map<Integer, List<Double>> monthlyGroupedData = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");

        for (Map.Entry<String, Double> entry : historicalData.entrySet()) {
            try {
                // 解析月份名称，如"Jan 2024"
                String[] parts = entry.getKey().split(" ");
                if (parts.length == 2) {
                    LocalDate date = LocalDate.parse("01 " + entry.getKey(),
                            DateTimeFormatter.ofPattern("dd MMM yyyy"));
                    int month = date.getMonthValue();

                    if (!monthlyGroupedData.containsKey(month)) {
                        monthlyGroupedData.put(month, new ArrayList<>());
                    }
                    monthlyGroupedData.get(month).add(entry.getValue());
                }
            } catch (Exception e) {
                // 解析错误，忽略
            }
        }

        // 计算各月份的平均值
        Map<Integer, Double> monthlyAverages = new HashMap<>();
        for (Map.Entry<Integer, List<Double>> entry : monthlyGroupedData.entrySet()) {
            double sum = 0;
            for (Double value : entry.getValue()) {
                sum += value;
            }
            monthlyAverages.put(entry.getKey(), sum / entry.getValue().size());
        }

        // 计算总体平均值
        double overallAverage = 0;
        for (Double value : monthlyAverages.values()) {
            overallAverage += value;
        }
        overallAverage /= monthlyAverages.size();

        // 检查是否有月份的平均值与总体平均值相差超过20%
        for (Double monthlyAvg : monthlyAverages.values()) {
            if (Math.abs(monthlyAvg - overallAverage) / overallAverage > 0.2) {
                return true; // 检测到季节性
            }
        }

        return false;
    }

    /**
     * Gets the detailed transaction list for a specific category and time period
     * 
     * @param category The category to filter by
     * @param months   Number of months to include
     * @return List of transactions matching the criteria
     */
    public List<Transaction> getTransactionsByCategory(String category, int months) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minus(months, ChronoUnit.MONTHS);

        return dataAccess.getTransactionsByCategory(category, startDate, endDate);
    }

    /**
     * Gets all transactions for a specific time period
     * 
     * @param months Number of months to include
     * @return List of all transactions in the time period
     */
    public List<Transaction> getAllTransactions(int months) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minus(months, ChronoUnit.MONTHS);

        return dataAccess.getTransactions(startDate, endDate);
    }

    /**
     * Gets all available transaction categories
     * 
     * @return List of category names
     */
    public List<String> getAllCategories() {
        return dataAccess.getCategories();
    }

    /**
     * Enables or disables AI-powered forecasting
     * 
     * @param useAi Whether to use AI for forecasting
     */
    public void setUseAiForecasting(boolean useAi) {
        this.useAiForecasting = useAi;
    }

    /**
     * Checks if AI-powered forecasting is enabled
     * 
     * @return True if AI forecasting is enabled
     */
    public boolean isUsingAiForecasting() {
        return useAiForecasting;
    }

    /**
     * 添加类别开支数据到指定的时间段
     * 
     * @param expense 类别开支数据
     * @param period  时间段（月）
     */
    public void addCategoryExpense(CategoryExpense expense, int period) {
        if (!periodExpenses.containsKey(period)) {
            periodExpenses.put(period, new ArrayList<>());
        }

        // 检查是否已存在相同类别，如果存在则更新金额
        boolean categoryExists = false;
        for (CategoryExpense existing : periodExpenses.get(period)) {
            if (existing.getCategory().equals(expense.getCategory())) {
                existing.setAmount(expense.getAmount());
                categoryExists = true;
                break;
            }
        }

        // 如果不存在则添加新的
        if (!categoryExists) {
            periodExpenses.get(period).add(expense);
        }
    }

    /**
     * 获取指定时间段的所有类别开支数据
     * 
     * @param period 时间段（月）
     * @return 类别开支数据列表
     */
    public List<CategoryExpense> getCategoryExpenses(int period) {
        return periodExpenses.getOrDefault(period, new ArrayList<>());
    }

    /**
     * 根据类别获取指定时间段的开支数据
     * 
     * @param category 类别名称
     * @param period   时间段（月）
     * @return 开支金额，如果不存在则返回0
     */
    public double getExpenseForCategory(String category, int period) {
        List<CategoryExpense> expenses = getCategoryExpenses(period);
        for (CategoryExpense expense : expenses) {
            if (expense.getCategory().equals(category)) {
                return expense.getAmount();
            }
        }
        return 0.0;
    }
}