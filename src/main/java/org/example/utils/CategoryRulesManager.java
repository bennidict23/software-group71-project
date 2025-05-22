package org.example.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 分类规则管理器
 * 负责管理用户自定义的分类规则，并将其用于增强自动分类
 */
public class CategoryRulesManager {
    private static final String RULES_FILE = "category_rules.txt";
    private static final Map<String, String> categoryRules = new HashMap<>();

    static {
        // 初始化时加载规则
        loadRules();
    }

    /**
     * 加载保存的分类规则
     */
    private static void loadRules() {
        File rulesFile = new File(RULES_FILE);
        if (!rulesFile.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(rulesFile), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|\\|");
                if (parts.length == 2) {
                    String keyword = parts[0].trim();
                    String category = parts[1].trim();
                    categoryRules.put(keyword, category);
                }
            }
            System.out.println("Loaded " + categoryRules.size() + " category rules");
        } catch (IOException e) {
            System.err.println("Error loading category rules: " + e.getMessage());
        }
    }

    /**
     * 保存所有分类规则
     */
    private static void saveRules() {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(RULES_FILE), StandardCharsets.UTF_8))) {
            for (Map.Entry<String, String> entry : categoryRules.entrySet()) {
                writer.write(entry.getKey() + "||" + entry.getValue());
                writer.newLine();
            }
            System.out.println("Saved " + categoryRules.size() + " category rules");
        } catch (IOException e) {
            System.err.println("Error saving category rules: " + e.getMessage());
        }
    }

    /**
     * 添加新的分类规则
     * 
     * @param keyword  关键词（通常是描述的一部分）
     * @param category 分类
     */
    public static void addRule(String keyword, String category) {
        // 只保存关键词的小写版本，以便不区分大小写进行匹配
        categoryRules.put(keyword.toLowerCase(), category);
        saveRules();
    }

    /**
     * 删除分类规则
     * 
     * @param keyword 要删除的规则的关键词
     */
    public static void removeRule(String keyword) {
        categoryRules.remove(keyword.toLowerCase());
        saveRules();
    }

    /**
     * 获取所有分类规则
     * 
     * @return 分类规则映射
     */
    public static Map<String, String> getAllRules() {
        return Collections.unmodifiableMap(categoryRules);
    }

    /**
     * 尝试根据描述匹配分类规则
     * 
     * @param description 交易描述
     * @return 如果找到匹配，返回分类；否则返回null
     */
    public static String findMatchingCategory(String description) {
        if (description == null || description.isEmpty()) {
            return null;
        }

        String lowerDesc = description.toLowerCase();

        // 尝试查找包含关键词的规则
        for (Map.Entry<String, String> entry : categoryRules.entrySet()) {
            if (lowerDesc.contains(entry.getKey().toLowerCase())) {
                return entry.getValue();
            }
        }

        return null;
    }

    /**
     * 生成分类提示文本用于AI模型
     * 
     * @return 用于AI提示的分类规则文本
     */
    public static String generateClassificationPrompt() {
        if (categoryRules.isEmpty()) {
            return "";
        }

        StringBuilder prompt = new StringBuilder();
        prompt.append("Apply these special classification rules:\n");

        for (Map.Entry<String, String> entry : categoryRules.entrySet()) {
            prompt.append("- If description contains \"")
                    .append(entry.getKey())
                    .append("\", classify as \"")
                    .append(entry.getValue())
                    .append("\"\n");
        }

        return prompt.toString();
    }
}