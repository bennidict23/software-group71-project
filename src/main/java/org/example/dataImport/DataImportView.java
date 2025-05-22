package org.example.dataImport;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;
import org.example.list.Transaction;

import java.time.LocalDate;

public class DataImportView {

    private final Stage stage;
    private TableView<Transaction> tableView;
    private DatePicker datePicker;
    private TextField amountField;
    private TextField categoryField;
    private TextField descriptionField;

    // Event handlers (to be set by controller)
    private Runnable onImportCSV;
    private Runnable onDownloadTemplate;
    private Runnable onSaveChanges;
    private Runnable onDeleteSelected;
    private Runnable onClearAll;
    private Runnable onAddRecord;
    private Runnable onBackToDashboard;

    public DataImportView(Stage stage) {
        this.stage = stage;
        setupView();
    }

    private void setupView() {
        stage.setTitle("CSV Import and Record Management Tool");

        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(20));

        // Create components
        HBox buttonBox = createButtonBox();
        tableView = createTableView();
        VBox formBox = createAddRecordForm();

        mainLayout.getChildren().addAll(buttonBox, tableView, formBox);

        Scene scene = new Scene(mainLayout, 1000, 800);
        stage.setScene(scene);
    }

    private HBox createButtonBox() {
        Button importButton = new Button("Import CSV File");
        importButton.setOnAction(e -> { if (onImportCSV != null) onImportCSV.run(); });

        Button templateButton = new Button("Download CSV Template");
        templateButton.setOnAction(e -> { if (onDownloadTemplate != null) onDownloadTemplate.run(); });

        Button saveButton = new Button("Save Changes");
        saveButton.setOnAction(e -> { if (onSaveChanges != null) onSaveChanges.run(); });

        Button deleteButton = new Button("Delete Selected");
        deleteButton.setOnAction(e -> { if (onDeleteSelected != null) onDeleteSelected.run(); });

        Button clearButton = new Button("Clear All Data");
        clearButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        clearButton.setOnAction(e -> { if (onClearAll != null) onClearAll.run(); });

        Button backButton = new Button("Dashboard");
        backButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        backButton.setOnAction(e -> { if (onBackToDashboard != null) onBackToDashboard.run(); });

        HBox buttonBox = new HBox(10, importButton, templateButton, saveButton,
                deleteButton, backButton, clearButton);
        buttonBox.setPadding(new Insets(10));
        return buttonBox;
    }

    private TableView<Transaction> createTableView() {
        TableView<Transaction> table = new TableView<>();
        table.setEditable(true);

        // Define columns
        TableColumn<Transaction, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idColumn.setPrefWidth(80);
        idColumn.setSortable(false);

        TableColumn<Transaction, String> userColumn = new TableColumn<>("User");
        userColumn.setCellValueFactory(new PropertyValueFactory<>("user"));

        TableColumn<Transaction, String> sourceColumn = new TableColumn<>("Source");
        sourceColumn.setCellValueFactory(new PropertyValueFactory<>("source"));

        TableColumn<Transaction, LocalDate> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateColumn.setSortType(TableColumn.SortType.DESCENDING);

        TableColumn<Transaction, Double> amountColumn = new TableColumn<>("Amount");
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amountColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));

        TableColumn<Transaction, String> categoryColumn = new TableColumn<>("Category");
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        categoryColumn.setCellFactory(TextFieldTableCell.forTableColumn());

        TableColumn<Transaction, String> descriptionColumn = new TableColumn<>("Description");
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        descriptionColumn.setCellFactory(TextFieldTableCell.forTableColumn());

        // Add edit commit handlers
        amountColumn.setOnEditCommit(event -> {
            Transaction transaction = event.getRowValue();
            transaction.amountProperty().set(event.getNewValue());
        });

        categoryColumn.setOnEditCommit(event -> {
            Transaction transaction = event.getRowValue();
            transaction.categoryProperty().set(event.getNewValue());
        });

        descriptionColumn.setOnEditCommit(event -> {
            Transaction transaction = event.getRowValue();
            transaction.descriptionProperty().set(event.getNewValue());
        });

        table.getColumns().addAll(idColumn, userColumn, sourceColumn, dateColumn,
                amountColumn, categoryColumn, descriptionColumn);

        return table;
    }

    private VBox createAddRecordForm() {
        VBox formBox = new VBox(10);
        formBox.setPadding(new Insets(15));
        formBox.setStyle("-fx-border-color: #cccccc; -fx-border-radius: 5;");

        Label formTitle = new Label("Add New Record");
        formTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

        GridPane formGrid = new GridPane();
        formGrid.setHgap(15);
        formGrid.setVgap(15);
        formGrid.setPadding(new Insets(15, 0, 15, 0));

        // First row: Date and Amount
        formGrid.add(new Label("Date:"), 0, 0);
        datePicker = new DatePicker(LocalDate.now());
        formGrid.add(datePicker, 1, 0);

        formGrid.add(new Label("Amount:"), 2, 0);
        amountField = new TextField();
        formGrid.add(amountField, 3, 0);

        // Second row: Category
        formGrid.add(new Label("Category:"), 0, 1);
        categoryField = new TextField();
        GridPane.setColumnSpan(categoryField, 3);
        formGrid.add(categoryField, 1, 1);

        // Third row: Description
        formGrid.add(new Label("Description:"), 0, 2);
        descriptionField = new TextField();
        GridPane.setColumnSpan(descriptionField, 3);
        formGrid.add(descriptionField, 1, 2);

        // Fourth row: Add button
        Button addButton = new Button("Add Record");
        addButton.setOnAction(e -> { if (onAddRecord != null) onAddRecord.run(); });
        GridPane.setColumnSpan(addButton, 2);
        GridPane.setHalignment(addButton, HPos.RIGHT);
        formGrid.add(addButton, 2, 3);

        formBox.getChildren().addAll(formTitle, formGrid);
        return formBox;
    }

    public void show() {
        stage.show();
    }

    public void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public boolean showConfirmDialog(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        return alert.showAndWait()
                .filter(response -> response == ButtonType.OK)
                .isPresent();
    }

    public void clearForm() {
        datePicker.setValue(LocalDate.now());
        amountField.clear();
        categoryField.clear();
        descriptionField.clear();
    }

    // Getters for form data
    public LocalDate getSelectedDate() {
        return datePicker.getValue();
    }

    public String getAmountText() {
        return amountField.getText().trim();
    }

    public String getCategoryText() {
        return categoryField.getText().trim();
    }

    public String getDescriptionText() {
        return descriptionField.getText().trim();
    }

    public TableView<Transaction> getTableView() {
        return tableView;
    }

    public Stage getStage() {
        return stage;
    }

    // Event handler setters
    public void setOnImportCSV(Runnable handler) {
        this.onImportCSV = handler;
    }

    public void setOnDownloadTemplate(Runnable handler) {
        this.onDownloadTemplate = handler;
    }

    public void setOnSaveChanges(Runnable handler) {
        this.onSaveChanges = handler;
    }

    public void setOnDeleteSelected(Runnable handler) {
        this.onDeleteSelected = handler;
    }

    public void setOnClearAll(Runnable handler) {
        this.onClearAll = handler;
    }

    public void setOnAddRecord(Runnable handler) {
        this.onAddRecord = handler;
    }

    public void setOnBackToDashboard(Runnable handler) {
        this.onBackToDashboard = handler;
    }
}