package org.example;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FormattedInput extends Application {

    private TableView<ObservableStringArray> tableView;
    private final ObservableList<ObservableStringArray> data = FXCollections.observableArrayList();
    private File lastSelectedFile;

    // Form controls
    private DatePicker datePicker;
    private TextField amountField;
    private TextField categoryField;
    private TextField descriptionField;

    public static void main(String[] args) {
        launch(args);
    }

    // Wrapper class to make String[] observable
    public static class ObservableStringArray {
        private final ObservableList<SimpleStringProperty> properties;

        public ObservableStringArray(String[] values) {
            this.properties = FXCollections.observableArrayList();
            for (String value : values) {
                properties.add(new SimpleStringProperty(value));
            }
        }

        public String get(int index) {
            return properties.get(index).get();
        }

        public void set(int index, String value) {
            properties.get(index).set(value);
        }

        public SimpleStringProperty getProperty(int index) {
            return properties.get(index);
        }

        public String[] toArray() {
            String[] array = new String[properties.size()];
            for (int i = 0; i < array.length; i++) {
                array[i] = properties.get(i).get();
            }
            return array;
        }
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("CSV Import and Record Management Tool");

        // Create main layout
        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(20));

        // Create button area
        Button importButton = new Button("Import CSV File");
        importButton.setOnAction(e -> importCSV(primaryStage));

        Button templateButton = new Button("Download CSV Template");
        templateButton.setOnAction(e -> downloadTemplate(primaryStage));

        Button saveButton = new Button("Save Changes");
        saveButton.setOnAction(e -> saveToTransactionCSV());

        Button deleteButton = new Button("Delete Selected");
        deleteButton.setOnAction(e -> deleteSelectedRows());

        HBox buttonBox = new HBox(10, importButton, templateButton, saveButton, deleteButton);

        // Create table view
        tableView = createTableView();

        // Create add record form
        VBox formBox = createAddRecordForm();

        // Create status label
        Label statusLabel = new Label("Ready");

        // Add components to layout
        mainLayout.getChildren().addAll(buttonBox, tableView, formBox, statusLabel);

        // Create scene
        Scene scene = new Scene(mainLayout, 1000, 800);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox createAddRecordForm() {
        VBox formBox = new VBox(10);
        formBox.setPadding(new Insets(15));
        formBox.setStyle("-fx-border-color: #cccccc; -fx-border-radius: 5;");

        Label formTitle = new Label("Add New Record");
        formTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

        // Create form fields
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

        // Fourth row: Add button (right-aligned)
        Button addButton = new Button("Add Record");
        addButton.setOnAction(e -> addRecord());
        GridPane.setColumnSpan(addButton, 2);
        GridPane.setHalignment(addButton, HPos.RIGHT);
        formGrid.add(addButton, 2, 3);

        formBox.getChildren().addAll(formTitle, formGrid);
        return formBox;
    }

    private TableView<ObservableStringArray> createTableView() {
        TableView<ObservableStringArray> table = new TableView<>();
        table.setEditable(true);

        // Create table columns
        String[] headers = {"User", "Source", "Date", "Amount", "Category", "Description"};
        for (int i = 0; i < headers.length; i++) {
            final int columnIndex = i;
            TableColumn<ObservableStringArray, String> column = new TableColumn<>(headers[i]);

            // Set cell value factory
            column.setCellValueFactory(cellData -> cellData.getValue().getProperty(columnIndex));

            // Make cells editable
            column.setCellFactory(TextFieldTableCell.forTableColumn(new DefaultStringConverter()));

            // Handle edit commits
            column.setOnEditCommit(event -> {
                ObservableStringArray row = event.getRowValue();
                row.set(columnIndex, event.getNewValue());
            });

            column.setPrefWidth(150);
            table.getColumns().add(column);
        }

        table.setItems(data);
        return table;
    }

    private void addRecord() {
        try {
            // Validate input
            if (datePicker.getValue() == null) {
                showErrorAlert("Input Error", "Please select a date");
                return;
            }

            // Validate amount is numeric
            try {
                Double.parseDouble(amountField.getText().trim());
            } catch (NumberFormatException e) {
                showErrorAlert("Input Error", "Amount must be a number");
                return;
            }

            // Create record array
            String[] record = new String[6];
            record[0] = "";  // User field empty
            record[1] = "manual";
            record[2] = datePicker.getValue().format(DateTimeFormatter.ISO_DATE);
            record[3] = amountField.getText().trim();
            record[4] = categoryField.getText().trim().isEmpty() ? "Uncategorized" : categoryField.getText().trim();
            record[5] = descriptionField.getText().trim();

            // Add to data list
            data.add(new ObservableStringArray(record));

            // Clear form
            datePicker.setValue(LocalDate.now());
            amountField.clear();
            categoryField.clear();
            descriptionField.clear();

        } catch (Exception e) {
            showErrorAlert("Add Record Error", "Failed to add record: " + e.getMessage());
        }
    }

    private void deleteSelectedRows() {
        ObservableList<ObservableStringArray> selectedRows = tableView.getSelectionModel().getSelectedItems();
        if (selectedRows.isEmpty()) {
            showErrorAlert("Delete Error", "No rows selected for deletion");
            return;
        }

        data.removeAll(selectedRows);
    }

    private void importCSV(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select CSV File to Import");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            lastSelectedFile = file;

            String[] encodings = {"GBK", "UTF-8", "GB18030", "GB2312", "ISO-8859-1"};
            boolean fileProcessed = false;

            for (String encoding : encodings) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding))) {
                    String line;
                    boolean isAlipayFile = false;
                    boolean isWechatFile = false;
                    int lineCount = 0;

                    // Detect file type by checking first few lines
                    while ((line = reader.readLine()) != null && lineCount < 10) {
                        if (line.contains("Alipay Account") || line.contains("Alipay") || line.contains("alipay")) {
                            isAlipayFile = true;
                            break;
                        }
                        if (line.contains("WeChat Payment Details") || line.contains("WeChat Pay")) {
                            isWechatFile = true;
                            break;
                        }
                        lineCount++;
                    }

                    // Process file based on detection
                    if (isAlipayFile) {
                        importAlipayWithEncoding(encoding);
                        fileProcessed = true;
                        break;
                    } else if (isWechatFile) {
                        importWechatWithEncoding(encoding);
                        fileProcessed = true;
                        break;
                    } else {
                        // Try to import as regular CSV with current encoding
                        try {
                            importRegularCSV(file, encoding);
                            fileProcessed = true;
                            break;
                        } catch (IOException e) {
                            // Import failed, try next encoding
                            continue;
                        }
                    }
                } catch (IOException e) {
                    // Current encoding failed, try next
                    continue;
                }
            }

            if (!fileProcessed) {
                showErrorAlert("Import Error", "Failed to import file, all encoding attempts failed.");
            }
        }
    }

    private void importRegularCSV(File file, String encoding) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding))) {
            data.clear();
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // Skip header
                }

                String[] rowData = parseCsvLine(line);
                String[] record = new String[6];
                record[0] = "To be imported"; // User
                record[1] = "import"; // Source
                record[2] = rowData.length > 0 ? rowData[0] : ""; // Date
                record[3] = rowData.length > 1 ? rowData[1] : ""; // Amount
                record[4] = rowData.length > 2 ? rowData[2] : "Uncategorized"; // Category
                record[5] = rowData.length > 3 ? rowData[3] : ""; // Description

                data.add(new ObservableStringArray(record));
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Import Successful");
            alert.setHeaderText(null);
            alert.setContentText("Successfully imported regular CSV file, encoding: " + encoding);
            alert.showAndWait();
        }
    }

    private void importWechatWithEncoding(String encoding) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(lastSelectedFile), encoding))) {
            data.clear();
            String line;
            int lineCount = 0;

            // Skip first 17 lines
            while (lineCount < 17 && (line = reader.readLine()) != null) {
                lineCount++;
            }

            // Process data from line 18
            while ((line = reader.readLine()) != null) {
                String[] rowData = parseCsvLine(line);

                // Ensure enough columns and not empty row
                if (rowData.length >= 6 && !isEmptyRow(rowData)) {
                    String[] fullRowData = new String[6];

                    // Process date - extract date part from transaction time
                    String rawDate = rowData[0];
                    String processedDate = processDate(rawDate);

                    // Process amount - remove currency symbol
                    String rawAmount = rowData[5];
                    String processedAmount = processAmount(rawAmount);

                    fullRowData[0] = "Default User";        // User (hardcoded)
                    fullRowData[1] = "wechat";             // Source
                    fullRowData[2] = processedDate;        // Date (column 1, processed)
                    fullRowData[3] = processedAmount;       // Amount (column 6, processed)
                    fullRowData[4] = "Uncategorized";      // Category (hardcoded)

                    // Combine columns 2 and 3 as description
                    String description = (rowData.length > 1 ? rowData[1] : "") +
                            (rowData.length > 2 ? " " + rowData[2] : "");
                    fullRowData[5] = description;          // Description (columns 2+3 combined)

                    data.add(new ObservableStringArray(fullRowData));
                }
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Import Successful");
            alert.setHeaderText(null);
            alert.setContentText("Successfully imported " + data.size() + " records from WeChat (encoding: " + encoding + ")");
            alert.showAndWait();

        } catch (IOException e) {
            showErrorAlert("Import Error", "Failed to import WeChat CSV with encoding " + encoding + ": " + e.getMessage());
        }
    }

    private void importAlipayWithEncoding(String encoding) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(lastSelectedFile), encoding))) {
            data.clear();
            String line;
            int lineCount = 0;

            // Skip first 25 lines
            while (lineCount < 25 && (line = reader.readLine()) != null) {
                lineCount++;
            }

            // Process data from line 26
            while ((line = reader.readLine()) != null) {
                String[] rowData = parseCsvLine(line);

                if (rowData.length >= 7 && !isEmptyRow(rowData)) {
                    String[] fullRowData = new String[6];

                    // Process date - extract date part
                    String rawDate = rowData[0];
                    String processedDate = processDate(rawDate);

                    fullRowData[0] = "Default User";        // User (hardcoded)
                    fullRowData[1] = "alipay";             // Source
                    fullRowData[2] = processedDate;       // Date (column 1, processed)
                    fullRowData[3] = rowData[6];           // Amount (column 7)
                    fullRowData[4] = "Uncategorized";     // Category (hardcoded)
                    fullRowData[5] = rowData[1] + rowData[4]; // Description (columns 2+5 combined)

                    data.add(new ObservableStringArray(fullRowData));
                }
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Import Successful");
            alert.setHeaderText(null);
            alert.setContentText("Successfully imported " + data.size() + " records from Alipay (encoding: " + encoding + ")");
            alert.showAndWait();

        } catch (IOException e) {
            showErrorAlert("Import Error", "Failed to import Alipay CSV with encoding " + encoding + ": " + e.getMessage());
        }
    }

    private boolean isEmptyRow(String[] rowData) {
        for (String cell : rowData) {
            if (cell != null && !cell.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private String processDate(String rawDate) {
        if (rawDate != null && rawDate.length() >= 10) {
            return rawDate.substring(0, 10);
        }
        return rawDate;
    }

    private String processAmount(String rawAmount) {
        if (rawAmount == null || rawAmount.isEmpty()) {
            return "0.00";
        }

        String processed = rawAmount.replace("¥", "").trim();
        processed = processed.replace("￥", "").replace("$", "").replace("€", "").replace("£", "");
        return processed;
    }

    private void downloadTemplate(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save CSV Template");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        fileChooser.setInitialFileName("csv_template.csv");

        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                // Write header
                writer.write("Date,Amount,Category,Description");
                writer.newLine();

                // Write sample data
                writer.write("2025-04-13,5000,,Salary");
                writer.newLine();
                writer.write("2025-04-14,-200,,Grocery Shopping");
                writer.newLine();

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Template Downloaded");
                alert.setHeaderText(null);
                alert.setContentText("CSV template successfully saved to: " + file.getAbsolutePath());
                alert.showAndWait();

            } catch (IOException e) {
                showErrorAlert("Download Error", "Failed to save CSV template: " + e.getMessage());
            }
        }
    }

    private String[] parseCsvLine(String line) {
        List<String> tokens = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder sb = new StringBuilder();
        char[] chars = line.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c == '"') {
                if (i < chars.length - 1 && chars[i + 1] == '"') {
                    sb.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString().trim());
        return tokens.toArray(new String[0]);
    }

    private void saveToTransactionCSV() {
        try {
            System.out.println("\n=== Data to be saved ===");
            System.out.println("Total records: " + data.size());
            for (int i = 0; i < data.size(); i++) {
                String[] record = data.get(i).toArray();
                System.out.printf("%d: [Source: %s, Date: %s, Amount: %s, Category: %s, Description: %s]%n",
                        i + 1,
                        record[1],
                        record[2],
                        record[3],
                        record[4],
                        record[5]);
            }
            System.out.println("===================\n");

            File file = new File("transactions.csv");

            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {

                // Always write header
                writer.write("User,Source,Date,Amount,Category,Description");
                writer.newLine();

                for (ObservableStringArray row : data) {
                    String[] record = row.toArray();
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < record.length; i++) {
                        String field = record[i];
                        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
                            sb.append("\"").append(field.replace("\"", "\"\"")).append("\"");
                        } else {
                            sb.append(field);
                        }

                        if (i < record.length - 1) {
                            sb.append(",");
                        }
                    }
                    writer.write(sb.toString());
                    writer.newLine();
                }

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Save Successful");
                alert.setHeaderText(null);
                alert.setContentText("Data successfully saved to: " + file.getAbsolutePath());
                alert.showAndWait();

            } catch (IOException e) {
                showErrorAlert("Save Error", "Failed to save data: " + e.getMessage());
            }
        } catch (Exception e) {
            showErrorAlert("Save Error", "Failed to save data: " + e.getMessage());
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}