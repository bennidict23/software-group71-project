package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    private User user;

    @BeforeEach
    public void setUp() {
        user = new User("testuser", "123456");
    }

    @Test
    public void testConstructorInitializesFieldsCorrectly() {
        assertEquals("testuser", user.getUsername());
        assertEquals("123456", user.getPassword());
        assertEquals(6000, user.getAnnualTarget());
        assertEquals(500, user.getMonthlyTarget());
        assertEquals(2000, user.getMonthlyBudget());
        assertEquals(2000.0 / 4, user.getTransportationBudget());
        assertEquals(3000, user.getSavedAmount());
        assertEquals(36000, user.getAnnualSavedAmount());
        assertEquals(LocalDate.now().getYear(), user.getCurrentYear());
        assertEquals(LocalDate.now().getMonthValue(), user.getCurrentMonth());
    }

    @Test
    public void testPasswordSetterGetter() {
        user.setPassword("newpass");
        assertEquals("newpass", user.getPassword());
    }

    @Test
    public void testAnnualTargetSetterGetter() {
        user.setAnnualTarget(8000);
        assertEquals(8000, user.getAnnualTarget());
    }

    @Test
    public void testMonthlyTargetSetterGetter() {
        user.setMonthlyTarget(999);
        assertEquals(999, user.getMonthlyTarget());
    }

    @Test
    public void testMonthlyBudgetSetterGetter() {
        user.setMonthlyBudget(4000);
        assertEquals(4000, user.getMonthlyBudget());
    }

    @Test
    public void testAllBudgetsSetterGetter() {
        user.setTransportationBudget(100);
        user.setShoppingBudget(200);
        user.setOtherBudget(300);
        user.setEntertainmentBudget(400);

        assertEquals(100, user.getTransportationBudget());
        assertEquals(200, user.getShoppingBudget());
        assertEquals(300, user.getOtherBudget());
        assertEquals(400, user.getEntertainmentBudget());
    }

    @Test
    public void testSavedAmountSetterGetter() {
        user.setSavedAmount(1234.56);
        assertEquals(1234.56, user.getSavedAmount());
    }

    @Test
    public void testAnnualSavedAmountSetterGetter() {
        user.setAnnualSavedAmount(5678.9);
        assertEquals(5678.9, user.getAnnualSavedAmount());
    }

    @Test
    public void testCurrentYearAndMonthSetterGetter() {
        user.setCurrentYear(2025);
        user.setCurrentMonth(7);
        assertEquals(2025, user.getCurrentYear());
        assertEquals(7, user.getCurrentMonth());
    }

    @Test
    public void testResetMonthlySettings() {
        user.setMonthlyTarget(1000);
        user.setMonthlyBudget(10000);
        user.setTransportationBudget(111);
        user.setShoppingBudget(222);
        user.setOtherBudget(333);
        user.setEntertainmentBudget(444);

        user.resetMonthlySettings();

        assertEquals(500, user.getMonthlyTarget());
        assertEquals(2000, user.getMonthlyBudget());
        assertEquals(500.0, user.getTransportationBudget(), 0.0001);
        assertEquals(500.0, user.getShoppingBudget(), 0.0001);
        assertEquals(500.0, user.getOtherBudget(), 0.0001);
        assertEquals(500.0, user.getEntertainmentBudget(), 0.0001);
    }

    @Test
    public void testResetAnnualSettings() {
        user.setAnnualTarget(10000);
        user.setMonthlyTarget(888);
        user.setMonthlyBudget(8888);

        user.resetAnnualSettings();

        assertEquals(6000, user.getAnnualTarget());
        assertEquals(500, user.getMonthlyTarget());
        assertEquals(2000, user.getMonthlyBudget());
    }
}