package com.financetracker.control;

import com.financetracker.entity.CategoryExpense;
import com.financetracker.entity.Transaction;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller class for handling data analysis operations.
 * This is a control class in the MVC pattern.
 */
public class DataAnalysisController {

    /**
     * Gets spending data by category for the specified time period
     * 
     * @param months Number of months to analyze
     * @return List of CategoryExpense objects with category names and expenses
     */
    public List<CategoryExpense> getSpendingByCategory(int months) {
        // In a real application, this would fetch data from a database or file
        // For now, we'll return dummy data
        List<CategoryExpense> result = new ArrayList<>();

        result.add(new CategoryExpense("Food & Dining", 3500));
        result.add(new CategoryExpense("Transportation", 1500));
        result.add(new CategoryExpense("Entertainment", 1000));
        result.add(new CategoryExpense("Shopping", 2000));
        result.add(new CategoryExpense("Utilities", 1200));
        result.add(new CategoryExpense("Others", 800));

        return result;
    }

    /**
     * Gets historical spending data for the specified time period
     * 
     * @param months Number of months of historical data to retrieve
     * @return Map with month names as keys and total spending as values
     */
    public Map<String, Double> getHistoricalSpending(int months) {
        // In a real application, this would fetch data from a database or file
        // For now, we'll return dummy data
        Map<String, Double> result = new HashMap<>();

        LocalDate today = LocalDate.now();
        for (int i = months - 1; i >= 0; i--) {
            LocalDate date = today.minus(i, ChronoUnit.MONTHS);
            String monthName = date.getMonth().toString().substring(0, 3);

            // Generate some dummy data with a bit of randomness
            double baseAmount = 4000;
            double randomFactor = 0.8 + Math.random() * 0.4; // Random between 0.8 and 1.2
            result.put(monthName, baseAmount * randomFactor);
        }

        return result;
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
            String monthName = date.getMonth().toString().substring(0, 3);

            // Add a slight upward trend and some randomness
            double trendFactor = 1.0 + (i * 0.03); // 3% increase per month
            double randomFactor = 0.9 + Math.random() * 0.2; // Random between 0.9 and 1.1
            forecast.put(monthName, avgMonthly * trendFactor * randomFactor);
        }

        return forecast;
    }
}