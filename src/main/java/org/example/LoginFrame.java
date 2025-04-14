package org.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class LoginFrame extends Application {

    private UserManager userManager = new UserManager();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Login");

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

        Button btnLogin = new Button("Login");
        btnLogin.setFont(new Font("Arial",16));
        grid.add(btnLogin, 1, 3);

        Hyperlink linkRegister = new Hyperlink("Register");
        linkRegister.setFont(new Font("Arial",16));
        grid.add(linkRegister, 1, 4);

        btnLogin.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            if (userManager.authenticate(username, password)) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Login");
                alert.setHeaderText(null);
                alert.setContentText("Login Successful!");
                alert.showAndWait();
                // 假设 authenticate 方法返回一个 User 对象，或者你可以通过 getUser(username) 获取
                User loggedInUser = userManager.getUser(username);
                DashboardView.setCurrentUser(loggedInUser);
                // 获取当前登录窗口的 Stage
                Stage loginStage = (Stage) btnLogin.getScene().getWindow();

                // 启动主页面（例如 FinanceTrackerFX 是你的主界面类）
                DashboardView mainApp = new DashboardView();
                Stage mainStage = new Stage();
                try {
                    mainApp.start(mainStage);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                // 关闭登录窗口
                loginStage.close();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Login Error");
                alert.setHeaderText(null);
                alert.setContentText("Invalid username or password.");
                alert.showAndWait();
            }
        });

        linkRegister.setOnAction(e -> {
            RegisterFrame registerView = new RegisterFrame();
            try {
                Stage registerStage = new Stage();
                registerView.start(registerStage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        Scene scene = new Scene(grid, 525, 375);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}


