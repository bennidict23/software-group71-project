package com.financetracker;

import com.financetracker.control.DataAnalysisController;
import com.financetracker.entity.CategoryExpense;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

/**
 * 简单测试应用，用于展示类别开支数据分析功能
 */
public class SimpleApp extends Application {

    private DataAnalysisController controller;
    private PieChart pieChart;
    private ComboBox<Integer> periodSelector;

    @Override
    public void start(Stage primaryStage) {
        controller = DataAnalysisController.getDefaultInstance();

        // 添加测试数据
        addTestData();

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // 创建图表
        pieChart = new PieChart();
        pieChart.setTitle("类别开支分析");

        // 创建控制面板
        HBox controlPanel = createControlPanel();

        // 设置布局
        root.setCenter(pieChart);
        root.setBottom(controlPanel);

        // 初始化显示3个月的数据
        updateChartData(3);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("财务追踪 - 类别开支分析");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private HBox createControlPanel() {
        HBox panel = new HBox(10);
        panel.setPadding(new Insets(10, 0, 0, 0));

        Label label = new Label("选择时间段:");

        periodSelector = new ComboBox<>();
        periodSelector.getItems().addAll(3, 6, 12);
        periodSelector.setValue(3);
        periodSelector.setOnAction(e -> updateChartData(periodSelector.getValue()));

        Button refreshButton = new Button("刷新数据");
        refreshButton.setOnAction(e -> updateChartData(periodSelector.getValue()));

        panel.getChildren().addAll(label, periodSelector, refreshButton);
        return panel;
    }

    private void updateChartData(int period) {
        pieChart.getData().clear();

        List<CategoryExpense> expenses = controller.getCategoryExpenses(period);
        if (expenses == null || expenses.isEmpty()) {
            pieChart.setTitle("没有可用数据");
            return;
        }

        pieChart.setTitle(period + "个月内的类别开支分析");

        for (CategoryExpense expense : expenses) {
            PieChart.Data slice = new PieChart.Data(
                    expense.getCategory() + " (" + String.format("%.2f", expense.getAmount()) + ")",
                    expense.getAmount());
            pieChart.getData().add(slice);
        }
    }

    private void addTestData() {
        // 3个月数据
        controller.addCategoryExpense(new CategoryExpense("餐饮", 1200.50, "三个月餐饮消费"), 3);
        controller.addCategoryExpense(new CategoryExpense("交通", 450.75, "三个月交通费用"), 3);
        controller.addCategoryExpense(new CategoryExpense("购物", 800.25, "三个月购物消费"), 3);
        controller.addCategoryExpense(new CategoryExpense("娱乐", 300.00, "三个月娱乐消费"), 3);

        // 6个月数据
        controller.addCategoryExpense(new CategoryExpense("餐饮", 2500.75, "六个月餐饮消费"), 6);
        controller.addCategoryExpense(new CategoryExpense("交通", 950.50, "六个月交通费用"), 6);
        controller.addCategoryExpense(new CategoryExpense("购物", 1800.25, "六个月购物消费"), 6);
        controller.addCategoryExpense(new CategoryExpense("娱乐", 650.00, "六个月娱乐消费"), 6);
        controller.addCategoryExpense(new CategoryExpense("医疗", 420.30, "六个月医疗支出"), 6);

        // 12个月数据
        controller.addCategoryExpense(new CategoryExpense("餐饮", 5200.90, "十二个月餐饮消费"), 12);
        controller.addCategoryExpense(new CategoryExpense("交通", 1900.75, "十二个月交通费用"), 12);
        controller.addCategoryExpense(new CategoryExpense("购物", 3600.50, "十二个月购物消费"), 12);
        controller.addCategoryExpense(new CategoryExpense("娱乐", 1250.25, "十二个月娱乐消费"), 12);
        controller.addCategoryExpense(new CategoryExpense("医疗", 950.80, "十二个月医疗支出"), 12);
        controller.addCategoryExpense(new CategoryExpense("住宿", 24000.00, "十二个月住宿费用"), 12);
    }

    public static void main(String[] args) {
        launch(args);
    }
}