package model;

import java.time.LocalDateTime;

public abstract class Transaction {

    private double amount;
    private LocalDateTime dateTime;
    private String category;
    private String description;

    public Transaction(double amount, LocalDateTime dateTime, String category, String description) {
        this.amount = Money.checkPositive(amount);
        this.dateTime = dateTime;
        this.category = category;
        if (description == null) {
            this.description = "";
        } else {
            this.description = description;
        }
    }

    public abstract double signedAmount();

    public abstract String typeLabel();

    public double getAmount() {
        return amount;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    void renameCategory(String newName) {
        this.category = newName;
    }

    @Override
    public String toString() {
        String result = dateTime.toLocalDate() + " | " + typeLabel() + " | "
                + category + " | " + Money.format(amount);
        if (!description.isEmpty()) {
            result = result + " (" + description + ")";
        }
        return result;
    }
}