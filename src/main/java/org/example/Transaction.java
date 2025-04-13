package org.example;

import java.time.LocalDate;

/**
 * Represents a financial transaction.
 */
public class Transaction {
    public LocalDate date;
    public double amount;
    public String category;
    public String description;

    public Transaction(LocalDate date, double amount, String description) {
        this.date = date;
        this.amount = amount;
        this.description = description;
        this.category = "Uncategorized";
    }
}

