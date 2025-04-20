package com.financetracker.entity;

/**
 * 表示特定类别的开支数据
 */
public class CategoryExpense {
    private String category;
    private double amount;
    private String description;

    /**
     * 创建一个新的类别开支
     *
     * @param category 开支类别
     * @param amount   开支金额
     */
    public CategoryExpense(String category, double amount) {
        this.category = category;
        this.amount = amount;
        this.description = "";
    }

    /**
     * 创建一个新的类别开支
     *
     * @param category    开支类别
     * @param amount      开支金额
     * @param description 描述信息
     */
    public CategoryExpense(String category, double amount, String description) {
        this.category = category;
        this.amount = amount;
        this.description = description;
    }

    /**
     * 获取开支类别
     *
     * @return 类别名称
     */
    public String getCategory() {
        return category;
    }

    /**
     * 设置开支类别
     *
     * @param category 类别名称
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * 获取开支金额
     *
     * @return 开支金额
     */
    public double getAmount() {
        return amount;
    }

    /**
     * 设置开支金额
     *
     * @param amount 开支金额
     */
    public void setAmount(double amount) {
        this.amount = amount;
    }

    /**
     * 获取开支描述
     *
     * @return 描述信息
     */
    public String getDescription() {
        return description;
    }

    /**
     * 设置开支描述
     *
     * @param description 描述信息
     */
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return String.format("%s: %.2f", category, amount);
    }
}