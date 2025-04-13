package org.example;


import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom TableModel for managing Transaction data in a JTable.
 */
public class TransactionTableModel extends AbstractTableModel {
    private final List<Transaction> transactions = new ArrayList<>();
    private final String[] COLUMNS = {"Date", "Amount", "Category", "Description"};

    @Override
    public int getRowCount() {
        return transactions.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

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
        // 仅允许编辑 Category 列
        return column == 2;
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
        fireTableRowsInserted(transactions.size() - 1, transactions.size() - 1);
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }
}
