package com.financetracker.entity;

import java.time.LocalDate;

/**
 * Entity class representing a financial transaction.
 * This is an entity class in the MVC pattern.
 */
public class Transaction {
    private String id;
    private LocalDate date;
    private double amount;
    private String category;
    private String description;
    private String source; // e.g., "WeChat", "Alipay", "Manual Entry"

    /**
     * Constructs a Transaction with the specified details
     * 
     * @param id          The transaction ID
     * @param date        The transaction date
     * @param amount      The transaction amount
     * @param category    The transaction category
     * @param description The transaction description
     * @param source      The transaction source
     */
    public Transaction(String id, LocalDate date, double amount, String category,
            String description, String source) {
        this.id = id;
        this.date = date;
        this.amount = amount;
        this.category = category;
        this.description = description;
        this.source = source;
    }

    // Getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id='" + id + '\'' +
                ", date=" + date +
                ", amount=" + amount +
                ", category='" + category + '\'' +
                ", description='" + description + '\'' +
                ", source='" + source + '\'' +
                '}';
    }
}