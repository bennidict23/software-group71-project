package org.example;

import org.junit.jupiter.api.*;
import java.nio.file.*;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class UserManagerTest {
    private static final String TEST_USERS_FILE = "test_users.csv";
    private UserManager userManager;

    @BeforeEach
    void setUp() throws IOException {
        // 清理测试文件
        Files.deleteIfExists(Paths.get(TEST_USERS_FILE));
        // 初始化测试用 UserManager
        userManager = new UserManager(TEST_USERS_FILE);
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(Paths.get(TEST_USERS_FILE));
    }

    @Test
    void testRegisterUserAndGetUser() {
        assertTrue(userManager.registerUser("alice", "pw123"));
        User user = userManager.getUser("alice");
        assertNotNull(user);
        assertEquals("alice", user.getUsername());
        assertEquals("pw123", user.getPassword());
    }

    @Test
    void testRegisterDuplicateUser() {
        assertTrue(userManager.registerUser("bob", "pw1"));
        assertFalse(userManager.registerUser("bob", "pw2")); // 不能重复注册
    }

    @Test
    void testAuthenticateSuccessAndFail() {
        userManager.registerUser("charlie", "123456");
        assertTrue(userManager.authenticate("charlie", "123456"));
        assertFalse(userManager.authenticate("charlie", "wrongpw"));
        assertFalse(userManager.authenticate("notexist", "123456"));
    }

    @Test
    void testUpdateUserPassword() {
        userManager.registerUser("daisy", "oldpw");
        assertTrue(userManager.updateUserPassword("daisy", "newpw"));
        User user = userManager.getUser("daisy");
        assertEquals("newpw", user.getPassword());
    }

    @Test
    void testResetPassword() {
        userManager.registerUser("eve", "abc");
        assertTrue(userManager.resetPassword("eve", "xyz"));
        User user = userManager.getUser("eve");
        assertEquals("xyz", user.getPassword());
    }

    @Test
    void testSaveAndLoadUserSettings() {
        userManager.registerUser("frank", "test");
        User user = userManager.getUser("frank");
        user.setMonthlyTarget(888);
        user.setMonthlyBudget(999);
        user.setAnnualTarget(7777);
        userManager.saveUserSettings(user);

        User user2 = userManager.getUser("frank");
        assertEquals(888, user2.getMonthlyTarget());
        assertEquals(999, user2.getMonthlyBudget());
        assertEquals(7777, user2.getAnnualTarget());
    }

    @Test
    void testCheckAndResetMonthlySettings() {
        userManager.registerUser("gina", "pw");
        User user = userManager.getUser("gina");
        user.setMonthlyTarget(1234);
        user.setMonthlyBudget(5555);
        user.setCurrentYear(2022);
        user.setCurrentMonth(1);
        userManager.saveUserSettings(user);

        userManager.checkAndResetMonthlySettings(user);
        User reloaded = userManager.getUser("gina");
        assertEquals(500, reloaded.getMonthlyTarget()); // 应重置为500
        assertEquals(2000, reloaded.getMonthlyBudget()); // 应重置为2000
    }
}