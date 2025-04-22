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
import javafx.util.converter.DefaultStringConverter;
import org.mozilla.universalchardet.UniversalDetector;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class FormattedInput extends Application {

    private TableView<ObservableStringArray> tableView;
    private final ObservableList<ObservableStringArray> data = FXCollections.observableArrayList();
    private File lastSelectedFile;
    private AtomicLong nextId = new AtomicLong(1); // Simple ID generator

    // Form controls
    private DatePicker datePicker;
    private TextField amountField;
    private TextField categoryField;
    private TextField descriptionField;

    public static void main(String[] args) {
        launch(args);
    }

    // Wrapper class to make String[] observable with ID
    public static class ObservableStringArray {
        private final ObservableList<SimpleStringProperty> properties;

        public ObservableStringArray(String[] values) {
            this.properties = FXCollections.observableArrayList();
            for (String value : values) {
                properties.add(new SimpleStringProperty(value));
            }
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
        loadNextId();
        // Create main layout
        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(20));

        // Create button area
        HBox buttonBox = gethBox(primaryStage);

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

    private HBox gethBox(Stage primaryStage) {
        Button importButton = new Button("Import CSV File");
        importButton.setOnAction(e -> importCSV(primaryStage));

        Button templateButton = new Button("Download CSV Template");
        templateButton.setOnAction(e -> downloadTemplate(primaryStage));

        Button saveButton = new Button("Save Changes");
        saveButton.setOnAction(e -> saveToTransactionCSV());

        Button deleteButton = new Button("Delete Selected");
        deleteButton.setOnAction(e -> deleteSelectedRows());

        Button clearButton = new Button("Clear All Data");
        clearButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        clearButton.setOnAction(e -> clearAllData());

        Button btnBack = new Button("Dashboard");
        btnBack.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        btnBack.setOnAction(e -> {
            try {
                DashboardView dashboard = new DashboardView();
                dashboard.start(primaryStage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        HBox buttonBox = new HBox(10,
                importButton,
                templateButton,
                saveButton,
                deleteButton,
                btnBack,
                clearButton
        );
        buttonBox.setPadding(new Insets(10));
        return buttonBox;
    }

    private TableView<ObservableStringArray> createTableView() {
        TableView<ObservableStringArray> table = new TableView<>();
        table.setEditable(true);

        // Add ID column as the first column
        String[] headers = {"ID", "User", "Source", "Date", "Amount", "Category", "Description"};

        for (int i = 0; i < headers.length; i++) {
            final int columnIndex = i;
            TableColumn<ObservableStringArray, String> column = new TableColumn<>(headers[i]);
            column.setCellValueFactory(cellData -> cellData.getValue().getProperty(columnIndex));

            // Make ID column not editable, only Source and beyond can be edited
            if (i > 1) {
                column.setCellFactory(TextFieldTableCell.forTableColumn(new DefaultStringConverter()));
                column.setOnEditCommit(event -> {
                    ObservableStringArray row = event.getRowValue();
                    row.set(columnIndex, event.getNewValue());
                });
            }
            column.setPrefWidth(i == 0 ? 80 : 120); // Make ID column narrower
            table.getColumns().add(column);
        }

        table.setItems(data);
        return table;
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

            // Create record array with ID as first element
            String[] record = new String[7]; // Now 7 elements including ID
            record[0] = String.valueOf(nextId.getAndIncrement()); // Assign and increment ID
            record[1] = DashboardView.getCurrentUser().getUsername(); // Username
            record[2] = "manual"; // Source
            record[3] = datePicker.getValue().format(DateTimeFormatter.ISO_DATE); // Date
            record[4] = amountField.getText().trim(); // Amount
            record[5] = categoryField.getText().trim().isEmpty() ? "Uncategorized" : categoryField.getText().trim(); // Category
            record[6] = descriptionField.getText().trim(); // Description

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

    private void clearAllData() {
        // Create confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Clear All Data");
        alert.setHeaderText("Are you sure you want to clear all data?");
        alert.setContentText("This action cannot be undone.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                data.clear(); // Clear all data
                nextId.set(1); // Reset ID counter
            }
        });
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

            // 1. First try to auto-detect encoding
            String detectedEncoding = detectFileEncoding(file);

            // 2. Fallback encoding list (prioritized)
            String[] fallbackEncodings = {"UTF-8", "GBK", "GB18030", "GB2312", "ISO-8859-1"};

            // 3. Try all possible encodings
            List<String> encodingsToTry = new ArrayList<>();
            if (detectedEncoding != null) {
                encodingsToTry.add(detectedEncoding);
            }
            encodingsToTry.addAll(Arrays.asList(fallbackEncodings));

            boolean fileProcessed = false;
            IOException lastError = null;

            for (String encoding : encodingsToTry) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(new FileInputStream(file), encoding))) {

                    // 4. Improved file type detection
                    FileType fileType = detectFileType(reader);

                    // 5. Choose import method based on file type
                    switch (fileType) {
                        case ALIPAY:
                            importAlipayWithEncoding(encoding);
                            break;
                        case WECHAT:
                            importWechatWithEncoding(encoding);
                            break;
                        case REGULAR:
                            importRegularCSV(file, encoding);
                            break;
                    }
                    long maxId = data.stream()
                            .mapToLong(row -> Long.parseLong(row.toArray()[0]))
                            .max()
                            .orElse(0);

                    nextId.set(maxId + 1);
                    saveNextId(); // 立即保存新的nextId

                    fileProcessed = true;
                    break;
                } catch (IOException e) {
                    lastError = e;
                    continue;
                }
            }

            if (!fileProcessed) {
                String errorMsg = "Failed to import file.\n";
                if (lastError != null) {
                    errorMsg += "Last error: " + lastError.getMessage();
                }
                showErrorAlert("Import Error", errorMsg);
            }
        }
    }

    /**
     * Auto-detect file encoding
     */
    private String detectFileEncoding(File file) {
        try {
            // Use juniversalchardet library to detect encoding
            UniversalDetector detector = new UniversalDetector(null);
            byte[] buf = new byte[4096];
            try (FileInputStream fis = new FileInputStream(file)) {
                int nread;
                while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
                    detector.handleData(buf, 0, nread);
                }
            }
            detector.dataEnd();

            String encoding = detector.getDetectedCharset();
            detector.reset();

            if (encoding != null) {
                System.out.println("Detected encoding: " + encoding);
                // Convert detected encoding to Java-supported encoding name
                if (encoding.equalsIgnoreCase("GB-18030")) {
                    return "GB18030";
                }
                if (encoding.equalsIgnoreCase("Big5")) {
                    return "MS950";
                }
                return encoding;
            }
        } catch (IOException e) {
            System.err.println("Error detecting encoding: " + e.getMessage());
        }
        return null;
    }

    /**
     * Improved file type detection method
     */
    private FileType detectFileType(BufferedReader reader) throws IOException {
        reader.mark(8192); // Mark to reset later
        String line;
        int lineCount = 0;
        boolean hasAlipayMarker = false;
        boolean hasWechatMarker = false;

        while ((line = reader.readLine()) != null && lineCount < 10) {
            // Alipay detection - enhanced feature detection
            if (line.contains("支付宝")) {
                hasAlipayMarker = true;
            }

            // WeChat detection - enhanced feature detection
            if (line.contains("微信支付账单明细")) {
                hasWechatMarker = true;
            }

            lineCount++;
        }

        reader.reset(); // Reset to marked position

        if (hasAlipayMarker) {
            return FileType.ALIPAY;
        }
        if (hasWechatMarker) {
            return FileType.WECHAT;
        }
        return FileType.REGULAR;
    }

    // File type enum
    private enum FileType {
        ALIPAY, WECHAT, REGULAR
    }

    private void importRegularCSV(File file, String encoding) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), encoding))) {
            String line;
            boolean isFirstLine = true;
            int recordsImported = 0;

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;  // Skip header line
                    continue;
                }

                String[] rowData = parseCsvLine(line);
                String[] record = new String[7]; // 7 elements including ID

                // Always assign new ID
                record[0] = String.valueOf(nextId.getAndIncrement());

                if (rowData.length >= 6) {  // At least 6 columns (excluding ID)
                    // Format: User,Source,Date,Amount,Category,Description
                    record[1] = rowData.length > 0 ? rowData[0].trim() : DashboardView.getCurrentUser().getUsername();
                    record[2] = rowData.length > 1 ? rowData[1].trim() : "import";
                    record[3] = rowData.length > 2 ? rowData[2].trim() : "";
                    record[4] = rowData.length > 3 ? rowData[3].trim() : "";
                    record[5] = rowData.length > 4 ? rowData[4].trim() : "Uncategorized";
                    record[6] = rowData.length > 5 ? rowData[5].trim() : "";
                } else {
                    // Less than 6 columns (old format or incomplete data)
                    record[1] = DashboardView.getCurrentUser().getUsername();
                    record[2] = "import";
                    record[3] = rowData.length > 0 ? rowData[0] : "";
                    record[4] = rowData.length > 1 ? rowData[1] : "";
                    record[5] = rowData.length > 2 ? rowData[2] : "Uncategorized";
                    record[6] = rowData.length > 3 ? rowData[3] : "";
                }

                data.add(new ObservableStringArray(record));
                recordsImported++;
            }

            // Save updated nextId
            saveNextId();

            // Notify Dashboard that import is complete
            DashboardView.setImportDone(true);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Import Successful");
            alert.setHeaderText(null);
            alert.setContentText("Successfully imported " + recordsImported + " records (Encoding: " + encoding + ")");
            alert.showAndWait();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void importWechatWithEncoding(String encoding) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(lastSelectedFile), encoding))) {
            String line;
            int lineCount = 0;
            int recordsImported = 0;

            // Skip first 17 lines
            while (lineCount < 17 && (line = reader.readLine()) != null) {
                lineCount++;
            }

            // Process data starting from line 18
            while ((line = reader.readLine()) != null) {
                String[] rowData = parseCsvLine(line);

                // Ensure enough columns and not empty row
                if (rowData.length >= 6 && !isEmptyRow(rowData)) {
                    String[] fullRowData = new String[7]; // 7 elements including ID

                    // Process date - extract date part from transaction time
                    String rawDate = rowData[0];
                    String processedDate = processDate(rawDate);

                    // Process amount - remove currency symbol
                    String rawAmount = rowData[5];
                    String processedAmount = processAmount(rawAmount);

                    // Assign new ID
                    fullRowData[0] = String.valueOf(nextId.getAndIncrement());
                    fullRowData[1] = DashboardView.getCurrentUser().getUsername();  // User
                    fullRowData[2] = "wechat";         // Source
                    fullRowData[3] = processedDate;     // Date (column 1, processed)
                    fullRowData[4] = processedAmount;   // Amount (column 6, processed)
                    fullRowData[5] = "Uncategorized";   // Category (hardcoded)
                    fullRowData[6] = rowData[1]+rowData[2]; // Description (columns 2+3 combined)

                    data.add(new ObservableStringArray(fullRowData));
                    recordsImported++;
                }
            }

            // Save updated nextId
            saveNextId();

            // Notify Dashboard that import is complete
            DashboardView.setImportDone(true);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Import Successful");
            alert.setHeaderText(null);
            alert.setContentText("Successfully imported " + recordsImported + " records from WeChat (Encoding: " + encoding + ")");
            alert.showAndWait();

        } catch (IOException e) {
            showErrorAlert("Import Error", "Failed to import WeChat CSV with encoding " + encoding + ": " + e.getMessage());
        }
    }

    private void importAlipayWithEncoding(String encoding) {
        boolean firstRowSkipped = false; // Flag for whether first row is skipped
        int recordsImported = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(lastSelectedFile), encoding))) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] rowData = parseCsvLine(line);

                // Check conditions and if it's the first time
                if (rowData.length >= 7 && !isEmptyRow(rowData)) {
                    if (!firstRowSkipped) {
                        firstRowSkipped = true; // Mark first row as skipped
                        continue; // Skip current line
                    }

                    String[] fullRowData = new String[7]; // 7 elements including ID
                    String rawDate = rowData[0];
                    String processedDate = processDate(rawDate);

                    // Assign new ID
                    fullRowData[0] = String.valueOf(nextId.getAndIncrement());
                    fullRowData[1] = DashboardView.getCurrentUser().getUsername();
                    fullRowData[2] = "alipay";
                    fullRowData[3] = processedDate;
                    fullRowData[4] = rowData[6];
                    fullRowData[5] = "Uncategorized";
                    fullRowData[6] = rowData[1] + rowData[4];

                    data.add(new ObservableStringArray(fullRowData));
                    recordsImported++;
                }
            }

            // Save updated nextId
            saveNextId();

            // Notify Dashboard that import is complete
            DashboardView.setImportDone(true);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Import Successful");
            alert.setHeaderText(null);
            alert.setContentText("Successfully imported " + recordsImported + " records from Alipay (Encoding: " + encoding + ")");
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
        fileChooser.setInitialFileName("transactions.csv");

        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                // Write header with ID matching table: ID,User,Source,Date,Amount,Category,Description
                writer.write("ID,User,Source,Date,Amount,Category,Description");
                writer.newLine();

                // Write example rows with IDs
                String currentUser = DashboardView.getCurrentUser().getUsername();
                writer.write(String.format("1,%s,manual,2025-04-13,5000,Uncategorized,Salary", currentUser));
                writer.newLine();
                writer.write(String.format("2,%s,manual,2025-04-14,-200,Uncategorized,Grocery Shopping", currentUser));
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
                System.out.printf("%d: [ID: %s, User: %s, Source: %s, Date: %s, Amount: %s, Category: %s, Description: %s]%n",
                        i + 1,
                        record[0],
                        record[1],
                        record[2],
                        record[3],
                        record[4],
                        record[5],
                        record[6]);
            }
            System.out.println("===================\n");

            // 创建用户特定的文件名
            String username = DashboardView.getCurrentUser().getUsername();
            File file = new File(username + "_transactions.csv");
            boolean fileExists = file.exists();

            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8))) {

                // 如果文件不存在，才写入表头
                if (!fileExists) {
                    writer.write("ID,User,Source,Date,Amount,Category,Description");
                    writer.newLine();
                }

                // 写入数据记录
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
                alert.setTitle("保存成功");
                alert.setHeaderText(null);
                alert.setContentText("数据成功追加保存到: " + file.getAbsolutePath());
                alert.showAndWait();
                saveNextId();
            } catch (IOException e) {
                showErrorAlert("保存错误", "保存数据失败: " + e.getMessage());
            }
        } catch (Exception e) {
            showErrorAlert("保存错误", "保存数据失败: " + e.getMessage());
        }

        // 保存后清空当前数据表，防止重复保存
        data.clear();
    }
    private void loadNextId() {
        File file = new File(DashboardView.getCurrentUser().getUsername()+"_nextId.txt");
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line = reader.readLine();
                if (line != null && !line.isEmpty()) {
                    long loadedId = Long.parseLong(line);
                    if (loadedId > 0) {
                        nextId.set(loadedId);
                    } else {
                        nextId.set(1); // 如果ID不合法，设为1
                        saveNextId();  // 保存更正后的值
                    }
                } else {
                    nextId.set(1); // 如果文件为空，设为1
                    saveNextId();
                }
            } catch (IOException | NumberFormatException e) {
                System.err.println("无法加载 nextId: " + e.getMessage());
                nextId.set(1); // 出错时使用默认值1
                saveNextId();
            }
        } else {
            nextId.set(1); // 文件不存在时使用默认值1
            saveNextId();
        }
    }
    private void saveNextId() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DashboardView.getCurrentUser().getUsername()+"_nextId.txt"))) {
            writer.write(String.valueOf(nextId.get()));
        } catch (IOException e) {
            System.err.println("无法保存 nextId: " + e.getMessage());
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