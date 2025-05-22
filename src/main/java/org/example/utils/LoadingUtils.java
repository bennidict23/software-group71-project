package org.example.utils;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

/**
 * 加载工具类 - 提供加载指示器和后台任务执行功能
 */
public class LoadingUtils {

    /**
     * 在容器上添加加载指示器，并在后台执行任务
     * 
     * @param container 要添加加载指示器的容器
     * @param task      要执行的任务
     * @param callback  任务完成后的回调
     */
    public static <T> void showLoadingIndicator(StackPane container, Task<T> task, Consumer<T> callback) {
        // 确保UI操作在JavaFX应用线程上执行
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> showLoadingIndicator(container, task, callback));
            return;
        }

        // 创建加载指示器
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxSize(100, 100);

        Label loadingLabel = new Label("加载中...");
        loadingLabel.setStyle("-fx-font-size: 14px;");

        VBox loadingBox = new VBox(10, progressIndicator, loadingLabel);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setStyle("-fx-background-color: rgba(255, 255, 255, 0.8);");

        // 保存原始内容以便后续恢复
        Node[] originalContent = container.getChildren().toArray(new Node[0]);

        // 显示加载指示器
        container.getChildren().add(loadingBox);

        // 设置任务完成后的操作
        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                // 移除加载指示器
                container.getChildren().remove(loadingBox);
                // 执行回调
                if (callback != null) {
                    callback.accept(task.getValue());
                }
            });
        });

        task.setOnFailed(event -> {
            Platform.runLater(() -> {
                // 移除加载指示器
                container.getChildren().remove(loadingBox);
                // 显示错误信息
                Throwable exception = task.getException();
                System.err.println("加载失败: " + exception.getMessage());
                exception.printStackTrace();
            });
        });

        // 启动任务
        new Thread(task).start();
    }

    /**
     * 简化版本，适用于不需要返回值的操作
     */
    public static void runTaskWithLoading(StackPane container, Runnable task, Runnable onComplete) {
        Task<Void> backgroundTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                task.run();
                return null;
            }
        };

        backgroundTask.setOnSucceeded(event -> {
            if (onComplete != null) {
                Platform.runLater(onComplete);
            }
        });

        showLoadingIndicator(container, backgroundTask, null);
    }
}