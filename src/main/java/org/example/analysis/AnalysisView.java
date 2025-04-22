package org.example.analysis;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.DashboardView;
import org.example.User;
import org.example.analysis.BudgetRecommendationView;

/**
 * AnalysisView类 - 数据分析页面
 * 提供三个核心分析功能：支出结构可视化、AI支出预测和智能预算推荐
 */
public class AnalysisView extends Application {
    private static final int WIDTH = 1200;
    private static final int HEIGHT = 800;

    private BorderPane mainContainer;
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
        // 创建顶部导航栏
        HBox topBar = createTopBar();
        mainContainer.setTop(topBar);

        // 创建左侧菜单
        VBox leftMenu = createLeftMenu();
        mainContainer.setLeft(leftMenu);

        // 初始显示欢迎界面
        showWelcomeScreen();
    }

    /**
     * 创建顶部导航栏
     */
    private HBox createTopBar() {
        HBox topBar = new HBox(10);
        topBar.setPadding(new Insets(10));
        topBar.setStyle("-fx-background-color: #f8f8f8; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");

        // 标题
        Label titleLabel = new Label("Financial Analysis");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // 填充空间
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // 用户信息
        Label userLabel = new Label("User: " + (currentUser != null ? currentUser.getUsername() : "Guest"));

        // 返回按钮 - 改为绿色小按钮
        Button backButton = new Button("Dashboard");
        backButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        backButton.setOnAction(e -> returnToDashboard());

        // 添加组件到顶部栏
        topBar.getChildren().addAll(titleLabel, spacer, userLabel, backButton);
        return topBar;
    }

    /**
     * 创建左侧菜单
     */
    private VBox createLeftMenu() {
        VBox menu = new VBox(10);
        menu.setPadding(new Insets(10));
        menu.setPrefWidth(200);
        menu.setStyle("-fx-background-color: #f0f0f0;");

        // 菜单标题
        Label menuLabel = new Label("Analysis Options");
        menuLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        menuLabel.setPadding(new Insets(0, 0, 10, 0));

        // 支出结构可视化按钮
        spendingStructureBtn = new Button("Spending Structure");
        spendingStructureBtn.setMaxWidth(Double.MAX_VALUE);
        spendingStructureBtn.setOnAction(e -> showSpendingStructure());

        // AI支出预测按钮
        spendingForecastBtn = new Button("AI Spending Forecast");
        spendingForecastBtn.setMaxWidth(Double.MAX_VALUE);
        spendingForecastBtn.setOnAction(e -> showSpendingForecast());

        // 智能预算推荐按钮
        budgetRecommendationBtn = new Button("Budget Recommendation");
        budgetRecommendationBtn.setMaxWidth(Double.MAX_VALUE);
        budgetRecommendationBtn.setOnAction(e -> showBudgetRecommendation());

        // 添加到菜单
        menu.getChildren().addAll(
                menuLabel,
                spendingStructureBtn,
                spendingForecastBtn,
                budgetRecommendationBtn);

        return menu;
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
        mainContainer.setCenter(welcomeBox);
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

            // 创建支出结构饼图
            SpendingStructureChart chart = new SpendingStructureChart(currentUser);

            // 设置到主容器
            mainContainer.setCenter(chart);
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

        try {
            if (currentUser == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("No user logged in");
                alert.showAndWait();
                return;
            }

            // 创建AI支出预测视图
            SpendingForecastView forecastView = new SpendingForecastView(currentUser);

            // 设置到主容器
            mainContainer.setCenter(forecastView);
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

        try {
            // 创建智能预算推荐视图
            BudgetRecommendationView recommendationView = new BudgetRecommendationView();

            // 设置到主容器
            mainContainer.setCenter(recommendationView);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to display budget recommendation: " + e.getMessage());
        }
    }

    /**
     * 高亮选中的按钮
     */
    private void highlightSelectedButton(Button selectedButton) {
        // 重置所有按钮样式
        spendingStructureBtn.setStyle("-fx-background-color: #f0f0f0;");
        spendingForecastBtn.setStyle("-fx-background-color: #f0f0f0;");
        budgetRecommendationBtn.setStyle("-fx-background-color: #f0f0f0;");

        // 设置选中按钮的样式
        selectedButton.setStyle("-fx-background-color: #c0d9e7; -fx-font-weight: bold;");
    }

    /**
     * 返回到仪表盘
     */
    private void returnToDashboard() {
        // 关闭当前窗口
        Stage currentStage = (Stage) mainContainer.getScene().getWindow();
        currentStage.close();

        // 打开仪表盘
        try {
            DashboardView dashboardView = new DashboardView();
            dashboardView.start(new Stage());
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Error", "Failed to return to dashboard: " + ex.getMessage());
        }
    }

    /**
     * 显示警告/错误信息
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * 主方法，用于单独运行此视图
     */
//    public static void main(String[] args) {
//        launch(args);
//    }
}