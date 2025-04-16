package org.example.list;

import java.io.IOException;

// TransactionController.java
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;

public class TransactionController {
    private final TransactionView view;
    private final TransactionLoader loader;
    private ObservableList<Transaction> data = FXCollections.observableArrayList();

    public TransactionController(TransactionView view, TransactionLoader loader) {
        this.view = view;
        this.loader = loader;
        bindActions();
    }

    private void bindActions() {
        view.getLoadButton().setOnAction(e -> loadData());
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

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}