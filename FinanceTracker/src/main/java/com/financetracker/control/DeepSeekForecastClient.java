package com.financetracker.control;

import com.financetracker.entity.Transaction;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/**
 * Client for DeepSeek API to generate spending forecasts using AI.
 * This class handles communication with the DeepSeek API for advanced
 * predictions.
 */
public class DeepSeekForecastClient {

    private static final String API_URL = "https://api.deepseek.com/v1/chat/completions"; // 实际API地址可能需要调整
    private static final String API_KEY = System.getenv("DEEPSEEK_API_KEY"); // 从环境变量获取API密钥

    private static final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * Gets a forecast for future spending based on historical transaction data
     * 
     * @param transactions List of historical transactions
     * @param forecastDays Number of days to forecast
     * @return Map with date strings as keys and predicted amounts as values
     */
    public Map<String, Double> getForecast(List<Transaction> transactions, int forecastDays) {
        try {
            // 准备交易数据的prompt
            String prompt = preparePrompt(transactions, forecastDays);

            // 调用API
            String response = callDeepSeekAPI(prompt);

            // 解析结果
            return parseResponse(response);
        } catch (Exception e) {
            System.err.println("Error getting forecast from DeepSeek API: " + e.getMessage());
            return getFallbackForecast(forecastDays); // 出错时返回备用预测
        }
    }

    /**
     * Prepares the prompt for the DeepSeek API based on transaction data
     */
    private String preparePrompt(List<Transaction> transactions, int forecastDays) {
        StringBuilder sb = new StringBuilder();

        // 添加指令
        sb.append("Based on the following historical transaction data, predict the daily spending trend ");
        sb.append("for the next ").append(forecastDays).append(" days, ");
        sb.append("considering seasonal factors such as Chinese holidays.\n\n");

        // 添加说明
        sb.append("Historical transaction data (most recent first):\n");

        // 仅使用最近的100条交易记录，以避免prompt过长
        List<Transaction> recentTransactions = getRecentTransactions(transactions, 100);

        // 添加交易数据
        for (Transaction t : recentTransactions) {
            sb.append(String.format("Date: %s, Amount: %.2f, Category: %s\n",
                    t.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
                    t.getAmount(),
                    t.getCategory()));
        }

        // 添加输出格式指令
        sb.append("\nPlease predict the spending for the next ").append(forecastDays).append(" days.\n");
        sb.append("Return your response ONLY in JSON format with the following structure:\n");
        sb.append("```json\n");
        sb.append("{\"predictions\": [{\"date\": \"YYYY-MM-DD\", \"amount\": xxx.xx}, ...]}\n");
        sb.append("```\n");
        sb.append(
                "Include a brief explanation of your prediction, considering seasonal factors and patterns in the data.\n");

        return sb.toString();
    }

    /**
     * Gets the most recent transactions up to a specified limit
     */
    private List<Transaction> getRecentTransactions(List<Transaction> transactions, int limit) {
        // 按日期排序
        transactions.sort((t1, t2) -> t2.getDate().compareTo(t1.getDate()));

        // 返回前limit条
        return transactions.size() <= limit ? new ArrayList<>(transactions)
                : new ArrayList<>(transactions.subList(0, limit));
    }

    /**
     * Calls the DeepSeek API with the prepared prompt
     */
    private String callDeepSeekAPI(String prompt) throws IOException, InterruptedException {
        // 构建请求体
        String requestBody = String.format(
                "{\"model\":\"deepseek-chat\",\"messages\":[{\"role\":\"user\",\"content\":\"%s\"}]}",
                prompt.replace("\"", "\\\"").replace("\n", "\\n"));

        // 构建请求
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        // 发送请求
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // 检查响应状态
        if (response.statusCode() != 200) {
            throw new IOException("API request failed with status code: " + response.statusCode());
        }

        return response.body();
    }

    /**
     * Parses the API response to extract prediction data
     */
    private Map<String, Double> parseResponse(String response) {
        // 简化版解析 - 在实际应用中，应使用JSON库如Jackson或Gson
        Map<String, Double> result = new HashMap<>();

        // 查找JSON部分
        int startJson = response.indexOf("{\"predictions\":");
        int endJson = response.lastIndexOf("}") + 1;

        if (startJson >= 0 && endJson > startJson) {
            String jsonPart = response.substring(startJson, endJson);

            // 简单解析日期和金额
            // 格式: {"date":"YYYY-MM-DD","amount":xxx.xx}
            int pos = 0;
            while ((pos = jsonPart.indexOf("\"date\":\"", pos)) >= 0) {
                int dateStart = pos + 8;
                int dateEnd = jsonPart.indexOf("\"", dateStart);
                String date = jsonPart.substring(dateStart, dateEnd);

                int amountPos = jsonPart.indexOf("\"amount\":", dateEnd);
                if (amountPos >= 0) {
                    int amountStart = amountPos + 9;
                    int amountEnd = jsonPart.indexOf("}", amountStart);
                    if (amountEnd >= 0) {
                        String amountStr = jsonPart.substring(amountStart, amountEnd).trim();
                        // 移除可能的逗号
                        amountStr = amountStr.replace(",", "");
                        try {
                            double amount = Double.parseDouble(amountStr);
                            result.put(date, amount);
                        } catch (NumberFormatException e) {
                            System.err.println("Failed to parse amount: " + amountStr);
                        }
                    }
                }

                pos = dateEnd;
            }
        }

        return result;
    }

    /**
     * Provides a fallback forecast in case the API call fails
     */
    private Map<String, Double> getFallbackForecast(int forecastDays) {
        Map<String, Double> result = new HashMap<>();
        LocalDate today = LocalDate.now();

        // 简单的线性预测，每天递增1%
        double baseAmount = 100.0; // 基础消费额

        for (int i = 0; i < forecastDays; i++) {
            LocalDate forecastDate = today.plusDays(i + 1);
            String dateStr = forecastDate.format(DateTimeFormatter.ISO_LOCAL_DATE);

            // 添加一些随机波动
            double randomFactor = 0.9 + Math.random() * 0.2; // 0.9到1.1之间
            double amount = baseAmount * (1 + 0.01 * i) * randomFactor;

            result.put(dateStr, amount);
        }

        return result;
    }
}