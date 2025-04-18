package org.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.list.TransactionViewer;

public class DashboardView extends Application {

    private static User currentUser;
    private UserManager userManager = new UserManager();

    private Label passwordLabel;
    private Label budgetLabel;
    private Label goalLabel;
    private ProgressBar progressBar;
    private Label progressLabel;

    private ComboBox<String> pageSelector;

    private FormattedInput formattedInput = null;

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        if (currentUser == null) {
            LoginFrame loginFrame = new LoginFrame();
            Stage loginStage = new Stage();
            try {
                loginFrame.start(loginStage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            primaryStage.close();
            return;
        }

        // 检查并重置月储蓄目标和月预算
        userManager.checkAndResetMonthlySettings(currentUser);

        primaryStage.setTitle("User Dashboard");

        if (formattedInput == null) {
            formattedInput = new FormattedInput();
        }

        pageSelector = new ComboBox<>();
        pageSelector.getItems().addAll("Formatted Input", "Transaction Viewer");
        pageSelector.setPromptText("Select a page...");
        pageSelector.setOnAction(e -> {
            String selectedPage = pageSelector.getValue();
            if (selectedPage == null || selectedPage.isEmpty()) {
                return;
            }
            if ("Formatted Input".equals(selectedPage)) {
                if (formattedInput != null) {
                    try {
                        Stage formattedStage = new Stage();
                        formattedInput.start(formattedStage);
                        primaryStage.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            } else if ("Transaction Viewer".equals(selectedPage)) {
                try {
                    Stage transactionStage = new Stage();
                    TransactionViewer transactionViewer = new TransactionViewer();
                    transactionStage.setTitle("交易记录查看器");
                    transactionViewer.start(transactionStage);
                    primaryStage.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            pageSelector.setValue(selectedPage);
        });
        HBox navigationBox = new HBox(pageSelector);
        navigationBox.setAlignment(Pos.TOP_CENTER);
        navigationBox.setPadding(new Insets(10));

        Label titleLabel = new Label("Dashboard");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label accountLabel = new Label("Account: " + (currentUser != null ? currentUser.getUsername() : "N/A"));
        passwordLabel = new Label("Password: " + (currentUser != null ? currentUser.getPassword() : "N/A"));
        Button btnChangePassword = new Button("Change Password");
        btnChangePassword.setOnAction(e -> showChangePasswordDialog(primaryStage));
        Button btnLogout = new Button("Logout");
        btnLogout.setOnAction(e -> logout(primaryStage));
        VBox personalInfoBox = new VBox(10, accountLabel, passwordLabel, btnChangePassword, btnLogout);
        personalInfoBox.setPadding(new Insets(10));
        personalInfoBox.setStyle("-fx-border-color: gray; -fx-border-radius: 5px; -fx-padding: 10px;");
        personalInfoBox.setPrefSize(300, 100);

        budgetLabel = new Label("Monthly Budget: $" + currentUser.getMonthlyBudget());
        goalLabel = new Label("Monthly Savings Goal: $" + currentUser.getMonthlyTarget());
        progressBar = new ProgressBar(currentUser.getSavedAmount() / currentUser.getMonthlyTarget());
        progressLabel = new Label("Savings Progress: " + (int) (currentUser.getSavedAmount() / currentUser.getMonthlyTarget() * 100) + "% (" + currentUser.getSavedAmount() + " saved)");
        Button btnSetGoal = new Button("Set Goal");
        btnSetGoal.setOnAction(e -> showGoalDialog(primaryStage));
        Button btnSetBudget = new Button("Set Budget");
        btnSetBudget.setOnAction(e -> showBudgetDialog(primaryStage));
        HBox buttonsBox = new HBox(10, btnSetGoal, btnSetBudget);
        buttonsBox.setAlignment(Pos.CENTER);
        VBox budgetBox = new VBox(10, budgetLabel, goalLabel, progressBar, progressLabel, buttonsBox);
        budgetBox.setPadding(new Insets(10));
        budgetBox.setStyle("-fx-border-color: gray; -fx-border-radius: 5px; -fx-padding: 10px;");
        budgetBox.setPrefSize(300, 250);

        HBox topBox = new HBox(20, personalInfoBox, budgetBox);
        topBox.setAlignment(Pos.TOP_CENTER);
        topBox.setPadding(new Insets(10));

        ImageView imageView = new ImageView();
        imageView.setFitHeight(200);
        imageView.setFitWidth(200);
        imageView.setStyle("-fx-border-color: gray; -fx-border-radius: 5px;");
        Label imageTitleLabel = new Label("Consumer Trend");
        imageTitleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        VBox imageBox = new VBox(10, imageTitleLabel, imageView);
        imageBox.setAlignment(Pos.TOP_CENTER);
        imageBox.setPadding(new Insets(10));
        imageBox.setStyle("-fx-border-color: gray; -fx-border-radius: 5px; -fx-padding: 10px;");
        imageBox.setPrefSize(300, 300);

        VBox mainLayout = new VBox(20, navigationBox, titleLabel, topBox, imageBox);
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setPadding(new Insets(20));

        Scene scene = new Scene(mainLayout, 1000, 800);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showChangePasswordDialog(Stage owner) {
        Stage dialog = new Stage();
        dialog.setTitle("Change Password");
        dialog.initOwner(owner);

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);

        Label newPassLabel = new Label("New Password:");
        PasswordField newPassField = new PasswordField();

        Label confirmPassLabel = new Label("Confirm Password:");
        PasswordField confirmPassField = new PasswordField();

        Button btnSubmit = new Button("Submit");

        grid.add(newPassLabel, 0, 0);
        grid.add(newPassField, 1, 0);
        grid.add(confirmPassLabel, 0, 1);
        grid.add(confirmPassField, 1, 1);
        grid.add(btnSubmit, 1, 2);

        btnSubmit.setOnAction(e -> {
            String newPass = newPassField.getText();
            String confirmPass = confirmPassField.getText();
            if (newPass.isEmpty() || confirmPass.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "Fields cannot be empty.");
            } else if (!newPass.equals(confirmPass)) {
                showAlert(Alert.AlertType.ERROR, "Error", "Passwords do not match.");
            } else {
                boolean syncResult = userManager.updateUserPassword(currentUser.getUsername(), newPass);
                if (syncResult) {
                    currentUser.setPassword(newPass);
                    passwordLabel.setText("Password: " + newPass);
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Password updated successfully.");
                    dialog.close();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to update password in file.");
                }
            }
        });

        Scene scene = new Scene(grid, 300, 200);
        dialog.setScene(scene);
        dialog.show();
    }

    private void showGoalDialog(Stage owner) {
        Stage dialog = new Stage();
        dialog.setTitle("Set Goal");
        dialog.initOwner(owner);

        ProgressBar annualProgressBar = new ProgressBar(currentUser.getAnnualSavedAmount() / currentUser.getAnnualTarget());
        annualProgressBar.setPrefWidth(550);
        int initPercent = (int) (currentUser.getAnnualSavedAmount() / currentUser.getAnnualTarget() * 100);
        Label annualProgressLabel = new Label("Annual Savings Progress: " + initPercent + "% (" + currentUser.getAnnualSavedAmount() + " saved)");
        VBox progressBox = new VBox(5, annualProgressBar, annualProgressLabel);
        progressBox.setAlignment(Pos.CENTER);
        progressBox.setPadding(new Insets(10));

        Label annualLabel = new Label("Annual Target:");
        TextField annualField = new TextField(String.valueOf(currentUser.getAnnualTarget()));
        Label annualRemarkLabel = new Label("Remark:");
        TextField annualRemarkField = new TextField();
        Button annualSetButton = new Button("SET");
        annualSetButton.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-pref-height: 40px;");

        VBox annualBox = new VBox(10, annualLabel, annualField, annualRemarkLabel, annualRemarkField, annualSetButton);
        annualBox.setPadding(new Insets(10));
        annualBox.setStyle("-fx-border-color: gray; -fx-border-radius: 5px; -fx-padding: 10px;");

        Label monthlyLabel = new Label("Monthly Target:");
        TextField monthlyField = new TextField(String.valueOf(currentUser.getMonthlyTarget()));
        Label monthlyRemarkLabel = new Label("Remark:");
        TextField monthlyRemarkField = new TextField();
        Button monthlySetButton = new Button("SET");
        monthlySetButton.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-pref-height: 40px;");

        VBox monthlyBox = new VBox(10, monthlyLabel, monthlyField, monthlyRemarkLabel, monthlyRemarkField, monthlySetButton);
        monthlyBox.setPadding(new Insets(10));
        monthlyBox.setStyle("-fx-border-color: gray; -fx-border-radius: 5px; -fx-padding: 10px;");

        HBox inputRow = new HBox(20, annualBox, monthlyBox);
        inputRow.setAlignment(Pos.CENTER);
        inputRow.setPadding(new Insets(10));

        VBox container = new VBox(10, progressBox, inputRow);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(10));

        Scene scene = new Scene(container, 600, 400);
        dialog.setScene(scene);
        dialog.show();

        annualSetButton.setOnAction(e -> {
            try {
                double newAnnual = Double.parseDouble(annualField.getText());
                if (newAnnual <= 0) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Annual value must be positive.");
                    return;
                }
                currentUser.setAnnualTarget(newAnnual);
                double ratio = currentUser.getAnnualSavedAmount() / newAnnual;
                annualProgressBar.setProgress(ratio);
                int percent = (int) (ratio * 100);
                annualProgressLabel.setText("Annual Savings Progress: " + percent + "% (" + currentUser.getAnnualSavedAmount() + " saved)");
                showAlert(Alert.AlertType.INFORMATION, "Success", "Annual target updated to: " + newAnnual);
                userManager.saveUserSettings(currentUser);
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Please enter a valid annual target number.");
            }
        });

        monthlySetButton.setOnAction(e -> {
            try {
                double newMonthly = Double.parseDouble(monthlyField.getText());
                if (newMonthly <= 0) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Monthly value must be positive.");
                    return;
                }
                currentUser.setMonthlyTarget(newMonthly);
                goalLabel.setText("Monthly Saving Goal: $" + newMonthly);
                progressBar.setProgress(currentUser.getSavedAmount() / newMonthly);
                progressLabel.setText("Savings Progress: " + (int) (currentUser.getSavedAmount() / newMonthly * 100) + "% (" + currentUser.getSavedAmount() + " saved)");
                showAlert(Alert.AlertType.INFORMATION, "Success", "Monthly target updated to: " + newMonthly);
                userManager.saveUserSettings(currentUser);
                dialog.close();
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Please enter a valid monthly target number.");
            }
        });
    }

    private void showBudgetDialog(Stage owner) {
        Stage dialog = new Stage();
        dialog.setTitle("Set Budget");
        dialog.initOwner(owner);

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setAlignment(Pos.CENTER);

        VBox shoppingBox = new VBox(10);
        shoppingBox.setAlignment(Pos.CENTER_LEFT);
        shoppingBox.setPadding(new Insets(10));
        shoppingBox.setStyle("-fx-border-color: gray; -fx-border-radius: 5px; -fx-padding: 10px;");

        Label shoppingLabel = new Label("Shopping Budget:");
        TextField shoppingField = new TextField(String.valueOf(currentUser.getShoppingBudget()));
        Label shoppingRemarkLabel = new Label("remark:");
        TextField shoppingRemarkField = new TextField();
        Button shoppingSetButton = new Button("SET");
        shoppingSetButton.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-pref-height: 40px;");

        shoppingBox.getChildren().addAll(shoppingLabel, shoppingField, shoppingRemarkLabel, shoppingRemarkField, shoppingSetButton);

        VBox transportBox = new VBox(10);
        transportBox.setAlignment(Pos.CENTER_LEFT);
        transportBox.setPadding(new Insets(10));
        transportBox.setStyle("-fx-border-color: gray; -fx-border-radius: 5px; -fx-padding: 10px;");

        Label transportLabel = new Label("Transport Budget:");
        TextField transportField = new TextField(String.valueOf(currentUser.getTransportBudget()));
        Label transportRemarkLabel = new Label("remark:");
        TextField transportRemarkField = new TextField();
        Button transportSetButton = new Button("SET");
        transportSetButton.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-pref-height: 40px;");

        transportBox.getChildren().addAll(transportLabel, transportField, transportRemarkLabel, transportRemarkField, transportSetButton);

        VBox dietBox = new VBox(10);
        dietBox.setAlignment(Pos.CENTER_LEFT);
        dietBox.setPadding(new Insets(10));
        dietBox.setStyle("-fx-border-color: gray; -fx-border-radius: 5px; -fx-padding: 10px;");

        Label dietLabel = new Label("Diet Budget:");
        TextField dietField = new TextField(String.valueOf(currentUser.getDietBudget()));
        Label dietRemarkLabel = new Label("remark:");
        TextField dietRemarkField = new TextField();
        Button dietSetButton = new Button("SET");
        dietSetButton.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-pref-height: 40px;");

        dietBox.getChildren().addAll(dietLabel, dietField, dietRemarkLabel, dietRemarkField, dietSetButton);

        VBox amusementBox = new VBox(10);
        amusementBox.setAlignment(Pos.CENTER_LEFT);
        amusementBox.setPadding(new Insets(10));
        amusementBox.setStyle("-fx-border-color: gray; -fx-border-radius: 5px; -fx-padding: 10px;");

        Label amusementLabel = new Label("Amusement Budget:");
        TextField amusementField = new TextField(String.valueOf(currentUser.getAmusementBudget()));
        Label amusementRemarkLabel = new Label("remark:");
        TextField amusementRemarkField = new TextField();
        Button amusementSetButton = new Button("SET");
        amusementSetButton.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-pref-height: 40px;");

        amusementBox.getChildren().addAll(amusementLabel, amusementField, amusementRemarkLabel, amusementRemarkField, amusementSetButton);

        grid.add(shoppingBox, 0, 0);
        grid.add(transportBox, 1, 0);
        grid.add(dietBox, 0, 1);
        grid.add(amusementBox, 1, 1);

        Scene scene = new Scene(grid, 600, 600);
        dialog.setScene(scene);
        dialog.show();

        shoppingSetButton.setOnAction(e -> {
            try {
                double newBudget = Double.parseDouble(shoppingField.getText());
                if (newBudget < 0) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Budget value must be non-negative.");
                    return;
                }
                currentUser.setShoppingBudget(newBudget);
                currentUser.setMonthlyBudget(
                        currentUser.getShoppingBudget() +
                                currentUser.getTransportBudget() +
                                currentUser.getDietBudget() +
                                currentUser.getAmusementBudget()
                );
                budgetLabel.setText("Monthly Budget: $" + currentUser.getMonthlyBudget());
                showAlert(Alert.AlertType.INFORMATION, "Success", "Shopping budget updated to: " + newBudget);
                userManager.saveUserSettings(currentUser);
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Please enter a valid budget number.");
            }
        });

        transportSetButton.setOnAction(e -> {
            try {
                double newBudget = Double.parseDouble(transportField.getText());
                if (newBudget < 0) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Budget value must be non-negative.");
                    return;
                }
                currentUser.setTransportBudget(newBudget);
                currentUser.setMonthlyBudget(
                        currentUser.getShoppingBudget() +
                                currentUser.getTransportBudget() +
                                currentUser.getDietBudget() +
                                currentUser.getAmusementBudget()
                );
                budgetLabel.setText("Monthly Budget: $" + currentUser.getMonthlyBudget());
                showAlert(Alert.AlertType.INFORMATION, "Success", "Transport budget updated to: " + newBudget);
                userManager.saveUserSettings(currentUser);
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Please enter a valid budget number.");
            }
        });

        dietSetButton.setOnAction(e -> {
            try {
                double newBudget = Double.parseDouble(dietField.getText());
                if (newBudget < 0) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Budget value must be non-negative.");
                    return;
                }
                currentUser.setDietBudget(newBudget);
                currentUser.setMonthlyBudget(
                        currentUser.getShoppingBudget() +
                                currentUser.getTransportBudget() +
                                currentUser.getDietBudget() +
                                currentUser.getAmusementBudget()
                );
                budgetLabel.setText("Monthly Budget: $" + currentUser.getMonthlyBudget());
                showAlert(Alert.AlertType.INFORMATION, "Success", "Diet budget updated to: " + newBudget);
                userManager.saveUserSettings(currentUser);
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Please enter a valid budget number.");
            }
        });

        amusementSetButton.setOnAction(e -> {
            try {
                double newBudget = Double.parseDouble(amusementField.getText());
                if (newBudget < 0) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Budget value must be non-negative.");
                    return;
                }
                currentUser.setAmusementBudget(newBudget);
                currentUser.setMonthlyBudget(
                        currentUser.getShoppingBudget() +
                                currentUser.getTransportBudget() +
                                currentUser.getDietBudget() +
                                currentUser.getAmusementBudget()
                );
                budgetLabel.setText("Monthly Budget: $" + currentUser.getMonthlyBudget());
                showAlert(Alert.AlertType.INFORMATION, "Success", "Amusement budget updated to: " + newBudget);
                userManager.saveUserSettings(currentUser);
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Please enter a valid budget number.");
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void logout(Stage primaryStage) {
        DashboardView.currentUser = null;
        primaryStage.close();
        LoginFrame loginFrame = new LoginFrame();
        Stage loginStage = new Stage();
        try {
            loginFrame.start(loginStage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}