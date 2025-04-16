package com.financetracker.boundary.analysis;

import javafx.geometry.Insets;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;

/**
 * The main view for the Analysis section, containing tabs for different
 * types of financial analysis.
 * This is a boundary class in the MVC pattern.
 */
public class AnalysisView extends BorderPane {

    private TabPane tabPane;
    private SpendingStructureView spendingStructureView;
    private SpendingForecastView spendingForecastView;
    private BudgetRecommendationView budgetRecommendationView;

    public AnalysisView() {
        initializeUI();
    }

    private void initializeUI() {
        setPadding(new Insets(10));

        tabPane = new TabPane();

        // Create tabs for different analysis views
        spendingStructureView = new SpendingStructureView();
        Tab spendingStructureTab = new Tab("Spending Structure");
        spendingStructureTab.setClosable(false);
        spendingStructureTab.setContent(spendingStructureView);

        spendingForecastView = new SpendingForecastView();
        Tab spendingForecastTab = new Tab("Spending Forecast");
        spendingForecastTab.setClosable(false);
        spendingForecastTab.setContent(spendingForecastView);

        budgetRecommendationView = new BudgetRecommendationView();
        Tab budgetRecommendationTab = new Tab("Budget Recommendation");
        budgetRecommendationTab.setClosable(false);
        budgetRecommendationTab.setContent(budgetRecommendationView);

        // Add tabs to the tab pane
        tabPane.getTabs().addAll(spendingStructureTab, spendingForecastTab, budgetRecommendationTab);

        // Set the tab pane as the center content
        setCenter(tabPane);
    }
}