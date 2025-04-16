package org.example.list;

// TransactionViewer.java
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class TransactionViewer extends Application {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    public static void showTransactionView() {
        launch();
    }

    @Override
    public void start(Stage primaryStage) {
        // 初始化各组件
        TransactionView view = new TransactionView();
        TransactionLoader loader = new TransactionLoader();
        new TransactionController(view, loader);

        // 创建场景
        Scene scene = new Scene(view.getView(), WIDTH, HEIGHT);
        
        // 配置舞台
        primaryStage.setTitle("交易记录查看器");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
