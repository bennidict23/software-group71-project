package org.example.list;

import java.time.LocalDate;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Transaction {
    private final IntegerProperty id;
    private final StringProperty username;
    private final StringProperty source;

    private final ObjectProperty<LocalDate> date;
    private final DoubleProperty amount;
    private final StringProperty category;
    private final StringProperty description;

    public Transaction(int id, String username, String source, LocalDate date,
            double amount, String category, String description) {
        this.id = new SimpleIntegerProperty(id);
        this.username = new SimpleStringProperty(username);
        this.source = new SimpleStringProperty(source);
        this.date = new SimpleObjectProperty<>(date);
        this.amount = new SimpleDoubleProperty(amount);
        this.category = new SimpleStringProperty(category);
        this.description = new SimpleStringProperty(description);
    }

    // JavaFX属性访问方法
    public IntegerProperty idProperty() {
        return id;
    }

    public StringProperty usernameProperty() {
        return username;
    }

    public StringProperty sourceProperty() {
        return source;
    }

    public ObjectProperty<LocalDate> dateProperty() {
        return date;
    }

    public DoubleProperty amountProperty() {
        return amount;
    }

    public StringProperty categoryProperty() {
        return category;
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    // 常规getter方法
    public int getId() {
        return id.get();
    }

    public String getUsername() {
        return username.get();
    }

    public String getSource() {
        return source.get();
    }

    public LocalDate getDate() {
        return date.get();
    }

    public double getAmount() {
        return amount.get();
    }

    public String getCategory() {
        return category.get();
    }

    public String getDescription() {
        return description.get();
    }

    // 添加setter方法用于支持编辑功能
    public void setId(int id) {
        this.id.set(id);
    }

    public void setUsername(String user) {
        this.username.set(user);
    }

    public void setSource(String source) {
        this.source.set(source);
    }

    public void setDate(LocalDate date) {
        this.date.set(date);
    }

    public void setAmount(double amount) {
        this.amount.set(amount);
    }

    public void setCategory(String category) {
        this.category.set(category);
    }

    public void setDescription(String description) {
        this.description.set(description);
    }
}