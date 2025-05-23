package org.example;

import javafx.scene.control.PasswordField;
import javafx.scene.control.Button;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class ChangePasswordViewTest {

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
    void testChangePassword() {
        ChangePasswordView changePasswordView = new ChangePasswordView(testUser, userManager, primaryStage);
        changePasswordView.start(primaryStage);

        Scene scene = primaryStage.getScene();
        PasswordField newPassField = (PasswordField) scene.lookup("#newPassField");
        PasswordField confirmPassField = (PasswordField) scene.lookup("#confirmPassField");

        newPassField.setText("newpassword");
        confirmPassField.setText("newpassword");

        Button btnSubmit = (Button) scene.lookup("#btnSubmit");
        btnSubmit.fire();

        assertEquals("newpassword", testUser.getPassword());
    }

    @AfterEach
    void tearDown() {
        primaryStage.close();
        userManager.removeUser(testUser.getUsername());
    }
}