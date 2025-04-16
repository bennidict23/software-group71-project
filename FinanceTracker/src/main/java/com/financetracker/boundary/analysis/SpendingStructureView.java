package com.financetracker.boundary.analysis;

import com.financetracker.control.DataAnalysisController;
import com.financetracker.entity.CategoryExpense;
import javafx.geometry.Insets;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.util.List;

/**
 * View component for displaying spending structure analysis with a pie chart
 * showing the percentage breakdown of spending by category.
 * This corresponds to the ANALYSIS-01 user story.
 * This is a boundary class in the MVC pattern.
 */
public class SpendingStructureView extends BorderPane {

    private PieChart spendingPieChart;
    private ComboBox<String> timePeriodComboBox;
    private Button refreshButton;
    private DataAnalysisController controller;

    public SpendingStructureView() {
        controller = DataAnalysisController.getDefaultInstance();
        initializeUI();
        updateChart(); // Load real data from the controller
    }

    private void initializeUI() {
        setPadding(new Insets(15));

        // Title
        Label titleLabel = new Label("Spending Structure Analysis");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Time period selector
        Label timeLabel = new Label("Time Period:");
        timePeriodComboBox = new ComboBox<>();
        timePeriodComboBox.getItems().addAll("Last Month", "Last 3 Months", "Last 6 Months", "This Year");
        timePeriodComboBox.setValue("Last 3 Months");
        timePeriodComboBox.setOnAction(e -> updateChart());

        // Refresh button
        refreshButton = new Button("Refresh Data");
        refreshButton.setOnAction(e -> updateChart());

        // Control panel layout
        HBox controlsBox = new HBox(10);
        controlsBox.setPadding(new Insets(10, 0, 20, 0));
        controlsBox.getChildren().addAll(timeLabel, timePeriodComboBox, refreshButton);

        // Pie chart
        spendingPieChart = new PieChart();
        spendingPieChart.setTitle("Spending by Category");
        spendingPieChart.setLabelsVisible(true);
        spendingPieChart.setLegendVisible(true);

        // Description
        Label descriptionLabel = new Label("This chart shows your spending breakdown by category. " +
                "Use it to identify areas where you might reduce expenses.");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setPadding(new Insets(20, 0, 0, 0));

        // Main layout
        VBox mainLayout = new VBox(10);
        mainLayout.getChildren().addAll(titleLabel, controlsBox, spendingPieChart, descriptionLabel);

        setCenter(mainLayout);
    }

    /**
     * Updates the chart with data based on the selected time period
     */
    private void updateChart() {
        String selectedPeriod = timePeriodComboBox.getValue();
        int months = getMonthsFromSelection(selectedPeriod);

        // Get real data from the controller
        List<CategoryExpense> spendingData = controller.getSpendingByCategory(months);

        // Clear existing data
        spendingPieChart.getData().clear();

        // Add data from the controller
        for (CategoryExpense expense : spendingData) {
            spendingPieChart.getData().add(
                    new PieChart.Data(expense.getCategory(), expense.getAmount()));
        }
    }

    /**
     * Converts the time period selection to number of months
     * 
     * @param selection The selected time period
     * @return The number of months for the analysis
     */
    private int getMonthsFromSelection(String selection) {
        return switch (selection) {
            case "Last Month" -> 1;
            case "Last 3 Months" -> 3;
            case "Last 6 Months" -> 6;
            case "This Year" -> 12;
            default -> 3; // Default to 3 months
        };
    }
}