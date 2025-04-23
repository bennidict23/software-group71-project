package org.example.analysis;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.DashboardView;
import org.example.User;
import org.example.analysis.BudgetRecommendationView;
import org.example.utils.LoadingUtils;

/**
 * AnalysisView类 - 数据分析页面
 * 提供三个核心分析功能：支出结构可视化、AI支出预测和智能预算推荐
 */
public class AnalysisView extends Application {
    private static final int WIDTH = 1200;
    private static final int HEIGHT = 800;

    private BorderPane mainContainer;
    private StackPane contentContainer;
    private User currentUser;
    private Button spendingStructureBtn;
    private Button spendingForecastBtn;
    private Button budgetRecommendationBtn;

    /**
     * 构造函数
     */
    public AnalysisView() {
        this.currentUser = DashboardView.getCurrentUser();
        this.mainContainer = new BorderPane();
        System.out.println("AnalysisView构造函数: currentUser = " +
                (currentUser != null ? currentUser.getUsername() : "null"));
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Finance Analysis Dashboard");

        System.out.println("AnalysisView.start(): currentUser = " +
                (currentUser != null ? currentUser.getUsername() : "null"));

        // 构建页面UI
        setupUI();

        // 创建和显示场景
        Scene scene = new Scene(mainContainer, WIDTH, HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * 设置UI组件和布局
     */
    private void setupUI() {
        // 创建内容容器为StackPane，支持加载指示器叠加
        contentContainer = new StackPane();
        contentContainer.setPadding(new Insets(20));

        // 显示欢迎屏幕
        showWelcomeScreen();

        // 创建左侧菜单
        VBox menuPanel = createMenuPanel();
        menuPanel.setPrefWidth(250);
        menuPanel.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 15px;");

        // 设置布局
        mainContainer.setLeft(menuPanel);
        mainContainer.setCenter(contentContainer);
    }

    /**
     * 创建左侧菜单面板
     */
    private VBox createMenuPanel() {
        VBox menuPanel = new VBox(15);
        menuPanel.setPadding(new Insets(20));
        menuPanel.setStyle("-fx-background-color: #eaeaea;");

        // 添加标题
        Label titleLabel = new Label("Analysis Tools");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        menuPanel.getChildren().add(titleLabel);

        // 创建菜单按钮
        spendingStructureBtn = createMenuButton("Spending Structure", e -> showSpendingStructure());
        spendingForecastBtn = createMenuButton("AI Spending Forecast", e -> showSpendingForecast());
        budgetRecommendationBtn = createMenuButton("Budget Recommendations", e -> showBudgetRecommendation());

        // 返回按钮
        Button backButton = createMenuButton("Return to Dashboard", e -> returnToDashboard());
        backButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        // 添加按钮到面板
        menuPanel.getChildren().addAll(
                new Separator(),
                spendingStructureBtn,
                spendingForecastBtn,
                budgetRecommendationBtn,
                new Separator(),
                backButton);

        return menuPanel;
    }

    /**
     * 创建菜单按钮
     */
    private Button createMenuButton(String text, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setPrefHeight(40);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setStyle("-fx-font-size: 14px;");
        button.setOnAction(handler);
        return button;
    }

    /**
     * 高亮选中的按钮
     */
    private void highlightSelectedButton(Button selectedButton) {
        // 重置所有按钮样式
        spendingStructureBtn.setStyle("-fx-font-size: 14px;");
        spendingForecastBtn.setStyle("-fx-font-size: 14px;");
        budgetRecommendationBtn.setStyle("-fx-font-size: 14px;");

        // 高亮选中的按钮
        selectedButton.setStyle("-fx-font-size: 14px; -fx-background-color: #d0d8e0; -fx-font-weight: bold;");
    }

    /**
     * 显示欢迎屏幕
     */
    private void showWelcomeScreen() {
        VBox welcomeBox = new VBox(20);
        welcomeBox.setAlignment(Pos.CENTER);
        welcomeBox.setPadding(new Insets(20));

        Label welcomeLabel = new Label("Welcome to Financial Analysis");
        welcomeLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label instructionLabel = new Label("Please select an analysis option from the menu on the left");
        instructionLabel.setStyle("-fx-font-size: 16px;");

        welcomeBox.getChildren().addAll(welcomeLabel, instructionLabel);

        // 清除现有内容并显示欢迎屏幕
        contentContainer.getChildren().clear();
        contentContainer.getChildren().add(welcomeBox);
    }

    /**
     * 显示支出结构可视化
     */
    private void showSpendingStructure() {
        // 高亮选中的按钮
        highlightSelectedButton(spendingStructureBtn);

        System.out.println("显示支出结构可视化");
        System.out.println("当前用户: " + (currentUser != null ? currentUser.getUsername() : "null"));

        try {
            if (currentUser == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("No user logged in");
                alert.showAndWait();
                return;
            }

            // 创建加载任务
            Task<SpendingStructureChart> loadTask = new Task<>() {
                @Override
                protected SpendingStructureChart call() throws Exception {
                    // 在后台线程中创建支出结构饼图
                    return new SpendingStructureChart(currentUser);
                }
            };

            // 清除现有内容
            contentContainer.getChildren().clear();

            // 显示加载指示器并执行任务
            LoadingUtils.showLoadingIndicator(contentContainer, loadTask, chart -> {
                // 设置到主容器
                contentContainer.getChildren().add(chart);
            });

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to display spending structure: " + e.getMessage());
        }
    }

    /**
     * 显示AI支出预测
     */
    private void showSpendingForecast() {
        // 高亮选中的按钮
        highlightSelectedButton(spendingForecastBtn);

        System.out.println("显示AI支出预测");
        System.out.println("当前用户: " + (currentUser != null ? currentUser.getUsername() : "null"));

        try {
            if (currentUser == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("No user logged in");
                alert.showAndWait();
                return;
            }

            // 创建加载任务
            Task<SpendingForecastView> loadTask = new Task<>() {
                @Override
                protected SpendingForecastView call() throws Exception {
                    // 在后台线程中创建支出预测视图
                    return new SpendingForecastView(currentUser);
                }
            };

            // 清除现有内容
            contentContainer.getChildren().clear();

            // 显示加载指示器并执行任务
            LoadingUtils.showLoadingIndicator(contentContainer, loadTask, forecastView -> {
                // 设置到主容器
                contentContainer.getChildren().add(forecastView);
            });

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to display spending forecast: " + e.getMessage());
        }
    }

    /**
     * 显示智能预算推荐
     */
    private void showBudgetRecommendation() {
        // 高亮选中的按钮
        highlightSelectedButton(budgetRecommendationBtn);

        System.out.println("显示智能预算推荐");
        System.out.println("当前用户: " + (currentUser != null ? currentUser.getUsername() : "null"));

        try {
            if (currentUser == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("No user logged in");
                alert.showAndWait();
                return;
            }

            // 创建加载任务
            Task<BudgetRecommendationView> loadTask = new Task<>() {
                @Override
                protected BudgetRecommendationView call() throws Exception {
                    // 在后台线程中创建预算推荐视图
                    return new BudgetRecommendationView();
                }
            };

            // 清除现有内容
            contentContainer.getChildren().clear();

            // 显示加载指示器并执行任务
            LoadingUtils.showLoadingIndicator(contentContainer, loadTask, recommendationView -> {
                // 设置到主容器
                contentContainer.getChildren().add(recommendationView);
            });

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to display budget recommendations: " + e.getMessage());
        }
    }

    /**
     * 返回仪表盘
     */
    private void returnToDashboard() {
        Stage currentStage = (Stage) mainContainer.getScene().getWindow();
        currentStage.close();

        try {
            DashboardView dashboard = new DashboardView();
            dashboard.start(new Stage());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to return to dashboard: " + e.getMessage());
        }
    }

    /**
     * 显示提示对话框
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}