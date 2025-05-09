package org.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.converter.DefaultStringConverter;
import org.example.utils.DeepSeekCategoryService;
import org.example.utils.LoadingUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class FormattedInput extends Application {

    private TableView<ObservableStringArray> tableView;
    private final ObservableList<ObservableStringArray> data = FXCollections.observableArrayList();
    private File lastSelectedFile;
    private StackPane rootPane; // 添加根StackPane用于显示加载指示器
    private DeepSeekCategoryService categoryService; // 添加分类服务

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

        // 初始化分类服务
        categoryService = new DeepSeekCategoryService();

        // 创建根布局为StackPane，用于叠加加载指示器
        rootPane = new StackPane();

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

        // 将主布局添加到根StackPane
        rootPane.getChildren().add(mainLayout);

        // Create scene
        Scene scene = new Scene(rootPane, 1000, 800);
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

        // 新增：返回 Dashboard 按钮
        Button btnBack = new Button("Dashboard");
        btnBack.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        btnBack.setOnAction(e -> {
            try {
                // 注：DashboardView 是你的主页面
                DashboardView dashboard = new DashboardView();
                dashboard.start(primaryStage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // 把 btnBack 放到 HBox 里
        HBox buttonBox = new HBox(10,
                importButton,
                templateButton,
                saveButton,
                deleteButton,
                btnBack // <- 这里
        );
        buttonBox.setPadding(new Insets(10));
        return buttonBox;
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

        // 新增 User 列，Source 列，以及后续列
        String[] headers = { "User", "Source", "Date", "Amount", "Category", "Description" };
        for (int i = 0; i < headers.length; i++) {
            final int columnIndex = i;
            TableColumn<ObservableStringArray, String> column = new TableColumn<>(headers[i]);
            column.setCellValueFactory(cellData -> cellData.getValue().getProperty(columnIndex));
            // 只有 Source 及后面的列可编辑，也可以选择允许 User 列不可编辑
            if (i >= 1) {
                column.setCellFactory(TextFieldTableCell.forTableColumn(new DefaultStringConverter()));
                column.setOnEditCommit(event -> {
                    ObservableStringArray row = event.getRowValue();
                    row.set(columnIndex, event.getNewValue());
                });
            }
            column.setPrefWidth(120);
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
            // 0: 当前用户名
            record[0] = DashboardView.getCurrentUser().getUsername();
            // 1: 手动添加的标记
            record[1] = "manual";
            // 2: 日期
            record[2] = datePicker.getValue().format(DateTimeFormatter.ISO_DATE);
            // 3: 金额
            record[3] = amountField.getText().trim();
            // 4: 类别
            record[4] = categoryField.getText().trim().isEmpty() ? "Uncategorized" : categoryField.getText().trim();
            // 5: 描述
            record[5] = descriptionField.getText().trim();

            data.add(new ObservableStringArray(record));

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
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            lastSelectedFile = file;

            // 创建后台任务
            Task<Boolean> importTask = new Task<>() {
                @Override
                protected Boolean call() throws Exception {
                    // 微信常见编码优先级更高
                    String[] encodings = { "UTF-8", "GBK", "GB18030", "GB2312", "ISO-8859-1" };
                    boolean fileProcessed = false;
                    IOException lastError = null;

                    for (String encoding : encodings) {
                        try (BufferedReader reader = new BufferedReader(
                                new InputStreamReader(new FileInputStream(file), encoding))) {

                            // 改进的文件类型检测
                            FileType fileType = detectFileType(reader);

                            if (fileType == FileType.ALIPAY) {
                                importAlipayWithEncoding(encoding);
                                fileProcessed = true;
                                break;
                            } else if (fileType == FileType.WECHAT) {
                                importWechatWithEncoding(encoding);
                                fileProcessed = true;
                                break;
                            } else {
                                try {
                                    importRegularCSV(file, encoding);
                                    fileProcessed = true;
                                    break;
                                } catch (IOException e) {
                                    lastError = e;
                                    continue;
                                }
                            }
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
                        final String finalErrorMsg = errorMsg;
                        Platform.runLater(() -> showErrorAlert("Import Error", finalErrorMsg));
                        return false;
                    }
                    return true;
                }
            };

            // 确保在JavaFX应用线程上显示加载指示器
            Platform.runLater(() -> {
                // 使用LoadingUtils显示加载指示器并执行后台任务
                LoadingUtils.showLoadingIndicator(rootPane, importTask, success -> {
                    if (success) {
                        // 导入成功后，通知Dashboard可以显示折线图了
                        DashboardView.setImportDone(true);
                    }
                });
            });
        }
    }

    // 改进的文件类型检测方法
    private FileType detectFileType(BufferedReader reader) throws IOException {
        String line;
        int lineCount = 0;

        // 读取前10行进行检测
        while ((line = reader.readLine()) != null && lineCount < 10) {
            // 支付宝检测
            if (line.contains("支付宝") || line.contains("Alipay")
                    || line.contains("交易号") || line.contains("商户订单号")) {
                return FileType.ALIPAY;
            }

            // 微信检测 - 增强特征检测
            if (line.contains("微信支付") || line.contains("WeChat Pay")
                    || line.contains("交易时间") || line.contains("交易单号")
                    || line.matches(".*微信.*账单.*") || line.contains("微信账单")) {
                return FileType.WECHAT;
            }

            lineCount++;
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
            data.clear();
            String line;

            // 读取表头行
            line = reader.readLine();
            if (line == null) {
                throw new IOException("CSV file is empty");
            }

            // 解析表头，确定每个字段的索引
            String[] headers = parseCsvLine(line);

            // 确定各列的索引位置（默认为-1表示未找到）
            int idIdx = -1;
            int userIdx = -1;
            int sourceIdx = -1;
            int dateIdx = -1;
            int amountIdx = -1;
            int categoryIdx = -1;
            int descriptionIdx = -1;

            // 查找各列的索引位置
            for (int i = 0; i < headers.length; i++) {
                String header = headers[i].trim().toLowerCase();
                if (header.equals("id")) {
                    idIdx = i;
                } else if (header.equals("user") || header.contains("user")) {
                    userIdx = i;
                } else if (header.equals("source")) {
                    sourceIdx = i;
                } else if (header.equals("date")) {
                    dateIdx = i;
                } else if (header.equals("amount")) {
                    amountIdx = i;
                } else if (header.equals("category")) {
                    categoryIdx = i;
                } else if (header.equals("description")) {
                    descriptionIdx = i;
                }
            }

            // 验证必要的列存在
            if (dateIdx == -1 || amountIdx == -1) {
                throw new IOException("CSV file must contain at least Date and Amount columns");
            }

            System.out.println("CSV头部分析结果：");
            System.out.println("ID列索引: " + idIdx);
            System.out.println("User列索引: " + userIdx);
            System.out.println("Source列索引: " + sourceIdx);
            System.out.println("Date列索引: " + dateIdx);
            System.out.println("Amount列索引: " + amountIdx);
            System.out.println("Category列索引: " + categoryIdx);
            System.out.println("Description列索引: " + descriptionIdx);

            // 收集所有行数据，以便之后批量分类
            List<String[]> rowsData = new ArrayList<>();
            List<String> descriptions = new ArrayList<>();
            List<String> amounts = new ArrayList<>();

            // 处理数据行
            int lineCount = 1; // 已经读取了表头
            while ((line = reader.readLine()) != null) {
                lineCount++;
                String[] rowData = parseCsvLine(line);

                // 跳过空行或格式不正确的行
                if (rowData.length <= Math.max(dateIdx, amountIdx)) {
                    System.out.println("警告：第" + lineCount + "行数据列数不足，已跳过");
                    continue;
                }

                String[] record = new String[6];

                // User列: 优先使用文件中的值，如果没有则使用当前用户名
                record[0] = (userIdx >= 0 && userIdx < rowData.length && !rowData[userIdx].isEmpty())
                        ? rowData[userIdx]
                        : DashboardView.getCurrentUser().getUsername();

                // Source列: 优先使用文件中的值，如果没有则使用"import"
                record[1] = (sourceIdx >= 0 && sourceIdx < rowData.length && !rowData[sourceIdx].isEmpty())
                        ? rowData[sourceIdx]
                        : "import";

                // Date列
                record[2] = (dateIdx >= 0 && dateIdx < rowData.length) ? rowData[dateIdx] : "";

                // Amount列
                record[3] = (amountIdx >= 0 && amountIdx < rowData.length) ? rowData[amountIdx] : "";

                // Description列
                record[5] = (descriptionIdx >= 0 && descriptionIdx < rowData.length) ? rowData[descriptionIdx] : "";

                // Category列
                // 如果CSV中有分类，就使用它，否则先用Uncategorized，后面会批量分类
                record[4] = (categoryIdx >= 0 && categoryIdx < rowData.length && !rowData[categoryIdx].isEmpty())
                        ? rowData[categoryIdx]
                        : "Uncategorized";

                // 收集数据用于后续批量分类
                rowsData.add(record);
                descriptions.add(record[5]); // 收集描述
                amounts.add(record[3]); // 收集金额
            }

            // 如果没有分类列，则使用DeepSeek批量分类
            if (categoryIdx == -1 && !rowsData.isEmpty()) {
                System.out.println("CSV文件没有Category列，将使用DeepSeek进行自动分类...");

                // 使用DeepSeekCategoryService进行分类（先计算好结果）
                final DeepSeekCategoryService tempService = new DeepSeekCategoryService();
                CompletableFuture<List<String>> categoriesFuture = tempService.classifyTransactionsAsync(descriptions,
                        amounts);

                // 在JavaFX应用线程上创建和显示任务
                Platform.runLater(() -> {
                    // 创建一个新的任务，在JavaFX线程中处理异步结果
                    Task<List<String>> classifyTask = new Task<>() {
                        @Override
                        protected List<String> call() throws Exception {
                            try {
                                // 等待CompletableFuture完成（这里等待已经在进行的分类任务）
                                return categoriesFuture.get();
                            } finally {
                                // 确保分类服务关闭
                                tempService.shutdown();
                            }
                        }
                    };

                    // 显示加载指示器并执行分类任务
                    LoadingUtils.showLoadingIndicator(rootPane, classifyTask, categories -> {
                        if (categories != null) {
                            // 更新每行的分类结果
                            for (int i = 0; i < Math.min(rowsData.size(), categories.size()); i++) {
                                rowsData.get(i)[4] = categories.get(i);
                            }

                            // 将所有数据添加到表格中
                            for (String[] record : rowsData) {
                                data.add(new ObservableStringArray(record));
                            }

                            // 显示成功信息
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Import Successful");
                            alert.setHeaderText(null);
                            alert.setContentText("Successfully imported and classified " + data.size() +
                                    " records (encoding: " + encoding + ")");
                            alert.showAndWait();

                            // 保存带分类的CSV文件
                            saveCategorizedCSV();

                            // 通知Dashboard可以显示折线图了
                            DashboardView.setImportDone(true);
                        }
                    });

                    // 启动任务（在执行LoadingUtils.showLoadingIndicator后）
                    Thread thread = new Thread(classifyTask);
                    thread.setDaemon(true);
                    thread.start();
                });
            } else {
                // 如果有分类列或没有数据，直接添加到表格
                for (String[] record : rowsData) {
                    data.add(new ObservableStringArray(record));
                }

                // 导入成功后，通知Dashboard可以显示折线图了
                DashboardView.setImportDone(true);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Import Successful");
                alert.setHeaderText(null);
                alert.setContentText("Successfully imported " + data.size() + " records (encoding: " + encoding + ")");
                alert.showAndWait();
            }
        }
    }

    // 新增：保存带分类的CSV文件
    private void saveCategorizedCSV() {
        try {
            File file = new File("transactions_categorized.csv");

            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {

                // 写入表头
                writer.write("User,Source,Date,Amount,Category,Description");
                writer.newLine();

                // 写入数据
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

                System.out.println("已将分类后的CSV保存到: " + file.getAbsolutePath());
            }
        } catch (IOException e) {
            e.printStackTrace();
            Platform.runLater(() -> showErrorAlert("Save Error", "Failed to save categorized CSV: " + e.getMessage()));
        }
    }

    private void importWechatWithEncoding(String encoding) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(lastSelectedFile), encoding))) {
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

                    fullRowData[0] = "Default User"; // User (hardcoded)
                    fullRowData[1] = "wechat"; // Source
                    fullRowData[2] = processedDate; // Date (column 1, processed)
                    fullRowData[3] = processedAmount; // Amount (column 6, processed)
                    fullRowData[4] = "Uncategorized"; // Category (hardcoded)
                    fullRowData[5] = rowData[1] + rowData[2]; // Description (columns 2+3 combined)

                    data.add(new ObservableStringArray(fullRowData));
                }
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Import Successful");
            alert.setHeaderText(null);
            alert.setContentText(
                    "Successfully imported " + data.size() + " records from WeChat (encoding: " + encoding + ")");
            alert.showAndWait();

        } catch (IOException e) {
            showErrorAlert("Import Error",
                    "Failed to import WeChat CSV with encoding " + encoding + ": " + e.getMessage());
        }
    }

    private void importAlipayWithEncoding(String encoding) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(lastSelectedFile), encoding))) {
            data.clear();
            String line;

            // 可选：只跳过第一行表头
            reader.readLine();

            // 从文件第二行开始处理所有行
            while ((line = reader.readLine()) != null) {
                String[] rowData = parseCsvLine(line);
                if (rowData.length >= 7 && !isEmptyRow(rowData)) {
                    String[] fullRowData = new String[6];

                    // 解析并填充
                    String rawDate = rowData[0];
                    String processedDate = processDate(rawDate);

                    fullRowData[0] = DashboardView.getCurrentUser().getUsername(); // 真实用户名
                    fullRowData[1] = "alipay";
                    fullRowData[2] = processedDate;
                    fullRowData[3] = rowData[6];
                    fullRowData[4] = "Uncategorized";
                    fullRowData[5] = rowData[1] + rowData[4];

                    data.add(new ObservableStringArray(fullRowData));
                }
            }

            // 通知 Dashboard 显示图表（如果你用了那种逻辑）
            DashboardView.setImportDone(true);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Import Successful");
            alert.setHeaderText(null);
            alert.setContentText("Successfully imported "
                    + data.size() + " records from Alipay (encoding: " + encoding + ")");
            alert.showAndWait();

        } catch (IOException e) {
            showErrorAlert("Import Error",
                    "Failed to import Alipay CSV with encoding " + encoding + ": " + e.getMessage());
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
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName("transactions.csv");

        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                // 写入与表格一致的表头：User,Source,Date,Amount,Category,Description
                writer.write("User,Source,Date,Amount,Category,Description");
                writer.newLine();

                // 写入示例行：当前用户名,来源,日期,金额,类别,描述
                String currentUser = DashboardView.getCurrentUser().getUsername();
                writer.write(String.format("%s,manual,2025-04-13,5000,Uncategorized,Salary", currentUser));
                writer.newLine();
                writer.write(String.format("%s,manual,2025-04-14,-200,Uncategorized,Grocery Shopping", currentUser));
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
        // 创建后台任务
        Task<Boolean> saveTask = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
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

                        return true;
                    } catch (IOException e) {
                        Platform.runLater(() -> showErrorAlert("Save Error", "Failed to save data: " + e.getMessage()));
                        return false;
                    }
                } catch (Exception e) {
                    Platform.runLater(() -> showErrorAlert("Save Error", "Failed to save data: " + e.getMessage()));
                    return false;
                }
            }
        };

        // 确保在JavaFX应用线程上显示加载指示器
        Platform.runLater(() -> {
            // 使用LoadingUtils显示加载指示器并执行后台任务
            LoadingUtils.showLoadingIndicator(rootPane, saveTask, success -> {
                if (success) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Save Successful");
                        alert.setHeaderText(null);
                        alert.setContentText("Data successfully saved to: transactions.csv");
                        alert.showAndWait();
                    });
                }
            });
        });
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void stop() {
        // 确保在应用关闭时关闭分类服务
        if (categoryService != null) {
            categoryService.shutdown();
        }
    }
}