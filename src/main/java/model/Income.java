package model;

import java.time.LocalDateTime;

public class Income extends Transaction {

    public Income(double amount, LocalDateTime date, String category, String description) {
        super(amount, date, category, description);
    }

    @Override
    public double signedAmount() {
        return getAmount();
    }

    @Override
    public String typeLabel() {
        return "Доход";
    }
}