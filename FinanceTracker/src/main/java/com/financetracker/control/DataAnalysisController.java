package com.financetracker.control;

import com.financetracker.entity.CategoryExpense;
import com.financetracker.entity.Transaction;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller class for handling data analysis operations.
 * This is a control class in the MVC pattern.
 */
public class DataAnalysisController {

    private final DataAccessInterface dataAccess;

    /**
     * Constructs a DataAnalysisController with the specified data access interface
     * 
     * @param dataAccess The data access interface to use
     */
    public DataAnalysisController(DataAccessInterface dataAccess) {
        this.dataAccess = dataAccess;
    }

    /**
     * Gets a default instance of the controller with mock data
     * 
     * @return A DataAnalysisController using mock data
     */
    public static DataAnalysisController getDefaultInstance() {
        return new DataAnalysisController(new MockDataAccess());
    }

    /**
     * Gets spending data by category for the specified time period
     * 
     * @param months Number of months to analyze
     * @return List of CategoryExpense objects with category names and expenses
     */
    public List<CategoryExpense> getSpendingByCategory(int months) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minus(months, ChronoUnit.MONTHS);

        Map<String, Double> spendingMap = dataAccess.getSpendingByCategory(startDate, endDate);

        // Convert map to list of CategoryExpense objects
        return spendingMap.entrySet().stream()
                .map(entry -> new CategoryExpense(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * Gets historical spending data for the specified time period
     * 
     * @param months Number of months of historical data to retrieve
     * @return Map with month names as keys and total spending as values
     */
    public Map<String, Double> getHistoricalSpending(int months) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minus(months, ChronoUnit.MONTHS);

        return dataAccess.getSpendingByMonth(startDate, endDate);
    }

    /**
     * Analyzes spending trends and provides a forecast for future spending
     * 
     * @param historicalMonths Number of months of historical data to use
     * @param forecastMonths   Number of months to forecast
     * @return Map with month names as keys and forecasted spending as values
     */
    public Map<String, Double> getForecastedSpending(int historicalMonths, int forecastMonths) {
        // Get historical data first
        Map<String, Double> historical = getHistoricalSpending(historicalMonths);
        Map<String, Double> forecast = new HashMap<>();

        // Calculate the average monthly spending
        double total = 0;
        for (Double amount : historical.values()) {
            total += amount;
        }
        double avgMonthly = total / historical.size();

        // Generate forecast data (with slight upward trend)
        LocalDate today = LocalDate.now();
        for (int i = 0; i < forecastMonths; i++) {
            LocalDate date = today.plus(i + 1, ChronoUnit.MONTHS);
            String monthKey = date.getMonth().toString().substring(0, 3) + " " + date.getYear();

            // Add a slight upward trend and some randomness
            double trendFactor = 1.0 + (i * 0.03); // 3% increase per month
            double randomFactor = 0.9 + Math.random() * 0.2; // Random between 0.9 and 1.1
            forecast.put(monthKey, avgMonthly * trendFactor * randomFactor);
        }

        return forecast;
    }

    /**
     * Gets the detailed transaction list for a specific category and time period
     * 
     * @param category The category to filter by
     * @param months   Number of months to include
     * @return List of transactions matching the criteria
     */
    public List<Transaction> getTransactionsByCategory(String category, int months) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minus(months, ChronoUnit.MONTHS);

        return dataAccess.getTransactionsByCategory(category, startDate, endDate);
    }

    /**
     * Gets all transactions for a specific time period
     * 
     * @param months Number of months to include
     * @return List of all transactions in the time period
     */
    public List<Transaction> getAllTransactions(int months) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minus(months, ChronoUnit.MONTHS);

        return dataAccess.getTransactions(startDate, endDate);
    }

    /**
     * Gets all available transaction categories
     * 
     * @return List of category names
     */
    public List<String> getAllCategories() {
        return dataAccess.getCategories();
    }
}