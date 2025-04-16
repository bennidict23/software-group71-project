package com.financetracker.ui.analysis;

import javafx.geometry.Insets;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * View component for displaying AI-powered spending forecast analysis
 * showing predicted future expenditure trends.
 * This corresponds to the ANALYSIS-02 user story.
 */
public class SpendingForecastView extends BorderPane {

    private LineChart<String, Number> forecastChart;
    private TextArea forecastExplanationArea;
    private Button generateForecastButton;

    public SpendingForecastView() {
        initializeUI();
        loadDummyData(); // For now, we'll use dummy data for visualization
    }

    private void initializeUI() {
        setPadding(new Insets(15));

        // Title
        Label titleLabel = new Label("AI Spending Forecast");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Chart initialization
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Month");
        yAxis.setLabel("Spending (¥)");

        forecastChart = new LineChart<>(xAxis, yAxis);
        forecastChart.setTitle("Spending Trend & Forecast");
        forecastChart.setAnimated(false);

        // Generate forecast button
        generateForecastButton = new Button("Generate New Forecast");
        generateForecastButton.setOnAction(e -> generateForecast());

        // Explanation area
        Label explanationLabel = new Label("Forecast Explanation:");
        explanationLabel.setStyle("-fx-font-weight: bold;");

        forecastExplanationArea = new TextArea();
        forecastExplanationArea.setEditable(false);
        forecastExplanationArea.setWrapText(true);
        forecastExplanationArea.setPrefRowCount(4);

        // Description
        Label descriptionLabel = new Label("This chart shows your historical spending patterns and " +
                "predicts future spending trends based on AI analysis of your transaction history and " +
                "seasonal factors. Use this forecast to plan ahead and prepare financially.");
        descriptionLabel.setWrapText(true);

        // Button container
        HBox buttonBox = new HBox(10);
        buttonBox.setPadding(new Insets(10, 0, 10, 0));
        buttonBox.getChildren().add(generateForecastButton);

        // Main layout
        VBox chartContainer = new VBox(10);
        chartContainer.getChildren().addAll(forecastChart);
        VBox.setVgrow(forecastChart, Priority.ALWAYS);

        VBox mainLayout = new VBox(10);
        mainLayout.getChildren().addAll(
                titleLabel,
                buttonBox,
                chartContainer,
                explanationLabel,
                forecastExplanationArea,
                descriptionLabel);
        VBox.setVgrow(chartContainer, Priority.ALWAYS);

        setCenter(mainLayout);
    }

    /**
     * Simulates generating a new forecast
     */
    private void generateForecast() {
        // In a real implementation, this would call the AI service
        // For now, we'll just reload the dummy data with some minor changes

        forecastChart.getData().clear();
        loadDummyData();

        // Update explanation with a more detailed one
        forecastExplanationArea.setText("Based on your historical spending patterns, we predict an " +
                "increase in spending in May due to anticipated Labor Day holidays. Your dining and " +
                "entertainment expenses typically rise by 20% during this period. " +
                "Additionally, we notice a general upward trend in your transportation costs, " +
                "which may require attention if you wish to reduce overall expenses.");
    }

    /**
     * Loads dummy data into the line chart for demonstration
     */
    private void loadDummyData() {
        // Clear existing data
        forecastChart.getData().clear();

        // Historical data series
        XYChart.Series<String, Number> historicalSeries = new XYChart.Series<>();
        historicalSeries.setName("Historical Spending");

        // Add historical data points (past 6 months)
        historicalSeries.getData().add(new XYChart.Data<>("Nov", 3200));
        historicalSeries.getData().add(new XYChart.Data<>("Dec", 4100));
        historicalSeries.getData().add(new XYChart.Data<>("Jan", 3600));
        historicalSeries.getData().add(new XYChart.Data<>("Feb", 4800));
        historicalSeries.getData().add(new XYChart.Data<>("Mar", 3900));
        historicalSeries.getData().add(new XYChart.Data<>("Apr", 4200));

        // Predicted data series
        XYChart.Series<String, Number> forecastSeries = new XYChart.Series<>();
        forecastSeries.setName("Predicted Spending");

        // Add forecast data points (next 3 months)
        forecastSeries.getData().add(new XYChart.Data<>("May", 5100));
        forecastSeries.getData().add(new XYChart.Data<>("Jun", 4700));
        forecastSeries.getData().add(new XYChart.Data<>("Jul", 4400));

        // Add both series to the chart
        forecastChart.getData().addAll(historicalSeries, forecastSeries);

        // Set initial explanation text
        forecastExplanationArea.setText("Based on your historical spending patterns, we predict a " +
                "spike in May due to holiday-related expenses, followed by a gradual decrease in the " +
                "following months. The forecast considers seasonal factors and your typical spending habits.");
    }
}