package org.example.list;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

// TransactionController.java
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.Alert;
import javafx.scene.input.KeyCode;

public class TransactionController {
    private final TransactionView view;
    private final TransactionLoader loader;
    private ObservableList<Transaction> data = FXCollections.observableArrayList();
    private final FilteredList<Transaction> filteredData;
    public TransactionController(TransactionView view, TransactionLoader loader) {
        this.view = view;
        this.loader = loader;
        bindActions();
        filteredData = new FilteredList<>(data);
        view.updateTable(filteredData);
        bindActions();
        loadData();
    }

    private void bindActions() {
        view.getLoadButton().setOnAction(e -> loadData());
        // Search button
        view.getSearchButton().setOnAction(e -> triggerSearch());
        
        // Search enter
        view.getSearchField().setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                triggerSearch();
            }
        });
    }

    private void loadData() {
        try {
            data.clear();
            data.addAll(loader.loadTransactions("transactions.csv"));
            view.updateTable(data);
        } catch (IOException ex) {
            showError("文件加载失败", ex.getMessage());
        } catch (Exception ex) {
            showError("数据格式错误", ex.getMessage());
        }
    }
    // Search
    //start
    private void updateSearchFilter(String filter) {
        filteredData.setPredicate(transaction -> {
            if (filter.isEmpty()) return true;

            return matchesField(transaction.getUser(), filter) ||
                   matchesField(transaction.getSource(), filter) ||
                   matchesField(transaction.getCategory(), filter) ||
                   matchesField(transaction.getDescription(), filter) ||
                   matchesNumber(transaction.getAmount(), filter) ||
                   matchesDate(transaction.getDate(), filter);
        });
    }
    private void triggerSearch() {
        String filter = view.getSearchField().getText().trim().toLowerCase();
        updateSearchFilter(filter);
        view.updateTable(filteredData);
    }
    private boolean matchesField(String value, String filter) {
        return value.toLowerCase().contains(filter);
    }
    private boolean matchesNumber(double amount, String filter) {
        try {
            double searchValue = Double.parseDouble(filter);
            return amount == searchValue;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean matchesDate(LocalDate date, String filter) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return formatter.format(date).contains(filter);
    }

    //end
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}