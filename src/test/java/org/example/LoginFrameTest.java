package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LoginFrameTest {

    private UserManager userManager;

    @BeforeEach
    void setUp() {
        userManager = new UserManager();
        // 确保存在一个用户
        userManager.registerUser("testuser", "password123");
    }

    @Test
    void testAuthenticateSuccess() {
        assertTrue(userManager.authenticate("testuser", "password123"),
                "Authentication should succeed for valid username/password.");
    }

    @Test
    void testAuthenticateFail() {
        assertFalse(userManager.authenticate("testuser", "wrongpass"),
                "Authentication should fail for invalid password.");
        assertFalse(userManager.authenticate("noexist", "password123"),
                "Authentication should fail for nonexistent user.");
    }

    @Test
    void testRegisterUser() {
        boolean result = userManager.registerUser("newuser", "newpass");
        assertTrue(result, "Registering a new user should succeed.");
        // 重复注册应失败
        boolean result2 = userManager.registerUser("newuser", "anotherpass");
        assertFalse(result2, "Registering an existing username should fail.");
    }

    @Test
    void testResetPassword() {
        boolean result = userManager.resetPassword("testuser", "newpass123");
        assertTrue(result, "Password reset should succeed for existing user.");
        // 用新密码登录
        assertTrue(userManager.authenticate("testuser", "newpass123"), "Should login with new password.");
        // 用旧密码登录应失败
        assertFalse(userManager.authenticate("testuser", "password123"), "Old password should not work after reset.");
    }

    @Test
    void testGetUser() {
        User user = userManager.getUser("testuser");
        assertNotNull(user, "User should not be null after registration.");
        assertEquals("testuser", user.getUsername());
        assertEquals("password123", user.getPassword());
    }
}

