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
        TextField transportationField = (TextField) scene.lookup("#transportationField");
        TextField shoppingField = (TextField) scene.lookup("#shoppingField");

        transportationField.setText("1500");
        shoppingField.setText("300");

        Button transportationSetButton = (Button) scene.lookup("#transportationSetButton");
        Button shoppingSetButton = (Button) scene.lookup("#shoppingSetButton");

        transportationSetButton.fire();
        shoppingSetButton.fire();

        assertEquals(1500.0, testUser.getTransportationBudget());
        assertEquals(300.0, testUser.getShoppingBudget());
    }

    @AfterEach
    void tearDown() {
        primaryStage.close();
        userManager.removeUser(testUser.getUsername());
    }
}