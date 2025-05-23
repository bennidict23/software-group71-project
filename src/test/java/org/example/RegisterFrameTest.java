package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegisterFrameTest {

    private UserManager userManager;

    @BeforeEach
    void setUp() {
        userManager = new UserManager();
    }

    @Test
    void testRegisterSuccess() {
        boolean result = userManager.registerUser("testuser1", "testpass");
        assertTrue(result, "Should register new user successfully.");
        User user = userManager.getUser("testuser1");
        assertNotNull(user, "User should exist after registration.");
        assertEquals("testuser1", user.getUsername());
        assertEquals("testpass", user.getPassword());
    }

    @Test
    void testRegisterDuplicateUser() {
        userManager.registerUser("duplicate", "123");
        boolean result = userManager.registerUser("duplicate", "456");
        assertFalse(result, "Should not register duplicate username.");
    }

    @Test
    void testRegisterWithEmptyFields() {
        // 由于UI已做校验，UserManager层面一般允许空，但你可以根据实际UserManager实现修改这里
        boolean result = userManager.registerUser("", "");
        assertTrue(result, "UserManager allows empty username/password unless逻辑特殊实现。");
    }
}

