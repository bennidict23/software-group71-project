package com.financetracker.boundary.analysis;

import com.financetracker.control.DataAnalysisController;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Map;

/**
 * View component for displaying spending forecast analysis with a line chart
 * showing historical spending and predicted future spending.
 * This is a boundary class in the MVC pattern.
 */
public class SpendingForecastView extends BorderPane {

    private LineChart<String, Number> forecastChart;
    private ComboBox<String> historyPeriodComboBox;
    private ComboBox<String> forecastPeriodComboBox;
    private Button refreshButton;
    private DataAnalysisController controller;

    public SpendingForecastView() {
        controller = DataAnalysisController.getDefaultInstance();
        initializeUI();
        updateChart();
    }

    private void initializeUI() {
        setPadding(new Insets(15));

        // Title
        Label titleLabel = new Label("Spending Forecast Analysis");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Control panel
        Label historyLabel = new Label("Historical Data:");
        historyPeriodComboBox = new ComboBox<>();
        historyPeriodComboBox.getItems().addAll("Last 3 Months", "Last 6 Months", "Last Year");
        historyPeriodComboBox.setValue("Last 6 Months");
        historyPeriodComboBox.setOnAction(e -> updateChart());

        Label forecastLabel = new Label("Forecast Period:");
        forecastPeriodComboBox = new ComboBox<>();
        forecastPeriodComboBox.getItems().addAll("Next 3 Months", "Next 6 Months", "Next Year");
        forecastPeriodComboBox.setValue("Next 3 Months");
        forecastPeriodComboBox.setOnAction(e -> updateChart());

        refreshButton = new Button("Refresh Data");
        refreshButton.setOnAction(e -> updateChart());

        // Control panel layout
        HBox controlsBox = new HBox(10);
        controlsBox.setPadding(new Insets(10, 0, 20, 0));
        controlsBox.getChildren().addAll(
                historyLabel, historyPeriodComboBox,
                forecastLabel, forecastPeriodComboBox,
                refreshButton);

        // Chart setup
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Month");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Spending Amount (¥)");

        forecastChart = new LineChart<>(xAxis, yAxis);
        forecastChart.setTitle("Spending Forecast");
        forecastChart.setAnimated(false);
        forecastChart.setLegendVisible(true);

        // Description
        Label descriptionLabel = new Label(
                "This chart shows your historical spending patterns and forecasts future spending based on trends. " +
                        "The forecast uses historical data and applies trend analysis to predict future spending levels.");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setPadding(new Insets(20, 0, 0, 0));

        // Main layout
        VBox mainLayout = new VBox(10);
        mainLayout.getChildren().addAll(titleLabel, controlsBox, forecastChart, descriptionLabel);

        setCenter(mainLayout);
    }

    /**
     * Updates the chart with data based on the selected time periods
     */
    private void updateChart() {
        // Get the selected periods
        int historyMonths = getMonthsFromSelection(historyPeriodComboBox.getValue(), true);
        int forecastMonths = getMonthsFromSelection(forecastPeriodComboBox.getValue(), false);

        // Clear existing data
        forecastChart.getData().clear();

        // Get historical data
        Map<String, Double> historicalData = controller.getHistoricalSpending(historyMonths);
        XYChart.Series<String, Number> historicalSeries = new XYChart.Series<>();
        historicalSeries.setName("Historical Spending");

        for (Map.Entry<String, Double> entry : historicalData.entrySet()) {
            historicalSeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        // Get forecasted data
        Map<String, Double> forecastData = controller.getForecastedSpending(historyMonths, forecastMonths);
        XYChart.Series<String, Number> forecastSeries = new XYChart.Series<>();
        forecastSeries.setName("Forecasted Spending");

        for (Map.Entry<String, Double> entry : forecastData.entrySet()) {
            forecastSeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        // Add data to chart
        forecastChart.getData().add(historicalSeries);
        forecastChart.getData().add(forecastSeries);
    }

    /**
     * Converts the time period selection to number of months
     * 
     * @param selection The selected time period
     * @param isHistory Whether this is for history or forecast
     * @return The number of months
     */
    private int getMonthsFromSelection(String selection, boolean isHistory) {
        if (isHistory) {
            return switch (selection) {
                case "Last 3 Months" -> 3;
                case "Last 6 Months" -> 6;
                case "Last Year" -> 12;
                default -> 6;
            };
        } else {
            return switch (selection) {
                case "Next 3 Months" -> 3;
                case "Next 6 Months" -> 6;
                case "Next Year" -> 12;
                default -> 3;
            };
        }
    }
}