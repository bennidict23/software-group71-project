package org.example;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import java.io.*;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UserManager {
    private final String usersFile;
    private ScheduledExecutorService scheduler;
    private boolean isLoggedIn = false; // 标志变量，表示是否有用户登录

    // 构造方法允许注入文件路径，便于测试
    public UserManager(String usersFile) {
        this.usersFile = usersFile;
        // 如果文件不存在，则创建并添加表头
        createFileIfNotExists(usersFile, "username,password\n");
        // 启动定时任务，每5秒检查一次transactions.csv文件的变化
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::checkTransactionsFile, 0, 5, TimeUnit.SECONDS);
    }

    // 无参构造默认用生产环境文件名（兼容原有调用）
    public UserManager() {
        this("users.csv");
    }

    private void createFileIfNotExists(String filePath, String header) {
        File file = new File(filePath);
        if (!file.exists()) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                writer.println(header);
            } catch (IOException e) {
                System.err.println("Failed to create file: " + filePath + ". Error: " + e.getMessage());
            }
        }
    }

    // 注册用户的方法
    public boolean registerUser(String username, String password) {
        if (getUser(username) != null) {
            return false;
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(usersFile, true))) {
            bw.write(username + "," + password);
            bw.newLine();
        } catch (IOException e) {
            System.err.println("Error writing to users file: " + e.getMessage());
            return false;
        }
        // 检查并创建交易记录文件
        checkAndCreateTransactionFile(username);
        return true;
    }

    // 根据用户名查找用户
    public User getUser(String username) {
        try (BufferedReader br = new BufferedReader(new FileReader(usersFile))) {
            String line;
            br.readLine(); // 跳过标题行
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2 && parts[0].equals(username)) {
                    User user = new User(parts[0], parts[1]);
                    loadUserSettings(user);
                    initializeSavedAmounts(user);
                    return user;
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("User file not found: " + usersFile + ". Please check the file path.");
        } catch (IOException e) {
            System.err.println("Error reading user file: " + e.getMessage());
        }
        return null;
    }

    // 更新用户密码并同步到CSV文件
    public boolean updateUserPassword(String username, String newPassword) {
        File inputFile = new File(usersFile);
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
                    lines.add(username + "," + newPassword);
                    updated = true;
                } else {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading users file: " + e.getMessage());
            return false;
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(tempFile))) {
            for (String l : lines) {
                writer.println(l);
            }
        } catch (IOException e) {
            System.err.println("Error writing to temporary file: " + e.getMessage());
            return false;
        }

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
            isLoggedIn = true;
            if (user.getPassword().equals(password)) {
                checkAndCreateTransactionFile(username);
                return true;
            }
        }
        return false;
    }

    // 重置用户密码
    public boolean resetPassword(String username, String newPassword) {
        return updateUserPassword(username, newPassword);
    }

    // 修改后的 loadUserSettings 方法
    public void loadUserSettings(User user) {
        String settingsFile = user.getUsername() + "_userSettings.csv";
        File file = new File(settingsFile);
        if (!file.exists()) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                // 表头顺序需与代码解析顺序一致
                writer.println("username,annualTarget,monthlyTarget,monthlyBudget,transportationBudget,shoppingBudget,otherBudget,entertainmentBudget,savedAmount,annualSavedAmount,currentYear,currentMonth,annualBudget");
            } catch (IOException e) {
                System.err.println("Failed to create settings file: " + settingsFile + ". Error: " + e.getMessage());
                return;
            }
        }
        try (BufferedReader br = new BufferedReader(new FileReader(settingsFile))) {
            String line;
            br.readLine(); // 跳过标题行
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 13 && parts[0].equals(user.getUsername())) {
                    int currentYear = LocalDate.now().getYear();
                    int currentMonth = LocalDate.now().getMonthValue();

                    // 修正字段索引和类型转换
                    double annualSavedAmount = Double.parseDouble(parts[9]); // 原 parts[9] 是 annualSavedAmount（double）
                    int userYear = (int) Double.parseDouble(parts[10]); // 原 parts[10] 是 currentYear（整数，可能带小数）
                    int userMonth = (int) Double.parseDouble(parts[11]); // 原 parts[11] 是 currentMonth（整数，可能带小数）
                    double annualBudget = Double.parseDouble(parts[12]); // 新增：年总预算

                    if (currentYear != userYear) {
                        user.resetAnnualSettings();
                        user.setCurrentYear(currentYear);
                        user.setCurrentMonth(currentMonth);
                        saveUserSettings(user);
                    } else if (currentMonth != userMonth) {
                        user.resetMonthlySettings();
                        user.setCurrentYear(currentYear);
                        user.setCurrentMonth(currentMonth);
                        saveUserSettings(user);
                    } else {
                        // 按顺序解析字段（注意 CSV 表头与代码索引对应）
                        user.setAnnualTarget(Double.parseDouble(parts[1]));
                        user.setMonthlyTarget(Double.parseDouble(parts[2]));
                        user.setMonthlyBudget(Double.parseDouble(parts[3]));
                        user.setTransportationBudget(Double.parseDouble(parts[4]));
                        user.setShoppingBudget(Double.parseDouble(parts[5]));
                        user.setOtherBudget(Double.parseDouble(parts[6]));
                        user.setEntertainmentBudget(Double.parseDouble(parts[7]));
                        user.setSavedAmount(Double.parseDouble(parts[8])); // parts[8] 是 savedAmount（double）
                        user.setAnnualSavedAmount(annualSavedAmount); // 使用解析后的 annualSavedAmount
                        user.setCurrentYear(userYear);
                        user.setCurrentMonth(userMonth);
                        user.setAnnualBudget(annualBudget); // 新增：设置年总预算
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("Settings file not found: " + settingsFile + ". Please check the file path.");
        } catch (IOException e) {
            System.err.println("Error reading settings file: " + e.getMessage());
        }
    }

    // 修改后的 saveUserSettings 方法
    public void saveUserSettings(User user) {
        String settingsFile = user.getUsername() + "_userSettings.csv";
        File inputFile = new File(settingsFile);
        if (!inputFile.exists()) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(inputFile))) {
                writer.println("username,annualTarget,monthlyTarget,monthlyBudget,transportationBudget,shoppingBudget,otherBudget,entertainmentBudget,savedAmount,annualSavedAmount,currentYear,currentMonth,annualBudget");
            } catch (IOException e) {
                System.err.println("Failed to create settings file: " + settingsFile + ". Error: " + e.getMessage());
                return;
            }
        }
        File tempFile = new File(user.getUsername() + "_settings_temp.csv");
        List<String> lines = new ArrayList<>();
        boolean found = false;

        try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
            String line = br.readLine();
            if (line != null) {
                lines.add(line);
            }
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[0].equals(user.getUsername())) {
                    lines.add(user.getUsername() + "," + user.getAnnualTarget() + "," + user.getMonthlyTarget() + ","
                            + user.getMonthlyBudget() + "," + user.getTransportationBudget() + "," + user.getShoppingBudget() + ","
                            + user.getOtherBudget() + "," + user.getEntertainmentBudget() + ","
                            + user.getSavedAmount() + "," + user.getAnnualSavedAmount() + ","
                            + user.getCurrentYear() + "," + user.getCurrentMonth() + ","
                            + user.getAnnualBudget()); // 新增：保存年总预算
                    found = true;
                } else {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading settings file: " + e.getMessage());
        }

        if (!found) {
            lines.add(user.getUsername() + "," + user.getAnnualTarget() + "," + user.getMonthlyTarget() + ","
                    + user.getMonthlyBudget() + "," + user.getTransportationBudget() + "," + user.getShoppingBudget() + ","
                    + user.getOtherBudget() + "," + user.getEntertainmentBudget() + ","
                    + user.getSavedAmount() + "," + user.getAnnualSavedAmount() + ","
                    + user.getCurrentYear() + "," + user.getCurrentMonth() + ","
                    + user.getAnnualBudget()); // 新增：保存年总预算
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(tempFile))) {
            for (String l : lines) {
                writer.println(l);
            }
        } catch (IOException e) {
            System.err.println("Error writing to temporary settings file: " + e.getMessage());
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
            saveUserSettings(user);
        }
    }

    // 初始化savedAmount和annualSavedAmount
    private void initializeSavedAmounts(User user) {
        double monthlyExpenses = getMonthlyTotalExpenses(user);
        double annualExpenses = getAnnualTotalExpenses(user);
        user.setSavedAmount(3000 - monthlyExpenses);
        System.out.println(monthlyExpenses);
        System.out.println(annualExpenses);
        user.setAnnualSavedAmount(36000 - annualExpenses);
        saveUserSettings(user);
    }

    public double getMonthlyTotalExpenses(User user) {
        LocalDate currentDate = LocalDate.now();
        int currentYear = currentDate.getYear();
        int currentMonth = currentDate.getMonthValue();
        double totalExpenses = 0.0;
        double totalIncome = 0.0;

        String transactionFile = user.getUsername() + "_transactions.csv";
        try (BufferedReader br = new BufferedReader(new FileReader(transactionFile))) {
            String line;
            br.readLine(); // 跳过标题行
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 7 && parts[1].equals(user.getUsername())) {
                    LocalDate date = LocalDate.parse(parts[3]);
                    double amount = Double.parseDouble(parts[4]);

                    if (date.getYear() == currentYear && date.getMonthValue() == currentMonth) {
                        if (amount < 0) {
                            totalIncome += Math.abs(amount);
                            //totalExpense += Math.abs(amount);
                        } else {
                            totalExpenses += amount;
                            //totalIncome += amount;
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("Transaction file not found: " + transactionFile + ". Please check the file path.");
        } catch (IOException e) {
            System.err.println("Error reading transaction file: " + e.getMessage());
        }
        return totalExpenses - totalIncome;
    }

    public double getAnnualTotalExpenses(User user) {
        LocalDate currentDate = LocalDate.now();
        int currentYear = currentDate.getYear();
        double totalExpenses = 0.0;
        double totalIncome = 0.0;

        String transactionFile = user.getUsername() + "_transactions.csv";
        try (BufferedReader br = new BufferedReader(new FileReader(transactionFile))) {
            String line;
            br.readLine(); // 跳过标题行
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 7 && parts[1].equals(user.getUsername())) {
                    LocalDate date = LocalDate.parse(parts[3]);
                    double amount = Double.parseDouble(parts[4]);

                    if (date.getYear() == currentYear) {
                        if (amount < 0) {
                            totalIncome += Math.abs(amount);
                            //totalExpense += Math.abs(amount);
                        } else {
                            totalExpenses += amount;
                            //totalIncome += amount;
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("Transaction file not found: " + transactionFile + ". Please check the file path.");
        } catch (IOException e) {
            System.err.println("Error reading transaction file: " + e.getMessage());
        }
        return totalExpenses - totalIncome;
    }

    // 检查交易文件的变化并更新 savedAmount 和 annualSavedAmount
    public void checkTransactionsFile() {
        if (!isLoggedIn) {
            return;
        }

        User currentUser = DashboardView.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        String transactionFile = currentUser.getUsername() + "_transactions.csv";
        File file = new File(transactionFile);
        if (!file.exists()) {
            System.err.println("Transaction file not found: " + transactionFile + ". Please check the file path.");
            return;
        }

        List<String> lines;
        try {
            lines = Files.readAllLines(file.toPath());
        } catch (IOException e) {
            System.err.println("Error reading transaction file: " + e.getMessage());
            return;
        }

        if (lines.size() < 2) return; // 没有实际交易数据
        List<String> currentTransactions = lines.subList(1, lines.size());
        List<String> lastTransactions = getLastTransactions(currentUser.getUsername());

        List<String> newTransactions = new ArrayList<>(currentTransactions);
        newTransactions.removeAll(lastTransactions);

        List<String> removedTransactions = new ArrayList<>(lastTransactions);
        removedTransactions.removeAll(currentTransactions);

        updateSavedAmounts(newTransactions, removedTransactions);
        saveLastTransactions(currentUser.getUsername(), currentTransactions);

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

                if (date.getYear() == LocalDate.now().getYear() && date.getMonthValue() == LocalDate.now().getMonthValue()) {
                    newMonthlySpent += amount;
                    //newMonthlySpent -= amount;
                }
                if (date.getYear() == LocalDate.now().getYear()) {
                    newYearlySpent += amount;
                    //newYearlySpent -= amount;
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

                if (date.getYear() == LocalDate.now().getYear() && date.getMonthValue() == LocalDate.now().getMonthValue()) {
                    removedMonthlySpent += amount;
                    //removedMonthlySpent -= amount;
                }
                if (date.getYear() == LocalDate.now().getYear()) {
                    removedYearlySpent += amount;
                    //removedYearlySpent -= amount;
                }
            }
        }

        currentUser.setSavedAmount(currentUser.getSavedAmount() - newMonthlySpent + removedMonthlySpent);
        currentUser.setAnnualSavedAmount(currentUser.getAnnualSavedAmount() - newYearlySpent + removedYearlySpent);
        saveUserSettings(currentUser);
    }

    // 获取上次检查时的交易记录
    private List<String> getLastTransactions(String username) {
        String lastTransactionsFile = username + "_lastTransactions.txt";
        File file = new File(lastTransactionsFile);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        List<String> lines;
        try {
            lines = Files.readAllLines(file.toPath());
        } catch (IOException e) {
            System.err.println("Error reading last transactions file: " + e.getMessage());
            return new ArrayList<>();
        }
        return lines;
    }

    // 保存当前检查时的交易记录
    private void saveLastTransactions(String username, List<String> transactions) {
        String lastTransactionsFile = username + "_lastTransactions.txt";
        File file = new File(lastTransactionsFile);
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            for (String line : transactions) {
                writer.println(line);
            }
        } catch (IOException e) {
            System.err.println("Error writing to last transactions file: " + e.getMessage());
        }
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
            double budgetPerCategory = newBudget / 4;
            user.setTransportationBudget(budgetPerCategory);
            user.setShoppingBudget(budgetPerCategory);
            user.setOtherBudget(budgetPerCategory);
            user.setEntertainmentBudget(budgetPerCategory);

            saveUserSettings(user);
            return true;
        }
        return false;
    }

    // 获取每种消费类型的本月总支出
    public double getMonthlyExpensesByCategory(User user, String category) {
        LocalDate currentDate = LocalDate.now();
        int currentYear = currentDate.getYear();
        int currentMonth = currentDate.getMonthValue();
        double totalExpenses = 0.0;
        double totalIncome = 0.0;

        String transactionFile = user.getUsername() + "_transactions.csv";
        try (BufferedReader br = new BufferedReader(new FileReader(transactionFile))) {
            String line;
            br.readLine(); // 跳过标题行
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 7 && parts[1].equals(user.getUsername()) && parts[5].equals(category)) {
                    LocalDate date = LocalDate.parse(parts[3]);
                    double amount = Double.parseDouble(parts[4]);

                    if (date.getYear() == currentYear && date.getMonthValue() == currentMonth) {
                        if (amount < 0) {
                            totalIncome += Math.abs(amount);
                            //totalExpense += Math.abs(amount);
                        } else {
                            totalExpenses += amount;
                            //totalIncome += amount;
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("Transaction file not found: " + transactionFile + ". Please check the file path.");
        } catch (IOException e) {
            System.err.println("Error reading transaction file: " + e.getMessage());
        }
        return totalExpenses - totalIncome;
    }

    // 检查并创建用户交易记录文件
    private void checkAndCreateTransactionFile(String username) {
        String transactionFile = username + "_transactions.csv";
        File file = new File(transactionFile);
        if (!file.exists()) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                writer.println("id,username,source,date,amount,category,description");
            } catch (IOException e) {
                System.err.println("Failed to create transaction file: " + transactionFile + ". Error: " + e.getMessage());
            }
        }
    }

    // 添加用户的方法
    public boolean addUser(String username, String password) {
        if (getUser(username) != null) {
            return false; // 用户已存在
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(usersFile, true))) {
            bw.write(username + "," + password);
            bw.newLine();
            return true;
        } catch (IOException e) {
            System.err.println("Error writing to users file: " + e.getMessage());
            return false;
        }
    }

    // 删除用户的方法
    public boolean removeUser(String username) {
        File inputFile = new File(usersFile);
        File tempFile = new File("users_temp.csv");

        List<String> lines = new ArrayList<>();
        boolean found = false;
        try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
            String line = br.readLine();
            if (line != null) { // 写入标题行
                lines.add(line);
            }
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2 && parts[0].equals(username)) {
                    found = true;
                } else {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading users file: " + e.getMessage());
            return false;
        }

        if (!found) {
            return false; // 用户未找到
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(tempFile))) {
            for (String l : lines) {
                writer.println(l);
            }
        } catch (IOException e) {
            System.err.println("Error writing to temporary file: " + e.getMessage());
            return false;
        }

        if (!inputFile.delete()) {
            System.out.println("Could not delete original file.");
            return false;
        }
        if (!tempFile.renameTo(inputFile)) {
            System.out.println("Could not rename temp file.");
            return false;
        }
        return true;
    }
}