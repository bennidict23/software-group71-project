package com.financetracker.ui;

import com.financetracker.ui.analysis.AnalysisView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;

/**
 * The main view of the application containing the tab navigation.
 */
public class MainView extends BorderPane {

    private TabPane tabPane;

    public MainView() {
        initializeUI();
    }

    private void initializeUI() {
        tabPane = new TabPane();

        // Create tabs for different sections
        Tab dashboardTab = new Tab("Dashboard");
        dashboardTab.setClosable(false);
        // Will be implemented later

        Tab transactionTab = new Tab("Transactions");
        transactionTab.setClosable(false);
        // Will be implemented later

        Tab budgetTab = new Tab("Budget");
        budgetTab.setClosable(false);
        // Will be implemented later

        Tab analysisTab = new Tab("Analysis");
        analysisTab.setClosable(false);
        analysisTab.setContent(new AnalysisView());

        // Add tabs to the tab pane
        tabPane.getTabs().addAll(dashboardTab, transactionTab, budgetTab, analysisTab);

        // Set the tab pane as the center content
        setCenter(tabPane);
    }
}