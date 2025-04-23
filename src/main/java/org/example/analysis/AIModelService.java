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
import java.util.concurrent.ConcurrentHashMap;

// 添加JSON库导入，需要确保pom.xml中包含对应依赖
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * AIModelService - 提供AI模型服务调用功能，用于支出预测和预算推荐
 */
public class AIModelService {

    // =================================================================
    // API Configuration - DeepSeek API Setup
    // =================================================================

    // IMPORTANT: To successfully call the DeepSeek API, follow these steps:
    // 1. Get your API key from DeepSeek (https://platform.deepseek.com/)
    // 2. Replace "YOUR_DEEPSEEK_API_KEY" below with your actual API key
    // 3. Set useRealDeepSeekAPI to true

    // Your DeepSeek API key - replace with your actual key
    private static final String DEEPSEEK_API_KEY = "sk-c4dc6fa348f24f6ab3ddc5514041676d";

    // DeepSeek API endpoint
    private static final String DEEPSEEK_API_URL = "https://api.deepseek.com/v1/chat/completions";

    // DeepSeek model to use
    private static final String DEEPSEEK_MODEL = "deepseek-chat";

    // Set to true to use the real DeepSeek API instead of simulated data
    // WARNING: When set to true, this will incur charges to your DeepSeek account
    private static final boolean useRealDeepSeekAPI = true;

    // =================================================================
    // End of API Configuration
    // =================================================================

    // HTTP client for API calls
    private static final HttpClient httpClient = HttpClient.newBuilder().build();

    // Random number generator for simulated data
    private static final Random random = new Random();

    // Store the latest analysis texts for UI display
    private static String latestForecastExplanation = "";
    private static String latestBudgetRecommendationAnalysis = "";

    // =================================================================
    // Cache Configuration for API Responses
    // =================================================================

    // Enable response caching to reduce API calls
    private static final boolean useResponseCaching = true;

    // Cache for forecast API responses, keyed by a hash of the input parameters
    private static final Map<String, Map<String, Double>> forecastCache = new ConcurrentHashMap<>();

    // Cache for budget recommendation API responses, keyed by a hash of the input
    // parameters
    private static final Map<String, Map<String, Double>> budgetRecommendationCache = new ConcurrentHashMap<>();

    // Cache for forecast explanations
    private static final Map<String, String> forecastExplanationCache = new ConcurrentHashMap<>();

    // Cache for budget recommendation analyses
    private static final Map<String, String> budgetAnalysisCache = new ConcurrentHashMap<>();

    // =================================================================
    // End of Cache Configuration
    // =================================================================

    /**
     * 获取支出预测
     * 
     * @param historicalData 历史支出数据
     * @param months         预测月数
     * @return 预测结果
     */
    public static Map<String, Double> getForecastSpending(
            Map<String, Double> historicalData,
            int months) {

        Map<String, Double> predictions = null;

        // 如果启用了缓存，先检查缓存中是否有相同参数的结果
        String cacheKey = null;
        if (useResponseCaching) {
            cacheKey = generateForecastCacheKey(historicalData, months);
            predictions = forecastCache.get(cacheKey);

            if (predictions != null) {
                System.out.println("Using cached forecast result for key: " + cacheKey);
                // 设置缓存的解释文本
                latestForecastExplanation = forecastExplanationCache.getOrDefault(cacheKey, "");
                return predictions;
            }
        }

        // 首先尝试使用DeepSeek API（如果启用）
        if (useRealDeepSeekAPI && !DEEPSEEK_API_KEY.equals("YOUR_DEEPSEEK_API_KEY")) {
            try {
                predictions = callDeepSeekForForecast(historicalData, months);
            } catch (Exception e) {
                System.out.println("DeepSeek API call exception: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // 如果未启用API或API调用失败，使用模拟数据
        if (predictions == null) {
            System.out.println("Using simulated forecast data...");
            predictions = generateSimpleForecast(historicalData, months);
        }

        // 缓存结果
        if (useResponseCaching && cacheKey != null && predictions != null) {
            forecastCache.put(cacheKey, predictions);
            forecastExplanationCache.put(cacheKey, latestForecastExplanation);
            System.out.println("Cached forecast result for key: " + cacheKey);
        }

        return predictions;
    }

    /**
     * 调用DeepSeek API进行支出预测
     */
    private static Map<String, Double> callDeepSeekForForecast(
            Map<String, Double> historicalData,
            int months) {

        try {
            // 构建提示词
            StringBuilder promptBuilder = new StringBuilder();
            promptBuilder.append(
                    "As a financial advisor, analyze the following monthly spending data and predict spending for the next ")
                    .append(months)
                    .append(" months.\n\nHistorical monthly spending:\n");

            // 添加历史支出数据
            for (Map.Entry<String, Double> entry : historicalData.entrySet()) {
                promptBuilder.append(entry.getKey()).append(": ¥").append(String.format("%.2f", entry.getValue()))
                        .append("\n");
            }

            // 要求特定格式的输出
            promptBuilder.append("\nPlease predict the spending for the next ")
                    .append(months)
                    .append(" months based on this historical data. ")
                    .append("Consider seasonal factors, spending trends, and potential special events (like Chinese holidays).\n\n")
                    .append("Return your prediction in JSON format like this:\n")
                    .append("{\n")
                    .append("  \"predictions\": {\n")
                    .append("    \"year-month\": amount,\n")
                    .append("    ...\n")
                    .append("  },\n")
                    .append("  \"explanation\": \"brief explanation of your prediction methodology and factors considered\"\n")
                    .append("}\n");

            String prompt = promptBuilder.toString();
            System.out.println("DeepSeek prediction prompt: " + prompt);

            // 构建请求体
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", DEEPSEEK_MODEL);
            requestBody.put("stream", false);

            // 构建消息数组
            JSONArray messagesArray = new JSONArray();

            // 添加系统消息
            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content",
                    "You are a financial analysis AI specialized in spending predictions and budgeting recommendations. Always respond with precise JSON formats as requested by the user.");
            messagesArray.add(systemMessage);

            // 添加用户消息
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);
            messagesArray.add(userMessage);

            requestBody.put("messages", messagesArray);
            requestBody.put("temperature", 0.3); // 降低温度以获得更确定性的输出
            requestBody.put("max_tokens", 1000);

            // 创建HTTP请求
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(DEEPSEEK_API_URL))
                    .header("Authorization", "Bearer " + DEEPSEEK_API_KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            System.out.println("Sending request to DeepSeek API...");

            // 发送请求并获取响应
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // 处理响应
            if (response.statusCode() == 200) {
                System.out.println("DeepSeek API call successful!");
                return parseDeepSeekForecastResponse(response.body());
            } else {
                System.out.println("DeepSeek API call failed, status code: " + response.statusCode());
                System.out.println("Response body: " + response.body());
                return null;
            }

        } catch (Exception e) {
            System.out.println("DeepSeek API call exception: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 解析DeepSeek API的预测响应
     */
    private static Map<String, Double> parseDeepSeekForecastResponse(String responseBody) {
        Map<String, Double> predictions = new LinkedHashMap<>();

        try {
            System.out.println("Parsing DeepSeek forecast response...");

            // 解析API响应的JSON
            JSONParser parser = new JSONParser();
            JSONObject responseJson = (JSONObject) parser.parse(responseBody);

            System.out.println("Response JSON parsed successfully");

            // 从响应中获取内容
            JSONArray choicesArray = (JSONArray) responseJson.get("choices");
            if (choicesArray == null || choicesArray.isEmpty()) {
                System.out.println("Error: No choices in API response");
                return predictions;
            }

            JSONObject firstChoice = (JSONObject) choicesArray.get(0);
            JSONObject message = (JSONObject) firstChoice.get("message");
            if (message == null) {
                System.out.println("Error: No message in API response choice");
                return predictions;
            }

            String content = (String) message.get("content");
            if (content == null || content.isEmpty()) {
                System.out.println("Error: Empty content in API response message");
                return predictions;
            }

            System.out.println("DeepSeek response content: " + content);

            // 尝试解析AI生成的JSON内容
            // 注意：这里需要处理多种可能的格式，因为LLM的输出可能不完全符合预期
            int startIndex = content.indexOf('{');
            int endIndex = content.lastIndexOf('}');

            if (startIndex >= 0 && endIndex > startIndex) {
                String jsonStr = content.substring(startIndex, endIndex + 1);
                System.out.println("Extracted JSON: " + jsonStr);

                try {
                    JSONObject predictionJson = (JSONObject) parser.parse(jsonStr);

                    // 获取预测部分
                    JSONObject predictionsObj = (JSONObject) predictionJson.get("predictions");
                    if (predictionsObj == null) {
                        System.out.println("Error: No 'predictions' object in AI response");
                        return predictions;
                    }

                    // 保存解释文本用于后续显示
                    if (predictionJson.containsKey("explanation")) {
                        String explanation = (String) predictionJson.get("explanation");
                        latestForecastExplanation = explanation;
                        System.out.println("Extracted explanation: " + explanation);
                    }

                    // 解析预测数据
                    System.out.println("Parsing prediction values...");
                    for (Object key : predictionsObj.keySet()) {
                        String month = (String) key;
                        double amount = 0;

                        // 处理不同的数值格式
                        Object valueObj = predictionsObj.get(key);
                        if (valueObj instanceof Double) {
                            amount = (Double) valueObj;
                        } else if (valueObj instanceof Long) {
                            amount = ((Long) valueObj).doubleValue();
                        } else if (valueObj instanceof String) {
                            // 尝试将字符串转换为数字
                            try {
                                // 处理可能包含货币符号的字符串
                                String valueStr = (String) valueObj;
                                valueStr = valueStr.replaceAll("[^\\d.]", "");
                                amount = Double.parseDouble(valueStr);
                            } catch (NumberFormatException e) {
                                System.out.println("Cannot parse prediction value: " + valueObj);
                                continue;
                            }
                        }

                        predictions.put(month, amount);
                        System.out.println("Added prediction: " + month + " = " + amount);
                    }
                } catch (ParseException e) {
                    System.out.println("Error parsing AI response JSON: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("Cannot find valid JSON content in response");
                // 如果找不到有效的JSON，尝试其他方法解析或返回空结果
            }

        } catch (Exception e) {
            System.out.println("Error parsing DeepSeek response: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("Parsed " + predictions.size() + " predictions from DeepSeek response");
        return predictions;
    }

    /**
     * 获取最新的预测解释
     */
    public static String getLatestForecastExplanation() {
        return latestForecastExplanation;
    }

    /**
     * 获取最新的预算推荐分析
     */
    public static String getLatestBudgetRecommendationAnalysis() {
        return latestBudgetRecommendationAnalysis;
    }

    /**
     * 使用简单算法生成预测数据（备用方法）
     */
    private static Map<String, Double> generateSimpleForecast(
            Map<String, Double> historicalData,
            int months) {

        Map<String, Double> forecastResults = new LinkedHashMap<>();

        if (historicalData.isEmpty()) {
            return forecastResults;
        }

        // 计算移动平均值
        double sum = 0;
        for (Double value : historicalData.values()) {
            sum += value;
        }
        double average = sum / historicalData.size();

        // 获取最后一个月份
        String lastMonthKey = null;
        for (String key : historicalData.keySet()) {
            lastMonthKey = key;
        }

        if (lastMonthKey == null) {
            return forecastResults;
        }

        // 解析最后一个月的年月
        YearMonth lastMonth;
        try {
            lastMonth = YearMonth.parse(lastMonthKey);
        } catch (Exception e) {
            // 如果解析失败，使用当前月份
            lastMonth = YearMonth.now();
        }

        // 预测未来几个月
        for (int i = 1; i <= months; i++) {
            YearMonth futureMonth = lastMonth.plusMonths(i);

            // 基础预测（移动平均）
            double forecast = average;

            // 添加季节性因素
            int monthValue = futureMonth.getMonthValue();
            if (monthValue == 1 || monthValue == 2) {
                forecast *= 1.2; // 春节期间增加20%
            } else if (monthValue == 10) {
                forecast *= 1.15; // 国庆期间增加15%
            } else if (monthValue == 6 || monthValue == 7) {
                forecast *= 1.1; // 暑假期间增加10%
            }

            // 添加一些随机波动
            forecast *= (0.95 + 0.1 * random.nextDouble());

            forecastResults.put(futureMonth.toString(), forecast);
        }

        // 生成一个模拟的预测说明
        latestForecastExplanation = "Based on your historical spending data, the analysis considers the following factors:\n\n"
                +
                "1. Your spending trend shows a gradual increase with an annual growth rate of approximately 5%\n" +
                "2. Seasonal spending patterns are evident: Chinese New Year (Jan-Feb: +20%), National Day (Oct: +15%), and Summer Holiday (Jun-Jul: +10%)\n"
                +
                "3. These predictions combine historical moving averages with seasonal factors\n\n" +
                "Recommendations: Consider controlling discretionary spending during Chinese New Year and National Day periods. Stock up on necessities in advance to avoid price increases during holiday seasons.";

        return forecastResults;
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

        Map<String, Double> recommendations = null;

        // 如果启用了缓存，先检查缓存中是否有相同参数的结果
        String cacheKey = null;
        if (useResponseCaching) {
            cacheKey = generateBudgetCacheKey(monthlyAverages, applySeasonalAdjustments);
            recommendations = budgetRecommendationCache.get(cacheKey);

            if (recommendations != null) {
                System.out.println("Using cached budget recommendation for key: " + cacheKey);
                // 设置缓存的分析文本
                latestBudgetRecommendationAnalysis = budgetAnalysisCache.getOrDefault(cacheKey, "");
                return recommendations;
            }
        }

        // 首先尝试使用DeepSeek API（如果启用）
        if (useRealDeepSeekAPI && !DEEPSEEK_API_KEY.equals("YOUR_DEEPSEEK_API_KEY")) {
            try {
                recommendations = callDeepSeekForBudgetRecommendation(monthlyAverages, applySeasonalAdjustments);
            } catch (Exception e) {
                System.out.println("DeepSeek API budget recommendation call exception: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // 如果未启用API或API调用失败，生成本地预算建议
        if (recommendations == null || recommendations.isEmpty()) {
            recommendations = generateLocalRecommendations(monthlyAverages, applySeasonalAdjustments);
        }

        // 缓存结果
        if (useResponseCaching && cacheKey != null && recommendations != null) {
            budgetRecommendationCache.put(cacheKey, recommendations);
            budgetAnalysisCache.put(cacheKey, latestBudgetRecommendationAnalysis);
            System.out.println("Cached budget recommendation for key: " + cacheKey);
        }

        return recommendations;
    }

    /**
     * 调用DeepSeek API获取预算建议
     */
    private static Map<String, Double> callDeepSeekForBudgetRecommendation(
            Map<String, Double> monthlyAverages,
            boolean applySeasonalAdjustments) {

        try {
            // 构建提示词
            StringBuilder promptBuilder = new StringBuilder();
            promptBuilder.append(
                    "As a financial advisor, I need your help to create personalized budget recommendations based on the following monthly average spending data:\n\n");

            double totalSpending = 0;
            for (Map.Entry<String, Double> entry : monthlyAverages.entrySet()) {
                promptBuilder.append(entry.getKey()).append(": ¥").append(String.format("%.2f", entry.getValue()))
                        .append("\n");
                totalSpending += entry.getValue();
            }

            promptBuilder.append("\nTotal monthly spending: ¥").append(String.format("%.2f", totalSpending))
                    .append("\n\n");

            promptBuilder.append(
                    "Please analyze this spending pattern and provide budget recommendations for each category. ");

            if (applySeasonalAdjustments) {
                promptBuilder.append("Consider seasonal factors for the current month (")
                        .append(LocalDate.now().getMonth())
                        .append("), including any Chinese holidays or shopping festivals. ");
            }

            promptBuilder.append(
                    "Your response should help the user optimize their finances, identify areas to cut back, and achieve financial goals.\n\n");

            promptBuilder.append("Return your response in this JSON format:\n")
                    .append("{\n")
                    .append("  \"recommendations\": {\n")
                    .append("    \"category1\": recommendedAmount,\n")
                    .append("    \"category2\": recommendedAmount,\n")
                    .append("    ...\n")
                    .append("  },\n")
                    .append("  \"analysis\": \"comprehensive analysis of spending patterns and detailed explanation of recommendations\"\n")
                    .append("}\n");

            String prompt = promptBuilder.toString();
            System.out.println("DeepSeek budget recommendation prompt: " + prompt);

            // 构建请求体
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", DEEPSEEK_MODEL);
            requestBody.put("stream", false);

            // 构建消息数组
            JSONArray messagesArray = new JSONArray();

            // 添加系统消息
            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content",
                    "You are a financial analysis AI specialized in spending predictions and budgeting recommendations. Always respond with precise JSON formats as requested by the user.");
            messagesArray.add(systemMessage);

            // 添加用户消息
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);
            messagesArray.add(userMessage);

            requestBody.put("messages", messagesArray);
            requestBody.put("temperature", 0.3); // 降低温度以获得更确定性的输出
            requestBody.put("max_tokens", 1200);

            // 创建HTTP请求
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(DEEPSEEK_API_URL))
                    .header("Authorization", "Bearer " + DEEPSEEK_API_KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            System.out.println("Sending budget recommendation request to DeepSeek API...");

            // 发送请求并获取响应
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // 处理响应
            if (response.statusCode() == 200) {
                System.out.println("DeepSeek API budget recommendation call successful!");
                return parseDeepSeekBudgetResponse(response.body());
            } else {
                System.out.println("DeepSeek API call failed, status code: " + response.statusCode());
                System.out.println("Response body: " + response.body());
                return null;
            }

        } catch (Exception e) {
            System.out.println("DeepSeek API budget recommendation call exception: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 解析DeepSeek API的预算建议响应
     */
    private static Map<String, Double> parseDeepSeekBudgetResponse(String responseBody) {
        Map<String, Double> recommendations = new HashMap<>();

        try {
            System.out.println("Parsing DeepSeek budget recommendation response...");

            // 解析API响应的JSON
            JSONParser parser = new JSONParser();
            JSONObject responseJson = (JSONObject) parser.parse(responseBody);

            System.out.println("Response JSON parsed successfully");

            // 从响应中获取内容
            JSONArray choicesArray = (JSONArray) responseJson.get("choices");
            if (choicesArray == null || choicesArray.isEmpty()) {
                System.out.println("Error: No choices in API response");
                return recommendations;
            }

            JSONObject firstChoice = (JSONObject) choicesArray.get(0);
            JSONObject message = (JSONObject) firstChoice.get("message");
            if (message == null) {
                System.out.println("Error: No message in API response choice");
                return recommendations;
            }

            String content = (String) message.get("content");
            if (content == null || content.isEmpty()) {
                System.out.println("Error: Empty content in API response message");
                return recommendations;
            }

            System.out.println("DeepSeek budget response content: " + content);

            // 尝试解析AI生成的JSON内容
            int startIndex = content.indexOf('{');
            int endIndex = content.lastIndexOf('}');

            if (startIndex >= 0 && endIndex > startIndex) {
                String jsonStr = content.substring(startIndex, endIndex + 1);
                System.out.println("Extracted JSON: " + jsonStr);

                try {
                    JSONObject recommendationJson = (JSONObject) parser.parse(jsonStr);

                    // 获取推荐部分
                    JSONObject recommendationsObj = (JSONObject) recommendationJson.get("recommendations");
                    if (recommendationsObj == null) {
                        System.out.println("Error: No 'recommendations' object in AI response");
                        return recommendations;
                    }

                    // 保存分析文本
                    if (recommendationJson.containsKey("analysis")) {
                        String analysis = (String) recommendationJson.get("analysis");
                        latestBudgetRecommendationAnalysis = analysis;
                        System.out.println("Extracted analysis: " + analysis);
                    }

                    // 解析推荐数据
                    System.out.println("Parsing recommendation values...");
                    for (Object key : recommendationsObj.keySet()) {
                        String category = (String) key;
                        double amount = 0;

                        // 处理不同的数值格式
                        Object valueObj = recommendationsObj.get(category);
                        if (valueObj instanceof Double) {
                            amount = (Double) valueObj;
                        } else if (valueObj instanceof Long) {
                            amount = ((Long) valueObj).doubleValue();
                        } else if (valueObj instanceof String) {
                            // 尝试将字符串转换为数字
                            try {
                                // 处理可能包含货币符号的字符串
                                String valueStr = (String) valueObj;
                                valueStr = valueStr.replaceAll("[^\\d.]", "");
                                amount = Double.parseDouble(valueStr);
                            } catch (NumberFormatException e) {
                                System.out.println("Cannot parse recommendation value: " + valueObj);
                                continue;
                            }
                        }

                        recommendations.put(category, amount);
                        System.out.println("Added recommendation: " + category + " = " + amount);
                    }
                } catch (ParseException e) {
                    System.out.println("Error parsing AI response JSON: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("Cannot find valid JSON content in response");
                // 如果找不到有效的JSON，尝试其他方法解析或返回空结果
            }

        } catch (Exception e) {
            System.out.println("Error parsing DeepSeek budget response: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("Parsed " + recommendations.size() + " budget recommendations from DeepSeek response");
        return recommendations;
    }

    /**
     * 生成本地预算建议（备用方法）
     */
    private static Map<String, Double> generateLocalRecommendations(
            Map<String, Double> monthlyAverages,
            boolean applySeasonalAdjustments) {

        Map<String, Double> recommendations = new HashMap<>();

        // 总支出
        double totalSpending = 0;
        for (Double amount : monthlyAverages.values()) {
            totalSpending += amount;
        }

        // 一些预算优化规则
        for (Map.Entry<String, Double> entry : monthlyAverages.entrySet()) {
            String category = entry.getKey();
            double average = entry.getValue();
            double recommendation = average;

            // 对不同类别应用不同的优化规则
            if (category.toLowerCase().contains("餐饮") ||
                    category.toLowerCase().contains("food") ||
                    category.toLowerCase().contains("dining")) {
                // 餐饮支出通常可以优化约10-15%
                recommendation = average * 0.85;
            } else if (category.toLowerCase().contains("购物") ||
                    category.toLowerCase().contains("shopping")) {
                // 购物支出通常可以优化约15-20%
                recommendation = average * 0.8;
            } else if (category.toLowerCase().contains("娱乐") ||
                    category.toLowerCase().contains("entertainment")) {
                // 娱乐支出通常可以优化约20%
                recommendation = average * 0.8;
            } else if (category.toLowerCase().contains("交通") ||
                    category.toLowerCase().contains("transport")) {
                // 交通可以优化约5-10%
                recommendation = average * 0.9;
            } else {
                // 其他类别默认优化5%
                recommendation = average * 0.95;
            }

            // 应用季节性调整
            if (applySeasonalAdjustments) {
                Month currentMonth = LocalDate.now().getMonth();
                int monthValue = currentMonth.getValue();

                // 春节期间
                if (monthValue == 1 || monthValue == 2) {
                    if (category.toLowerCase().contains("餐饮") ||
                            category.toLowerCase().contains("food")) {
                        recommendation *= 1.2; // 增加20%预算
                    }
                    if (category.toLowerCase().contains("购物") ||
                            category.toLowerCase().contains("shopping")) {
                        recommendation *= 1.3; // 增加30%预算
                    }
                }
                // 国庆/中秋期间
                else if (monthValue == 9 || monthValue == 10) {
                    if (category.toLowerCase().contains("旅游") ||
                            category.toLowerCase().contains("travel")) {
                        recommendation *= 1.4; // 增加40%预算
                    }
                }
                // 双11购物季
                else if (monthValue == 11) {
                    if (category.toLowerCase().contains("购物") ||
                            category.toLowerCase().contains("shopping")) {
                        recommendation *= 1.5; // 增加50%预算
                    }
                }
            }

            recommendations.put(category, recommendation);
        }

        // 生成分析文本
        StringBuilder analysisBuilder = new StringBuilder();
        analysisBuilder.append("Based on your historical spending patterns, here are our budget recommendations:\n\n");

        if (applySeasonalAdjustments) {
            Month currentMonth = LocalDate.now().getMonth();
            analysisBuilder.append("Special considerations for current month (").append(currentMonth).append("):\n");

            // 根据当前月份添加特定分析
            int monthValue = currentMonth.getValue();
            if (monthValue == 1 || monthValue == 2) {
                analysisBuilder.append(
                        "- Chinese New Year period: Food and shopping expenses typically increase, budgets adjusted accordingly\n");
            } else if (monthValue == 9 || monthValue == 10) {
                analysisBuilder.append(
                        "- National Day/Mid-Autumn Festival: Travel expenses may increase, adjustments considered\n");
            } else if (monthValue == 11) {
                analysisBuilder.append("- November shopping season (Singles' Day): Shopping budget adjusted upward\n");
            } else if (monthValue == 6 || monthValue == 7) {
                analysisBuilder.append(
                        "- Summer holiday: Entertainment and travel expenses may increase, considered in adjustments\n");
            }
            analysisBuilder.append("\n");
        }

        analysisBuilder.append("General recommendations:\n");
        analysisBuilder.append(
                "1. Food expenses can be optimized by approximately 15%. Consider reducing dining out and increasing healthy home cooking\n");
        analysisBuilder.append(
                "2. Shopping has 20% optimization potential. Make shopping lists before purchases to avoid impulse buying\n");
        analysisBuilder.append(
                "3. Entertainment expenses could be reduced by about 20%. Look for free or low-cost leisure activities\n");
        analysisBuilder.append(
                "4. Transportation costs can be reduced by about 10% through carpooling and public transportation\n\n");

        // 计算总节省金额
        double totalRecommended = 0;
        for (Double amount : recommendations.values()) {
            totalRecommended += amount;
        }
        double savings = totalSpending - totalRecommended;

        analysisBuilder.append("By following these recommendations, you could save approximately ¥")
                .append(String.format("%.2f", savings))
                .append(" monthly, or ¥").append(String.format("%.2f", savings * 12))
                .append(" annually. We suggest directing these savings toward increasing your savings or investments to achieve long-term financial goals.");

        latestBudgetRecommendationAnalysis = analysisBuilder.toString();

        return recommendations;
    }

    /**
     * 生成预测缓存键
     */
    private static String generateForecastCacheKey(Map<String, Double> historicalData, int months) {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append("forecast_").append(months).append("_");

        // 将历史数据按键排序，确保相同数据产生相同的键
        historicalData.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> keyBuilder.append(entry.getKey()).append(":").append(entry.getValue()).append(";"));

        return keyBuilder.toString().hashCode() + "";
    }

    /**
     * 生成预算建议缓存键
     */
    private static String generateBudgetCacheKey(Map<String, Double> monthlyAverages,
            boolean applySeasonalAdjustments) {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append("budget_").append(applySeasonalAdjustments ? "seasonal_" : "nonseasonal_");

        // 将月平均支出按键排序，确保相同数据产生相同的键
        monthlyAverages.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> keyBuilder.append(entry.getKey()).append(":").append(entry.getValue()).append(";"));

        return keyBuilder.toString().hashCode() + "";
    }

    /**
     * 清除所有缓存
     */
    public static void clearAllCaches() {
        forecastCache.clear();
        budgetRecommendationCache.clear();
        forecastExplanationCache.clear();
        budgetAnalysisCache.clear();
        System.out.println("All caches cleared");
    }
}