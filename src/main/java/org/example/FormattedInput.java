package org.example;

import javafx.application.Application;
import javafx.geometry.HPos;
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
import java.util.Arrays;
import java.util.List;

public class FormattedInput extends Application {

    private TableView<String[]> tableView;
    private final List<String[]> data = new ArrayList<>();
    private File lastSelectedFile;

    // 表单控件
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

        Button templateButton = new Button("下载CSV模板");
        templateButton.setOnAction(e -> downloadTemplate(primaryStage));

        HBox buttonBox = new HBox(20, importButton, templateButton);

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
        formGrid.setHgap(15);
        formGrid.setVgap(15);
        formGrid.setPadding(new Insets(15, 0, 15, 0));

        // 第一行：日期和金额
        formGrid.add(new Label("日期:"), 0, 0);
        datePicker = new DatePicker(LocalDate.now());
        formGrid.add(datePicker, 1, 0);

        formGrid.add(new Label("金额:"), 2, 0);
        amountField = new TextField();
        formGrid.add(amountField, 3, 0);

        // 第二行：类别
        formGrid.add(new Label("类别:"), 0, 1);
        categoryField = new TextField();
        GridPane.setColumnSpan(categoryField, 3);
        formGrid.add(categoryField, 1, 1);

        // 第三行：描述
        formGrid.add(new Label("描述:"), 0, 2);
        descriptionField = new TextField();
        GridPane.setColumnSpan(descriptionField, 3);
        formGrid.add(descriptionField, 1, 2);

        // 第四行：添加按钮（居右）
        Button addButton = new Button("添加记录");
        addButton.setOnAction(e -> addRecord());
        GridPane.setColumnSpan(addButton, 2);
        GridPane.setHalignment(addButton, HPos.RIGHT);
        formGrid.add(addButton, 2, 3);

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
            if (datePicker.getValue() == null) {
                showErrorAlert("输入错误", "请选择日期");
                return;
            }

            // 验证金额是否为数字
            try {
                Double.parseDouble(amountField.getText().trim());
            } catch (NumberFormatException e) {
                showErrorAlert("输入错误", "金额必须是数字");
                return;
            }

            // 创建记录数组
            String[] record = new String[6];
            record[0] = "";  // 用户字段留空
            record[1] = "manual";
            record[2] = datePicker.getValue().format(DateTimeFormatter.ISO_DATE);
            record[3] = amountField.getText().trim();
            record[4] = categoryField.getText().trim().isEmpty() ? "未识别" : categoryField.getText().trim();
            record[5] = descriptionField.getText().trim();

            // 添加到数据列表
            data.add(record);

            // 更新表格
            updateTableView();

            // 保存数据
            saveToTransactionCSV();

            // 清空表单
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

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                String line;
                boolean isFirstLine = true;
                boolean isAlipayFile = false;
                boolean isWechatFile = false;

                // 读取前几行，检测文件类型
                int lineCount = 0;
                while ((line = reader.readLine()) != null && lineCount < 5) {
                    if (line.contains("支付宝账户") ) {
                        isAlipayFile = true;
                        break;
                    }
                    if (line.contains("微信支付账单明细")) {
                        isWechatFile = true;
                        break;
                    }
                    lineCount++;
                }

                // 根据检测到的文件类型调用相应的导入方法
                if (isAlipayFile) {
                    importAlipay();
                    return;
                } else if (isWechatFile) {
                   // importWechat();
                    return;
                }

                // 如果不是特殊格式，按常规CSV处理
                data.clear();
                reader.close();

                // 重新打开文件从头开始读取
                try (BufferedReader reReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                    isFirstLine = true;
                    while ((line = reReader.readLine()) != null) {
                        // 跳过第一行（标题行）
                        if (isFirstLine) {
                            isFirstLine = false;
                            continue;
                        }

                        // 解析CSV行
                        String[] rowData = parseCsvLine(line);

                        // 创建完整记录
                        String[] fullRowData = new String[6];
                        fullRowData[0] = "";  // 用户
                        fullRowData[1] = "import";  // 来源
                        fullRowData[2] = rowData.length > 0 ? rowData[0] : "";  // 日期
                        fullRowData[3] = rowData.length > 1 ? rowData[1] : "";  // 金额
                        fullRowData[4] = rowData.length > 2 ? rowData[2] : "";  // 类别
                        fullRowData[5] = rowData.length > 3 ? rowData[3] : "";  // 描述

                        data.add(fullRowData);
                        System.out.println("导入记录: " + Arrays.toString(fullRowData));
                    }

                    // 更新表格视图
                    updateTableView();

                    // 保存数据
                    saveToTransactionCSV();

                    // 显示导入成功消息
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("导入成功");
                    alert.setHeaderText(null);
                    alert.setContentText("成功从 " + file.getName() + " 导入了 " + data.size() + " 条记录");
                    alert.showAndWait();
                }
            } catch (IOException e) {
                showErrorAlert("导入错误", "无法导入CSV文件: " + e.getMessage());
            }
        }
    }
    private void importAlipay() {
        data.clear();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(lastSelectedFile), StandardCharsets.UTF_8))) {
            String line;
            int lineCount = 0;

            // 跳过前25行
            while (lineCount < 25 && (line = reader.readLine()) != null) {
                lineCount++;
            }

            // 从第26行开始处理数据
            while ((line = reader.readLine()) != null) {
                String[] rowData = parseCsvLine(line);

                // 确保有足够的列数
                if (rowData.length >= 7) {
                    // 创建完整记录
                    String[] fullRowData = new String[6];

                    // 处理日期 - 将第1列的交易时间精确到日
                    String rawDate = rowData[0];
                    String processedDate = processDate(rawDate);

                    fullRowData[0] = "默认用户";              // 用户（写死）
                    fullRowData[1] = "alipay";              // 来源
                    fullRowData[2] = processedDate;         // 日期（第1列，处理后）
                    fullRowData[3] = rowData[6];            // 金额（第7列）
                    fullRowData[4] = "未分类";               // 类别（写死为"未分类"）
                    fullRowData[5] = rowData[4];            // 描述（第5列商品说明）

                    data.add(fullRowData);
                    System.out.println("导入支付宝记录: " + Arrays.toString(fullRowData));
                }
            }

            // 更新表格视图
            updateTableView();

            // 保存数据
            saveToTransactionCSV();

            // 显示导入成功消息
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("导入成功");
            alert.setHeaderText(null);
            alert.setContentText("成功从支付宝账单导入了 " + data.size() + " 条记录");
            alert.showAndWait();

        } catch (IOException e) {
            showErrorAlert("导入错误", "无法导入支付宝CSV文件: " + e.getMessage());
        }
    }

    // 处理日期格式，将支付宝交易时间精确到日
    private String processDate(String rawDate) {
        // 支付宝的交易时间格式可能是"2023-10-15 14:30:25"
        // 我们需要将其精确到日，即"2023-10-15"
        if (rawDate != null && rawDate.length() >= 10) {
            return rawDate.substring(0, 10);
        }
        return rawDate;
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
                // 写入表头
                writer.write("Date,Amount,Category,Description");
                writer.newLine();

                // 写入示例数据
                writer.write("2025-04-13,5000,,工资");
                writer.newLine();
                writer.write("2025-04-14,-200,,超市购物");
                writer.newLine();

                // 显示下载成功消息
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("模板下载成功");
                alert.setHeaderText(null);
                alert.setContentText("CSV模板已成功保存到: " + file.getAbsolutePath());
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

    private void updateTableView() {
        tableView.getItems().clear();
        tableView.getItems().addAll(data);
    }

    private void saveToTransactionCSV() {
        try {
            // 打印数据内容到控制台
            System.out.println("\n=== 即将保存的数据 ===");
            System.out.println("记录总数: " + data.size());
            for (int i = 0; i < data.size(); i++) {
                String[] record = data.get(i);
                System.out.printf("%d: [来源: %s, 日期: %s, 金额: %s, 类别: %s, 描述: %s]%n",
                        i + 1,
                        record[1],
                        record[2],
                        record[3],
                        record[4],
                        record[5]);
            }
            System.out.println("===================\n");

            File file = new File("transactions.csv");

            // 检查文件是否存在，如果不存在则写入表头
            boolean fileExists = file.exists();

            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(file, true), StandardCharsets.UTF_8))) {

                // 如果文件不存在，写入表头
                if (!fileExists) {
                    writer.write("User,Source,Date,Amount,Category,Description");
                    writer.newLine();
                }

                // 只写入新增的数据（从data列表的最后一个开始）
                // 这里假设每次保存都是新增数据，如果要实现增量保存，需要更复杂的逻辑
                for (String[] row : data) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < row.length; i++) {
                        String field = row[i];
                        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
                            sb.append("\"").append(field.replace("\"", "\"\"")).append("\"");
                        } else {
                            sb.append(field);
                        }

                        if (i < row.length - 1) {
                            sb.append(",");
                        }
                    }
                    writer.write(sb.toString());
                    writer.newLine();
                }
            }
            System.out.println("数据已成功追加到: " + file.getAbsolutePath());

        } catch (IOException e) {
            showErrorAlert("保存错误", "无法保存数据: " + e.getMessage());
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