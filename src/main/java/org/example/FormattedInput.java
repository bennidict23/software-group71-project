package org.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormattedInput extends Application {

    private TableView<String[]> tableView;
    private final List<String[]> data = new ArrayList<>();
    private File lastSelectedFile;

    // 添加表单控件
    private TextField userField;
    private DatePicker datePicker;
    private TextField amountField;
    private TextField categoryField;
    private TextField descriptionField;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("CSV 导入与记录管理工具");

        // 创建主布局
        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(20));

        // 创建按钮区域
        Button importButton = new Button("导入CSV文件");
        importButton.setOnAction(e -> importCSV(primaryStage));

        Button importAlipayButton = new Button("导入支付宝CSV");
        importAlipayButton.setOnAction(e -> importAlipayCSV(primaryStage));

        Button templateButton = new Button("下载CSV模板");
        templateButton.setOnAction(e -> downloadTemplate(primaryStage));

        HBox buttonBox = new HBox(20, importButton, importAlipayButton, templateButton);

        // 创建表格视图
        tableView = createTableView();

        // 创建添加记录表单
        VBox formBox = createAddRecordForm();

        // 创建状态标签
        Label statusLabel = new Label("准备就绪");

        // 添加组件到布局
        mainLayout.getChildren().addAll(buttonBox, tableView, formBox, statusLabel);

        // 创建场景
        Scene scene = new Scene(mainLayout, 900, 700);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox createAddRecordForm() {
        VBox formBox = new VBox(10);
        formBox.setPadding(new Insets(15));
        formBox.setStyle("-fx-border-color: #cccccc; -fx-border-radius: 5;");

        Label formTitle = new Label("添加新记录");
        formTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

        // 创建表单字段
        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);
        formGrid.setPadding(new Insets(10, 0, 10, 0));

        // 用户
        formGrid.add(new Label("用户:"), 0, 0);
        userField = new TextField();
        formGrid.add(userField, 1, 0);

        // 日期
        formGrid.add(new Label("日期:"), 0, 1);
        datePicker = new DatePicker(LocalDate.now());
        formGrid.add(datePicker, 1, 1);

        // 金额
        formGrid.add(new Label("金额:"), 0, 2);
        amountField = new TextField();
        formGrid.add(amountField, 1, 2);

        // 类别
        formGrid.add(new Label("类别:"), 2, 0);
        categoryField = new TextField();
        formGrid.add(categoryField, 3, 0);

        // 描述
        formGrid.add(new Label("描述:"), 2, 1);
        descriptionField = new TextField();
        formGrid.add(descriptionField, 3, 1);

        // 添加按钮
        Button addButton = new Button("添加记录");
        addButton.setOnAction(e -> addRecord());
        formGrid.add(addButton, 3, 2);

        formBox.getChildren().addAll(formTitle, formGrid);
        return formBox;
    }

    private TableView<String[]> createTableView() {
        TableView<String[]> table = new TableView<>();

        // 创建表格列
        String[] headers = {"User", "Source", "Date", "Amount", "Category", "Description"};
        for (int i = 0; i < headers.length; i++) {
            final int columnIndex = i;
            TableColumn<String[], String> column = new TableColumn<>(headers[i]);
            column.setCellValueFactory(cellData -> {
                String[] row = cellData.getValue();
                if (row != null && columnIndex < row.length) {
                    return javafx.beans.binding.Bindings.createStringBinding(() -> row[columnIndex]);
                } else {
                    return javafx.beans.binding.Bindings.createStringBinding(() -> "");
                }
            });
            column.setPrefWidth(120);
            table.getColumns().add(column);
        }

        return table;
    }

    private void addRecord() {
        try {
            // 验证输入
            if (userField.getText().trim().isEmpty()) {
                showErrorAlert("输入错误", "请输入用户名");
                return;
            }

            if (datePicker.getValue() == null) {
                showErrorAlert("输入错误", "请选择日期");
                return;
            }

            // 验证金额是否为数字
            double amount;
            try {
                amount = Double.parseDouble(amountField.getText().trim());
            } catch (NumberFormatException e) {
                showErrorAlert("输入错误", "金额必须是数字");
                return;
            }

            // 创建记录数组
            String[] record = new String[6];
            record[0] = userField.getText().trim();
            record[1] = "manual";  // 直接输入的记录，Source自动设为manual
            record[2] = datePicker.getValue().format(DateTimeFormatter.ISO_DATE);
            record[3] = amountField.getText().trim();

            // 修复类别字段逻辑
            if(categoryField.getText() == null || categoryField.getText().trim().isEmpty()) {
                record[4] = "未识别";
            } else {
                record[4] = categoryField.getText().trim();
            }

            record[5] = descriptionField.getText().trim();

            // 添加到数据列表
            data.add(record);

            // 更新表格
            updateTableView();

            // 保存数据到transaction.csv
            saveToTransactionCSV();

            // 清空表单
            userField.clear();
            datePicker.setValue(LocalDate.now());
            amountField.clear();
            categoryField.clear();
            descriptionField.clear();

        } catch (Exception e) {
            showErrorAlert("添加记录错误", "无法添加记录: " + e.getMessage());
        }
    }

    private void importCSV(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择CSV文件导入");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV文件", "*.csv")
        );

        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            lastSelectedFile = file;
            boolean isAlipayFile = checkIfAlipayFile(file);

            if (isAlipayFile) {
                importAlipayCSV(file, stage);
                return;
            }

            data.clear();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                String line;
                boolean isFirstLine = true;

                while ((line = reader.readLine()) != null) {
                    // 跳过第一行（如果是标题）
                    if (isFirstLine) {
                        isFirstLine = false;
                        continue;
                    }

                    // 解析CSV行
                    String[] rowData = parseCsvLine(line);

                    // 创建一个新数组，包含Source列
                    String[] fullRowData = new String[6];
                    fullRowData[0] = rowData.length > 0 ? rowData[0] : "";  // User
                    fullRowData[1] = "import";  // Source 改为import以区分导入的数据
                    fullRowData[2] = rowData.length > 1 ? rowData[1] : "";  // Date (原CSV中第2列)
                    fullRowData[3] = rowData.length > 2 ? rowData[2] : "";  // Amount (原CSV中第3列)
                    fullRowData[4] = rowData.length > 3 ? rowData[3] : "";  // Category (原CSV中第4列)
                    fullRowData[5] = rowData.length > 4 ? rowData[4] : "";  // Description (原CSV中第5列)

                    data.add(fullRowData);
                }

                // 更新表格视图
                updateTableView();

                // 保存数据到transaction.csv
                saveToTransactionCSV();

                // 显示导入成功消息
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("导入成功");
                alert.setHeaderText(null);
                alert.setContentText("成功从 " + file.getName() + " 导入了 " + (data.size()) + " 条记录，并已保存到transaction.csv");
                alert.showAndWait();

            } catch (IOException e) {
                showErrorAlert("导入错误", "无法导入CSV文件: " + e.getMessage());
            }
        }
    }

    private boolean checkIfAlipayFile(File file) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            int lineCount = 0;

            while ((line = reader.readLine()) != null && lineCount < 10) {
                lineCount++;
                // 检查是否包含支付宝交易记录的特征文本
                if (line.contains("Ц§ё¶±¦") || line.contains("支付宝") || line.contains("Alipay")) {
                    return true;
                }
            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }

    private void importAlipayCSV(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择支付宝CSV文件导入");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV文件", "*.csv")
        );

        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            importAlipayCSV(file, stage);
        }
    }

    private void importAlipayCSV(File file, Stage stage) {
        if (file != null) {
            lastSelectedFile = file;

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                String line;
                boolean dataStarted = false;
                int recordCount = 0;

                while ((line = reader.readLine()) != null) {
                    // 寻找数据开始的行，通常是在包含交易时间的行之后
                    if (line.contains("Ѕ»ТЧК±јд") || line.contains("交易时间")) {
                        dataStarted = true;
                        continue;
                    }

                    // 如果已经找到数据部分且不是空行
                    if (dataStarted && !line.trim().isEmpty() && !line.startsWith("---")) {
                        // 解析支付宝CSV行
                        String[] rowData = parseCsvLine(line);

                        if (rowData.length >= 8) { // 确保有足够的列
                            // 创建记录数组
                            String[] record = new String[6];

                            // 用户名 (使用当前登录用户)
                            record[0] = extractUserNameFromAlipayFile(file);

                            // Source标记为alipay
                            record[1] = "alipay";

                            // 日期 - 从交易时间中提取日期部分
                            String dateStr = rowData[0];
                            if (dateStr.contains(" ")) {
                                dateStr = dateStr.split(" ")[0]; // 只取日期部分，不要时间
                            }
                            record[2] = dateStr;

                            // 金额 - 添加符号表示收支
                            String amount = rowData[6];
                            if (rowData[5] != null && rowData[5].contains("Ц§іц")) {
                                amount = "-" + amount; // 支出为负
                            }
                            record[3] = amount;

                            // 类别 - 使用交易分类
                            record[4] = rowData[1];

                            // 描述 - 使用商品说明
                            record[5] = rowData[4];

                            // 添加到数据列表
                            data.add(record);
                            recordCount++;
                        }
                    }
                }

                // 更新表格视图
                updateTableView();

                // 保存数据到transaction.csv
                saveToTransactionCSV();

                // 显示导入成功消息
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("导入成功");
                alert.setHeaderText(null);
                alert.setContentText("成功从支付宝导出文件导入了 " + recordCount + " 条记录，并已保存到transaction.csv");
                alert.showAndWait();

            } catch (IOException e) {
                showErrorAlert("导入错误", "无法导入支付宝CSV文件: " + e.getMessage());
            }
        }
    }

    private String extractUserNameFromAlipayFile(File file) {
        // 尝试从文件中提取用户名
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;

            while ((line = reader.readLine()) != null) {
                // 查找包含姓名信息的行
                if (line.contains("РХГыЈє") || line.contains("姓名：")) {
                    // 提取姓名
                    Pattern pattern = Pattern.compile("РХГыЈє(.+)|姓名：(.+)");
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        String name = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
                        if (name != null && !name.trim().isEmpty()) {
                            return name.trim();
                        }
                    }
                }
            }
        } catch (IOException e) {
            // 如果无法读取，使用默认值
        }

        // 默认使用支付宝用户
        return "支付宝用户";
    }

    private void downloadTemplate(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("保存CSV模板");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV文件", "*.csv")
        );
        fileChooser.setInitialFileName("csv_template.csv");

        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                // 写入表头 (不包含Source列)
                writer.write("User,Date,Amount,Category,Description");
                writer.newLine();

                // 写入示例数据
                writer.write("张三,2025-04-13,5000,收入,四月工资");
                writer.newLine();
                writer.write("李四,2025-04-12,-200,支出,超市购物");
                writer.newLine();

                // 显示下载成功消息
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("模板下载成功");
                alert.setHeaderText(null);
                alert.setContentText("CSV模板已成功保存到: " + file.getAbsolutePath() +
                        "\n\n您可以使用Excel或其他电子表格软件编辑此文件，然后导入系统。" +
                        "\n注意：导入时Source列将自动填充为\"import\"。");
                alert.showAndWait();

            } catch (IOException e) {
                showErrorAlert("模板下载错误", "无法保存CSV模板: " + e.getMessage());
            }
        }
    }

    private String[] parseCsvLine(String line) {
        List<String> tokens = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder sb = new StringBuilder();

        for (char c : line.toCharArray()) {
            if (c == ',' && !inQuotes) {
                tokens.add(sb.toString());
                sb = new StringBuilder();
            } else if (c == '"') {
                inQuotes = !inQuotes;
            } else {
                sb.append(c);
            }
        }

        tokens.add(sb.toString());
        return tokens.toArray(new String[0]);
    }

    private void updateTableView() {
        tableView.getItems().clear();
        tableView.getItems().addAll(data);
    }

    private void saveToTransactionCSV() {
        try {
            File file = new File("transactions.csv");
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                // 写入表头
                writer.write("User,Source,Date,Amount,Category,Description");
                writer.newLine();

                // 写入所有数据
                for (String[] row : data) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < row.length; i++) {
                        // 处理包含逗号的字段，使用引号包围
                        String field = row[i];
                        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
                            sb.append("\"").append(field.replace("\"", "\"\"")).append("\"");
                        } else {
                            sb.append(field);
                        }

                        // 除了最后一个字段外，添加逗号分隔符
                        if (i < row.length - 1) {
                            sb.append(",");
                        }
                    }
                    writer.write(sb.toString());
                    writer.newLine();
                }

            }
            System.out.println("数据已成功保存到: " + file.getAbsolutePath());

        } catch (IOException e) {
            showErrorAlert("保存错误", "无法保存到transaction.csv: " + e.getMessage());
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