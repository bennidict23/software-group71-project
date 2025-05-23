package org.example.dataImport;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.list.Transaction;
import org.example.utils.DeepSeekCategoryService;
import org.mozilla.universalchardet.UniversalDetector;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class DataImportModel {

    private final ObservableList<Transaction> transactions = FXCollections.observableArrayList();
    private AtomicLong nextId = new AtomicLong(1);
    private String currentUser;

    public DataImportModel(String username) {
        this.currentUser = username;
        loadNextId();
    }

    // Getters
    public ObservableList<Transaction> getTransactions() {
        return transactions;
    }

    public long getNextId() {
        return nextId.getAndIncrement();
    }

    // Transaction operations
    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
    }

    public void removeTransactions(List<Transaction> transactionsToRemove) {
        transactions.removeAll(transactionsToRemove);
    }

    public void clearAllTransactions() {
        transactions.clear();
        nextId.set(1);
    }

    // File operations
    public ImportResult importCSV(File file) {
        String detectedEncoding = detectFileEncoding(file);
        String[] fallbackEncodings = { "UTF-8", "GBK", "GB18030", "GB2312", "ISO-8859-1" };

        List<String> encodingsToTry = new ArrayList<>();
        if (detectedEncoding != null) {
            encodingsToTry.add(detectedEncoding);
        }
        encodingsToTry.addAll(Arrays.asList(fallbackEncodings));

        for (String encoding : encodingsToTry) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), encoding))) {

                FileType fileType = detectFileType(reader);
                int recordsImported = 0;

                switch (fileType) {
                    case ALIPAY:
                        recordsImported = importAlipay(file, encoding);
                        break;
                    case WECHAT:
                        recordsImported = importWechat(file, encoding);
                        break;
                    case REGULAR:
                        recordsImported = importRegular(file, encoding);
                        break;
                }

                updateMaxId();
                saveNextId();
                return new ImportResult(true, recordsImported, encoding, null);

            } catch (IOException e) {
                continue;
            }
        }

        return new ImportResult(false, 0, null, "Failed to import with any encoding");
    }

    private int importRegular(File file, String encoding) throws IOException {
        int recordsImported = 0;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), encoding))) {

            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                String[] rowData = parseCsvLine(line);
                if (rowData.length >= 6) {
                    try {
                        Transaction transaction = createRegularTransaction(rowData);
                        transactions.add(transaction);
                        recordsImported++;
                    } catch (Exception e) {
                        System.err.println("Failed to parse line: " + line);
                    }
                }
            }
        }
        return recordsImported;
    }

    private int importWechat(File file, String encoding) throws IOException {
        int recordsImported = 0;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), encoding))) {

            // Skip first 17 lines
            for (int i = 0; i < 17; i++) {
                if (reader.readLine() == null)
                    break;
            }

            String line;
            while ((line = reader.readLine()) != null) {
                String[] rowData = parseCsvLine(line);
                if (rowData.length >= 6 && !isEmptyRow(rowData)) {
                    Transaction transaction = createWechatTransaction(rowData);
                    transactions.add(transaction);
                    recordsImported++;
                }
            }
        }
        return recordsImported;
    }

    private int importAlipay(File file, String encoding) throws IOException {
        int recordsImported = 0;
        boolean firstRowSkipped = false;

        System.out.println("开始导入支付宝数据...");
        System.out.println("文件路径: " + file.getAbsolutePath());
        System.out.println("使用编码: " + encoding);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), encoding))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] rowData = parseCsvLine(line);
                if (rowData.length >= 7 && !isEmptyRow(rowData)) {
                    if (!firstRowSkipped) {
                        firstRowSkipped = true;
                        continue;
                    }

                    Transaction transaction = createAlipayTransaction(rowData);
                    transactions.add(transaction);
                    recordsImported++;
                    System.out.println("已导入交易: " + transaction);
                }
            }
        }

        System.out.println("导入完成，共导入 " + recordsImported + " 条记录");

        // 先保存所有导入的数据到文件
        if (recordsImported > 0) {
            System.out.println("保存导入的数据到文件...");
            saveToCSV();

            // 然后对未分类的交易进行AI分类
            System.out.println("开始AI分类...");
            categorizeUncategorizedTransactions();
        }

        reloadTransactionsFromFile();
        return recordsImported;
    }

    private void categorizeUncategorizedTransactions() {
        // 从文件中读取所有交易记录
        List<Transaction> allTransactions = new ArrayList<>();
        File file = new File(currentUser + "_transactions.csv");

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                String[] data = line.split(",");
                if (data.length >= 7) {
                    Transaction transaction = new Transaction(
                            Integer.parseInt(data[0]),
                            data[1],
                            data[2],
                            LocalDate.parse(data[3]),
                            Double.parseDouble(data[4]),
                            data[5],
                            data[6]);
                    allTransactions.add(transaction);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // 找出所有未分类的交易
        List<Transaction> uncategorizedTransactions = allTransactions.stream()
                .filter(t -> "Uncategorized".equals(t.getCategory()))
                .collect(Collectors.toList());

        if (!uncategorizedTransactions.isEmpty()) {
            try {
                System.out.println("开始对 " + uncategorizedTransactions.size() + " 条未分类交易进行AI分类");
                // 调用AI分类服务
                List<String> categories = getCategoriesFromAI(uncategorizedTransactions);

                // 更新交易类别
                for (int i = 0; i < uncategorizedTransactions.size(); i++) {
                    uncategorizedTransactions.get(i).setCategory(categories.get(i));
                }

                System.out.println("AI分类完成，准备保存更新后的交易记录");

                // 保存所有交易记录（包括更新后的类别）
                try (BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {

                    // 写入标题行
                    writer.write("id,username,source,date,amount,category,description");
                    writer.newLine();

                    // 写入所有交易记录
                    for (Transaction transaction : allTransactions) {
                        String line = String.format("%d,%s,%s,%s,%.2f,%s,%s",
                                transaction.getId(),
                                transaction.getUser(),
                                transaction.getSource(),
                                transaction.getDate().format(DateTimeFormatter.ISO_DATE),
                                transaction.getAmount(),
                                transaction.getCategory(),
                                transaction.getDescription());
                        writer.write(line);
                        writer.newLine();
                    }
                }
                System.out.println("文件更新完成");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("没有需要分类的交易记录");
        }

        reloadTransactionsFromFile();
    }

    private List<String> getCategoriesFromAI(List<Transaction> transactions) throws Exception {
        // 预定义的类别列表
        List<String> predefinedCategories = Arrays.asList(
                "Food & Dining",
                "Shopping",
                "Transportation",
                "Entertainment",
                "Bills & Utilities",
                "Health & Medical",
                "Travel",
                "Education",
                "Personal Care",
                "Gifts & Donations",
                "Investments",
                "Income",
                "Other");

        // 准备发送给AI的数据
        List<Map<String, String>> transactionData = transactions.stream()
                .map(t -> {
                    Map<String, String> data = new HashMap<>();
                    data.put("date", t.getDate().toString());
                    data.put("amount", String.valueOf(t.getAmount()));
                    data.put("description", t.getDescription());
                    return data;
                })
                .collect(Collectors.toList());

        // TODO: 调用DeepSeek API
        // 这里需要实现实际的API调用
        // 返回分类结果列表
        return callDeepSeekAPI(transactionData, predefinedCategories);
    }

    private List<String> callDeepSeekAPI(List<Map<String, String>> transactions, List<String> categories)
            throws Exception {
        DeepSeekCategoryService categoryService = new DeepSeekCategoryService();

        // 准备描述和金额列表
        List<String> descriptions = transactions.stream()
                .map(t -> t.get("description"))
                .collect(Collectors.toList());

        List<String> amounts = transactions.stream()
                .map(t -> t.get("amount"))
                .collect(Collectors.toList());

        // 使用现有的服务进行批量分类
        List<String> results = categoryService.classifyTransactionsAsync(descriptions, amounts).get();

        // 关闭服务
        categoryService.shutdown();

        return results;
    }

    private Transaction createRegularTransaction(String[] rowData) {
        long id = nextId.getAndIncrement();
        String user = rowData.length > 0 ? rowData[0].trim() : currentUser;
        String source = rowData.length > 1 ? rowData[1].trim() : "import";
        LocalDate date = rowData.length > 2 ? LocalDate.parse(rowData[2].trim()) : LocalDate.now();
        double amount = rowData.length > 3 ? Double.parseDouble(rowData[3].trim()) : 0.0;
        String category = rowData.length > 4 ? rowData[4].trim() : "Uncategorized";
        String description = rowData.length > 5 ? rowData[5].trim() : "";

        return new Transaction((int) id, user, source, date, amount, category, description);
    }

    private Transaction createWechatTransaction(String[] rowData) {
        String processedDate = processDate(rowData[0]);
        String processedAmount = processAmount(rowData[5]);

        return new Transaction(
                (int) nextId.getAndIncrement(),
                currentUser,
                "wechat",
                LocalDate.parse(processedDate),
                Double.parseDouble(processedAmount),
                "Uncategorized",
                rowData[1] + rowData[2]);
    }

    private Transaction createAlipayTransaction(String[] rowData) {
        String processedDate = processDate(rowData[0]);

        return new Transaction(
                (int) nextId.getAndIncrement(),
                currentUser,
                "alipay",
                LocalDate.parse(processedDate),
                Double.parseDouble(rowData[6]),
                "Uncategorized",
                rowData[1] + rowData[4]);
    }

    public void saveToCSV() throws IOException {
        if (transactions.isEmpty()) {
            System.out.println("没有交易记录需要保存");
            return; // 如果没有交易记录，不创建文件
        }

        File file = new File(currentUser + "_transactions.csv");
        boolean fileExists = file.exists();
        System.out.println("保存交易记录到文件: " + file.getAbsolutePath());
        System.out.println("文件是否存在: " + fileExists);
        System.out.println("待保存的交易记录数: " + transactions.size());

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8))) {

            // 只在文件不存在时写入标题行
            if (!fileExists) {
                writer.write("id,username,source,date,amount,category,description");
                writer.newLine();
                System.out.println("写入标题行");
            }

            for (Transaction transaction : transactions) {
                String line = String.format("%d,%s,%s,%s,%.2f,%s,%s",
                        transaction.getId(),
                        transaction.getUser(),
                        transaction.getSource(),
                        transaction.getDate().format(DateTimeFormatter.ISO_DATE),
                        transaction.getAmount(),
                        transaction.getCategory(),
                        transaction.getDescription());
                writer.write(line);
                writer.newLine();
                System.out.println("写入交易记录: " + line);
            }
        }

        System.out.println("保存完成");
        // 只有在成功保存交易记录后才保存nextId
        saveNextId();
        transactions.clear();
    }

    public void downloadTemplate(File file) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {

            writer.write("ID,User,Source,Date,Amount,Category,Description");
            writer.newLine();
            writer.write(String.format("1,%s,manual,2025-04-13,5000,Uncategorized,Salary", currentUser));
            writer.newLine();
            writer.write(String.format("2,%s,manual,2025-04-14,-200,Uncategorized,Grocery Shopping", currentUser));
            writer.newLine();
        }
    }

    // Utility methods
    private String detectFileEncoding(File file) {
        try {
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

    private FileType detectFileType(BufferedReader reader) throws IOException {
        reader.mark(8192);
        String line;
        int lineCount = 0;
        boolean hasAlipayMarker = false;
        boolean hasWechatMarker = false;

        while ((line = reader.readLine()) != null && lineCount < 10) {
            if (line.contains("支付宝")) {
                hasAlipayMarker = true;
            }
            if (line.contains("微信支付账单明细")) {
                hasWechatMarker = true;
            }
            lineCount++;
        }

        reader.reset();

        if (hasAlipayMarker)
            return FileType.ALIPAY;
        if (hasWechatMarker)
            return FileType.WECHAT;
        return FileType.REGULAR;
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

    private void updateMaxId() {
        long maxId = transactions.stream()
                .mapToLong(Transaction::getId)
                .max()
                .orElse(0);
        nextId.set(maxId + 1);
    }

    private void loadNextId() {
        File file = new File(currentUser + "_nextId.txt");
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line = reader.readLine();
                if (line != null && !line.isEmpty()) {
                    long loadedId = Long.parseLong(line);
                    nextId.set(loadedId > 0 ? loadedId : 1);
                } else {
                    nextId.set(1);
                }
            } catch (IOException | NumberFormatException e) {
                nextId.set(1);
            }
        } else {
            nextId.set(1);
        }
    }

    private void saveNextId() {
        File file = new File(currentUser + "_nextId.txt");
        if (!file.exists()) {
            return; // 如果文件不存在，不创建新文件
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(String.valueOf(nextId.get()));
        } catch (IOException e) {
            System.err.println("Cannot save nextId: " + e.getMessage());
        }
    }

    public void reloadTransactionsFromFile() {
        transactions.clear();
        File file = new File(currentUser + "_transactions.csv");
        if (!file.exists())
            return;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean isFirstLine = true;
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                String[] data = line.split(",");
                if (data.length >= 7) {
                    Transaction transaction = new Transaction(
                            Integer.parseInt(data[0]),
                            data[1],
                            data[2],
                            LocalDate.parse(data[3]),
                            Double.parseDouble(data[4]),
                            data[5],
                            data[6]);
                    transactions.add(transaction);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Inner classes
    public enum FileType {
        ALIPAY, WECHAT, REGULAR
    }

    public static class ImportResult {
        private final boolean success;
        private final int recordsImported;
        private final String encoding;
        private final String errorMessage;

        public ImportResult(boolean success, int recordsImported, String encoding, String errorMessage) {
            this.success = success;
            this.recordsImported = recordsImported;
            this.encoding = encoding;
            this.errorMessage = errorMessage;
        }

        public boolean isSuccess() {
            return success;
        }

        public int getRecordsImported() {
            return recordsImported;
        }

        public String getEncoding() {
            return encoding;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}