package org.example;

import javafx.scene.chart.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class ConsumerTrendChart {
    private static final String TRANSACTION_FILE = "transactions.csv";
    private final User currentUser;

    public ConsumerTrendChart(User currentUser) {
        this.currentUser = currentUser;
    }

    public LineChart<String, Number> createChart() {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Date");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Total Spent ($)");

        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("7‑Day Spending Trend");
        // 不显示图例（因为只有一条线，无需 source 图例）
        lineChart.setLegendVisible(false);

        // 构造一个「汇总」系列
        XYChart.Series<String, Number> totalSeries = new XYChart.Series<>();
        totalSeries.setName("Total Spending");

        // calculateDailySpending 已经把所有 source 的金额按日期累加到一起了
        Map<LocalDate, Double> daily = calculateDailySpending();
        for (Map.Entry<LocalDate, Double> e : daily.entrySet()) {
            totalSeries.getData().add(
                    new XYChart.Data<>(e.getKey().toString(), e.getValue())
            );
        }

        lineChart.getData().add(totalSeries);
        return lineChart;
    }

    private Map<LocalDate, Double> calculateDailySpending() {
        Map<LocalDate, Double> dailySpending = new HashMap<>();
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysAgo = today.minusDays(6);

        try (BufferedReader br = new BufferedReader(new FileReader(TRANSACTION_FILE))) {
            String line = br.readLine(); // 跳过标题
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");

                if (parts.length < 6 || !parts[0].equals(currentUser.getUsername()))
                    continue;

                // 解析日期（兼容 yyyy‑MM‑dd 和 yyyy/M/d）
                String raw = parts[3].replace('/', '-').trim();
                LocalDate date;
                try {
                    date = LocalDate.parse(raw, DateTimeFormatter.ISO_LOCAL_DATE);
                } catch (DateTimeParseException ex) {
                    date = LocalDate.parse(raw, DateTimeFormatter.ofPattern("yyyy-M-d"));
                }


                if (!date.isBefore(sevenDaysAgo) && !date.isAfter(today)) {
                    double amt = Double.parseDouble(parts[4]);
                    dailySpending.merge(date, amt, Double::sum);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 确保每一天都有数据点
        for (LocalDate d = sevenDaysAgo; !d.isAfter(today); d = d.plusDays(1)) {
            dailySpending.putIfAbsent(d, 0.0);
        }

        return new TreeMap<>(dailySpending);
    }
}
