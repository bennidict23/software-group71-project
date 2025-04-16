package com.financetracker.entity;

/**
 * Entity class representing an expense category with its total amount.
 * This is an entity class in the MVC pattern.
 */
public class CategoryExpense {
    private String category;
    private double amount;

    /**
     * Constructs a CategoryExpense with the specified category and amount
     * 
     * @param category The expense category name
     * @param amount   The total expense amount for this category
     */
    public CategoryExpense(String category, double amount) {
        this.category = category;
        this.amount = amount;
    }

    /**
     * Gets the category name
     * 
     * @return The category name
     */
    public String getCategory() {
        return category;
    }

    /**
     * Sets the category name
     * 
     * @param category The category name to set
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * Gets the expense amount
     * 
     * @return The expense amount
     */
    public double getAmount() {
        return amount;
    }

    /**
     * Sets the expense amount
     * 
     * @param amount The expense amount to set
     */
    public void setAmount(double amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "CategoryExpense{" +
                "category='" + category + '\'' +
                ", amount=" + amount +
                '}';
    }
}