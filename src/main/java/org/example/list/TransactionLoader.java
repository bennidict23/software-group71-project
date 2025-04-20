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
            br.readLine(); // Skip header
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length >= 6) {
                    Transaction transaction = new Transaction(
                        Integer.parseInt(values[0].trim()),
                        values[1].trim(),
                        values[2].trim(),
                        LocalDate.parse(values[3].trim()),
                        Double.parseDouble(values[4].trim()),
                        values[5].trim(),
                        values[6].trim()
                    );
                    transactions.add(transaction);
                }
            }
        }
        return transactions;
    }
    /**
     * 从CSV文件删除指定交易记录
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
            if (line.startsWith("Id,User,Source,Date,Amount,Category,Description")) continue;
            
            // 解析ID并比较
            String[] parts = line.split(",");
            Integer recordId = Integer.parseInt(parts[0].trim());
            if (!recordId.equals(transaction.getId())) {
                remainingLines.add(line);
            }
        }

        // 写回文件
        Files.write(path, remainingLines, StandardCharsets.UTF_8);
    }
}