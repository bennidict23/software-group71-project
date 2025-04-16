package com.financetracker.control;

import com.financetracker.entity.Transaction;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Mock implementation of DataAccessInterface for testing purposes.
 * Provides simulated financial data without requiring a real data source.
 */
public class MockDataAccess implements DataAccessInterface {

    private final List<Transaction> mockTransactions;
    private final List<String> categories;

    public MockDataAccess() {
        // Initialize mock data
        mockTransactions = new ArrayList<>();
        categories = Arrays.asList(
                "Food & Dining", "Transportation", "Housing", "Entertainment",
                "Shopping", "Utilities", "Healthcare", "Travel", "Education", "Others");

        // Generate mock transactions for the past year
        LocalDate today = LocalDate.now();
        LocalDate oneYearAgo = today.minus(1, ChronoUnit.YEARS);

        Random random = new Random(42); // Fixed seed for reproducible results

        // Transaction ID format
        DateTimeFormatter idFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        for (LocalDate date = oneYearAgo; !date.isAfter(today); date = date.plusDays(1)) {
            // Generate 0-3 transactions per day
            int transactionsPerDay = random.nextInt(4);

            for (int i = 0; i < transactionsPerDay; i++) {
                String id = date.format(idFormatter) + "-" + i;
                String category = categories.get(random.nextInt(categories.size()));

                // Amount between 10 and 1000
                double amount = 10 + (random.nextDouble() * 990);

                // Different sources
                String[] sources = { "WeChat", "Alipay", "Bank", "Cash", "Credit Card" };
                String source = sources[random.nextInt(sources.length)];

                // Description based on category
                String description = generateDescription(category, random);

                mockTransactions.add(new Transaction(id, date, amount, category, description, source));
            }
        }
    }

    private String generateDescription(String category, Random random) {
        // Generate a realistic description based on category
        Map<String, String[]> descriptions = new HashMap<>();
        descriptions.put("Food & Dining", new String[] { "Lunch at restaurant", "Grocery shopping", "Coffee shop",
                "Dinner with friends", "Food delivery" });
        descriptions.put("Transportation",
                new String[] { "Bus fare", "Taxi ride", "Gas", "Parking fee", "Subway ticket" });
        descriptions.put("Housing",
                new String[] { "Rent payment", "Property management fee", "Home repair", "Furniture", "Utilities" });
        descriptions.put("Entertainment",
                new String[] { "Movie tickets", "Concert", "Online subscription", "Video games", "Book purchase" });
        descriptions.put("Shopping", new String[] { "Clothing purchase", "Electronics", "Online shopping",
                "Department store", "Gift purchase" });
        descriptions.put("Utilities",
                new String[] { "Electricity bill", "Water bill", "Internet bill", "Phone bill", "Gas bill" });
        descriptions.put("Healthcare",
                new String[] { "Doctor visit", "Medicine", "Health insurance", "Dental care", "Eye care" });
        descriptions.put("Travel",
                new String[] { "Hotel booking", "Flight tickets", "Travel insurance", "Tour package", "Souvenir" });
        descriptions.put("Education",
                new String[] { "Tuition fee", "Textbooks", "Online course", "School supplies", "Training program" });
        descriptions.put("Others",
                new String[] { "Miscellaneous expenses", "Service fee", "Donation", "Gift", "Other payment" });

        String[] options = descriptions.getOrDefault(category, new String[] { "Payment" });
        return options[random.nextInt(options.length)];
    }

    @Override
    public List<Transaction> getTransactions(LocalDate startDate, LocalDate endDate) {
        return mockTransactions.stream()
                .filter(t -> !t.getDate().isBefore(startDate) && !t.getDate().isAfter(endDate))
                .toList();
    }

    @Override
    public List<Transaction> getTransactionsByCategory(String category, LocalDate startDate, LocalDate endDate) {
        return mockTransactions.stream()
                .filter(t -> t.getCategory().equals(category))
                .filter(t -> !t.getDate().isBefore(startDate) && !t.getDate().isAfter(endDate))
                .toList();
    }

    @Override
    public List<String> getCategories() {
        return new ArrayList<>(categories);
    }

    @Override
    public int loadTransactionsFromFile(String filePath, String fileType) throws Exception {
        // Mock implementation - pretend to load file
        return 50; // Return fake number of loaded transactions
    }

    @Override
    public Map<String, Double> getSpendingByCategory(LocalDate startDate, LocalDate endDate) {
        Map<String, Double> result = new HashMap<>();

        // Filter transactions by date range
        List<Transaction> filteredTransactions = getTransactions(startDate, endDate);

        // Group by category and sum amounts
        for (Transaction transaction : filteredTransactions) {
            String category = transaction.getCategory();
            double amount = transaction.getAmount();

            result.put(category, result.getOrDefault(category, 0.0) + amount);
        }

        return result;
    }

    @Override
    public Map<String, Double> getSpendingByMonth(LocalDate startDate, LocalDate endDate) {
        Map<String, Double> result = new HashMap<>();

        // Filter transactions by date range
        List<Transaction> filteredTransactions = getTransactions(startDate, endDate);

        // Group by month and sum amounts
        for (Transaction transaction : filteredTransactions) {
            LocalDate date = transaction.getDate();
            String monthKey = date.getMonth().toString().substring(0, 3) + " " + date.getYear();
            double amount = transaction.getAmount();

            result.put(monthKey, result.getOrDefault(monthKey, 0.0) + amount);
        }

        // Sort by date (need to convert to list for sorting)
        List<Map.Entry<String, Double>> entries = new ArrayList<>(result.entrySet());
        entries.sort((e1, e2) -> {
            String[] parts1 = e1.getKey().split(" ");
            String[] parts2 = e2.getKey().split(" ");

            int year1 = Integer.parseInt(parts1[1]);
            int year2 = Integer.parseInt(parts2[1]);

            if (year1 != year2) {
                return year1 - year2;
            }

            Month month1 = Month.valueOf(parts1[0].toUpperCase() + parts1[0].substring(3).toLowerCase());
            Month month2 = Month.valueOf(parts2[0].toUpperCase() + parts2[0].substring(3).toLowerCase());

            return month1.compareTo(month2);
        });

        // Convert back to map (LinkedHashMap to maintain order)
        Map<String, Double> sortedResult = new LinkedHashMap<>();
        for (Map.Entry<String, Double> entry : entries) {
            sortedResult.put(entry.getKey(), entry.getValue());
        }

        return sortedResult;
    }
}