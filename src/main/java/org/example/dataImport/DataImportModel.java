package org.example.dataImport;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
        String[] fallbackEncodings = {"UTF-8", "GBK", "GB18030", "GB2312", "ISO-8859-1"};

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
                if (reader.readLine() == null) break;
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
                }
            }
        }
        return recordsImported;
    }

    private Transaction createRegularTransaction(String[] rowData) {
        long id = nextId.getAndIncrement();
        String user = rowData.length > 0 ? rowData[0].trim() : currentUser;
        String source = rowData.length > 1 ? rowData[1].trim() : "import";
        LocalDate date = rowData.length > 2 ? LocalDate.parse(rowData[2].trim()) : LocalDate.now();
        double amount = rowData.length > 3 ? Double.parseDouble(rowData[3].trim()) : 0.0;
        String category = rowData.length > 4 ? rowData[4].trim() : "Uncategorized";
        String description = rowData.length > 5 ? rowData[5].trim() : "";

        return new Transaction((int)id, user, source, date, amount, category, description);
    }


    private Transaction createWechatTransaction(String[] rowData) {
        String processedDate = processDate(rowData[0]);

        // 处理金额，并根据交易类型调整符号
        double amount = Double.parseDouble(processAmount(rowData[5]));
        if ("支出".equals(rowData[4])) {
            amount = -amount;
        }
        String processedAmount = processAmount(String.valueOf(amount));

        return new Transaction(
                (int)nextId.getAndIncrement(),
                currentUser,
                "wechat",
                LocalDate.parse(processedDate),
                Double.parseDouble(processedAmount),
                "Uncategorized",
                rowData[1] + rowData[2]
        );
    }

    private Transaction createAlipayTransaction(String[] rowData) {
        String processedDate = processDate(rowData[0]);

        // 处理金额，并根据交易类型调整符号
        double amount = Double.parseDouble(processAmount(rowData[6]));
        if ("支出".equals(rowData[4])) {
            amount = -amount;
        }
        String processedAmount = processAmount(String.valueOf(amount));

        return new Transaction(
                (int)nextId.getAndIncrement(),
                currentUser,
                "alipay",
                LocalDate.parse(processedDate),
                Double.parseDouble(processedAmount),
                "Uncategorized",
                rowData[1] + rowData[4]
        );
    }
    public void saveToCSV() throws IOException {
        File file = new File(currentUser + "_transactions.csv");
        boolean fileExists = file.exists();

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8))) {

            if (!fileExists) {
                writer.write("ID,User,Source,Date,Amount,Category,Description");
                writer.newLine();
            }

            for (Transaction transaction : transactions) {
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
        }

        transactions.clear();
        saveNextId();
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

        if (hasAlipayMarker) return FileType.ALIPAY;
        if (hasWechatMarker) return FileType.WECHAT;
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
        saveNextId();
    }

    private void saveNextId() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(currentUser + "_nextId.txt"))) {
            writer.write(String.valueOf(nextId.get()));
        } catch (IOException e) {
            System.err.println("Cannot save nextId: " + e.getMessage());
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

        public boolean isSuccess() { return success; }
        public int getRecordsImported() { return recordsImported; }
        public String getEncoding() { return encoding; }
        public String getErrorMessage() { return errorMessage; }
    }
}