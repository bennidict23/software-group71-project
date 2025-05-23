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
        assertEquals(2000.0 / 8, user.getHousingBudget());
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
        user.setHousingBudget(100);
        user.setShoppingBudget(200);
        user.setFoodDiningBudget(300);
        user.setGiftsDonationsBudget(400);
        user.setTransportationBudget(500);
        user.setEntertainmentBudget(600);
        user.setPersonalCareBudget(700);
        user.setHealthcareBudget(800);

        assertEquals(100, user.getHousingBudget());
        assertEquals(200, user.getShoppingBudget());
        assertEquals(300, user.getFoodDiningBudget());
        assertEquals(400, user.getGiftsDonationsBudget());
        assertEquals(500, user.getTransportationBudget());
        assertEquals(600, user.getEntertainmentBudget());
        assertEquals(700, user.getPersonalCareBudget());
        assertEquals(800, user.getHealthcareBudget());
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
        // 先改变相关属性为不同值
        user.setMonthlyTarget(1000);
        user.setMonthlyBudget(10000);
        user.setHousingBudget(111);
        user.setShoppingBudget(222);
        user.setFoodDiningBudget(333);
        user.setGiftsDonationsBudget(444);
        user.setTransportationBudget(555);
        user.setEntertainmentBudget(666);
        user.setPersonalCareBudget(777);
        user.setHealthcareBudget(888);

        user.resetMonthlySettings();

        assertEquals(500, user.getMonthlyTarget());
        assertEquals(2000, user.getMonthlyBudget());
        assertEquals(2000.0 / 8, user.getHousingBudget(), 0.0001);
        assertEquals(2000.0 / 8, user.getShoppingBudget(), 0.0001);
        assertEquals(2000.0 / 8, user.getFoodDiningBudget(), 0.0001);
        assertEquals(2000.0 / 8, user.getGiftsDonationsBudget(), 0.0001);
        assertEquals(2000.0 / 8, user.getTransportationBudget(), 0.0001);
        assertEquals(2000.0 / 8, user.getEntertainmentBudget(), 0.0001);
        assertEquals(2000.0 / 8, user.getPersonalCareBudget(), 0.0001);
        assertEquals(2000.0 / 8, user.getHealthcareBudget(), 0.0001);
    }

    @Test
    public void testResetAnnualSettings() {
        // 先改变属性
        user.setAnnualTarget(10000);
        user.setMonthlyTarget(888);
        user.setMonthlyBudget(8888);

        user.resetAnnualSettings();

        assertEquals(6000, user.getAnnualTarget());
        assertEquals(500, user.getMonthlyTarget());
        assertEquals(2000, user.getMonthlyBudget());
    }
}

