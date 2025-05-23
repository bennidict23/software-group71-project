package org.example;

import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class BudgetSettingsViewTest {

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
    void testShowBudgetSettings() {
        BudgetSettingsView budgetSettingsView = new BudgetSettingsView(testUser, userManager);
        budgetSettingsView.showBudgetSettings(primaryStage);

        Scene scene = primaryStage.getScene();
        TextField housingField = (TextField) scene.lookup("#housingField");
        TextField shoppingField = (TextField) scene.lookup("#shoppingField");

        housingField.setText("1500");
        shoppingField.setText("300");

        Button housingSetButton = (Button) scene.lookup("#housingSetButton");
        Button shoppingSetButton = (Button) scene.lookup("#shoppingSetButton");

        housingSetButton.fire();
        shoppingSetButton.fire();

        assertEquals(1500.0, testUser.getHousingBudget());
        assertEquals(300.0, testUser.getShoppingBudget());
    }

    @AfterEach
    void tearDown() {
        primaryStage.close();
        userManager.removeUser(testUser.getUsername());
    }
}