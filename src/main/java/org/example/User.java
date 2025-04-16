package org.example;

public class User {
    private String username;
    private String password;
    private double annualTarget;
    private double monthlyTarget;
    private double monthlyBudget;
    private double shoppingBudget;
    private double transportBudget;
    private double dietBudget;
    private double amusementBudget;
    private double savedAmount; // 添加当前存款金额字段
    private double annualSavedAmount; // 添加年度存款金额字段

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.annualTarget = 20000.0; // 默认值
        this.monthlyTarget = 5000.0; // 默认值
        this.monthlyBudget = 0.0; // 默认值
        this.shoppingBudget = 0.0; // 默认值
        this.transportBudget = 0.0; // 默认值
        this.dietBudget = 0.0; // 默认值
        this.amusementBudget = 0.0; // 默认值
        this.savedAmount = 1500.0; // 默认值
        this.annualSavedAmount = 6000.0; // 默认值
    }

    // Getters and Setters for all fields
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public double getAnnualTarget() {
        return annualTarget;
    }

    public void setAnnualTarget(double annualTarget) {
        this.annualTarget = annualTarget;
    }

    public double getMonthlyTarget() {
        return monthlyTarget;
    }

    public void setMonthlyTarget(double monthlyTarget) {
        this.monthlyTarget = monthlyTarget;
    }

    public double getMonthlyBudget() {
        return monthlyBudget;
    }

    public void setMonthlyBudget(double monthlyBudget) {
        this.monthlyBudget = monthlyBudget;
    }

    public double getShoppingBudget() {
        return shoppingBudget;
    }

    public void setShoppingBudget(double shoppingBudget) {
        this.shoppingBudget = shoppingBudget;
    }

    public double getTransportBudget() {
        return transportBudget;
    }

    public void setTransportBudget(double transportBudget) {
        this.transportBudget = transportBudget;
    }

    public double getDietBudget() {
        return dietBudget;
    }

    public void setDietBudget(double dietBudget) {
        this.dietBudget = dietBudget;
    }

    public double getAmusementBudget() {
        return amusementBudget;
    }

    public void setAmusementBudget(double amusementBudget) {
        this.amusementBudget = amusementBudget;
    }

    public double getSavedAmount() {
        return savedAmount;
    }

    public void setSavedAmount(double savedAmount) {
        this.savedAmount = savedAmount;
    }

    public double getAnnualSavedAmount() {
        return annualSavedAmount;
    }

    public void setAnnualSavedAmount(double annualSavedAmount) {
        this.annualSavedAmount = annualSavedAmount;
    }
}