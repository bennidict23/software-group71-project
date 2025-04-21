package org.example.list;

import java.io.IOException;
// TransactionView.java
import java.time.LocalDate;

import org.example.DashboardView;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class TransactionView {
    private final TextField searchField = new TextField();
    private final BorderPane root = new BorderPane();
    private final TableView<Transaction> table = new TableView<>();
    private final Button loadButton = new Button("Update Data");
    private final Button searchButton = new Button("Search");
    public TransactionView() {
        configureTable();
        layoutUI();
    }

    private void configureTable() {
        TableColumn<Transaction, String> userCol = new TableColumn<>("User");
        TableColumn<Transaction, String> sourceCol = new TableColumn<>("Source");
        TableColumn<Transaction, LocalDate> dateCol = new TableColumn<>("Date");
        TableColumn<Transaction, Double> amountCol = new TableColumn<>("Amount");
        TableColumn<Transaction, String> categoryCol = new TableColumn<>("Category");
        TableColumn<Transaction, String> descCol = new TableColumn<>("Description");
        TableColumn<Transaction, Void> actionCol = new TableColumn<>("Delete");

        userCol.setCellValueFactory(data -> data.getValue().userProperty());
        sourceCol.setCellValueFactory(data -> data.getValue().sourceProperty());
        dateCol.setCellValueFactory(data -> data.getValue().dateProperty());
        amountCol.setCellValueFactory(data -> data.getValue().amountProperty().asObject());
        categoryCol.setCellValueFactory(data -> data.getValue().categoryProperty());
        descCol.setCellValueFactory(data -> data.getValue().descriptionProperty());
            // 新增操作列
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
                        new Alert(Alert.AlertType.ERROR, "文件删除失败: " + ex.getMessage()).show();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteBtn);
            }
        });

        table.getColumns().addAll(userCol, sourceCol, dateCol, amountCol, categoryCol, descCol,actionCol);
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
    
        // 主布局容器
        VBox mainContainer = new VBox(10);
        mainContainer.setPadding(new Insets(10));
        
        // 按层级添加组件
        mainContainer.getChildren().addAll(
            btnBack,       // 顶部返回按钮
            searchBox,     // 搜索栏
            table,         // 数据表格
            loadButton     // 底部加载按钮
        );
    
        // 设置表格自动填充
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    
        // 设置根布局
        root.setCenter(mainContainer);
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
    
    public TextField getSearchField() {
        return searchField;
    }
    public Button getSearchButton() {
        return searchButton;
    }

    public void updateTable(ObservableList<Transaction> data) {
        table.setItems(data);
    }
}