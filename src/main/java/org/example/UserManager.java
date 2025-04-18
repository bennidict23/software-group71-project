package org.example;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class UserManager {
    private static final String USERS_FILE = "users.csv";
    private static final String SETTINGS_FILE = "user_settings.csv";

    public UserManager() {
        // 如果文件不存在，则创建并添加表头
        File file = new File(USERS_FILE);
        if (!file.exists()) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                writer.println("username,password");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        file = new File(SETTINGS_FILE);
        if (!file.exists()) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                writer.println("username,annualTarget,monthlyTarget,monthlyBudget,shoppingBudget,transportBudget,dietBudget,amusementBudget,savedAmount,annualSavedAmount,currentYear,currentMonth");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 注册用户的方法
    public boolean registerUser(String username, String password) {
        if (getUser(username) != null) {
            return false;
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(USERS_FILE, true))) {
            bw.write(username + "," + password);
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    // 根据用户名查找用户
    public User getUser(String username) {
        try (BufferedReader br = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            br.readLine(); // 跳过标题行
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2 && parts[0].equals(username)) {
                    User user = new User(parts[0], parts[1]);
                    loadUserSettings(user); // 确保加载用户设置
                    return user;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 新增方法：重置用户密码并同步到CSV文件
    public boolean resetPassword(String username, String newPassword) {
        return updateUserPassword(username, newPassword);
    }

    // 更新用户密码并同步到CSV文件
    public boolean updateUserPassword(String username, String newPassword) {
        File inputFile = new File(USERS_FILE);
        File tempFile = new File("users_temp.csv");

        List<String> lines = new ArrayList<>();
        boolean updated = false;
        try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
            String line = br.readLine();
            if (line != null) { // 写入标题行
                lines.add(line);
            }
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2 && parts[0].equals(username)) {
                    // 替换密码
                    lines.add(username + "," + newPassword);
                    updated = true;
                } else {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        // 将修改后的内容写入临时文件
        try (PrintWriter writer = new PrintWriter(new FileWriter(tempFile))) {
            for (String l : lines) {
                writer.println(l);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        // 删除原始文件，并将临时文件重命名
        if (!inputFile.delete()) {
            System.out.println("Could not delete original file.");
            return false;
        }
        if (!tempFile.renameTo(inputFile)) {
            System.out.println("Could not rename temp file.");
            return false;
        }
        return updated;
    }

    // 用户登录认证
    public boolean authenticate(String username, String password) {
        User user = getUser(username);
        if (user != null) {
            return user.getPassword().equals(password);
        }
        return false;
    }

    // 修改 UserManager 类中的 loadUserSettings 方法
    public void loadUserSettings(User user) {
        try (BufferedReader br = new BufferedReader(new FileReader(SETTINGS_FILE))) {
            String line;
            br.readLine(); // 跳过标题行
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 12 && parts[0].equals(user.getUsername())) {
                    int currentYear = LocalDate.now().getYear(); // 获取当前年份
                    int userYear = Integer.parseInt(parts[10]); // 获取用户设置中的年份

                    // 如果当前年份与用户设置中的年份不一致，重置年储蓄目标
                    if (currentYear != userYear) {
                        user.setAnnualTarget(0.0); // 重置年储蓄目标
                        user.setAnnualSavedAmount(0.0); // 重置年已储蓄金额
                        user.setCurrentYear(currentYear); // 更新年份为当前年份
                        saveUserSettings(user); // 保存更新后的设置
                    } else {
                        // 如果年份一致，加载用户设置
                        user.setAnnualTarget(Double.parseDouble(parts[1]));
                        user.setMonthlyTarget(Double.parseDouble(parts[2]));
                        user.setMonthlyBudget(Double.parseDouble(parts[3]));
                        user.setShoppingBudget(Double.parseDouble(parts[4]));
                        user.setTransportBudget(Double.parseDouble(parts[5]));
                        user.setDietBudget(Double.parseDouble(parts[6]));
                        user.setAmusementBudget(Double.parseDouble(parts[7]));
                        user.setSavedAmount(Double.parseDouble(parts[8]));
                        user.setAnnualSavedAmount(Double.parseDouble(parts[9]));
                        user.setCurrentYear(Integer.parseInt(parts[10]));
                        user.setCurrentMonth(Integer.parseInt(parts[11]));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 保存用户设置
    public void saveUserSettings(User user) {
        File inputFile = new File(SETTINGS_FILE);
        File tempFile = new File("settings_temp.csv");

        List<String> lines = new ArrayList<>();
        boolean found = false;
        try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
            String line = br.readLine();
            if (line != null) { // 写入标题行
                lines.add(line);
            }
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 12 && parts[0].equals(user.getUsername())) {
                    // 更新现有设置
                    lines.add(user.getUsername() + "," + user.getAnnualTarget() + "," + user.getMonthlyTarget() + "," + user.getMonthlyBudget() + "," + user.getShoppingBudget() + "," + user.getTransportBudget() + "," + user.getDietBudget() + "," + user.getAmusementBudget() + "," + user.getSavedAmount() + "," + user.getAnnualSavedAmount() + "," + user.getCurrentYear() + "," + user.getCurrentMonth());
                    found = true;
                } else {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!found) {
            lines.add(user.getUsername() + "," + user.getAnnualTarget() + "," + user.getMonthlyTarget() + "," + user.getMonthlyBudget() + "," + user.getShoppingBudget() + "," + user.getTransportBudget() + "," + user.getDietBudget() + "," + user.getAmusementBudget() + "," + user.getSavedAmount() + "," + user.getAnnualSavedAmount() + "," + user.getCurrentYear() + "," + user.getCurrentMonth());
        }

        // 将修改后的内容写入临时文件
        try (PrintWriter writer = new PrintWriter(new FileWriter(tempFile))) {
            for (String l : lines) {
                writer.println(l);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // 删除原始文件，并将临时文件重命名
        if (!inputFile.delete()) {
            System.out.println("Could not delete original file.");
            return;
        }
        if (!tempFile.renameTo(inputFile)) {
            System.out.println("Could not rename temp file.");
            return;
        }
    }

    // 新增方法：检查并重置月储蓄目标和月预算
    public void checkAndResetMonthlySettings(User user) {
        int currentYear = LocalDate.now().getYear();
        int currentMonth = LocalDate.now().getMonthValue();

        // 打印当前的年份和月份
//        System.out.println("Current Year: " + currentYear);
//        System.out.println("Current Month: " + currentMonth);
//
//        // 打印用户的当前年份和月份
//        System.out.println("User's Current Year: " + user.getCurrentYear());
//        System.out.println("User's Current Month: " + user.getCurrentMonth());

        if (user.getCurrentYear() != currentYear || user.getCurrentMonth() != currentMonth) {
            user.resetMonthlySettings();
            user.setCurrentYear(currentYear);
            user.setCurrentMonth(currentMonth);
            saveUserSettings(user); // 保存更新后的设置
        }
    }
}