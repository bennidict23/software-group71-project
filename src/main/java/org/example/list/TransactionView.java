package org.example.list;

import java.io.IOException;
import java.time.LocalDate;

import org.example.DashboardView;
import org.example.User;
import org.example.utils.CategoryRulesManager;
import org.example.utils.DeepSeekCategoryService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.LocalDateStringConverter;

public class TransactionView {
    private final User currentUser = DashboardView.getCurrentUser();
    private final String username = currentUser.getUsername();
    private final TextField searchField = new TextField();
    private final BorderPane root = new BorderPane();
    private final TableView<Transaction> table = new TableView<>();
    private final Button loadButton = new Button("Update Data");
    private final Button searchButton = new Button("Search");
    private final Button saveChangesButton = new Button("Save Changes");

    // 预定义的分类列表
    private final ObservableList<String> predefinedCategories = FXCollections.observableArrayList(
            DeepSeekCategoryService.getPredefinedCategories());

    public TransactionView() {
        configureTable();
        layoutUI();
    }

    private void configureTable() {
        // 启用表格编辑
        table.setEditable(true);

        // TableColumn<Transaction, String> userCol = new TableColumn<>("User");
        TableColumn<Transaction, String> sourceCol = new TableColumn<>("Source");
        TableColumn<Transaction, LocalDate> dateCol = new TableColumn<>("Date");
        TableColumn<Transaction, Double> amountCol = new TableColumn<>("Amount");
        TableColumn<Transaction, String> categoryCol = new TableColumn<>("Category");
        TableColumn<Transaction, String> descCol = new TableColumn<>("Description");
        TableColumn<Transaction, Void> actionCol = new TableColumn<>("Actions");

        // 配置列值工厂
        // userCol.setCellValueFactory(data -> data.getValue().userProperty());
        sourceCol.setCellValueFactory(data -> data.getValue().sourceProperty());
        dateCol.setCellValueFactory(data -> data.getValue().dateProperty());
        amountCol.setCellValueFactory(data -> data.getValue().amountProperty().asObject());
        categoryCol.setCellValueFactory(data -> data.getValue().categoryProperty());
        descCol.setCellValueFactory(data -> data.getValue().descriptionProperty());

        // 设置列编辑工厂
        // userCol.setCellFactory(TextFieldTableCell.forTableColumn());
        sourceCol.setCellFactory(TextFieldTableCell.forTableColumn());
        dateCol.setCellFactory(TextFieldTableCell.forTableColumn(new LocalDateStringConverter()));
        amountCol.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));

        // 为Category列使用ComboBox
        categoryCol.setCellFactory(ComboBoxTableCell.forTableColumn(predefinedCategories));
        descCol.setCellFactory(TextFieldTableCell.forTableColumn());

        // 配置编辑事件
        // userCol.setOnEditCommit(event -> {
        //     Transaction transaction = event.getRowValue();
        //     transaction.setUser(event.getNewValue());
        // });

        sourceCol.setOnEditCommit(event -> {
            Transaction transaction = event.getRowValue();
            transaction.setSource(event.getNewValue());
        });

        dateCol.setOnEditCommit(event -> {
            Transaction transaction = event.getRowValue();
            transaction.setDate(event.getNewValue());
        });

        amountCol.setOnEditCommit(event -> {
            Transaction transaction = event.getRowValue();
            transaction.setAmount(event.getNewValue());
        });

        // 为类别编辑添加特殊处理
        categoryCol.setOnEditCommit(event -> {
            Transaction transaction = event.getRowValue();
            String oldCategory = transaction.getCategory();
            String newCategory = event.getNewValue();
            String description = transaction.getDescription();

            // 先暂时设置新分类
            transaction.setCategory(newCategory);

            // 询问用户是否要保存此修改为新的分类规则
            if (!oldCategory.equals(newCategory)) {
                boolean saved = showCategoryRuleConfirmation(description, newCategory);
                if (!saved) {
                    // 如果用户选择不保存规则，恢复原始分类值
                    transaction.setCategory(oldCategory);
                    // 刷新表格显示
                    table.refresh();
                }
            }
        });

        descCol.setOnEditCommit(event -> {
            Transaction transaction = event.getRowValue();
            transaction.setDescription(event.getNewValue());
        });

        // 操作列（删除和编辑按钮）
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button deleteBtn = new Button("Delete");

            {
                deleteBtn.setOnAction(e -> {
                    Transaction transaction = getTableView().getItems().get(getIndex());

                    // 从表格删除
                    table.getItems().remove(transaction);

                    // 从文件删除
                    try {
                        new TransactionLoader().deleteTransaction(transaction);
                    } catch (IOException ex) {
                        new Alert(Alert.AlertType.ERROR, "Failed to delete file: " + ex.getMessage()).show();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteBtn);
            }
        });

        // 设置列宽
        // userCol.setPrefWidth(100);
        sourceCol.setPrefWidth(100);
        dateCol.setPrefWidth(120);
        amountCol.setPrefWidth(100);
        categoryCol.setPrefWidth(150);
        descCol.setPrefWidth(200);
        actionCol.setPrefWidth(100);

        table.getColumns().addAll(sourceCol, dateCol, amountCol, categoryCol, descCol, actionCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void layoutUI() {
        // 创建返回按钮
        Button btnBack = new Button("Dashboard");
        btnBack.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        btnBack.setOnAction(e -> returnToDashboard());

        // 搜索组件
        searchField.setPromptText("Search...");
        searchButton.setPrefWidth(80);
        HBox searchBox = new HBox(10, searchField, searchButton);
        searchBox.setPadding(new Insets(0, 10, 10, 10));

        // 添加保存更改按钮
        saveChangesButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        saveChangesButton.setPrefWidth(120);

        // 按钮栏
        HBox buttonBar = new HBox(10, loadButton, saveChangesButton);
        buttonBar.setPadding(new Insets(10));

        // 主布局容器
        VBox mainContainer = new VBox(10);
        mainContainer.setPadding(new Insets(10));

        // 按层级添加组件
        mainContainer.getChildren().addAll(
                btnBack, // 顶部返回按钮
                searchBox, // 搜索栏
                table, // 数据表格
                buttonBar // 底部按钮栏
        );

        // 设置表格自动填充
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // 设置根布局
        root.setCenter(mainContainer);
    }

    /**
     * 显示分类规则确认对话框
     * 
     * @return 如果用户选择保存规则则返回true，否则返回false
     */
    private boolean showCategoryRuleConfirmation(String description, String category) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Save Category Rule");
        alert.setHeaderText("Save New Category Rule");
        alert.setContentText("Do you want to save this categorization as a new rule?\n" +
                "Description containing: \"" + description + "\"\n" +
                "Will be categorized as: \"" + category + "\"");

        ButtonType buttonTypeYes = new ButtonType("Yes");
        ButtonType buttonTypeNo = new ButtonType("No");

        alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == buttonTypeYes) {
                // 保存新的分类规则
                CategoryRulesManager.addRule(description, category);

                // 显示确认消息
                Alert confirmation = new Alert(AlertType.INFORMATION);
                confirmation.setTitle("Rule Saved");
                confirmation.setHeaderText(null);
                confirmation.setContentText("New category rule has been saved successfully!");
                confirmation.showAndWait();
            }
        });

        return alert.getResult() == buttonTypeYes;
    }

    private void returnToDashboard() {
        Stage currentStage = (Stage) root.getScene().getWindow();
        currentStage.close();

        // 保留用户状态
        DashboardView dashboard = new DashboardView();
        try {
            dashboard.start(new Stage());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public BorderPane getView() {
        return root;
    }

    public Button getLoadButton() {
        return loadButton;
    }

    public Button getSaveChangesButton() {
        return saveChangesButton;
    }

    public TextField getSearchField() {
        return searchField;
    }

    public Button getSearchButton() {
        return searchButton;
    }

    public void updateTable(ObservableList<Transaction> data) {
        table.setItems(data);
    }

    public String getCurrentUsername(){
        return this.username;
    }
}