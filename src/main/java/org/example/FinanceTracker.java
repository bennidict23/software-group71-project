package org.example;

// File: FinanceTracker.java
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import org.jfree.chart.*;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

// Transaction数据模型
class Transaction {
    LocalDate date;
    double amount;
    String category;
    String description;

    public Transaction(LocalDate date, double amount, String description) {
        this.date = date;
        this.amount = amount;
        this.description = description;
        this.category = "Uncategorized";
    }
}

// 自定义Table Model
class TransactionTableModel extends AbstractTableModel {
    private final List<Transaction> transactions = new ArrayList<>();
    private final String[] COLUMNS = {"Date", "Amount", "Category", "Description"};

    @Override
    public int getRowCount() { return transactions.size(); }

    @Override
    public int getColumnCount() { return COLUMNS.length; }

    @Override
    public String getColumnName(int column) { return COLUMNS[column]; }

    @Override
    public Object getValueAt(int row, int column) {
        Transaction t = transactions.get(row);
        return switch (column) {
            case 0 -> t.date;
            case 1 -> String.format("¥%.2f", t.amount);
            case 2 -> t.category;
            case 3 -> t.description;
            default -> null;
        };
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return column == 2; // 只允许编辑Category列
    }

    @Override
    public void setValueAt(Object value, int row, int column) {
        if (column == 2) {
            transactions.get(row).category = (String) value;
            fireTableCellUpdated(row, column);
        }
    }

    public void addTransaction(Transaction t) {
        transactions.add(t);
        fireTableRowsInserted(transactions.size()-1, transactions.size()-1);
    }

    public List<Transaction> getTransactions() { return transactions; }
}

// 主界面
public class FinanceTracker extends JFrame {
    private final TransactionTableModel model = new TransactionTableModel();
    private final JTable table = new JTable(model);
    private final JFileChooser fileChooser = new JFileChooser();

    public FinanceTracker() {
        // 初始化界面
        setTitle("AI Finance Tracker");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // 菜单栏
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem importItem = new JMenuItem("Import CSV");
        JMenuItem saveItem = new JMenuItem("Save Data");
        JMenuItem exitItem = new JMenuItem("Exit");

        importItem.addActionListener(e -> importCSV());
        saveItem.addActionListener(e -> saveData());
        exitItem.addActionListener(e -> System.exit(0));

        fileMenu.add(importItem);
        fileMenu.add(saveItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        // 工具栏
        JPanel toolbar = new JPanel();
        JButton addButton = new JButton("Add Transaction");
        JButton chartButton = new JButton("Show Chart");

        addButton.addActionListener(e -> showAddDialog());
        chartButton.addActionListener(e -> showChart());

        toolbar.add(addButton);
        toolbar.add(chartButton);

        // 主布局
        add(toolbar, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // 加载初始数据
        loadData();
    }

    private void showAddDialog() {
        JDialog dialog = new JDialog(this, "Add Transaction", true);
        JPanel panel = new JPanel(new GridLayout(4, 2));

        JTextField dateField = new JTextField(LocalDate.now().toString());
        JTextField amountField = new JTextField();
        JTextField descField = new JTextField();
        JButton saveButton = new JButton("Save");

        panel.add(new JLabel("Date (YYYY-MM-DD):"));
        panel.add(dateField);
        panel.add(new JLabel("Amount:"));
        panel.add(amountField);
        panel.add(new JLabel("Description:"));
        panel.add(descField);
        panel.add(new JLabel());
        panel.add(saveButton);

        saveButton.addActionListener(e -> {
            try {
                LocalDate date = LocalDate.parse(dateField.getText());
                double amount = Double.parseDouble(amountField.getText());
                Transaction t = new Transaction(date, amount, descField.getText());
                predictCategory(t); // AI分类
                model.addTransaction(t);
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input format");
            }
        });

        dialog.add(panel);
        dialog.pack();
        dialog.setVisible(true);
    }

    private void predictCategory(Transaction t) {
        // 基于关键词的简单AI分类
        String desc = t.description.toLowerCase();
        if (desc.contains("restaurant") || desc.contains("coffee")) {
            t.category = "Food";
        } else if (desc.contains("metro") || desc.contains("gas")) {
            t.category = "Transport";
        } else if (desc.contains("movie") || desc.contains("concert")) {
            t.category = "Entertainment";
        } else {
            t.category = "Other";
        }
    }

    private void importCSV() {
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                Files.lines(Paths.get(fileChooser.getSelectedFile().getPath()))
                        .skip(1) // 跳过标题行
                        .forEach(line -> {
                            String[] parts = line.split(",");
                            if (parts.length >= 3) {
                                Transaction t = new Transaction(
                                        LocalDate.parse(parts[0]),
                                        Double.parseDouble(parts[1]),
                                        parts[2]
                                );
                                predictCategory(t);
                                model.addTransaction(t);
                            }
                        });
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error reading file");
            }
        }
    }

    private void saveData() {
        try (PrintWriter writer = new PrintWriter("transactions.csv")) {
            writer.println("Date,Amount,Category,Description");
            for (Transaction t : model.getTransactions()) {
                writer.printf("%s,%.2f,%s,%s\n",
                        t.date, t.amount, t.category, t.description);
            }
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Error saving data");
        }
    }

    private void loadData() {
        if (new File("transactions.csv").exists()) {
            try {
                Files.lines(Paths.get("transactions.csv"))
                        .skip(1)
                        .forEach(line -> {
                            String[] parts = line.split(",");
                            if (parts.length >= 4) {
                                Transaction t = new Transaction(
                                        LocalDate.parse(parts[0]),
                                        Double.parseDouble(parts[1]),
                                        parts[3]
                                );
                                t.category = parts[2];
                                model.addTransaction(t);
                            }
                        });
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error loading data");
            }
        }
    }

    private void showChart() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        model.getTransactions().stream()
                .collect(Collectors.groupingBy(t -> t.category,
                        Collectors.summingDouble(t -> t.amount)))
                .forEach(dataset::setValue);

        JFreeChart chart = ChartFactory.createPieChart(
                "Spending Analysis", dataset, true, true, false);

        ChartFrame frame = new ChartFrame("Chart", chart);
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FinanceTracker().setVisible(true));
    }
}
