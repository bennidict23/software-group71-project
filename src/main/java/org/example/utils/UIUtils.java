package org.example.utils;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

/**
 * UI工具类，提供通用的UI功能
 */
public class UIUtils {

    private static Stage loadingStage;

    /**
     * 显示加载指示器
     * 
     * @param parentWindow 父窗口
     * @param message      显示的消息
     * @return 加载指示器窗口
     */
    public static Stage showLoading(Window parentWindow, String message) {
        // 如果已经有一个加载窗口在显示，先关闭它
        hideLoading();

        // 创建进度指示器
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxSize(50, 50);

        // 创建消息标签
        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-font-size: 14px;");

        // 创建垂直布局容器
        VBox vbox = new VBox(20);
        vbox.setAlignment(Pos.CENTER);
        vbox.getChildren().addAll(progressIndicator, messageLabel);
        vbox.setStyle("-fx-background-color: white; -fx-padding: 20px; -fx-background-radius: 5;");

        // 创建场景
        Scene scene = new Scene(vbox, 250, 150);

        // 创建舞台 (窗口)
        loadingStage = new Stage();
        loadingStage.initModality(Modality.APPLICATION_MODAL); // 模态窗口
        loadingStage.initStyle(StageStyle.UNDECORATED); // 无边框窗口
        loadingStage.setScene(scene);
        loadingStage.setResizable(false);

        // 设置窗口位置
        if (parentWindow != null) {
            loadingStage.initOwner(parentWindow);
            loadingStage.setX(parentWindow.getX() + parentWindow.getWidth() / 2 - 125);
            loadingStage.setY(parentWindow.getY() + parentWindow.getHeight() / 2 - 75);
        }

        // 显示窗口
        loadingStage.show();

        return loadingStage;
    }

    /**
     * 隐藏加载指示器
     */
    public static void hideLoading() {
        if (loadingStage != null && loadingStage.isShowing()) {
            loadingStage.close();
            loadingStage = null;
        }
    }
}