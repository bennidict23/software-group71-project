package org.example;

import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class GoalSettingsViewTest {

    private User testUser;
    private UserManager userManager;
    private Stage primaryStage;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "password");
        userManager = new UserManager();
        userManager.addUser(testUser.getUsername(), testUser.getPassword());
        primaryStage = new Stage();
    }

    @Test
    void testShowGoalSettings() {
        GoalSettingsView goalSettingsView = new GoalSettingsView(testUser, userManager);
        goalSettingsView.showGoalSettings(primaryStage);

        Scene scene = primaryStage.getScene();
        ProgressBar annualProgressBar = (ProgressBar) scene.lookup("#annualProgressBar");
        ProgressBar monthlyProgressBar = (ProgressBar) scene.lookup("#monthlyProgressBar");

        assertNotNull(annualProgressBar);
        assertNotNull(monthlyProgressBar);

        TextField annualField = (TextField) scene.lookup("#annualField");
        TextField monthlyField = (TextField) scene.lookup("#monthlyField");

        annualField.setText("5000");
        monthlyField.setText("1000");

        Button annualSetButton = (Button) scene.lookup("#annualSetButton");
        Button monthlySetButton = (Button) scene.lookup("#monthlySetButton");

        annualSetButton.fire();
        monthlySetButton.fire();

        assertEquals(5000.0, testUser.getAnnualTarget());
        assertEquals(1000.0, testUser.getMonthlyTarget());
    }

    @AfterEach
    void tearDown() {
        primaryStage.close();
        userManager.removeUser(testUser.getUsername());
    }
}