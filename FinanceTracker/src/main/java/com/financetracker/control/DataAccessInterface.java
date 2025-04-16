package com.financetracker.control;

import com.financetracker.entity.Transaction;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Interface for accessing financial data.
 * This interface abstracts data retrieval operations, allowing the analysis
 * module
 * to work independently of how data is stored or loaded.
 */
public interface DataAccessInterface {

    /**
     * Retrieves all transactions within a date range
     * 
     * @param startDate The start date (inclusive)
     * @param endDate   The end date (inclusive)
     * @return List of transactions in the specified date range
     */
    List<Transaction> getTransactions(LocalDate startDate, LocalDate endDate);

    /**
     * Retrieves transactions filtered by category within a date range
     * 
     * @param category  The transaction category
     * @param startDate The start date (inclusive)
     * @param endDate   The end date (inclusive)
     * @return List of transactions matching the criteria
     */
    List<Transaction> getTransactionsByCategory(String category, LocalDate startDate, LocalDate endDate);

    /**
     * Gets all available transaction categories
     * 
     * @return List of category names
     */
    List<String> getCategories();

    /**
     * Loads transaction data from a file
     * 
     * @param filePath Path to the file containing transaction data
     * @param fileType Type of file (e.g., "CSV", "EXCEL", "WECHAT", "ALIPAY")
     * @return Number of transactions successfully loaded
     * @throws Exception If there is an error loading the file
     */
    int loadTransactionsFromFile(String filePath, String fileType) throws Exception;

    /**
     * Gets the total spending by category within a date range
     * 
     * @param startDate The start date (inclusive)
     * @param endDate   The end date (inclusive)
     * @return Map with category names as keys and total spending as values
     */
    Map<String, Double> getSpendingByCategory(LocalDate startDate, LocalDate endDate);

    /**
     * Gets the total spending by month within a date range
     * 
     * @param startDate The start date (inclusive)
     * @param endDate   The end date (inclusive)
     * @return Map with month names as keys and total spending as values
     */
    Map<String, Double> getSpendingByMonth(LocalDate startDate, LocalDate endDate);
}