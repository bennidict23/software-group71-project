package org.example.list;

// TransactionLoader.java
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransactionLoader {
    public List<Transaction> loadTransactions(String filePath) throws IOException {
        List<Transaction> transactions = new ArrayList<>();
        File file = new File(filePath);

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String headerLine = br.readLine(); // Skip header
            System.out.println("Header: " + headerLine);

            // 解析CSV标题获取索引
            String[] headers = headerLine.split(",");
            int idIdx = 0;
            int userIdx = -1, sourceIdx = -1, dateIdx = -1, amountIdx = -1, categoryIdx = -1, descriptionIdx = -1;

            // 查找各列的索引位置
            for (int i = 0; i < headers.length; i++) {
                String header = headers[i].trim().toLowerCase();
                if (header.equals("user"))
                    userIdx = i;
                else if (header.equals("source"))
                    sourceIdx = i;
                else if (header.equals("date"))
                    dateIdx = i;
                else if (header.equals("amount"))
                    amountIdx = i;
                else if (header.equals("category"))
                    categoryIdx = i;
                else if (header.equals("description"))
                    descriptionIdx = i;
            }

            if (dateIdx == -1 || amountIdx == -1) {
                throw new IOException("CSV文件必须包含Date和Amount列");
            }

            System.out.println("Columns: User=" + userIdx + ", Source=" + sourceIdx +
                    ", Date=" + dateIdx + ", Amount=" + amountIdx +
                    ", Category=" + categoryIdx + ", Description=" + descriptionIdx);

            int lineCount = 1;
            String line;
            while ((line = br.readLine()) != null) {
                lineCount++;
                String[] values = line.split(",");

                // 确保有足够的列
                if (values.length <= Math.max(dateIdx, Math.max(amountIdx, Math.max(categoryIdx, descriptionIdx)))) {
                    System.out.println("警告: 第" + lineCount + "行数据列数不足，已跳过");
                    continue;
                }

                try {
                    // 解析金额，正确处理负号
                    double amount;
                    String amountStr = values[amountIdx].trim();

                    // 清理金额字符串：移除货币符号、空格等
                    amountStr = amountStr.replace("¥", "")
                            .replace("￥", "")
                            .replace("$", "")
                            .replace("€", "")
                            .replace("£", "")
                            .replace(" ", "")
                            .trim();

                    // 尝试解析金额
                    try {
                        amount = Double.parseDouble(amountStr);
                    } catch (NumberFormatException e) {
                        System.out.println("警告: 无法解析金额 '" + amountStr + "' 在第" + lineCount + "行，已跳过");
                        continue;
                    }

                    // 创建Transaction对象并添加到列表中
                    Transaction transaction = new Transaction(
                            lineCount, // 使用行号作为ID
                            userIdx >= 0 && userIdx < values.length ? values[userIdx].trim() : "",
                            sourceIdx >= 0 && sourceIdx < values.length ? values[sourceIdx].trim() : "",
                            LocalDate.parse(values[dateIdx].trim()),
                            amount,
                            categoryIdx >= 0 && categoryIdx < values.length ? values[categoryIdx].trim() : "",
                            descriptionIdx >= 0 && descriptionIdx < values.length ? values[descriptionIdx].trim() : "");
                    transactions.add(transaction);
                } catch (Exception e) {
                    System.out.println("警告: 处理第" + lineCount + "行数据出错: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        return transactions;
    }

    /**
     * 从CSV文件删除指定交易记录
     * 
     * @param transaction 要删除的交易对象
     */
    public void deleteTransaction(Transaction transaction) throws IOException {
        Path path = Paths.get("transactions.csv");

        // 读取所有行
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);

        // 筛选出不需要删除的行
        List<String> remainingLines = new ArrayList<>();
        for (String line : lines) {
            // 跳过标题行
            if (line.startsWith("User,Source,Date,Amount,Category,Description") ||
                    line.startsWith("Id,User,Source,Date,Amount,Category,Description")) {
                remainingLines.add(line);
                continue;
            }

            // 解析ID并比较
            String[] parts = line.split(",");
            if (parts.length > 0) {
                try {
                    Integer recordId = Integer.parseInt(parts[0].trim());
                    if (!recordId.equals(transaction.getId())) {
                        remainingLines.add(line);
                    }
                } catch (NumberFormatException e) {
                    // 如果无法解析ID，保留该行
                    remainingLines.add(line);
                }
            }
        }

        // 写回文件
        Files.write(path, remainingLines, StandardCharsets.UTF_8);
    }
}