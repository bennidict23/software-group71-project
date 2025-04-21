package org.example;

import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.io.*;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UserManager {
    private static final String USERS_FILE = "users.csv";
    private static final String SETTINGS_FILE = "user_settings.csv";
    private static final String TRANSACTION_FILE = "transactions.csv";

    private ScheduledExecutorService scheduler;

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
                writer.println(
                        "username,annualTarget,monthlyTarget,monthlyBudget,shoppingBudget,transportBudget,dietBudget,amusementBudget,savedAmount,annualSavedAmount,currentYear,currentMonth");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        file = new File(TRANSACTION_FILE);
        if (!file.exists()) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                writer.println("User,Source,Date,Amount,Category,Description");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 启动定时任务，每十秒检查一次 transactions.csv 文件的变化
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::checkTransactionsFile, 0, 10, TimeUnit.SECONDS);
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
            // 跳过标题行
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2 && parts[0].equals(username)) {
                    User user = new User(parts[0], parts[1]);
                    // 确保加载用户设置
                    loadUserSettings(user);
                    return user;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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

    // 重置用户密码
    public boolean resetPassword(String username, String newPassword) {
        return updateUserPassword(username, newPassword);
    }

    // 修改后的加载用户设置方法
    public void loadUserSettings(User user) {
        try (BufferedReader br = new BufferedReader(new FileReader(SETTINGS_FILE))) {
            String line;
            br.readLine(); // 跳过标题行
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 12 && parts[0].equals(user.getUsername())) {
                    // 获取当前年份
                    int currentYear = LocalDate.now().getYear();
                    // 获取用户设置中的年份
                    int userYear = Integer.parseInt(parts[10]);

                    // 如果当前年份与用户设置中的年份不一致，重置年储蓄目标
                    if (currentYear != userYear) {
                        // 调用 resetAnnualSettings 方法重置所有年相关设置
                        user.resetAnnualSettings();
                        user.setCurrentYear(currentYear);
                        // 保存更新后的设置
                        saveUserSettings(user);
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
        // —— 如果不存在，就先创建并写入标题
        if (!inputFile.exists()) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(inputFile))) {
                writer.println("username,annualTarget,monthlyTarget,monthlyBudget,"
                        + "shoppingBudget,transportBudget,dietBudget,amusementBudget,"
                        + "savedAmount,annualSavedAmount");
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }

        File tempFile = new File("settings_temp.csv");
        List<String> lines = new ArrayList<>();
        boolean found = false;

        // 下面就照原来的逻辑：先把所有行读进来，更新这一行，然后写回……
        try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
            String line = br.readLine();
            if (line != null) lines.add(line);
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[0].equals(user.getUsername())) {
                    // 这里拼一行新的
                    lines.add(user.getUsername() + "," + user.getAnnualTarget() + "," + user.getMonthlyTarget() + ","
                            + user.getMonthlyBudget() + "," + user.getShoppingBudget() + "," + user.getTransportBudget() + ","
                            + user.getDietBudget() + "," + user.getAmusementBudget() + ","
                            + user.getSavedAmount() + "," + user.getAnnualSavedAmount());
                    found = true;
                } else {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!found) {
            lines.add(user.getUsername() + "," + user.getAnnualTarget() + "," + user.getMonthlyTarget() + ","
                    + user.getMonthlyBudget() + "," + user.getShoppingBudget() + "," + user.getTransportBudget() + ","
                    + user.getDietBudget() + "," + user.getAmusementBudget() + ","
                    + user.getSavedAmount() + "," + user.getAnnualSavedAmount());
        }

        // 写回临时文件然后替换
        try (PrintWriter writer = new PrintWriter(new FileWriter(tempFile))) {
            for (String l : lines) writer.println(l);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        if (!inputFile.delete() || !tempFile.renameTo(inputFile)) {
            System.err.println("Failed to update settings file");
        }
    }


    // 检查并重置月储蓄目标和月预算
    public void checkAndResetMonthlySettings(User user) {
        int currentYear = LocalDate.now().getYear();
        int currentMonth = LocalDate.now().getMonthValue();

        if (user.getCurrentYear() != currentYear || user.getCurrentMonth() != currentMonth) {
            user.resetMonthlySettings();
            user.setCurrentYear(currentYear);
            user.setCurrentMonth(currentMonth);
            saveUserSettings(user); // 保存更新后的设置
        }
    }

    // 检查 transactions.csv 文件的变化并更新 savedAmount 和 annualSavedAmount
    public void checkTransactionsFile() {
        File file = new File(TRANSACTION_FILE);
        if (!file.exists()) {
            return;
        }

        List<String> lines;
        try {
            lines = Files.readAllLines(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // 跳过标题行
        List<String> currentTransactions = lines.subList(1, lines.size());

        // 获取上次检查时的交易记录
        List<String> lastTransactions = getLastTransactions();

        // 比较当前交易记录和上次交易记录
        List<String> newTransactions = new ArrayList<>(currentTransactions);
        newTransactions.removeAll(lastTransactions);

        List<String> removedTransactions = new ArrayList<>(lastTransactions);
        removedTransactions.removeAll(currentTransactions);

        updateSavedAmounts(newTransactions, removedTransactions);

        // 更新上次检查时的交易记录
        saveLastTransactions(currentTransactions);

        // 如果有新的交易记录，显示更新提示
        if (!newTransactions.isEmpty()) {
            showUpdateNotification(newTransactions.size());
        }
    }

    private void showUpdateNotification(int newTransactionCount) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Transaction Update");
            alert.setHeaderText(null);
            alert.setContentText("There are " + newTransactionCount + " new transaction(s) recorded.");
            alert.showAndWait();
        });
    }

    // 更新 savedAmount 和 annualSavedAmount
    public void updateSavedAmounts(List<String> newTransactions, List<String> removedTransactions) {
        User currentUser = DashboardView.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        double newMonthlySpent = 0.0;
        double newYearlySpent = 0.0;
        double removedMonthlySpent = 0.0;
        double removedYearlySpent = 0.0;

        for (String line : newTransactions) {
            String[] parts = line.split(",");
            if (parts.length >= 7 && parts[1].equals(currentUser.getUsername())) {
                double amount = Double.parseDouble(parts[4]);
                String dateStr = parts[3];
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate date = LocalDate.parse(dateStr, formatter);

                if (date.getYear() == LocalDate.now().getYear()
                        && date.getMonthValue() == LocalDate.now().getMonthValue()) {
                    newMonthlySpent += amount;
                }
                if (date.getYear() == LocalDate.now().getYear()) {
                    newYearlySpent += amount;
                }
            }
        }

        for (String line : removedTransactions) {
            String[] parts = line.split(",");
            if (parts.length >= 7 && parts[1].equals(currentUser.getUsername())) {
                double amount = Double.parseDouble(parts[4]);
                String dateStr = parts[3];
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate date = LocalDate.parse(dateStr, formatter);

                if (date.getYear() == LocalDate.now().getYear()
                        && date.getMonthValue() == LocalDate.now().getMonthValue()) {
                    removedMonthlySpent += amount;
                }
                if (date.getYear() == LocalDate.now().getYear()) {
                    removedYearlySpent += amount;
                }
            }
        }

        // 更新 savedAmount 和 annualSavedAmount
        currentUser.setSavedAmount(currentUser.getSavedAmount() - newMonthlySpent + removedMonthlySpent);
        currentUser.setAnnualSavedAmount(currentUser.getAnnualSavedAmount() - newYearlySpent + removedYearlySpent);

        // 保存更新后的设置
        saveUserSettings(currentUser);
    }

    // 获取上次检查时的交易记录
    private List<String> getLastTransactions() {
        File file = new File("last_transactions.txt");
        if (!file.exists()) {
            return new ArrayList<>();
        }

        List<String> lines;
        try {
            lines = Files.readAllLines(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
        return lines;
    }

    // 保存当前检查时的交易记录
    private void saveLastTransactions(List<String> transactions) {
        File file = new File("last_transactions.txt");
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            for (String line : transactions) {
                writer.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void checkMonthlyExpenses(User user) {
        String username = user.getUsername();
        double shoppingSpent = 0.0;
        double transportSpent = 0.0;
        double dietSpent = 0.0;
        double amusementSpent = 0.0;
        double totalSpent = 0.0;

        try (BufferedReader br = new BufferedReader(new FileReader(TRANSACTION_FILE))) {
            String line;
            br.readLine(); // 跳过标题行
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 7 && parts[1].equals(username)) {
                    String dateStr = parts[3];
                    double amount = Double.parseDouble(parts[4]);
                    String category = parts[5];

                    // 解析日期
                    // 先把所有"/"换成"-"，再用 yyyy-MM-dd 模式解析日期
                    String normalized = dateStr.trim().replace('/', '-');
                    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    LocalDate date;
                    try {
                        date = LocalDate.parse(normalized, fmt);
                    } catch (DateTimeParseException ex) {
                        // 如果还是解析失败，就跳过这条记录
                        continue;
                    }

                    // 检查是否为本月的交易
                    if (date.getYear() == LocalDate.now().getYear()
                            && date.getMonthValue() == LocalDate.now().getMonthValue()) {
                        switch (category.toLowerCase()) {
                            case "shopping":
                                shoppingSpent += amount;
                                break;
                            case "transport":
                                transportSpent += amount;
                                break;
                            case "diet":
                                dietSpent += amount;
                                break;
                            case "amusement":
                                amusementSpent += amount;
                                break;
                        }
                        totalSpent += amount;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 检查是否超过预算并发出警告
        if (shoppingSpent > user.getShoppingBudget()) {
            showAlert("Warning",
                    "Shopping budget exceeded: $" + shoppingSpent + " (Budget: $" + user.getShoppingBudget() + ")");
        }
        if (transportSpent > user.getTransportBudget()) {
            showAlert("Warning",
                    "Transport budget exceeded: $" + transportSpent + " (Budget: $" + user.getTransportBudget() + ")");
        }
        if (dietSpent > user.getDietBudget()) {
            showAlert("Warning", "Diet budget exceeded: $" + dietSpent + " (Budget: $" + user.getDietBudget() + ")");
        }
        if (amusementSpent > user.getAmusementBudget()) {
            showAlert("Warning",
                    "Amusement budget exceeded: $" + amusementSpent + " (Budget: $" + user.getAmusementBudget() + ")");
        }
        if (totalSpent > user.getMonthlyBudget()) {
            showAlert("Warning",
                    "Total monthly budget exceeded: $" + totalSpent + " (Budget: $" + user.getMonthlyBudget() + ")");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // 关闭定时任务
    public void shutdownScheduler() {
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }

    // 更新用户预算
    public boolean updateUserBudget(String username, double newBudget) {
        User user = getUser(username);
        if (user != null) {
            user.setMonthlyBudget(newBudget);
            saveUserSettings(user);
            return true;
        }
        return false;
    }
}