package org.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.util.Callback;

public class LoginFrame extends Application {

    private UserManager userManager = new UserManager();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Login");

        VBox vBox = new VBox(20); // 20 是组件之间的间距
        vBox.setAlignment(Pos.CENTER);
        vBox.setPadding(new Insets(25, 25, 25, 25));

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);

        Label usernameLabel = new Label("Username:");
        usernameLabel.setFont(new Font("Arial", 16));
        grid.add(usernameLabel, 0, 1);
        TextField usernameField = new TextField();
        usernameField.setFont(new Font("Arial", 16));
        grid.add(usernameField, 1, 1);

        Label passwordLabel = new Label("Password:");
        passwordLabel.setFont(new Font("Arial", 16));
        grid.add(passwordLabel, 0, 2);
        PasswordField passwordField = new PasswordField();
        passwordField.setFont(new Font("Arial", 16));
        grid.add(passwordField, 1, 2);

        Button btnLogin = new Button("Login");
        btnLogin.setFont(new Font("Arial", 16));

        Button btnRegister = new Button("Register");
        btnRegister.setFont(new Font("Arial", 16));

        Hyperlink linkForgotPassword = new Hyperlink("Forgot Password");
        linkForgotPassword.setFont(new Font("Arial", 16));

        // 使用 HBox 将按钮和链接水平排列
        HBox hBox = new HBox(10); // 10 是按钮之间的间距
        hBox.setAlignment(Pos.CENTER);
        hBox.getChildren().addAll(btnLogin, btnRegister);

        vBox.getChildren().addAll(grid, hBox, linkForgotPassword);

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

        btnRegister.setOnAction(e -> {
            RegisterFrame registerView = new RegisterFrame();
            try {
                Stage registerStage = new Stage();
                registerView.start(registerStage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        linkForgotPassword.setOnAction(e -> {
            String username = usernameField.getText();
            if (username.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Forgot Password Error");
                alert.setHeaderText(null);
                alert.setContentText("Please enter your username.");
                alert.showAndWait();
                return;
            }

            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("Reset Password");
            dialog.setHeaderText("Enter a new password and confirm it");

            GridPane gridDialog = new GridPane();
            gridDialog.setHgap(10);
            gridDialog.setVgap(10);
            gridDialog.setPadding(new Insets(20, 150, 10, 10));

            Label newPasswordLabel = new Label("New Password:");
            newPasswordLabel.setFont(new Font("Arial", 14));
            gridDialog.add(newPasswordLabel, 0, 1);
            PasswordField newPasswordField = new PasswordField();
            newPasswordField.setFont(new Font("Arial", 14));
            gridDialog.add(newPasswordField, 1, 1);

            Label confirmPasswordLabel = new Label("Confirm Password:");
            confirmPasswordLabel.setFont(new Font("Arial", 14));
            gridDialog.add(confirmPasswordLabel, 0, 2);
            PasswordField confirmPasswordField = new PasswordField();
            confirmPasswordField.setFont(new Font("Arial", 14));
            gridDialog.add(confirmPasswordField, 1, 2);

            dialog.getDialogPane().setContent(gridDialog);

            ButtonType buttonTypeOk = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
            ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            dialog.getDialogPane().getButtonTypes().addAll(buttonTypeOk, buttonTypeCancel);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == buttonTypeOk) {
                    String newPassword = newPasswordField.getText();
                    String confirmPassword = confirmPasswordField.getText();
                    if (newPassword.equals(confirmPassword)) {
                        if (userManager.resetPassword(username, newPassword)) {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Password Reset");
                            alert.setHeaderText(null);
                            alert.setContentText("Password reset successful. Please login with your new password.");
                            alert.showAndWait();
                            newPasswordField.clear();
                            confirmPasswordField.clear();
                            usernameField.setText(username);
                        } else {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Password Reset Error");
                            alert.setHeaderText(null);
                            alert.setContentText("Failed to reset password.");
                            alert.showAndWait();
                        }
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Password Mismatch");
                        alert.setHeaderText(null);
                        alert.setContentText("Passwords do not match.");
                        alert.showAndWait();
                    }
                    return null;
                }
                return null;
            });

            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(primaryStage);
            dialog.showAndWait();
        });

        Scene scene = new Scene(vBox, 525, 375);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}