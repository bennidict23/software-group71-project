package com.financetracker.boundary.analysis;

import com.financetracker.control.DataAnalysisController;
import com.financetracker.control.SeasonalFactorService;
import com.financetracker.entity.CategoryExpense;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.*;

/**
 * View component for displaying intelligent budget recommendations based on
 * historical spending patterns and seasonal factors.
 * This is a boundary class in the MVC pattern.
 */
public class BudgetRecommendationView extends BorderPane {

    private DataAnalysisController controller;
    private ComboBox<String> historyPeriodComboBox;
    private TableView<BudgetRecommendation> recommendationTable;
    private Label totalBudgetLabel;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.CHINA);
    private CheckBox applySeasonalFactorsCheckBox;

    public BudgetRecommendationView() {
        controller = DataAnalysisController.getDefaultInstance();
        initializeUI();
        updateRecommendations();
    }

    private void initializeUI() {
        setPadding(new Insets(15));

        // 创建标题
        Label titleLabel = new Label("Intelligent Budget Recommendations");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // 控制面板
        Label historyLabel = new Label("Based on spending from:");
        historyPeriodComboBox = new ComboBox<>();
        historyPeriodComboBox.getItems().addAll("Last 3 Months", "Last 6 Months", "Last Year");
        historyPeriodComboBox.setValue("Last 6 Months");
        historyPeriodComboBox.setOnAction(e -> updateRecommendations());

        applySeasonalFactorsCheckBox = new CheckBox("Apply Seasonal Adjustments");
        applySeasonalFactorsCheckBox.setSelected(true);
        applySeasonalFactorsCheckBox.setOnAction(e -> updateRecommendations());

        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> updateRecommendations());

        HBox controlsBox = new HBox(10);
        controlsBox.setPadding(new Insets(10, 0, 20, 0));
        controlsBox.getChildren().addAll(historyLabel, historyPeriodComboBox, applySeasonalFactorsCheckBox,
                refreshButton);

        // 预算推荐表格
        recommendationTable = new TableView<>();

        TableColumn<BudgetRecommendation, String> categoryColumn = new TableColumn<>("Category");
        categoryColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCategory()));
        categoryColumn.setPrefWidth(150);

        TableColumn<BudgetRecommendation, String> budgetColumn = new TableColumn<>("Recommended Budget");
        budgetColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                currencyFormat.format(cellData.getValue().getRecommendedBudget())));
        budgetColumn.setPrefWidth(150);

        TableColumn<BudgetRecommendation, String> noteColumn = new TableColumn<>("Seasonal Note");
        noteColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSeasonalNote()));
        noteColumn.setPrefWidth(220);

        recommendationTable.getColumns().addAll(categoryColumn, budgetColumn, noteColumn);
        recommendationTable.setPrefHeight(300);

        // 总预算标签
        totalBudgetLabel = new Label();
        totalBudgetLabel.setFont(Font.font(null, FontWeight.BOLD, 14));

        // 说明
        Label explanationLabel = new Label(
                "These budget recommendations are based on your historical spending patterns, " +
                        "adjusted for seasonal factors and optimized for your financial goals.");
        explanationLabel.setWrapText(true);
        explanationLabel.setPadding(new Insets(20, 0, 0, 0));

        // 布局
        VBox mainLayout = new VBox(15);
        mainLayout.getChildren().addAll(
                titleLabel,
                controlsBox,
                recommendationTable,
                totalBudgetLabel,
                explanationLabel);

        setCenter(mainLayout);
    }

    /**
     * Updates the budget recommendations based on selected time period
     */
    private void updateRecommendations() {
        // 获取选择的月数
        int months = getMonthsFromSelection();

        // 获取类别支出数据
        List<CategoryExpense> expenses = controller.getSpendingByCategory(months);

        // 计算推荐预算
        List<BudgetRecommendation> recommendations = calculateRecommendedBudgets(expenses, months);

        // 更新表格
        recommendationTable.setItems(FXCollections.observableArrayList(recommendations));

        // 更新总预算
        double total = recommendations.stream()
                .mapToDouble(BudgetRecommendation::getRecommendedBudget)
                .sum();
        totalBudgetLabel.setText("Total Recommended Budget: " + currencyFormat.format(total));
    }

    /**
     * Gets the number of months from the combobox selection
     */
    private int getMonthsFromSelection() {
        String selected = historyPeriodComboBox.getValue();
        return switch (selected) {
            case "Last 3 Months" -> 3;
            case "Last 6 Months" -> 6;
            case "Last Year" -> 12;
            default -> 6;
        };
    }

    /**
     * Calculates recommended budgets based on historical spending data
     */
    private List<BudgetRecommendation> calculateRecommendedBudgets(List<CategoryExpense> expenses, int months) {
        List<BudgetRecommendation> recommendations = new ArrayList<>();
        LocalDate currentDate = LocalDate.now();
        boolean applySeasonalFactors = applySeasonalFactorsCheckBox.isSelected();

        for (CategoryExpense expense : expenses) {
            String category = expense.getCategory();

            // 计算月平均支出
            double monthlyAverage = expense.getAmount() / months;

            // 应用预算调整逻辑
            double recommendedBudget;
            String seasonalNote;

            if (applySeasonalFactors) {
                // 使用季节性因素服务
                double seasonalFactor = SeasonalFactorService.getSeasonalFactor(category, currentDate);
                seasonalNote = SeasonalFactorService.getSeasonalNote(category, currentDate);

                // 应用基础调整和季节性因素
                double baseAdjustment = 0.95 + (Math.random() * 0.1); // 0.95-1.05之间随机
                recommendedBudget = monthlyAverage * baseAdjustment * seasonalFactor;
            } else {
                // 不应用季节性因素，只使用基础调整
                recommendedBudget = monthlyAverage * (0.95 + (Math.random() * 0.1));
                seasonalNote = "No seasonal adjustments applied";
            }

            // 检查是否节假日期间
            String holiday = SeasonalFactorService.getHolidayName(currentDate);
            if (holiday != null && applySeasonalFactors) {
                seasonalNote += " (During " + holiday + ")";
            }

            recommendations.add(new BudgetRecommendation(category, recommendedBudget, seasonalNote));
        }

        // 按推荐预算从高到低排序
        recommendations.sort(Comparator.comparing(BudgetRecommendation::getRecommendedBudget).reversed());

        return recommendations;
    }

    /**
     * 预算推荐数据模型类
     */
    public static class BudgetRecommendation {
        private final String category;
        private final double recommendedBudget;
        private final String seasonalNote;

        public BudgetRecommendation(String category, double recommendedBudget, String seasonalNote) {
            this.category = category;
            this.recommendedBudget = recommendedBudget;
            this.seasonalNote = seasonalNote;
        }

        public String getCategory() {
            return category;
        }

        public double getRecommendedBudget() {
            return recommendedBudget;
        }

        public String getSeasonalNote() {
            return seasonalNote;
        }
    }
}