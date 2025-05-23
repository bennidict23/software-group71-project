package org.example;

import java.time.LocalDate;

public class User {
    private final String username;
    private String password;
    private double annualTarget;   // 年储蓄目标
    private double monthlyTarget;  // 月储蓄目标
    private double monthlyBudget;  // 月预算总额
    private double transportationBudget; // 交通预算
    private double shoppingBudget; // 购物预算
    private double otherBudget;    // 其他预算
    private double entertainmentBudget; // 娱乐预算
    private double savedAmount;    // 本月已攒金额（初始=月工资+支出）
    private double annualSavedAmount; // 本年已攒金额（初始=年工资+支出）
    private int currentYear;       // 当前年份
    private int currentMonth;      // 当前月份
    private double annualBudget;   // 年总预算

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        // 初始化目标和预算
        this.annualTarget = 6000;    // 年目标默认6000
        this.monthlyTarget = 500;    // 月目标默认500
        this.monthlyBudget = 2000;   // 月预算默认2000
        this.transportationBudget = monthlyBudget / 4; // 均分预算（4类）
        this.shoppingBudget = monthlyBudget / 4;
        this.otherBudget = monthlyBudget / 4;
        this.entertainmentBudget = monthlyBudget / 4;
        // 初始化储蓄金额（启动时由UserManager填充实际支出）
        this.savedAmount = 3000;     // 模拟月工资3000
        this.annualSavedAmount = 36000; // 模拟年工资36000
        // 初始化当前时间
        LocalDate now = LocalDate.now();
        this.currentYear = now.getYear();
        this.currentMonth = now.getMonthValue();
        this.annualBudget = 24000;   // 年总预算默认24000
    }

    // -------------------- Getter & Setter --------------------
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public double getAnnualTarget() { return annualTarget; }
    public void setAnnualTarget(double annualTarget) { this.annualTarget = annualTarget; }

    public double getMonthlyTarget() { return monthlyTarget; }
    public void setMonthlyTarget(double monthlyTarget) { this.monthlyTarget = monthlyTarget; }

    public double getMonthlyBudget() { return monthlyBudget; }
    public void setMonthlyBudget(double monthlyBudget) { this.monthlyBudget = monthlyBudget; }

    public double getTransportationBudget() { return transportationBudget; }
    public void setTransportationBudget(double transportationBudget) { this.transportationBudget = transportationBudget; }

    public double getShoppingBudget() { return shoppingBudget; }
    public void setShoppingBudget(double shoppingBudget) { this.shoppingBudget = shoppingBudget; }

    public double getOtherBudget() { return otherBudget; }
    public void setOtherBudget(double otherBudget) { this.otherBudget = otherBudget; }

    public double getEntertainmentBudget() { return entertainmentBudget; }
    public void setEntertainmentBudget(double entertainmentBudget) { this.entertainmentBudget = entertainmentBudget; }

    public double getSavedAmount() { return savedAmount; }
    public void setSavedAmount(double savedAmount) { this.savedAmount = savedAmount; }

    public double getAnnualSavedAmount() { return annualSavedAmount; }
    public void setAnnualSavedAmount(double annualSavedAmount) { this.annualSavedAmount = annualSavedAmount; }

    public int getCurrentYear() { return currentYear; }
    public int getCurrentMonth() { return currentMonth; }
    public void setCurrentYear(int currentYear) { this.currentYear = currentYear; }
    public void setCurrentMonth(int currentMonth) { this.currentMonth = currentMonth; }

    public double getAnnualBudget() { return annualBudget; }
    public void setAnnualBudget(double annualBudget) { this.annualBudget = annualBudget; }

    // -------------------- 重置方法 --------------------
    /** 重置月目标和预算（每月自动调用） */
    public void resetMonthlySettings() {
        this.monthlyTarget = 500;           // 月目标重置为500
        this.monthlyBudget = 2000;          // 月预算重置为2000
        this.transportationBudget = monthlyBudget / 4; // 均分预算（4类）
        this.shoppingBudget = monthlyBudget / 4;
        this.otherBudget = monthlyBudget / 4;
        this.entertainmentBudget = monthlyBudget / 4;
    }

    /** 重置年目标和预算（每年自动调用） */
    public void resetAnnualSettings() {
        resetMonthlySettings(); // 先重置月设置
        this.annualTarget = 6000; // 年目标重置为6000
        this.annualBudget = 24000; // 年总预算重置为24000
    }
}