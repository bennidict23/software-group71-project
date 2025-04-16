package org.example.list;

// TransactionView.java
import java.time.LocalDate;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class TransactionView {
    private final BorderPane root = new BorderPane();
    private final TableView<Transaction> table = new TableView<>();
    private final Button loadButton = new Button("Load Data");

    public TransactionView() {
        configureTable();
        layoutUI();
    }

    private void configureTable() {
        TableColumn<Transaction, String> userCol = new TableColumn<>("User");
        TableColumn<Transaction, String> sourceCol = new TableColumn<>("Source");
        TableColumn<Transaction, LocalDate> dateCol = new TableColumn<>("Date");
        TableColumn<Transaction, Double> amountCol = new TableColumn<>("Amount");
        TableColumn<Transaction, String> categoryCol = new TableColumn<>("Category");
        TableColumn<Transaction, String> descCol = new TableColumn<>("Description");

        userCol.setCellValueFactory(data -> data.getValue().userProperty());
        sourceCol.setCellValueFactory(data -> data.getValue().sourceProperty());
        dateCol.setCellValueFactory(data -> data.getValue().dateProperty());
        amountCol.setCellValueFactory(data -> data.getValue().amountProperty().asObject());
        categoryCol.setCellValueFactory(data -> data.getValue().categoryProperty());
        descCol.setCellValueFactory(data -> data.getValue().descriptionProperty());

        table.getColumns().addAll(userCol, sourceCol, dateCol, amountCol, categoryCol, descCol);
    }

    private void layoutUI() {
        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(table, loadButton);
        root.setCenter(vbox);
    }

    public BorderPane getView() {
        return root;
    }

    public Button getLoadButton() {
        return loadButton;
    }

    public void updateTable(ObservableList<Transaction> data) {
        table.setItems(data);
    }
}