package org.example;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.converter.DefaultStringConverter;
import org.example.list.Transaction;
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

    private TableView<Transaction> tableView;
    private final ObservableList<Transaction> data = FXCollections.observableArrayList();
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

    private TableView<Transaction> createTableView() {
        TableView<Transaction> table = new TableView<>();
        table.setEditable(true);

        // Define columns
        TableColumn<Transaction, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idColumn.setPrefWidth(80);
        idColumn.setSortable(false); // ID 通常不需要排序

        TableColumn<Transaction, String> userColumn = new TableColumn<>("User");
        userColumn.setCellValueFactory(new PropertyValueFactory<>("user"));

        TableColumn<Transaction, String> sourceColumn = new TableColumn<>("Source");
        sourceColumn.setCellValueFactory(new PropertyValueFactory<>("source"));

        TableColumn<Transaction, LocalDate> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateColumn.setSortType(TableColumn.SortType.DESCENDING); // 默认按日期降序排序

        TableColumn<Transaction, Double> amountColumn = new TableColumn<>("Amount");
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amountColumn.setCellFactory(TextFieldTableCell.forTableColumn(new javafx.util.converter.DoubleStringConverter()));

        TableColumn<Transaction, String> categoryColumn = new TableColumn<>("Category");
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        categoryColumn.setCellFactory(TextFieldTableCell.forTableColumn());

        TableColumn<Transaction, String> descriptionColumn = new TableColumn<>("Description");
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        descriptionColumn.setCellFactory(TextFieldTableCell.forTableColumn());

        // 添加列到 TableView
        table.getColumns().addAll(idColumn, userColumn, sourceColumn, dateColumn, amountColumn, categoryColumn, descriptionColumn);

        // 设置单元格可编辑
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

        // 使用 ObservableList<Transaction> 作为数据源
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
            if (datePicker.getValue() == null) {
                showErrorAlert("Input Error", "Please select a date");
                return;
            }

            try {
                Double.parseDouble(amountField.getText().trim());
            } catch (NumberFormatException e) {
                showErrorAlert("Input Error", "Amount must be a number");
                return;
            }

            // 创建 Transaction 对象
            Transaction transaction = new Transaction(
                    (int)nextId.getAndIncrement(),
                    DashboardView.getCurrentUser().getUsername(),
                    "manual",
                    datePicker.getValue(),
                    Double.parseDouble(amountField.getText().trim()),
                    categoryField.getText().trim().isEmpty() ? "Uncategorized" : categoryField.getText().trim(),
                    descriptionField.getText().trim()
            );

            data.add(transaction);

            // 清空表单
            datePicker.setValue(LocalDate.now());
            amountField.clear();
            categoryField.clear();
            descriptionField.clear();

        } catch (Exception e) {
            showErrorAlert("Add Record Error", "Failed to add record: " + e.getMessage());
        }
    }

    private void deleteSelectedRows() {
        ObservableList<Transaction> selectedRows = tableView.getSelectionModel().getSelectedItems();
        if (selectedRows.isEmpty()) {
            showErrorAlert("Delete Error", "No rows selected for deletion");
            return;
        }

        data.removeAll(selectedRows);
    }

    private void clearAllData() {
        // 创建确认对话框
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Clear All Data");
        alert.setHeaderText("Are you sure you want to clear all data?");
        alert.setContentText("This action cannot be undone.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                data.clear(); // 清空所有数据
                nextId.set(1); // 重置 ID 计数器
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

            // 1. 首先尝试自动检测编码
            String detectedEncoding = detectFileEncoding(file);

            // 2. 回退编码列表（优先级排序）
            String[] fallbackEncodings = {"UTF-8", "GBK", "GB18030", "GB2312", "ISO-8859-1"};

            // 3. 尝试所有可能的编码
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

                    // 4. 改进的文件类型检测
                    FileType fileType = detectFileType(reader);

                    // 5. 根据文件类型选择导入方法
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
                            .mapToLong(Transaction::getId)
                            .max()
                            .orElse(0);

                    nextId.set(maxId + 1);
                    saveNextId(); // 立即保存新的 nextId

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
     * 自动检测文件编码
     */
    private String detectFileEncoding(File file) {
        try {
            // 使用 juniversalchardet 库检测编码
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
                // 将检测到的编码转换为 Java 支持的编码名称
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
     * 改进的文件类型检测方法
     */
    private FileType detectFileType(BufferedReader reader) throws IOException {
        reader.mark(8192); // 标记以便稍后重置
        String line;
        int lineCount = 0;
        boolean hasAlipayMarker = false;
        boolean hasWechatMarker = false;

        while ((line = reader.readLine()) != null && lineCount < 10) {
            // Alipay 检测 - 增强特征检测
            if (line.contains("支付宝")) {
                hasAlipayMarker = true;
            }

            // WeChat 检测 - 增强特征检测
            if (line.contains("微信支付账单明细")) {
                hasWechatMarker = true;
            }

            lineCount++;
        }

        reader.reset(); // 重置到标记位置

        if (hasAlipayMarker) {
            return FileType.ALIPAY;
        }
        if (hasWechatMarker) {
            return FileType.WECHAT;
        }
        return FileType.REGULAR;
    }

    // 文件类型枚举
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
                    isFirstLine = false; // 跳过标题行
                    continue;
                }

                String[] rowData = parseCsvLine(line);

                if (rowData.length >= 6) { // 至少需要 6 列
                    try {
                        long id = nextId.getAndIncrement();
                        String user = rowData.length > 0 ? rowData[0].trim() : DashboardView.getCurrentUser().getUsername();
                        String source = rowData.length > 1 ? rowData[1].trim() : "import";
                        LocalDate date = rowData.length > 2 ? LocalDate.parse(rowData[2].trim()) : LocalDate.now();
                        double amount = rowData.length > 3 ? Double.parseDouble(rowData[3].trim()) : 0.0;
                        String category = rowData.length > 4 ? rowData[4].trim() : "Uncategorized";
                        String description = rowData.length > 5 ? rowData[5].trim() : "";

                        Transaction transaction = new Transaction((int)id, user, source, date, amount, category, description);
                        data.add(transaction);
                        recordsImported++;
                    } catch (Exception e) {
                        System.err.println("Failed to parse line: " + line);
                    }
                }
            }

            // 保存 updated nextId
            saveNextId();

            // 通知 Dashboard 导入完成
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

            // 跳过前 17 行
            while (lineCount < 17 && (line = reader.readLine()) != null) {
                lineCount++;
            }

            // 从第 18 行开始处理数据
            while ((line = reader.readLine()) != null) {
                String[] rowData = parseCsvLine(line);

                // 确保足够的列且不是空行
                if (rowData.length >= 6 && !isEmptyRow(rowData)) {
                    String[] fullRowData = new String[7]; // 7 个元素包括 ID

                    // 处理日期 - 从交易时间中提取日期部分
                    String rawDate = rowData[0];
                    String processedDate = processDate(rawDate);

                    // 处理金额 - 移除货币符号
                    String rawAmount = rowData[5];
                    String processedAmount = processAmount(rawAmount);

                    // 分配新的 ID
                    fullRowData[0] = String.valueOf(nextId.getAndIncrement());
                    fullRowData[1] = DashboardView.getCurrentUser().getUsername(); // 用户
                    fullRowData[2] = "wechat"; // 来源
                    fullRowData[3] = processedDate; // 日期（列 1，已处理）
                    fullRowData[4] = processedAmount; // 金额（列 6，已处理）
                    fullRowData[5] = "Uncategorized"; // 类别（硬编码）
                    fullRowData[6] = rowData[1] + rowData[2]; // 描述（列 2 + 列 3 组合）

                    // 创建 Transaction 对象并添加到数据列表
                    data.add(new Transaction(
                            Integer.parseInt(fullRowData[0]),
                            fullRowData[1],
                            fullRowData[2],
                            LocalDate.parse(fullRowData[3]),
                            Double.parseDouble(fullRowData[4]),
                            fullRowData[5],
                            fullRowData[6]
                    ));
                    recordsImported++;
                }
            }

            // 保存 updated nextId
            saveNextId();

            // 通知 Dashboard 导入完成
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
        boolean firstRowSkipped = false; // 标记是否跳过第一行
        int recordsImported = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(lastSelectedFile), encoding))) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] rowData = parseCsvLine(line);

                // 检查条件并且是否是第一行
                if (rowData.length >= 7 && !isEmptyRow(rowData)) {
                    if (!firstRowSkipped) {
                        firstRowSkipped = true; // 标记第一行已跳过
                        continue; // 跳过当前行
                    }

                    String[] fullRowData = new String[7]; // 7 个元素包括 ID
                    String rawDate = rowData[0];
                    String processedDate = processDate(rawDate);

                    // 分配新的 ID
                    fullRowData[0] = String.valueOf(nextId.getAndIncrement());
                    fullRowData[1] = DashboardView.getCurrentUser().getUsername();
                    fullRowData[2] = "alipay";
                    fullRowData[3] = processedDate;
                    fullRowData[4] = rowData[6];
                    fullRowData[5] = "Uncategorized";
                    fullRowData[6] = rowData[1] + rowData[4];

                    // 创建 Transaction 对象并添加到数据列表
                    data.add(new Transaction(
                            Integer.parseInt(fullRowData[0]),
                            fullRowData[1],
                            fullRowData[2],
                            LocalDate.parse(fullRowData[3]),
                            Double.parseDouble(fullRowData[4]),
                            fullRowData[5],
                            fullRowData[6]
                    ));
                    recordsImported++;
                }
            }

            // 保存 updated nextId
            saveNextId();

            // 通知 Dashboard 导入完成
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
                // 写入表头
                writer.write("ID,User,Source,Date,Amount,Category,Description");
                writer.newLine();

                // 写入示例行
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
            // 创建用户特定的文件名
            String username = DashboardView.getCurrentUser().getUsername();
            File file = new File(username + "_transactions.csv");
            boolean fileExists = file.exists();

            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8))) {

                // 如果文件不存在，写入表头
                if (!fileExists) {
                    writer.write("ID,User,Source,Date,Amount,Category,Description");
                    writer.newLine();
                }

                // 写入数据记录
                for (Transaction transaction : data) {
                    writer.write(String.format("%d,%s,%s,%s,%.2f,%s,%s",
                            transaction.getId(),
                            transaction.getUser(),
                            transaction.getSource(),
                            transaction.getDate().format(DateTimeFormatter.ISO_DATE),
                            transaction.getAmount(),
                            transaction.getCategory(),
                            transaction.getDescription()));
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

        // 清空当前数据表，防止重复保存
        data.clear();
    }

    private void loadNextId() {
        File file = new File(DashboardView.getCurrentUser().getUsername() + "_nextId.txt");
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line = reader.readLine();
                if (line != null && !line.isEmpty()) {
                    long loadedId = Long.parseLong(line);
                    if (loadedId > 0) {
                        nextId.set(loadedId);
                    } else {
                        nextId.set(1); // 如果 ID 不合法，设为 1
                        saveNextId();  // 保存更正后的值
                    }
                } else {
                    nextId.set(1); // 如果文件为空，设为 1
                    saveNextId();
                }
            } catch (IOException | NumberFormatException e) {
                System.err.println("无法加载 nextId: " + e.getMessage());
                nextId.set(1); // 出错时使用默认值 1
                saveNextId();
            }
        } else {
            nextId.set(1); // 文件不存在时使用默认值 1
            saveNextId();
        }
    }

    private void saveNextId() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DashboardView.getCurrentUser().getUsername() + "_nextId.txt"))) {
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