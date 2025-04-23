package org.example.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DeepSeekCategoryService {

    // 预定义的分类列表
    private static final List<String> PREDEFINED_CATEGORIES = Arrays.asList(
            "Food & Dining",
            "Shopping",
            "Housing",
            "Transportation",
            "Entertainment",
            "Healthcare",
            "Education",
            "Personal Care",
            "Travel",
            "Gifts & Donations",
            "Investments",
            "Income",
            "Other");

    // API URL和Key（实际使用时应从配置文件加载）
    private static final String API_URL = "https://api.deepseek.com/v1/chat/completions";
    private static final String API_KEY = "sk-c4dc6fa348f24f6ab3ddc5514041676d"; // 更新为用户提供的API密钥

    private final ExecutorService executorService;

    public DeepSeekCategoryService() {
        // 创建一个固定大小的线程池用于异步处理请求
        this.executorService = Executors.newFixedThreadPool(3);
    }

    /**
     * 异步分类交易
     * 
     * @param description 交易描述
     * @param amount      交易金额
     * @return 返回包含分类结果的CompletableFuture
     */
    public CompletableFuture<String> classifyTransactionAsync(String description, String amount) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return classifyTransaction(description, amount);
            } catch (IOException e) {
                e.printStackTrace();
                return "Other"; // 默认分类
            }
        }, executorService);
    }

    /**
     * 批量分类交易
     * 
     * @param descriptions 交易描述列表
     * @param amounts      交易金额列表
     * @return 返回包含所有分类结果的CompletableFuture
     */
    public CompletableFuture<List<String>> classifyTransactionsAsync(List<String> descriptions, List<String> amounts) {
        // 创建一个CompletableFuture数组
        @SuppressWarnings("unchecked")
        CompletableFuture<String>[] futures = new CompletableFuture[descriptions.size()];

        // 为每个交易创建一个异步任务
        for (int i = 0; i < descriptions.size(); i++) {
            String description = descriptions.get(i);
            String amount = amounts.get(i);
            futures[i] = classifyTransactionAsync(description, amount);
        }

        // 合并所有结果
        return CompletableFuture.allOf(futures)
                .thenApply(v -> {
                    return Arrays.stream(futures)
                            .map(CompletableFuture::join)
                            .toList();
                });
    }

    /**
     * 同步分类单个交易
     * 
     * @param description 交易描述
     * @param amount      交易金额
     * @return 返回分类结果
     * @throws IOException 如果API请求失败
     */
    private String classifyTransaction(String description, String amount) throws IOException {
        // 首先尝试使用用户定义的规则进行分类
        String category = CategoryRulesManager.findMatchingCategory(description);
        if (category != null) {
            System.out.println("使用自定义规则将描述 \"" + description + "\" 分类为: " + category);
            return category;
        }

        // 如果没有匹配的规则，使用模拟API调用（演示用）
        // 在实际项目中，这里应当替换为真正的API调用代码
        if (System.getenv("DEEPSEEK_API_KEY") == null || System.getenv("DEEPSEEK_API_KEY").isEmpty()) {
            // 使用硬编码的API密钥，而不是环境变量
            // 没有环境变量API密钥时，使用硬编码的API_KEY
            // return mockClassification(description, amount);
        }

        // 构建API请求
        URL url = new URL(API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
        connection.setDoOutput(true);

        // 获取用户定义的分类规则提示
        String userRulesPrompt = CategoryRulesManager.generateClassificationPrompt();

        // 创建请求体，包含用户定义的规则
        String systemPrompt = "You are a financial transaction classifier. " +
                "Classify the transaction into exactly one of these categories: " +
                String.join(", ", PREDEFINED_CATEGORIES) + ". " +
                "Only respond with the category name, nothing else.";

        // 如果有用户定义的规则，添加到系统提示中
        if (!userRulesPrompt.isEmpty()) {
            systemPrompt = systemPrompt + "\n\n" + userRulesPrompt;
        }

        // 完整的请求体
        String requestBody = String.format(
                "{\"model\":\"deepseek-chat\",\"messages\":[{\"role\":\"system\",\"content\":\"%s\"},{\"role\":\"user\",\"content\":\"Transaction description: %s, Amount: %s\"}]}",
                systemPrompt.replace("\"", "\\\""),
                description.replace("\"", "\\\""),
                amount.replace("\"", "\\\""));

        // 发送请求
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = requestBody.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        // 读取响应
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        } catch (IOException e) {
            // 如果API调用失败，使用模拟分类
            return mockClassification(description, amount);
        }

        // 解析响应（简化版，实际应当使用JSON库）
        String result = response.toString();
        // 从JSON响应中提取分类结果
        // 这是一个简化处理，实际中应使用JSON解析库
        int contentStart = result.indexOf("\"content\":\"") + 11;
        int contentEnd = result.indexOf("\"", contentStart);
        String responseCategory = result.substring(contentStart, contentEnd);

        // 验证结果是否在我们的类别中
        if (PREDEFINED_CATEGORIES.contains(responseCategory)) {
            return responseCategory;
        } else {
            return "Other";
        }
    }

    /**
     * 模拟分类逻辑，当API不可用时使用
     */
    private String mockClassification(String description, String amount) {
        description = description.toLowerCase();

        // 根据描述关键词进行简单分类
        if (description.contains("rent") || description.contains("mortgage") || description.contains("housing")) {
            return "Housing";
        } else if (description.contains("food") || description.contains("restaurant") || description.contains("grocery")
                || description.contains("dining")) {
            return "Food & Dining";
        } else if (description.contains("uber") || description.contains("lyft") || description.contains("taxi")
                || description.contains("gas") || description.contains("bus") || description.contains("train")) {
            return "Transportation";
        } else if (description.contains("movie") || description.contains("netflix") || description.contains("spotify")
                || description.contains("concert")) {
            return "Entertainment";
        } else if (description.contains("doctor") || description.contains("hospital")
                || description.contains("pharmacy") || description.contains("medicine")) {
            return "Healthcare";
        } else if (description.contains("school") || description.contains("tuition") || description.contains("book")
                || description.contains("education")) {
            return "Education";
        } else if (description.contains("gift") || description.contains("donation")
                || description.contains("charity")) {
            return "Gifts & Donations";
        } else if (description.contains("salary") || description.contains("income") || description.contains("payment")
                || description.contains("deposit")) {
            return "Income";
        } else if (description.contains("investment") || description.contains("stock")
                || description.contains("dividend")) {
            return "Investments";
        } else if (description.contains("shopping") || description.contains("amazon") || description.contains("walmart")
                || description.contains("target")) {
            return "Shopping";
        }

        // 尝试从金额判断
        try {
            double amountValue = Double.parseDouble(amount.replace(",", ""));
            if (amountValue > 0) {
                return "Income";
            } else if (amountValue < -1000) {
                return "Housing"; // 大额支出可能是房租
            }
        } catch (NumberFormatException e) {
            // 忽略解析错误
        }

        return "Other";
    }

    /**
     * 关闭服务
     */
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    /**
     * 获取预定义分类列表
     */
    public static List<String> getPredefinedCategories() {
        return PREDEFINED_CATEGORIES;
    }
}