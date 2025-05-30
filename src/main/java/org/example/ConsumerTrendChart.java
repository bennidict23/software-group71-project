package org.example;

import javafx.scene.chart.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class ConsumerTrendChart {
    private final User currentUser;

    public ConsumerTrendChart(User currentUser) {
        this.currentUser = currentUser;
    }

    /**
     * 把折线图换成 7 个月趋势
     */
    public LineChart<String, Number> createChart() {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Month");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Total Spent ($)");
        yAxis.setForceZeroInRange(false);

        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("7‑Month Spending Trend");

        // 构造一个系列
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Spending");

        // 拿到按月聚合的数据
        Map<YearMonth, Double> monthly = calculateMonthlySpending();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy‑MM");
        for (Map.Entry<YearMonth, Double> e : monthly.entrySet()) {
            series.getData().add(new XYChart.Data<>(
                    e.getKey().format(fmt),
                    e.getValue()
            ));
        }

        chart.getData().add(series);
        return chart;
    }

    /**
     * 聚合过去 7 个月（含当月）的消费金额
     */
    private Map<YearMonth, Double> calculateMonthlySpending() {
        Map<YearMonth, Double> monthlySpending = new HashMap<>();
        YearMonth current = YearMonth.now();
        YearMonth sixMonthsAgo = current.minusMonths(6);

        // 构造特定用户的交易记录文件名
        String transactionFile = currentUser.getUsername() + "_transactions.csv";

        try (BufferedReader br = new BufferedReader(new FileReader(transactionFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 7) continue; // 确保至少有7个字段
                // 只统计本用户
                if (!parts[1].equals(currentUser.getUsername())) continue; // 索引从0变为1
                // 解析日期
                String raw = parts[3].replace('/', '-').trim(); // 索引从2变为3
                LocalDate date;
                try {
                    date = LocalDate.parse(raw, DateTimeFormatter.ISO_LOCAL_DATE);
                } catch (DateTimeParseException ex) {
                    date = LocalDate.parse(raw, DateTimeFormatter.ofPattern("yyyy-M-d"));
                }
                YearMonth ym = YearMonth.from(date);
                // 只保留最近 6 个月 + 本月
                if (ym.isBefore(sixMonthsAgo) || ym.isAfter(current)) continue;
                double amt = Double.parseDouble(parts[4]); // 索引从3变为4
                monthlySpending.merge(ym, amt, Double::sum);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 补齐缺失的月份
        for (YearMonth ym = sixMonthsAgo; !ym.isAfter(current); ym = ym.plusMonths(1)) {
            monthlySpending.putIfAbsent(ym, 0.0);
        }

        // 按月排序
        return new TreeMap<>(monthlySpending);
    }
}