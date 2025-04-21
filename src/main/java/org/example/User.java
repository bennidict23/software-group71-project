package org.example;

import java.time.LocalDate;

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
    private double savedAmount;
    private double annualSavedAmount;
    private int currentYear;
    private int currentMonth;


    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.annualTarget = 0.0;
        this.monthlyTarget = 0.0;
        this.monthlyBudget = 0.0;
        this.shoppingBudget = 0.0;
        this.transportBudget = 0.0;
        this.dietBudget = 0.0;
        this.amusementBudget = 0.0;
        this.savedAmount = 2000.0;
        this.annualSavedAmount = 24000.0;
        this.currentYear = LocalDate.now().getYear();
        this.currentMonth = LocalDate.now().getMonthValue();
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

    public int getCurrentYear() {
        return currentYear;
    }

    public int getCurrentMonth() {
        return currentMonth;
    }

    public void setCurrentYear(int currentYear) {
        this.currentYear = currentYear;
    }

    public void setCurrentMonth(int currentMonth) {
        this.currentMonth = currentMonth;
    }



    // 新增方法：重置月目标和月预算
    public void resetMonthlySettings() {
        this.monthlyTarget = 0.0;
        this.monthlyBudget = 0.0;
        this.shoppingBudget = 0.0;
        this.transportBudget = 0.0;
        this.dietBudget = 0.0;
        this.amusementBudget = 0.0;
    }

    // 新增方法：重置年目标和年预算
    public void resetAnnualSettings() {
        this.annualTarget = 0.0;
        this.monthlyTarget = 0.0;
        this.monthlyBudget = 0.0;
        this.shoppingBudget = 0.0;
        this.transportBudget = 0.0;
        this.dietBudget = 0.0;
        this.amusementBudget = 0.0;
    }
}