package org.example.list;
import javafx.beans.property.*;
import java.time.LocalDate;

public class Transaction {
    private final StringProperty user;
    private final StringProperty source;
    private final ObjectProperty<LocalDate> date;
    private final DoubleProperty amount;
    private final StringProperty category;
    private final StringProperty description;

    public Transaction(String user, String source, LocalDate date,
                      double amount, String category, String description) {
        this.user = new SimpleStringProperty(user);
        this.source = new SimpleStringProperty(source);
        this.date = new SimpleObjectProperty<>(date);
        this.amount = new SimpleDoubleProperty(amount);
        this.category = new SimpleStringProperty(category);
        this.description = new SimpleStringProperty(description);
    }

    // JavaFX属性访问方法
    public StringProperty userProperty() { return user; }
    public StringProperty sourceProperty() { return source; }
    public ObjectProperty<LocalDate> dateProperty() { return date; }
    public DoubleProperty amountProperty() { return amount; }
    public StringProperty categoryProperty() { return category; }
    public StringProperty descriptionProperty() { return description; }

    // 常规getter方法
    public String getUser() { return user.get(); }
    public String getSource() { return source.get(); }
    public LocalDate getDate() { return date.get(); }
    public double getAmount() { return amount.get(); }
    public String getCategory() { return category.get(); }
    public String getDescription() { return description.get(); }
}