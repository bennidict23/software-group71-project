package org.example;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import java.util.HashMap;
import java.util.Map;

public class BudgetSettingsView {

    private final User currentUser;
    private final UserManager userManager;

    public BudgetSettingsView(User currentUser, UserManager userManager) {
        this.currentUser = currentUser;
        this.userManager = userManager;
    }

    public void showBudgetSettings(Stage primaryStage) {
        // 创建预算设置表单布局
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setAlignment(Pos.CENTER);

        // 设置输入框和按钮的宽度
        double inputWidth = 200;

        // 定义预算类别及其对应的 getter 和 setter 方法
        String[] budgetCategories = {
                "Transportation", "Shopping", "Other", "Entertainment", "Annual Budget"
        };

        Map<String, TextField> budgetFields = new HashMap<>();
        Map<String, Button> setButtons = new HashMap<>();
        Map<String, TextField> warningFields = new HashMap<>();

        // 循环创建每个预算类别的输入框和按钮
        for (int i = 0; i < budgetCategories.length; i++) {
            String category = budgetCategories[i];
            VBox budgetBox = new VBox(10);
            budgetBox.setAlignment(Pos.CENTER_LEFT);
            budgetBox.setPadding(new Insets(10));
            budgetBox.setStyle("-fx-border-color: gray; -fx-border-radius: 5px; -fx-padding: 10px;");

            Label budgetLabel = new Label(category + " Budget:");
            TextField budgetField = new TextField();
            budgetField.setPrefWidth(inputWidth);
            Label warningLabel = new Label("Warning:");
            TextField warningField = new TextField();
            warningField.setPrefWidth(inputWidth);
            warningField.setEditable(false); // 设置为不可编辑
            Button setButton = new Button("SET");
            setButton.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-pref-height: 40px;");
            setButton.setPrefWidth(inputWidth);

            budgetFields.put(category, budgetField);
            setButtons.put(category, setButton);
            warningFields.put(category, warningField);

            budgetBox.getChildren().addAll(budgetLabel, budgetField, warningLabel, warningField, setButton);

            // 将预算框添加到网格布局中
            grid.add(budgetBox, i % 2, i / 2);
        }

        // 创建返回仪表盘按钮
        Button backToDashboardButton = new Button("Back to Dashboard");
        backToDashboardButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        backToDashboardButton.setOnAction(e -> {
            DashboardView dashboardView = new DashboardView();
            try {
                dashboardView.start(primaryStage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // 将返回按钮添加到网格布局中
        grid.add(backToDashboardButton, 0, budgetCategories.length / 2 + 1, 2, 1);

        // 初始化预算字段的值
        for (String category : budgetCategories) {
            double budgetValue = getBudgetValue(category);
            budgetFields.get(category).setText(String.valueOf(budgetValue));

            // 检查预算和支出
            if (category.equals("Annual Budget")) {
                double annualExpenses = userManager.getAnnualTotalExpenses(currentUser);
                if (budgetValue <= annualExpenses) {
                    warningFields.get(category).setText("Annual expenses exceed annual budget!");
                    // 设置文本颜色为红色
                    warningFields.get(category).setStyle("-fx-text-fill: red;");
                } else {
                    warningFields.get(category).setText("normal");
                    warningFields.get(category).setStyle("-fx-text-fill: green;");
                }
            } else {
                double monthlyExpenses = userManager.getMonthlyExpensesByCategory(currentUser, category);
                if (budgetValue <= monthlyExpenses) {
                    warningFields.get(category).setText("Less than or equal to your current expenses!");
                    // 设置文本颜色为红色
                    warningFields.get(category).setStyle("-fx-text-fill: red;");
                } else {
                    warningFields.get(category).setText("normal");
                    warningFields.get(category).setStyle("-fx-text-fill: green;");
                }
            }
        }

        // 设置每个预算设置按钮的点击逻辑
        for (String category : budgetCategories) {
            setButtons.get(category).setOnAction(e -> {
                try {
                    double newBudget = Double.parseDouble(budgetFields.get(category).getText());
                    if (newBudget < 0) {
                        showAlert(Alert.AlertType.ERROR, "Error", "Budget value must be non-negative.");
                        return;
                    }
                    setBudgetValue(category, newBudget);
                    showAlert(Alert.AlertType.INFORMATION, "Success", category + " budget updated to: " + newBudget);
                    userManager.saveUserSettings(currentUser);

                    // 再次检查预算和支出
                    if (category.equals("Annual Budget")) {
                        double annualExpenses = userManager.getAnnualTotalExpenses(currentUser);
                        if (newBudget <= annualExpenses) {
                            warningFields.get(category).setText("Annual expenses exceed annual budget!");
                            // 设置文本颜色为红色
                            warningFields.get(category).setStyle("-fx-text-fill: red;");
                        } else {
                            warningFields.get(category).setText("normal");
                            warningFields.get(category).setStyle("-fx-text-fill: green;");
                        }
                    } else {
                        double monthlyExpenses = userManager.getMonthlyExpensesByCategory(currentUser, category);
                        if (newBudget <= monthlyExpenses) {
                            warningFields.get(category).setText("Less than or equal to your current expenses!");
                            // 设置文本颜色为红色
                            warningFields.get(category).setStyle("-fx-text-fill: red;");
                        } else {
                            warningFields.get(category).setText("normal");
                            warningFields.get(category).setStyle("-fx-text-fill: green;");
                        }
                    }
                } catch (NumberFormatException ex) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Please enter a valid budget number.");
                }
            });
        }

        Scene scene = new Scene(grid, 800, 800);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private double getBudgetValue(String category) {
        switch (category) {
            case "Transportation":
                return currentUser.getTransportationBudget();
            case "Shopping":
                return currentUser.getShoppingBudget();
            case "Other":
                return currentUser.getOtherBudget();
            case "Entertainment":
                return currentUser.getEntertainmentBudget();
            case "Annual Budget":
                return currentUser.getAnnualBudget();
            default:
                return 0.0;
        }
    }

    private void setBudgetValue(String category, double value) {
        switch (category) {
            case "Transportation":
                currentUser.setTransportationBudget(value);
                break;
            case "Shopping":
                currentUser.setShoppingBudget(value);
                break;
            case "Other":
                currentUser.setOtherBudget(value);
                break;
            case "Entertainment":
                currentUser.setEntertainmentBudget(value);
                break;
            case "Annual Budget":
                currentUser.setAnnualBudget(value);
                break;
        }
        // 更新月预算总额
        currentUser.setMonthlyBudget(
                currentUser.getTransportationBudget() +
                        currentUser.getShoppingBudget() +
                        currentUser.getOtherBudget() +
                        currentUser.getEntertainmentBudget());
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}