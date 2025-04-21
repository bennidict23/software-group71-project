package org.example.analysis;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

// 添加JSON库导入，需要确保pom.xml中包含对应依赖
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * AIModelService - 提供AI模型服务调用功能，用于支出预测和预算推荐
 */
public class AIModelService {

    // API密钥和端点
    private static final String HUGGING_FACE_API_KEY = System.getenv("HUGGING_FACE_API_KEY");
    private static final String OPENAI_API_KEY = System.getenv("OPENAI_API_KEY");
    private static final String HUGGING_FACE_API_URL = "https://api-inference.huggingface.co/models/";
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/completions";

    // 使用的模型名称
    private static final String FORECAST_MODEL = "facebook/bart-large";
    private static final String RECOMMENDATION_MODEL = "gpt-3.5-turbo";

    // HTTP客户端
    private static final HttpClient httpClient = HttpClient.newBuilder().build();

    // 随机数生成器（用于模拟数据）
    private static final Random random = new Random();

    /**
     * 获取支出预测
     * 
     * @param historicalData 历史支出数据
     * @param categories     支出类别
     * @param months         预测月数
     * @return 预测结果
     */
    public static Map<String, Map<String, Double>> getSpendingForecast(
            Map<String, Map<Month, Double>> historicalData,
            String[] categories,
            int months) {

        // 尝试调用Hugging Face API
        Map<String, Map<String, Double>> predictions = callHuggingFaceAPI(historicalData, categories, months);

        // 如果API调用失败，尝试OpenAI API
        if (predictions == null) {
            predictions = callOpenAIAPI(historicalData, categories, months);
        }

        // 如果两种API都失败，使用模拟数据
        if (predictions == null) {
            predictions = generateMockForecastData(historicalData, categories, months);
        }

        return predictions;
    }

    /**
     * 获取预算建议
     * 
     * @param monthlyAverages          月平均支出
     * @param applySeasonalAdjustments 是否应用季节性调整
     * @return 预算建议
     */
    public static Map<String, Double> getBudgetRecommendations(
            Map<String, Double> monthlyAverages,
            boolean applySeasonalAdjustments) {

        Map<String, Double> recommendations = new HashMap<>();

        // 尝试调用API获取预算建议
        recommendations = callRecommendationAPI(monthlyAverages, applySeasonalAdjustments);

        // 如果API调用失败，生成本地预算建议
        if (recommendations == null || recommendations.isEmpty()) {
            recommendations = generateLocalRecommendations(monthlyAverages, applySeasonalAdjustments);
        }

        return recommendations;
    }

    /**
     * 调用Hugging Face API进行预测
     */
    private static Map<String, Map<String, Double>> callHuggingFaceAPI(
            Map<String, Map<Month, Double>> historicalData,
            String[] categories,
            int months) {

        if (HUGGING_FACE_API_KEY == null || HUGGING_FACE_API_KEY.isEmpty()) {
            System.out.println("Hugging Face API密钥未配置，跳过API调用");
            return null;
        }

        try {
            // 准备请求数据
            JSONObject requestData = new JSONObject();
            requestData.put("inputs", formatHistoricalDataForModel(historicalData));

            JSONObject parameters = new JSONObject();
            parameters.put("months", months);

            // 将类别数组转换为列表
            StringBuilder categoriesStr = new StringBuilder();
            for (String category : categories) {
                if (categoriesStr.length() > 0) {
                    categoriesStr.append(",");
                }
                categoriesStr.append(category);
            }
            parameters.put("categories", categoriesStr.toString());

            requestData.put("parameters", parameters);

            // 创建HTTP请求
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(HUGGING_FACE_API_URL + FORECAST_MODEL))
                    .header("Authorization", "Bearer " + HUGGING_FACE_API_KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestData.toString()))
                    .build();

            // 发送请求并获取响应
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // 处理响应
            if (response.statusCode() == 200) {
                return parseModelResponse(response.body(), categories, months);
            } else {
                System.out.println("Hugging Face API调用失败: " + response.statusCode() + " " + response.body());
                return null;
            }

        } catch (URISyntaxException | IOException | InterruptedException e) {
            System.out.println("Hugging Face API调用异常: " + e.getMessage());
            return null;
        }
    }

    /**
     * 调用OpenAI API进行预测
     */
    private static Map<String, Map<String, Double>> callOpenAIAPI(
            Map<String, Map<Month, Double>> historicalData,
            String[] categories,
            int months) {

        if (OPENAI_API_KEY == null || OPENAI_API_KEY.isEmpty()) {
            System.out.println("OpenAI API密钥未配置，跳过API调用");
            return null;
        }

        try {
            // 准备提示文本
            String prompt = "Based on the following historical spending data:\n" +
                    formatHistoricalDataForModel(historicalData) +
                    "\n\nPredict the spending for the next " + months + " months for categories: " +
                    String.join(", ", categories) + "\n\nFormat the response as JSON with categories and months.";

            // 创建请求数据
            JSONObject requestData = new JSONObject();
            requestData.put("model", RECOMMENDATION_MODEL);
            requestData.put("prompt", prompt);
            requestData.put("max_tokens", 500);
            requestData.put("temperature", 0.7);

            // 创建HTTP请求
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(OPENAI_API_URL))
                    .header("Authorization", "Bearer " + OPENAI_API_KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestData.toString()))
                    .build();

            // 发送请求并获取响应
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // 处理响应
            if (response.statusCode() == 200) {
                return parseOpenAIResponse(response.body(), categories, months);
            } else {
                System.out.println("OpenAI API调用失败: " + response.statusCode() + " " + response.body());
                return null;
            }

        } catch (Exception e) {
            System.out.println("OpenAI API调用异常: " + e.getMessage());
            return null;
        }
    }

    /**
     * 调用预算建议API
     */
    private static Map<String, Double> callRecommendationAPI(
            Map<String, Double> monthlyAverages,
            boolean applySeasonalAdjustments) {

        if (OPENAI_API_KEY == null || OPENAI_API_KEY.isEmpty()) {
            System.out.println("预算建议API密钥未配置，跳过API调用");
            return null;
        }

        try {
            // 准备提示文本
            StringBuilder promptBuilder = new StringBuilder();
            promptBuilder.append("Based on these monthly average expenses:\n\n");

            for (Map.Entry<String, Double> entry : monthlyAverages.entrySet()) {
                promptBuilder.append(entry.getKey()).append(": ¥").append(String.format("%.2f", entry.getValue()))
                        .append("\n");
            }

            promptBuilder.append("\nProvide budget recommendations for each category. ");

            if (applySeasonalAdjustments) {
                promptBuilder.append("Consider seasonal factors for the current month (")
                        .append(LocalDate.now().getMonth())
                        .append("). ");
            }

            promptBuilder.append(
                    "Format your response as a JSON object with category names as keys and recommended budget amounts as values.");

            // 创建请求数据
            JSONObject requestData = new JSONObject();
            requestData.put("model", RECOMMENDATION_MODEL);
            requestData.put("prompt", promptBuilder.toString());
            requestData.put("max_tokens", 500);
            requestData.put("temperature", 0.7);

            // 创建HTTP请求
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(OPENAI_API_URL))
                    .header("Authorization", "Bearer " + OPENAI_API_KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestData.toString()))
                    .build();

            // 发送请求并获取响应
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // 处理响应
            if (response.statusCode() == 200) {
                // 解析JSON响应
                JSONParser parser = new JSONParser();
                try {
                    Object jsonObject = parser.parse(response.body());
                    if (jsonObject instanceof JSONObject) {
                        JSONObject jsonResponse = (JSONObject) jsonObject;
                        Object choices = jsonResponse.get("choices");

                        // 假设choices是一个列表，我们取第一个元素
                        if (choices instanceof java.util.List) {
                            Object firstChoice = ((java.util.List<?>) choices).get(0);
                            if (firstChoice instanceof JSONObject) {
                                JSONObject choiceObj = (JSONObject) firstChoice;
                                String predictionsText = (String) choiceObj.get("text");

                                // 尝试解析预测文本为JSON
                                try {
                                    Object budgetObj = parser.parse(predictionsText);
                                    if (budgetObj instanceof JSONObject) {
                                        JSONObject budgetJson = (JSONObject) budgetObj;

                                        Map<String, Double> recommendations = new HashMap<>();
                                        for (Object key : budgetJson.keySet()) {
                                            String category = (String) key;
                                            Object value = budgetJson.get(key);

                                            if (value instanceof Number) {
                                                recommendations.put(category, ((Number) value).doubleValue());
                                            } else if (value instanceof String) {
                                                try {
                                                    recommendations.put(category, Double.parseDouble((String) value));
                                                } catch (NumberFormatException e) {
                                                    System.out.println("无法将值转换为数字: " + value);
                                                }
                                            }
                                        }

                                        return recommendations;
                                    }
                                } catch (ParseException e) {
                                    System.out.println("解析API响应为JSON失败: " + e.getMessage());
                                }
                            }
                        }
                    }
                } catch (ParseException e) {
                    System.out.println("解析API响应失败: " + e.getMessage());
                }

                return null;
            } else {
                System.out.println("预算建议API调用失败: " + response.statusCode() + " " + response.body());
                return null;
            }

        } catch (Exception e) {
            System.out.println("预算建议API调用异常: " + e.getMessage());
            return null;
        }
    }

    /**
     * 格式化历史数据为模型输入
     */
    private static String formatHistoricalDataForModel(Map<String, Map<Month, Double>> historicalData) {
        StringBuilder sb = new StringBuilder();

        for (Map.Entry<String, Map<Month, Double>> categoryEntry : historicalData.entrySet()) {
            String category = categoryEntry.getKey();
            sb.append(category).append(":\n");

            for (Map.Entry<Month, Double> monthEntry : categoryEntry.getValue().entrySet()) {
                sb.append("  ").append(monthEntry.getKey()).append(": ¥")
                        .append(String.format("%.2f", monthEntry.getValue())).append("\n");
            }

            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * 解析模型响应
     */
    private static Map<String, Map<String, Double>> parseModelResponse(
            String responseBody,
            String[] categories,
            int months) {
        // 这里应该实现解析模型返回的JSON响应
        // 简化起见，这里直接返回null，使用模拟数据
        return null;
    }

    /**
     * 解析OpenAI响应
     */
    private static Map<String, Map<String, Double>> parseOpenAIResponse(
            String responseText,
            String[] categories,
            int months) {
        // 这里应该实现解析OpenAI返回的文本响应
        // 简化起见，这里直接返回null，使用模拟数据
        return null;
    }

    /**
     * 生成模拟预测数据
     */
    private static Map<String, Map<String, Double>> generateMockForecastData(
            Map<String, Map<Month, Double>> historicalData,
            String[] categories,
            int monthsToPredict) {

        Map<String, Map<String, Double>> predictions = new HashMap<>();
        Month currentMonth = LocalDate.now().getMonth();

        // 为每个类别生成预测
        for (String category : categories) {
            Map<String, Double> categoryPredictions = new HashMap<>();

            // 计算基准金额（基于历史数据或随机生成）
            double baseAmount;

            // 获取此类别的历史数据
            Map<Month, Double> historyForCategory = historicalData.get(category);
            if (historyForCategory != null && !historyForCategory.isEmpty()) {
                // 计算历史平均值
                double sum = 0;
                for (Double amount : historyForCategory.values()) {
                    sum += amount;
                }
                baseAmount = sum / historyForCategory.size();
            } else {
                // 没有历史数据，使用随机基准金额
                baseAmount = 500 + random.nextDouble() * 1000;
            }

            // 为未来几个月生成预测
            for (int i = 0; i < monthsToPredict; i++) {
                // 计算预测月份
                Month targetMonth = Month.values()[(currentMonth.ordinal() + i) % 12];

                // 添加随机波动（±15%）
                double randomFactor = 0.85 + (random.nextDouble() * 0.3);

                // 添加季节性因素
                double seasonalFactor = getSeasonalFactor(targetMonth, category);

                // 计算最终预测金额
                double predictedAmount = baseAmount * randomFactor * seasonalFactor;

                // 向上取整到整数
                predictedAmount = Math.ceil(predictedAmount);

                // 添加到预测结果
                categoryPredictions.put(targetMonth.name(), predictedAmount);
            }

            predictions.put(category, categoryPredictions);
        }

        return predictions;
    }

    /**
     * 根据月份和类别获取季节性因子
     */
    private static double getSeasonalFactor(Month month, String category) {
        // 基于月份和类别的季节性因子表
        Map<String, Map<String, Double>> seasonalFactors = new HashMap<>();

        // 为常见类别定义季节性因子
        Map<String, Double> groceriesFactors = new HashMap<>();
        groceriesFactors.put(month.name(), 1.05); // 春节前采购增加
        groceriesFactors.put(Month.FEBRUARY.name(), 0.95); // 春节期间消费略降
        groceriesFactors.put(Month.JUNE.name(), 1.1); // 夏季水果增加
        groceriesFactors.put(Month.DECEMBER.name(), 1.15); // 年末购物增加
        seasonalFactors.put("Groceries", groceriesFactors);

        Map<String, Double> diningFactors = new HashMap<>();
        diningFactors.put(Month.FEBRUARY.name(), 1.2); // 春节聚餐增加
        diningFactors.put(Month.MAY.name(), 1.1); // 五一假期
        diningFactors.put(Month.OCTOBER.name(), 1.15); // 国庆假期
        diningFactors.put(Month.DECEMBER.name(), 1.2); // 圣诞/元旦聚餐
        seasonalFactors.put("Dining", diningFactors);

        Map<String, Double> travelFactors = new HashMap<>();
        travelFactors.put(Month.JANUARY.name(), 0.9); // 春节前旅行减少
        travelFactors.put(Month.FEBRUARY.name(), 1.3); // 春节旅行高峰
        travelFactors.put(Month.JULY.name(), 1.4); // 暑假旅行高峰
        travelFactors.put(Month.AUGUST.name(), 1.4); // 暑假旅行高峰
        travelFactors.put(Month.OCTOBER.name(), 1.3); // 国庆旅行高峰
        seasonalFactors.put("Travel", travelFactors);

        Map<String, Double> shoppingFactors = new HashMap<>();
        shoppingFactors.put(Month.JANUARY.name(), 1.1); // 新年购物
        shoppingFactors.put(Month.JUNE.name(), 1.15); // 618购物节
        shoppingFactors.put(Month.NOVEMBER.name(), 1.3); // 双11购物节
        shoppingFactors.put(Month.DECEMBER.name(), 1.2); // 圣诞/元旦购物
        seasonalFactors.put("Shopping", shoppingFactors);

        Map<String, Double> utilitiesFactors = new HashMap<>();
        utilitiesFactors.put(Month.JANUARY.name(), 1.2); // 冬季取暖
        utilitiesFactors.put(Month.FEBRUARY.name(), 1.2); // 冬季取暖
        utilitiesFactors.put(Month.JULY.name(), 1.15); // 夏季空调
        utilitiesFactors.put(Month.AUGUST.name(), 1.15); // 夏季空调
        seasonalFactors.put("Utilities", utilitiesFactors);

        // 获取指定类别的季节性因子
        Map<String, Double> categoryFactors = seasonalFactors.get(category);

        if (categoryFactors != null) {
            // 获取指定月份的因子
            Double factor = categoryFactors.get(month.name());
            if (factor != null) {
                return factor;
            }
        }

        // 默认返回1.0（无季节性影响）
        return 1.0;
    }

    /**
     * 生成本地预算建议
     */
    private static Map<String, Double> generateLocalRecommendations(
            Map<String, Double> monthlyAverages,
            boolean applySeasonalAdjustments) {

        Map<String, Double> recommendations = new HashMap<>();
        Month currentMonth = LocalDate.now().getMonth();

        // 预算调整因子
        Map<String, Double> budgetFactors = new HashMap<>();
        // 基本生活必需品维持原样
        budgetFactors.put("Groceries", 1.0);
        budgetFactors.put("Housing", 1.0);
        budgetFactors.put("Utilities", 1.0);
        budgetFactors.put("Healthcare", 1.0);
        // 可控支出适度压缩
        budgetFactors.put("Transportation", 0.95);
        budgetFactors.put("Education", 1.0);
        budgetFactors.put("Entertainment", 0.8);
        budgetFactors.put("Dining", 0.85);
        budgetFactors.put("Shopping", 0.75);
        budgetFactors.put("Travel", 0.7);

        for (Map.Entry<String, Double> entry : monthlyAverages.entrySet()) {
            String category = entry.getKey();
            double average = entry.getValue();

            // 应用预算因子
            double factor = budgetFactors.getOrDefault(category, 0.9); // 默认为90%

            // 应用季节性调整
            if (applySeasonalAdjustments) {
                factor *= getSeasonalFactor(currentMonth, category);
            }

            // 计算建议预算
            double recommended = average * factor;

            // 四舍五入到整数
            recommended = Math.round(recommended);

            // 保存建议预算
            recommendations.put(category, recommended);
        }

        return recommendations;
    }

    /**
     * 获取支出预测 - 简化版本，接收字符串键的Map（如YearMonth.toString()）
     * 
     * @param historicalData 历史支出数据，键为字符串形式的年月
     * @param months         预测月数
     * @return 预测结果，键为字符串形式的年月
     */
    public static Map<String, Double> getForecastSpending(
            Map<String, Double> historicalData,
            int months) {

        // 生成输出结果
        Map<String, Double> result = new LinkedHashMap<>();

        // 获取当前月
        LocalDate currentDate = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(currentDate);

        // 从历史数据计算平均值
        double total = 0;
        for (Double value : historicalData.values()) {
            total += value;
        }
        double average = total / Math.max(1, historicalData.size());

        // 生成简单预测
        for (int i = 1; i <= months; i++) {
            YearMonth forecastMonth = currentMonth.plusMonths(i);
            String monthKey = forecastMonth.toString();

            // 基本预测值（历史平均值）
            double forecastAmount = average;

            // 添加随机波动 (±10%)
            double randomFactor = 0.9 + (random.nextDouble() * 0.2);

            // 添加季节性因素
            int monthValue = forecastMonth.getMonthValue();
            double seasonalFactor = 1.0;

            // 春节期间 (1-2月)
            if (monthValue == 1 || monthValue == 2) {
                seasonalFactor = 1.2; // 增加20%
            }
            // 国庆节 (10月)
            else if (monthValue == 10) {
                seasonalFactor = 1.15; // 增加15%
            }
            // 618购物节 (6月)
            else if (monthValue == 6) {
                seasonalFactor = 1.1; // 增加10%
            }
            // 双11购物节 (11月)
            else if (monthValue == 11) {
                seasonalFactor = 1.25; // 增加25%
            }

            // 计算最终预测金额
            forecastAmount = forecastAmount * randomFactor * seasonalFactor;

            // 四舍五入到两位小数
            forecastAmount = Math.round(forecastAmount * 100) / 100.0;

            // 添加到结果集
            result.put(monthKey, forecastAmount);
        }

        return result;
    }
}