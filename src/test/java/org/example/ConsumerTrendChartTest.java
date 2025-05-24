package org.example;

import org.junit.jupiter.api.*;
import java.io.*;
import java.nio.file.*;
import java.time.YearMonth;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ConsumerTrendChartTest {

    private User testUser;
    private Path tempDir;
    private Path csvFile;

    @BeforeEach
    void setUp() throws IOException {
        // 创建测试用户
        testUser = new User("chartuser", "secret");
        // 临时文件夹用于存放测试数据
        tempDir = Files.createTempDirectory("testdata");
        csvFile = tempDir.resolve("chartuser_transactions.csv");

        // 写入7个月的测试消费记录（header + 7条记录，部分不同月）
        try (BufferedWriter writer = Files.newBufferedWriter(csvFile)) {
            writer.write("ID,User,Source,Date,Amount,Category,Description\n");
            writer.write("1,chartuser,manual," + YearMonth.now().minusMonths(6) + "-10,100,Uncategorized,Test\n");
            writer.write("2,chartuser,manual," + YearMonth.now().minusMonths(5) + "-05,200,Uncategorized,Test\n");
            writer.write("3,chartuser,manual," + YearMonth.now().minusMonths(4) + "-15,50,Uncategorized,Test\n");
            writer.write("4,chartuser,manual," + YearMonth.now().minusMonths(2) + "-08,80,Uncategorized,Test\n");
            writer.write("5,chartuser,manual," + YearMonth.now() + "-01,30,Uncategorized,Test\n");
            // 其它用户
            writer.write("6,otheruser,manual," + YearMonth.now() + "-02,999,Uncategorized,Test\n");
        }
        // 确保当前目录下能被ConsumerTrendChart读取
        Files.copy(csvFile, Paths.get("chartuser_transactions.csv"), StandardCopyOption.REPLACE_EXISTING);
    }

    @AfterEach
    void tearDown() throws IOException {
        // 删除临时和拷贝的测试文件
        Files.deleteIfExists(csvFile);
        Files.deleteIfExists(Paths.get("chartuser_transactions.csv"));
        Files.deleteIfExists(tempDir);
    }

    @Test
    void testCalculateMonthlySpendingAggregation() {
        ConsumerTrendChart chart = new ConsumerTrendChart(testUser);
        // 用反射/包内可见性测试 private 方法（或将其包内可见/public做测试用）
        Map<YearMonth, Double> monthly = chartTestHelper(chart);

        // 断言最近7个月每个月都存在键，部分为0
        assertEquals(7, monthly.size());

        // 验证某个月的金额（比如6个月前应该有100）
        YearMonth targetMonth = YearMonth.now().minusMonths(6);
        assertEquals(100.0, monthly.get(targetMonth));

        // 验证当前月金额（比如30）
        YearMonth now = YearMonth.now();
        assertEquals(30.0, monthly.get(now));

        // 验证非本用户数据不会被统计
        YearMonth otherMonth = YearMonth.now();
        assertNotEquals(999.0, monthly.get(otherMonth));
    }

    // 利用包内可见性简化测试 private 方法
    private Map<YearMonth, Double> chartTestHelper(ConsumerTrendChart chart) {
        try {
            var method = ConsumerTrendChart.class.getDeclaredMethod("calculateMonthlySpending");
            method.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<YearMonth, Double> result = (Map<YearMonth, Double>) method.invoke(chart);
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

