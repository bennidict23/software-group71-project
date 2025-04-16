package com.financetracker.boundary.analysis;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

/**
 * Placeholder class for the budget recommendation view.
 * This will be implemented with full functionality later.
 */
public class BudgetRecommendationView extends BorderPane {

    public BudgetRecommendationView() {
        setPadding(new Insets(15));

        Label placeholderLabel = new Label("Budget Recommendations - Coming Soon");
        placeholderLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        setCenter(placeholderLabel);
    }
}