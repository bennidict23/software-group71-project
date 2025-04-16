package com.financetracker;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * A very simple JavaFX application with minimal dependencies.
 * Used to test if JavaFX is working correctly.
 */
public class SimpleApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Create a simple label
        Label label = new Label("Hello, JavaFX!");
        label.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // Create a button
        Button button = new Button("Click me!");
        button.setOnAction(e -> {
            label.setText("Button clicked!");
        });

        // Create layout and add components
        VBox root = new VBox(20); // 20px spacing between components
        root.setStyle("-fx-padding: 20px;");
        root.getChildren().addAll(label, button);

        // Create scene and set it on the stage
        Scene scene = new Scene(root, 300, 200);
        primaryStage.setTitle("Simple JavaFX App");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Main method to launch the application
     */
    public static void main(String[] args) {
        launch(args);
    }
}