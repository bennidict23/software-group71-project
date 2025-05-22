package org.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class RegisterFrame extends Application {

    private UserManager userManager = new UserManager();

    @Override
    public void start(Stage stage) {
        stage.setTitle("Register");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Label usernameLabel = new Label("Username:");
        usernameLabel.setFont(new Font("Arial",16));
        grid.add(usernameLabel, 0, 1);
        TextField usernameField = new TextField();
        usernameField.setFont(new Font("Arial",16));
        grid.add(usernameField, 1, 1);

        Label passwordLabel = new Label("Password:");
        passwordLabel.setFont(new Font("Arial",16));
        grid.add(passwordLabel, 0, 2);
        PasswordField passwordField = new PasswordField();
        passwordField.setFont(new Font("Arial",16));
        grid.add(passwordField, 1, 2);

        Label confirmPasswordLabel = new Label("Confirm Password:");
        confirmPasswordLabel.setFont(new Font("Arial",16));
        grid.add(confirmPasswordLabel, 0, 3);
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setFont(new Font("Arial",16));
        grid.add(confirmPasswordField, 1, 3);

        Button btnRegister = new Button("Register");
        btnRegister.setFont(new Font("Arial",16));
        grid.add(btnRegister, 1, 4);

        btnRegister.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "Username and password cannot be empty.");
                return;
            }
            if (!password.equals(confirmPassword)) {
                showAlert(Alert.AlertType.ERROR, "Error", "Passwords do not match.");
                return;
            }

            boolean success = userManager.registerUser(username, password);
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Registration", "Registration successful!");
                stage.close();
                // 打开登录页面
                LoginFrame loginView = new LoginFrame();
                Stage loginStage = new Stage();
                try {
                    loginView.start(loginStage);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Registration Error", "Username already exists.");
            }
        });

        Scene scene = new Scene(grid, 525, 375);
        stage.setScene(scene);
        stage.show();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message){
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}