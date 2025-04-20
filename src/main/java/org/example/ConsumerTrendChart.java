package org.example;

import javafx.application.Platform;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class ConsumerTrendChart {

    private static final String TRANSACTION_FILE = "transactions.csv";
    private User currentUser; // 当前用户

    public ConsumerTrendChart(User currentUser) {
        this.currentUser = currentUser; // 注入当前用户
    }

    public BarChart<String, Number> createChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);

        // 设置横坐标的标签
        xAxis.setLabel("Date");
        yAxis.setLabel("Total Spent ($)");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Total Daily Spending");

        Map<LocalDate, Double> dailySpending = calculateDailySpending();
        for (Map.Entry<LocalDate, Double> entry : dailySpending.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey().toString(), entry.getValue()));
        }

        barChart.getData().add(series);
        return barChart;
    }

    private Map<LocalDate, Double> calculateDailySpending() {
        Map<LocalDate, Double> dailySpending = new HashMap<>();
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysAgo = today.minusDays(6); // 包括今天，所以是6天前

        try (BufferedReader br = new BufferedReader(new FileReader(TRANSACTION_FILE))) {
            String line = br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 6) {
                    // 获取交易记录中的用户信息
                    String user = parts[0];
                    LocalDate date = LocalDate.parse(parts[2], DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    double amount = Double.parseDouble(parts[3]);

                    // 只处理当前用户的记录
                    if (user.equals(currentUser.getUsername()) && !date.isBefore(sevenDaysAgo) && !date.isAfter(today)) {
                        dailySpending.put(date, dailySpending.getOrDefault(date, 0.0) + amount);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 确保最近七天的日期都在map中，即使没有消费记录
        for (LocalDate date = sevenDaysAgo; !date.isAfter(today); date = date.plusDays(1)) {
            dailySpending.putIfAbsent(date, 0.0);
        }

        // 将Map转换为TreeMap，按日期排序
        Map<LocalDate, Double> sortedDailySpending = new TreeMap<>(dailySpending);

        return sortedDailySpending;
    }
}