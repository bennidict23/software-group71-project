package com.financetracker.ui.analysis;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 * View component for displaying AI-powered budget recommendations
 * based on historical spending patterns.
 * This corresponds to the ANALYSIS-03 user story.
 */
public class BudgetRecommendationView extends BorderPane {

    private TableView<BudgetRecommendation> recommendationTable;
    private Button generateRecommendationsButton;
    private TextFlow explanationTextFlow;

    public BudgetRecommendationView() {
        initializeUI();
        loadDummyData(); // For now, we'll use dummy data for demonstration
    }

    private void initializeUI() {
        setPadding(new Insets(15));

        // Title
        Label titleLabel = new Label("Intelligent Budget Recommendations");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Explanation text
        Text explanationText = new Text(
                "Based on your historical spending patterns, these are our recommended budget " +
                        "values for each category. These recommendations consider your past spending habits, " +
                        "seasonal factors, and typical spending fluctuations.");
        explanationText.setWrappingWidth(800);

        explanationTextFlow = new TextFlow(explanationText);
        explanationTextFlow.setPadding(new Insets(10, 0, 10, 0));

        // Generate recommendations button
        generateRecommendationsButton = new Button("Generate New Recommendations");
        generateRecommendationsButton.setOnAction(e -> generateRecommendations());

        // Table setup
        recommendationTable = new TableView<>();

        TableColumn<BudgetRecommendation, String> categoryColumn = new TableColumn<>("Category");
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        categoryColumn.setPrefWidth(150);

        TableColumn<BudgetRecommendation, Double> currentBudgetColumn = new TableColumn<>("Current Budget");
        currentBudgetColumn.setCellValueFactory(new PropertyValueFactory<>("currentBudget"));
        currentBudgetColumn.setPrefWidth(120);

        TableColumn<BudgetRecommendation, Double> avgSpendingColumn = new TableColumn<>("Avg. Monthly Spending");
        avgSpendingColumn.setCellValueFactory(new PropertyValueFactory<>("avgSpending"));
        avgSpendingColumn.setPrefWidth(170);

        TableColumn<BudgetRecommendation, Double> recommendedBudgetColumn = new TableColumn<>("Recommended Budget");
        recommendedBudgetColumn.setCellValueFactory(new PropertyValueFactory<>("recommendedBudget"));
        recommendedBudgetColumn.setPrefWidth(170);

        TableColumn<BudgetRecommendation, String> reasonColumn = new TableColumn<>("Reason");
        reasonColumn.setCellValueFactory(new PropertyValueFactory<>("reason"));
        reasonColumn.setPrefWidth(250);

        recommendationTable.getColumns().addAll(
                categoryColumn,
                currentBudgetColumn,
                avgSpendingColumn,
                recommendedBudgetColumn,
                reasonColumn);

        // Button container
        HBox buttonBox = new HBox(10);
        buttonBox.setPadding(new Insets(10, 0, 10, 0));
        buttonBox.getChildren().add(generateRecommendationsButton);

        // Apply button
        Button applyRecommendationsButton = new Button("Apply Recommendations");
        applyRecommendationsButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        applyRecommendationsButton.setOnAction(e -> applyRecommendations());
        buttonBox.getChildren().add(applyRecommendationsButton);

        // Main layout
        VBox mainLayout = new VBox(10);
        mainLayout.getChildren().addAll(
                titleLabel,
                explanationTextFlow,
                buttonBox,
                recommendationTable);

        setCenter(mainLayout);
    }

    /**
     * Budget recommendation data class
     */
    public static class BudgetRecommendation {
        private String category;
        private double currentBudget;
        private double avgSpending;
        private double recommendedBudget;
        private String reason;

        public BudgetRecommendation(String category, double currentBudget,
                double avgSpending, double recommendedBudget,
                String reason) {
            this.category = category;
            this.currentBudget = currentBudget;
            this.avgSpending = avgSpending;
            this.recommendedBudget = recommendedBudget;
            this.reason = reason;
        }

        // Getters and setters
        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public double getCurrentBudget() {
            return currentBudget;
        }

        public void setCurrentBudget(double currentBudget) {
            this.currentBudget = currentBudget;
        }

        public double getAvgSpending() {
            return avgSpending;
        }

        public void setAvgSpending(double avgSpending) {
            this.avgSpending = avgSpending;
        }

        public double getRecommendedBudget() {
            return recommendedBudget;
        }

        public void setRecommendedBudget(double recommendedBudget) {
            this.recommendedBudget = recommendedBudget;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }

    /**
     * Simulates generating new budget recommendations
     */
    private void generateRecommendations() {
        // In a real implementation, this would call the AI service
        // For now, we'll just reload the dummy data with some variations

        recommendationTable.getItems().clear();
        loadDummyData();

        // Update the explanation text
        Text newExplanationText = new Text(
                "NEW RECOMMENDATIONS GENERATED: Based on your recent spending patterns over the last 6 months, " +
                        "we've updated our budget recommendations. We noticed increased spending in Food & Dining " +
                        "and reduced expenses in Transportation, which are reflected in the new recommendations.");
        newExplanationText.setWrappingWidth(800);

        explanationTextFlow.getChildren().clear();
        explanationTextFlow.getChildren().add(newExplanationText);
    }

    /**
     * Simulates applying the recommended budgets
     */
    private void applyRecommendations() {
        // In a real implementation, this would update the budget settings
        // For now, we'll just show a confirmation message

        Text confirmationText = new Text(
                "✓ Recommendations applied successfully! Your budget settings have been updated " +
                        "based on the recommended values.");
        confirmationText.setStyle("-fx-fill: #4CAF50;");
        confirmationText.setWrappingWidth(800);

        explanationTextFlow.getChildren().clear();
        explanationTextFlow.getChildren().add(confirmationText);
    }

    /**
     * Loads dummy data into the table for demonstration
     */
    private void loadDummyData() {
        // Clear existing data
        recommendationTable.getItems().clear();

        // Add dummy data
        recommendationTable.getItems().addAll(
                new BudgetRecommendation(
                        "Food & Dining",
                        1500.0,
                        1720.0,
                        1800.0,
                        "Your spending consistently exceeds budget"),
                new BudgetRecommendation(
                        "Transportation",
                        1000.0,
                        850.0,
                        900.0,
                        "You're spending less than budgeted"),
                new BudgetRecommendation(
                        "Entertainment",
                        800.0,
                        820.0,
                        850.0,
                        "Slight increase to match typical spending"),
                new BudgetRecommendation(
                        "Shopping",
                        1200.0,
                        1450.0,
                        1500.0,
                        "Historical spending is 20% above budget"),
                new BudgetRecommendation(
                        "Utilities",
                        600.0,
                        580.0,
                        600.0,
                        "Current budget is appropriate"),
                new BudgetRecommendation(
                        "Health & Fitness",
                        400.0,
                        200.0,
                        250.0,
                        "Significantly under-utilized budget"),
                new BudgetRecommendation(
                        "Education",
                        300.0,
                        320.0,
                        350.0,
                        "Slight increase due to seasonal factors"));
    }
}