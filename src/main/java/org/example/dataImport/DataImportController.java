package org.example.dataImport;

import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.DashboardView;
import org.example.list.Transaction;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DataImportController {

    private final DataImportModel model;
    private final DataImportView view;

    public DataImportController(Stage stage, String username) {
        this.model = new DataImportModel(username);
        this.view = new DataImportView(stage);

        initializeView();
        setupEventHandlers();
    }

    private void initializeView() {
        // Set the model data to the table view
        view.getTableView().setItems(model.getTransactions());
    }

    private void setupEventHandlers() {
        view.setOnImportCSV(this::handleImportCSV);
        view.setOnDownloadTemplate(this::handleDownloadTemplate);
        view.setOnSaveChanges(this::handleSaveChanges);
        view.setOnDeleteSelected(this::handleDeleteSelected);
        view.setOnClearAll(this::handleClearAll);
        view.setOnAddRecord(this::handleAddRecord);
        view.setOnBackToDashboard(this::handleBackToDashboard);
    }

    private void handleImportCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select CSV File to Import");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        File file = fileChooser.showOpenDialog(view.getStage());
        if (file != null) {
            DataImportModel.ImportResult result = model.importCSV(file);

            if (result.isSuccess()) {
                DashboardView.setImportDone(true);

                view.showAlert(Alert.AlertType.INFORMATION,
                        "Import Successful",
                        String.format("Successfully imported %d records (Encoding: %s)",
                                result.getRecordsImported(),
                                result.getEncoding()));
            } else {
                view.showAlert(Alert.AlertType.ERROR,
                        "Import Error",
                        result.getErrorMessage());
            }
        }
    }

    private void handleDownloadTemplate() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save CSV Template");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName("transactions.csv");

        File file = fileChooser.showSaveDialog(view.getStage());
        if (file != null) {
            try {
                model.downloadTemplate(file);
                view.showAlert(Alert.AlertType.INFORMATION,
                        "Template Downloaded",
                        "CSV template successfully saved to: " + file.getAbsolutePath());
            } catch (Exception e) {
                view.showAlert(Alert.AlertType.ERROR,
                        "Download Error",
                        "Failed to save CSV template: " + e.getMessage());
            }
        }
    }

    private void handleSaveChanges() {
        try {
            model.saveToCSV();
            view.showAlert(Alert.AlertType.INFORMATION,
                    "Save Successful",
                    "Data successfully saved to CSV file");
        } catch (Exception e) {
            view.showAlert(Alert.AlertType.ERROR,
                    "Save Error",
                    "Failed to save data: " + e.getMessage());
        }
    }

    private void handleDeleteSelected() {
        ObservableList<Transaction> selectedTransactions = view.getTableView().getSelectionModel().getSelectedItems();

        if (selectedTransactions.isEmpty()) {
            view.showAlert(Alert.AlertType.ERROR,
                    "Delete Error",
                    "No rows selected for deletion");
            return;
        }

        // Create a copy of selected items to avoid concurrent modification
        List<Transaction> toDelete = new ArrayList<>(selectedTransactions);
        model.removeTransactions(toDelete);
    }

    private void handleClearAll() {
        boolean confirmed = view.showConfirmDialog(
                "Confirm Clear All Data",
                "Are you sure you want to clear all data?",
                "This action cannot be undone.");

        if (confirmed) {
            model.clearAllTransactions();
        }
    }

    private void handleAddRecord() {
        // Validate input
        if (view.getSelectedDate() == null) {
            view.showAlert(Alert.AlertType.ERROR, "Input Error", "Please select a date");
            return;
        }

        String amountText = view.getAmountText();
        if (amountText.isEmpty()) {
            view.showAlert(Alert.AlertType.ERROR, "Input Error", "Amount cannot be empty");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            view.showAlert(Alert.AlertType.ERROR, "Input Error", "Amount must be a number");
            return;
        }

        // Create transaction
        String username;
        try {
            username = DashboardView.getCurrentUser().getUsername();
        } catch (Exception e) {
            username = "default_user"; // Fallback if DashboardView is not available
        }

        Transaction transaction = new Transaction(
                (int) model.getNextId(),
                username,
                "manual",
                view.getSelectedDate(),
                amount,
                view.getCategoryText().isEmpty() ? "Uncategorized" : view.getCategoryText(),
                view.getDescriptionText());

        model.addTransaction(transaction);
        view.clearForm();
    }

    private void handleBackToDashboard() {
        try {
            DashboardView dashboard = new DashboardView();
            dashboard.start(view.getStage());
        } catch (Exception e) {
            view.showAlert(Alert.AlertType.ERROR,
                    "Navigation Error",
                    "Failed to return to dashboard: " + e.getMessage());
        }
    }

    public void show() {
        view.show();
    }
}