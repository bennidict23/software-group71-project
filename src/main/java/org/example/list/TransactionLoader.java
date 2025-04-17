package org.example.list;

// TransactionLoader.java
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
                        values[0].trim(),
                        values[1].trim(),
                        LocalDate.parse(values[2].trim()),
                        Double.parseDouble(values[3].trim()),
                        values[4].trim(),
                        values[5].trim()
                    );
                    transactions.add(transaction);
                }
            }
        }
        return transactions;
    }
}