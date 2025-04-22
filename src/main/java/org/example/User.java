package org.example;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
    private List<String> warnings; // 新增警告信息列表
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.annualTarget = 36000.0;
        this.monthlyTarget = 3000.0;
        this.monthlyBudget = 2000.0;
        this.shoppingBudget = 500.0;
        this.transportBudget = 500.0;
        this.dietBudget = 500.0;
        this.amusementBudget = 500.0;
        this.savedAmount = 2000.0;
        this.annualSavedAmount = 24000.0;
        this.currentYear = LocalDate.now().getYear();
        this.currentMonth = LocalDate.now().getMonthValue();
        // 初始化警告信息列表
        this.warnings = new ArrayList<>();
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
        updateCurrentYearAndMonth();
    }

    public double getMonthlyTarget() {
        return monthlyTarget;
    }

    public void setMonthlyTarget(double monthlyTarget) {
        this.monthlyTarget = monthlyTarget;
        updateCurrentYearAndMonth();
    }

    public double getMonthlyBudget() {
        return monthlyBudget;
    }

    public void setMonthlyBudget(double monthlyBudget) {
        this.monthlyBudget = monthlyBudget;
        updateCurrentYearAndMonth();
    }

    public double getShoppingBudget() {
        return shoppingBudget;
    }

    public void setShoppingBudget(double shoppingBudget) {
        this.shoppingBudget = shoppingBudget;
        updateCurrentYearAndMonth();
    }

    public double getTransportBudget() {
        return transportBudget;
    }

    public void setTransportBudget(double transportBudget) {
        this.transportBudget = transportBudget;
        updateCurrentYearAndMonth();
    }

    public double getDietBudget() {
        return dietBudget;
    }

    public void setDietBudget(double dietBudget) {
        this.dietBudget = dietBudget;
        updateCurrentYearAndMonth();
    }

    public double getAmusementBudget() {
        return amusementBudget;
    }

    public void setAmusementBudget(double amusementBudget) {
        this.amusementBudget = amusementBudget;
        updateCurrentYearAndMonth();
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

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    // 新增方法：更新当前年份和月份
    private void updateCurrentYearAndMonth() {
        this.currentYear = LocalDate.now().getYear();
        this.currentMonth = LocalDate.now().getMonthValue();
    }

    // 新增方法：重置月目标和月预算
    public void resetMonthlySettings() {
        this.monthlyTarget = 3000.0;
        this.monthlyBudget = 2000.0;
        this.shoppingBudget = 500.0;
        this.transportBudget = 500.0;
        this.dietBudget = 500.0;
        this.amusementBudget = 500.0;
        updateCurrentYearAndMonth();
    }

    // 新增方法：重置年目标和年预算
    public void resetAnnualSettings() {
        this.annualTarget = 36000.0;
        this.monthlyTarget = 3000.0;
        this.monthlyBudget = 2000.0;
        this.shoppingBudget = 500.0;
        this.transportBudget = 500.0;
        this.dietBudget = 500.0;
        this.amusementBudget = 500.0;
        updateCurrentYearAndMonth();
    }
}