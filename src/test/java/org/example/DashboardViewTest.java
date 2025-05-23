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
        testUser = new User("testuser", "123456");
        testUser.setMonthlyBudget(2400.0);
        testUser.setSavedAmount(1000.0);
        testUser.setShoppingBudget(600.0);
        testUser.setTransportationBudget(400.0);
        testUser.setEntertainmentBudget(200.0);
        testUser.setAnnualTarget(7000.0);
        testUser.setMonthlyTarget(600.0);
        testUser.setAnnualSavedAmount(3500.0);

        mockUserManager = mock(UserManager.class);
        DashboardView.setCurrentUser(testUser);

        dashboardView = new DashboardView();
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
    }

    @Test
    public void testCalculateRemainingBudget() {
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
        testUser.setMonthlyTarget(999.0);
        testUser.setMonthlyBudget(8888.0);
        testUser.setTransportationBudget(777.0);

        testUser.resetMonthlySettings();
        assertEquals(500.0, testUser.getMonthlyTarget(), 0.001);
        assertEquals(2000.0, testUser.getMonthlyBudget(), 0.001);
        assertEquals(500.0, testUser.getTransportationBudget(), 0.001);
    }

    @Test
    public void testUserResetAnnualSettings() {
        testUser.setAnnualTarget(9999.0);
        testUser.resetAnnualSettings();
        assertEquals(6000.0, testUser.getAnnualTarget(), 0.001);
        assertEquals(500.0, testUser.getMonthlyTarget(), 0.001);
        assertEquals(2000.0, testUser.getMonthlyBudget(), 0.001);
    }

    @Test
    public void testUpdateSavedAmounts_callsUserManagerCheckTransactionsFile() {
        dashboardView.updateSavedAmounts();
        verify(mockUserManager, atLeastOnce()).checkTransactionsFile();
    }
}