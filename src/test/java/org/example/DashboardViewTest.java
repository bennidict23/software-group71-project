package org.example;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.mockito.Mockito;

public class DashboardViewTest {

    private DashboardView dashboardView;
    private User testUser;
    private UserManager mockUserManager;

    @BeforeEach
    public void setUp() {
        // 创建模拟用户
        testUser = new User("testuser", "123456");
        // 自定义预算、目标、储蓄金额等
        testUser.setMonthlyBudget(2400.0);
        testUser.setSavedAmount(1000.0);
        testUser.setShoppingBudget(600.0);
        testUser.setFoodDiningBudget(300.0);
        testUser.setTransportationBudget(400.0);
        testUser.setEntertainmentBudget(200.0);
        testUser.setAnnualTarget(7000.0);
        testUser.setMonthlyTarget(600.0);
        testUser.setAnnualSavedAmount(3500.0);

        // mock UserManager
        mockUserManager = mock(UserManager.class);

        // 设置当前用户
        DashboardView.setCurrentUser(testUser);

        // 创建 DashboardView 实例，并注入 mockUserManager
        dashboardView = new DashboardView();
        // 反射注入 userManager
        try {
            var field = DashboardView.class.getDeclaredField("userManager");
            field.setAccessible(true);
            field.set(dashboardView, mockUserManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterEach
    public void tearDown() {
        DashboardView.setCurrentUser(null);
    }

    @Test
    public void testCurrentUserStatic() {
        assertEquals("testuser", DashboardView.getCurrentUser().getUsername());
        assertEquals("123456", DashboardView.getCurrentUser().getPassword());
    }

    @Test
    public void testSetImportDone() {
        DashboardView.setImportDone(true);
        // 无法直接断言 private 变量，但后续功能可间接判断。
    }

    @Test
    public void testCalculateRemainingBudget() {
        // monthlyBudget=2400, monthlyExpenses=1350, 剩余=1050
        when(mockUserManager.getMonthlyTotalExpenses(testUser)).thenReturn(1350.0);
        double remaining = dashboardView.calculateRemainingBudget(testUser);
        assertEquals(1050.0, remaining, 0.001);
    }

    @Test
    public void testCalculateRemainingBudget_nullUser() {
        double remaining = dashboardView.calculateRemainingBudget(null);
        assertEquals(0.0, remaining, 0.001);
    }

    @Test
    public void testSetAndGetCurrentUser() {
        User another = new User("another", "abcde");
        DashboardView.setCurrentUser(another);
        assertEquals("another", DashboardView.getCurrentUser().getUsername());
        assertEquals("abcde", DashboardView.getCurrentUser().getPassword());
    }

    @Test
    public void testUserResetMonthlySettings() {
        // 修改为非默认
        testUser.setMonthlyTarget(999.0);
        testUser.setMonthlyBudget(8888.0);
        testUser.setFoodDiningBudget(777.0);

        testUser.resetMonthlySettings();
        assertEquals(500.0, testUser.getMonthlyTarget(), 0.001);
        assertEquals(2000.0, testUser.getMonthlyBudget(), 0.001);
        assertEquals(250.0, testUser.getFoodDiningBudget(), 0.001); // 2000/8
    }

    @Test
    public void testUserResetAnnualSettings() {
        testUser.setAnnualTarget(9999.0);
        testUser.resetAnnualSettings();
        assertEquals(6000.0, testUser.getAnnualTarget(), 0.001);
        // 同时月设置也会重置
        assertEquals(500.0, testUser.getMonthlyTarget(), 0.001);
        assertEquals(2000.0, testUser.getMonthlyBudget(), 0.001);
    }

    @Test
    public void testUpdateSavedAmounts_callsUserManagerCheckTransactionsFile() {
        dashboardView.updateSavedAmounts();
        verify(mockUserManager, atLeastOnce()).checkTransactionsFile();
    }
}


