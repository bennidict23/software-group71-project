package com.financetracker.ui.analysis;

import javafx.geometry.Insets;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * View component for displaying spending structure analysis with a pie chart
 * showing the percentage breakdown of spending by category.
 * This corresponds to the ANALYSIS-01 user story.
 */
public class SpendingStructureView extends BorderPane {

    private PieChart spendingPieChart;
    private ComboBox<String> timePeriodComboBox;
    private Button refreshButton;

    public SpendingStructureView() {
        initializeUI();
        loadDummyData(); // For now, we'll use dummy data for visualization
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
        timePeriodComboBox.setValue("Last Month");
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
        // In a real implementation, we would fetch data from the data service
        // based on the selected time period

        // For now, we'll just simulate a refresh with the same dummy data
        spendingPieChart.getData().clear();
        loadDummyData();
    }

    /**
     * Loads dummy data into the pie chart for demonstration
     */
    private void loadDummyData() {
        // Clear existing data
        spendingPieChart.getData().clear();

        // Add dummy data
        spendingPieChart.getData().add(new PieChart.Data("Food & Dining", 35));
        spendingPieChart.getData().add(new PieChart.Data("Transportation", 15));
        spendingPieChart.getData().add(new PieChart.Data("Entertainment", 10));
        spendingPieChart.getData().add(new PieChart.Data("Shopping", 20));
        spendingPieChart.getData().add(new PieChart.Data("Utilities", 12));
        spendingPieChart.getData().add(new PieChart.Data("Others", 8));
    }
}