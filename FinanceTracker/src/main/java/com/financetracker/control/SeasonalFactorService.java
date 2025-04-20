package com.financetracker.control;

import java.time.LocalDate;
import java.time.Month;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for calculating seasonal factors that affect spending patterns.
 * This service provides information about Chinese holidays and seasonal trends
 * to improve budget recommendations and spending forecasts.
 */
public class SeasonalFactorService {

    // 节假日信息存储
    private static final Map<Month, String[]> CHINESE_HOLIDAYS = new HashMap<>();

    static {
        // 初始化中国主要节假日信息
        CHINESE_HOLIDAYS.put(Month.JANUARY, new String[] { "New Year", "Spring Festival preparations" });
        CHINESE_HOLIDAYS.put(Month.FEBRUARY, new String[] { "Spring Festival", "Lantern Festival" });
        CHINESE_HOLIDAYS.put(Month.APRIL, new String[] { "Tomb Sweeping Day", "Labor Day preparations" });
        CHINESE_HOLIDAYS.put(Month.MAY, new String[] { "Labor Day", "Dragon Boat Festival preparations" });
        CHINESE_HOLIDAYS.put(Month.JUNE, new String[] { "Dragon Boat Festival", "Summer vacation begins" });
        CHINESE_HOLIDAYS.put(Month.AUGUST, new String[] { "Summer vacation", "Ghost Festival" });
        CHINESE_HOLIDAYS.put(Month.SEPTEMBER, new String[] { "Mid-Autumn Festival", "National Day preparations" });
        CHINESE_HOLIDAYS.put(Month.OCTOBER, new String[] { "National Day Golden Week" });
        CHINESE_HOLIDAYS.put(Month.DECEMBER, new String[] { "Christmas", "New Year preparations" });
    }

    /**
     * Gets spending adjustment factor for a specific category and date
     * 
     * @param category Expense category
     * @param date     Date to check for seasonal factors
     * @return Adjustment factor (1.0 = no change, >1.0 = increase, <1.0 = decrease)
     */
    public static double getSeasonalFactor(String category, LocalDate date) {
        Month month = date.getMonth();
        int day = date.getDayOfMonth();

        // 基础系数（无调整）
        double factor = 1.0;

        // 节假日和季节性调整
        switch (category) {
            case "Food & Dining":
                // 春节期间食品支出增加
                if (month == Month.JANUARY && day > 15 || month == Month.FEBRUARY && day < 20) {
                    factor = 1.3; // +30%
                }
                // 十一黄金周期间
                else if (month == Month.OCTOBER && day <= 7) {
                    factor = 1.2; // +20%
                }
                break;

            case "Entertainment":
                // 春节和暑假期间娱乐支出增加
                if ((month == Month.JANUARY && day > 15) ||
                        (month == Month.FEBRUARY && day < 20) ||
                        month == Month.JULY ||
                        month == Month.AUGUST) {
                    factor = 1.25; // +25%
                }
                break;

            case "Travel":
                // 黄金周期间旅行支出大幅增加
                if ((month == Month.OCTOBER && day <= 7) ||
                        (month == Month.FEBRUARY && day < 15) ||
                        (month == Month.MAY && day <= 5)) {
                    factor = 1.5; // +50%
                }
                // 暑假期间旅行支出增加
                else if (month == Month.JULY || month == Month.AUGUST) {
                    factor = 1.3; // +30%
                }
                break;

            case "Shopping":
                // 春节前后购物增加
                if ((month == Month.JANUARY && day > 10) ||
                        (month == Month.FEBRUARY && day < 10)) {
                    factor = 1.4; // +40%
                }
                // 双十一、双十二购物节
                else if ((month == Month.NOVEMBER && day == 11) ||
                        (month == Month.DECEMBER && day == 12)) {
                    factor = 1.7; // +70%
                }
                break;

            case "Utilities":
                // 冬季取暖费用增加
                if (month == Month.DECEMBER || month == Month.JANUARY ||
                        month == Month.FEBRUARY) {
                    factor = 1.2; // +20%
                }
                // 夏季空调费用增加
                else if (month == Month.JULY || month == Month.AUGUST) {
                    factor = 1.15; // +15%
                }
                break;

            case "Education":
                // 开学季教育支出增加
                if (month == Month.FEBRUARY || month == Month.SEPTEMBER) {
                    factor = 1.4; // +40%
                }
                break;
        }

        return factor;
    }

    /**
     * Checks if the given date falls on or near a Chinese holiday
     * 
     * @param date Date to check
     * @return Holiday name or null if not a holiday
     */
    public static String getHolidayName(LocalDate date) {
        Month month = date.getMonth();
        int day = date.getDayOfMonth();

        // 检查是否为节假日
        if (CHINESE_HOLIDAYS.containsKey(month)) {
            String[] holidays = CHINESE_HOLIDAYS.get(month);

            // 简单模拟主要节假日日期范围
            if (month == Month.JANUARY && day == 1) {
                return "New Year's Day";
            } else if ((month == Month.JANUARY && day > 21) ||
                    (month == Month.FEBRUARY && day < 20)) {
                return "Spring Festival";
            } else if (month == Month.APRIL && day >= 4 && day <= 6) {
                return "Tomb Sweeping Day";
            } else if (month == Month.MAY && day <= 5) {
                return "Labor Day";
            } else if (month == Month.JUNE && day >= 20 && day <= 22) {
                return "Dragon Boat Festival";
            } else if (month == Month.SEPTEMBER && day >= 10 && day <= 15) {
                return "Mid-Autumn Festival";
            } else if (month == Month.OCTOBER && day <= 7) {
                return "National Day";
            } else if (month == Month.DECEMBER && day == 25) {
                return "Christmas";
            }
        }

        return null;
    }

    /**
     * Gets a seasonal note for budget recommendations
     * 
     * @param category Expense category
     * @param date     Date to generate note for
     * @return Explanation of seasonal adjustments
     */
    public static String getSeasonalNote(String category, LocalDate date) {
        Month month = date.getMonth();

        switch (category) {
            case "Food & Dining":
                if (month == Month.JANUARY || month == Month.FEBRUARY) {
                    return "Increased for Spring Festival celebrations";
                } else if (month == Month.OCTOBER) {
                    return "Adjusted for National Day holiday";
                }
                break;

            case "Entertainment":
                if (month == Month.JANUARY || month == Month.FEBRUARY) {
                    return "Higher due to Spring Festival activities";
                } else if (month == Month.JULY || month == Month.AUGUST) {
                    return "Increased for summer leisure activities";
                }
                break;

            case "Travel":
                if (month == Month.OCTOBER) {
                    return "Golden Week travel season";
                } else if (month == Month.JULY || month == Month.AUGUST) {
                    return "Summer vacation travel period";
                } else if (month == Month.FEBRUARY) {
                    return "Spring Festival travel rush";
                }
                break;

            case "Shopping":
                if (month == Month.NOVEMBER) {
                    return "Singles Day (11.11) shopping festival";
                } else if (month == Month.DECEMBER) {
                    return "End of year and 12.12 shopping events";
                } else if (month == Month.JANUARY) {
                    return "New Year and Spring Festival shopping";
                }
                break;

            case "Utilities":
                if (month == Month.DECEMBER || month == Month.JANUARY || month == Month.FEBRUARY) {
                    return "Winter heating costs";
                } else if (month == Month.JULY || month == Month.AUGUST) {
                    return "Summer cooling expenses";
                }
                break;

            case "Education":
                if (month == Month.FEBRUARY) {
                    return "Spring semester expenses";
                } else if (month == Month.SEPTEMBER) {
                    return "Fall semester start-up costs";
                }
                break;
        }

        return "Based on typical seasonal patterns";
    }
}