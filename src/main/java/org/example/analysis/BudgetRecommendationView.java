package org.example.analysis;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.DashboardView;
import org.example.User;
import org.example.UserManager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 预算推荐视图 - 基于用户历史支出提供个性化预算分配建议
 */
public class BudgetRecommendationView extends BorderPane {

    private static final String TRANSACTION_FILE = "transactions.csv";
    private final User currentUser;
    private final UserManager userManager;
    private final Map<String, Double> monthlyAverages = new HashMap<>();
    private final Map<String, Double> recommendedBudgets = new HashMap<>();

    private VBox budgetCardsContainer;
    private Label totalBudgetLabel;
    private double totalBudget = 0.0;

    /**
     * 构造函数
     */
    public BudgetRecommendationView() {
        this.currentUser = DashboardView.getCurrentUser();
        this.userManager = new UserManager();

        setPadding(new Insets(20));

        // 分析历史数据
        analyzeHistoricalData();

        // 生成预算建议
        generateRecommendations();

        // 构建UI界面
        initializeUI();
    }

    /**
     * 初始化UI组件
     */
    private void initializeUI() {
        VBox mainContainer = new VBox(20);
        mainContainer.setAlignment(Pos.TOP_CENTER);

        // 标题
        Label titleLabel = new Label("Smart Budget Recommendations");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // 说明文字
        Label descriptionLabel = new Label(
                "Based on your historical spending patterns, we've created personalized budget " +
                        "recommendations to help you manage your finances more effectively.");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxWidth(700);

        // 预算卡片容器
        budgetCardsContainer = new VBox(15);
        budgetCardsContainer.setPadding(new Insets(10));

        // 创建滚动面板
        ScrollPane scrollPane = new ScrollPane(budgetCardsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);

        // 总预算标签
        totalBudgetLabel = new Label("Total Recommended Monthly Budget: $0.00");
        totalBudgetLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // 应用预算按钮
        Button applyBudgetButton = new Button("Apply Recommended Budgets");
        applyBudgetButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px;");
        applyBudgetButton.setOnAction(e -> applyRecommendedBudgets());

        // 底部按钮区域
        HBox buttonBox = new HBox(20, applyBudgetButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10));

        // 添加所有组件到主容器
        mainContainer.getChildren().addAll(
                titleLabel,
                descriptionLabel,
                scrollPane,
                totalBudgetLabel,
                buttonBox);

        // 如果没有数据，显示提示信息
        if (monthlyAverages.isEmpty()) {
            showNoDataMessage(mainContainer);
        } else {
            // 否则显示预算建议
            displayRecommendations();
        }

        this.setCenter(mainContainer);
    }

    /**
     * 分析历史消费数据
     */
    private void analyzeHistoricalData() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(TRANSACTION_FILE));

            // 跳过标题行
            String line = reader.readLine();

            // 按类别统计支出
            Map<String, List<Double>> categoryExpenses = new HashMap<>();

            // 日期格式化
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            // 当前日期和6个月前的日期
            LocalDate now = LocalDate.now();
            LocalDate sixMonthsAgo = now.minusMonths(6);

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 6 && parts[0].equals(currentUser.getUsername())) {
                    try {
                        // 解析日期
                        LocalDate date = LocalDate.parse(parts[2], formatter);

                        // 只考虑最近6个月的数据
                        if (date.isAfter(sixMonthsAgo)) {
                            // 解析金额和类别
                            double amount = Double.parseDouble(parts[3]);
                            String category = parts[4].trim();

                            // 只考虑支出（负数金额）
                            if (amount < 0) {
                                amount = Math.abs(amount); // 转为正数用于计算

                                // 添加到类别统计
                                if (!categoryExpenses.containsKey(category)) {
                                    categoryExpenses.put(category, new ArrayList<>());
                                }
                                categoryExpenses.get(category).add(amount);
                            }
                        }
                    } catch (Exception e) {
                        // 忽略解析错误的记录
                        continue;
                    }
                }
            }

            reader.close();

            // 计算每个类别的月均支出
            for (Map.Entry<String, List<Double>> entry : categoryExpenses.entrySet()) {
                String category = entry.getKey();
                List<Double> expenses = entry.getValue();

                double total = 0.0;
                for (Double expense : expenses) {
                    total += expense;
                }

                // 计算月平均支出
                double monthlyAverage = total / 6.0; // 假设6个月数据
                monthlyAverages.put(category, monthlyAverage);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Data Analysis Error",
                    "Could not analyze historical data: " + e.getMessage());
        }
    }

    /**
     * 生成预算建议
     */
    private void generateRecommendations() {
        totalBudget = 0.0;

        // 基于不同类别的重要性进行预算分配
        Map<String, Double> budgetFactors = new HashMap<>();
        // 基本生活必需品维持原样
        budgetFactors.put("Groceries", 1.0);
        budgetFactors.put("Housing", 1.0);
        budgetFactors.put("Utilities", 1.0);
        budgetFactors.put("Healthcare", 1.0);
        // 可控支出适度压缩
        budgetFactors.put("Transportation", 0.95);
        budgetFactors.put("Education", 1.0);
        budgetFactors.put("Entertainment", 0.8);
        budgetFactors.put("Dining", 0.85);
        budgetFactors.put("Shopping", 0.75);
        budgetFactors.put("Travel", 0.7);

        // 为每个类别生成建议预算
        for (Map.Entry<String, Double> entry : monthlyAverages.entrySet()) {
            String category = entry.getKey();
            double average = entry.getValue();

            // 应用预算因子
            double factor = budgetFactors.getOrDefault(category, 0.9); // 默认为90%
            double recommended = average * factor;

            // 四舍五入到整数
            recommended = Math.round(recommended);

            // 保存建议预算
            recommendedBudgets.put(category, recommended);
            totalBudget += recommended;
        }

        // 更新总预算标签
        if (totalBudgetLabel != null) {
            totalBudgetLabel.setText(String.format("Total Recommended Monthly Budget: $%.2f", totalBudget));
        }
    }

    /**
     * 显示预算建议卡片
     */
    private void displayRecommendations() {
        // 清空容器
        budgetCardsContainer.getChildren().clear();

        // 按预算金额降序排序
        List<Map.Entry<String, Double>> sortedBudgets = new ArrayList<>(recommendedBudgets.entrySet());
        sortedBudgets.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        // 创建每个类别的预算卡片
        for (Map.Entry<String, Double> entry : sortedBudgets) {
            String category = entry.getKey();
            double recommendedBudget = entry.getValue();
            double averageSpending = monthlyAverages.get(category);

            // 创建预算卡片
            HBox budgetCard = createBudgetCard(category, recommendedBudget, averageSpending);
            budgetCardsContainer.getChildren().add(budgetCard);
        }
    }

    /**
     * 创建单个预算卡片
     */
    private HBox createBudgetCard(String category, double recommendedBudget, double averageSpending) {
        // 创建卡片容器
        HBox card = new HBox(20);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: #f8f8f8; -fx-border-color: #e0e0e0; -fx-border-radius: 5px;");
        card.setPrefHeight(100);

        // 类别标签
        Label categoryLabel = new Label(category);
        categoryLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        categoryLabel.setPrefWidth(150);

        // 历史支出信息
        VBox historyBox = new VBox(5);
        Label historyLabel = new Label("Historical Monthly Average:");
        historyLabel.setStyle("-fx-font-size: 12px;");
        Label historyValueLabel = new Label(String.format("$%.2f", averageSpending));
        historyValueLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        historyBox.getChildren().addAll(historyLabel, historyValueLabel);

        // 建议预算信息
        VBox recommendationBox = new VBox(5);
        Label recommendationLabel = new Label("Recommended Budget:");
        recommendationLabel.setStyle("-fx-font-size: 12px;");
        Label recommendationValueLabel = new Label(String.format("$%.2f", recommendedBudget));
        recommendationValueLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        recommendationBox.getChildren().addAll(recommendationLabel, recommendationValueLabel);

        // 变化百分比
        double difference = recommendedBudget - averageSpending;
        double percentChange = (difference / averageSpending) * 100;
        String changeText = String.format("%.1f%% %s", Math.abs(percentChange),
                difference < 0 ? "Reduction" : "Increase");

        VBox changeBox = new VBox(5);
        Label changeLabel = new Label("Change:");
        changeLabel.setStyle("-fx-font-size: 12px;");
        Label changeValueLabel = new Label(changeText);
        changeValueLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        // 设置变化值的颜色
        if (difference < 0) {
            changeValueLabel.setTextFill(Color.GREEN); // 减少为绿色
        } else if (difference > 0) {
            changeValueLabel.setTextFill(Color.RED); // 增加为红色
        }

        changeBox.getChildren().addAll(changeLabel, changeValueLabel);

        // 可调整的预算输入
        VBox adjustmentBox = new VBox(5);
        Label adjustmentLabel = new Label("Adjust Budget:");
        adjustmentLabel.setStyle("-fx-font-size: 12px;");

        TextField budgetField = new TextField(String.format("%.2f", recommendedBudget));
        budgetField.setPrefWidth(100);

        // 添加输入验证和更新逻辑
        budgetField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (!newValue.isEmpty()) {
                    double value = Double.parseDouble(newValue);

                    // 更新推荐预算
                    recommendedBudgets.put(category, value);

                    // 重新计算总预算
                    updateTotalBudget();
                }
            } catch (NumberFormatException e) {
                // 恢复之前的值
                budgetField.setText(oldValue);
            }
        });

        adjustmentBox.getChildren().addAll(adjustmentLabel, budgetField);

        // 添加所有部分到卡片
        card.getChildren().addAll(categoryLabel, historyBox, recommendationBox, changeBox, adjustmentBox);

        return card;
    }

    /**
     * 更新总预算金额
     */
    private void updateTotalBudget() {
        totalBudget = 0.0;
        for (Double budget : recommendedBudgets.values()) {
            totalBudget += budget;
        }
        totalBudgetLabel.setText(String.format("Total Recommended Monthly Budget: $%.2f", totalBudget));
    }

    /**
     * 应用推荐预算
     */
    private void applyRecommendedBudgets() {
        if (currentUser != null) {
            // 设置总预算
            currentUser.setMonthlyBudget(totalBudget);

            // 更新数据库中的用户预算
            boolean success = userManager.updateUserBudget(currentUser.getUsername(), totalBudget);

            // 根据结果显示反馈
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Budget Updated",
                        "Your monthly budget has been updated successfully.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Update Failed",
                        "Failed to update your monthly budget. Please try again later.");
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "User Error",
                    "No user is currently logged in. Please log in first.");
        }
    }

    /**
     * 显示无数据提示
     */
    private void showNoDataMessage(VBox container) {
        // 清空现有内容
        container.getChildren().clear();

        VBox messageBox = new VBox(15);
        messageBox.setAlignment(Pos.CENTER);
        messageBox.setPadding(new Insets(50));

        Label titleLabel = new Label("No Transaction Data Available");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 20));

        Label descLabel = new Label(
                "We don't have enough transaction data to generate personalized budget recommendations. " +
                        "Please add more transaction records and try again.");
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(500);

        messageBox.getChildren().addAll(titleLabel, descLabel);
        container.getChildren().add(messageBox);
    }

    /**
     * 显示提示对话框
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}