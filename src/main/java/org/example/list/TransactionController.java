package org.example.list;

import java.io.*;
import java.nio.charset.StandardCharsets;
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

        // 绑定保存更改按钮
        view.getSaveChangesButton().setOnAction(e -> saveChanges());

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
            data.addAll(loader.loadTransactions("transactions.csv",view.getCurrentUsername()));
            view.updateTable(data);
        } catch (IOException ex) {
            showError("File Load Error", ex.getMessage());
        } catch (Exception ex) {
            showError("Data Format Error", ex.getMessage());
        }
    }

    /**
     * 保存对交易数据的所有更改到CSV文件
     */
    private void saveChanges() {
        try {
            saveTransactionsToCSV(data, "transactions.csv");
            showSuccess("Changes Saved", "All changes have been successfully saved to the transactions file.");
        } catch (IOException ex) {
            showError("Save Error", "Failed to save changes: " + ex.getMessage());
        }
    }

    /**
     * 将交易数据保存到CSV文件
     * 
     * @param transactions 要保存的交易数据
     * @param filePath     文件路径
     * @throws IOException 如果保存失败
     */
    private void saveTransactionsToCSV(ObservableList<Transaction> transactions, String filePath) throws IOException {
        File file = new File(filePath);

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {

            // 写入标题行
            writer.write("User,Source,Date,Amount,Category,Description");
            writer.newLine();

            // 写入每条交易记录
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            for (Transaction transaction : transactions) {
                StringBuilder line = new StringBuilder();

                // 添加用户
                appendField(line, transaction.getUser(), true);

                // 添加来源
                appendField(line, transaction.getSource(), true);

                // 添加日期
                appendField(line, transaction.getDate().format(dateFormatter), true);

                // 添加金额
                appendField(line, String.valueOf(transaction.getAmount()), true);

                // 添加分类
                appendField(line, transaction.getCategory(), true);

                // 添加描述（最后一个字段不带逗号）
                appendField(line, transaction.getDescription(), false);

                // 写入行
                writer.write(line.toString());
                writer.newLine();
            }
        }
    }

    /**
     * 向CSV行添加一个字段
     * 
     * @param builder  StringBuilder
     * @param value    字段值
     * @param addComma 是否在末尾添加逗号
     */
    private void appendField(StringBuilder builder, String value, boolean addComma) {
        if (value == null) {
            value = "";
        }

        // 如果值包含逗号、引号或换行符，需要用引号括起来
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            // 将值中的引号替换为两个引号
            value = value.replace("\"", "\"\"");
            builder.append("\"").append(value).append("\"");
        } else {
            builder.append(value);
        }

        if (addComma) {
            builder.append(",");
        }
    }

    // Search
    // start
    private void updateSearchFilter(String filter) {
        filteredData.setPredicate(transaction -> {
            if (filter.isEmpty())
                return true;

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

    // end
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}