package org.example.analysis;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Side;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.User;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 支出结构饼图类
 * 展示用户各类别支出的占比和金额
 */
public class SpendingStructureChart extends BorderPane {

    private static final String TRANSACTION_FILE = "transactions.csv";
    private final User currentUser;
    private final Map<String, Double> categoryTotals = new HashMap<>();
    private double totalSpending = 0.0;

    /**
     * 构造函数
     * 
     * @param currentUser 当前用户
     */
    public SpendingStructureChart(User currentUser) {
        this.currentUser = currentUser;
        this.setPadding(new Insets(10));

        // 加载数据
        loadTransactionData();

        // 创建图表
        setupChart();
    }

    /**
     * 从transactions.csv加载交易数据
     */
    private void loadTransactionData() {
        try (BufferedReader reader = new BufferedReader(new FileReader(TRANSACTION_FILE))) {
            // 跳过标题行
            reader.readLine();

            String line;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");

                // 检查数据格式和用户匹配
                if (parts.length < 6)
                    continue;
                if (!parts[0].equals(currentUser.getUsername()))
                    continue;

                try {
                    // 解析金额和类别
                    double amount = Double.parseDouble(parts[3]);
                    String category = parts[4].trim();

                    // 仅考虑支出（负数金额）
                    if (amount < 0) {
                        amount = Math.abs(amount); // 转为正数以便计算

                        // 累加到类别总计
                        categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) + amount);
                        totalSpending += amount;
                    }
                } catch (NumberFormatException e) {
                    // 忽略格式错误的数据
                    continue;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置饼图
     */
    private void setupChart() {
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
            double percentage = (amount / totalSpending) * 100;

            // 添加类别、金额和百分比信息
            String label = String.format("%s: $%.2f (%.1f%%)", category, amount, percentage);
            pieChartData.add(new PieChart.Data(label, amount));
        }

        // 创建饼图
        PieChart chart = new PieChart(pieChartData);
        chart.setTitle("Spending Structure by Category");
        chart.setLegendSide(Side.RIGHT);
        chart.setLabelsVisible(false); // 禁用默认标签
        chart.setAnimated(true);

        // 添加数据标签提示
        for (PieChart.Data data : chart.getData()) {
            Tooltip tooltip = new Tooltip(data.getName());
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

        // 创建摘要标签
        Label summaryLabel = new Label(String.format("Total Expenditure: $%.2f", totalSpending));
        summaryLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        // 设置布局
        VBox chartContainer = new VBox(10);
        chartContainer.getChildren().addAll(chart, summaryLabel);
        chartContainer.setAlignment(Pos.CENTER);

        // 添加到面板
        this.setCenter(chartContainer);
    }

    /**
     * 显示无数据信息
     */
    private void showNoDataMessage() {
        VBox messageBox = new VBox(10);
        messageBox.setAlignment(Pos.CENTER);

        Label noDataLabel = new Label("No spending data available");
        noDataLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        noDataLabel.setTextFill(Color.GRAY);

        Label infoLabel = new Label("Add transactions with negative amounts to see your spending structure");
        infoLabel.setTextFill(Color.GRAY);

        messageBox.getChildren().addAll(noDataLabel, infoLabel);
        this.setCenter(messageBox);
    }
}