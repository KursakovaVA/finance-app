package service;

import model.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StatisticsService {

    private AuthService authService;

    public StatisticsService(AuthService authService) {
        this.authService = authService;
    }

    private Wallet getWallet() {
        return authService.getCurrentUser().getWallet();
    }

    public double getTotalIncome() {
        double sum = 0;
        for (Transaction transaction : getWallet().getTransactions()) {
            if (transaction instanceof Income) {
                sum += transaction.getAmount();
            }
        }
        return Money.round(sum);
    }

    public double getTotalExpense() {
        double sum = 0;
        for (Transaction transaction : getWallet().getTransactions()) {
            if (transaction instanceof Expense) {
                sum += transaction.getAmount();
            }
        }
        return Money.round(sum);
    }

    public double getIncomeInCategory(String category) {
        double sum = 0;
        for (Transaction transaction : getWallet().getTransactions()) {
            if (transaction instanceof Income && transaction.getCategory().equals(category)) {
                sum += transaction.getAmount();
            }
        }
        return Money.round(sum);
    }

    public double getExpenseInCategory(String category) {
        double sum = 0;
        for (Transaction transaction : getWallet().getTransactions()) {
            if (transaction instanceof Expense && transaction.getCategory().equals(category)) {
                sum += transaction.getAmount();
            }
        }
        return Money.round(sum);
    }

    public Map<String, Double> getIncomeByCategory() {
        Map<String, Double> result = new LinkedHashMap<>();
        for (String category : getWallet().getIncomeCategories()) {
            result.put(category, getIncomeInCategory(category));
        }
        return result;
    }

    public Map<String, Double> getExpenseByCategory() {
        Map<String, Double> result = new LinkedHashMap<>();
        for (String category : getWallet().getExpenseCategories()) {
            result.put(category, getExpenseInCategory(category));
        }
        return result;
    }

    public double getRemainingBudget(String category) {
        Double limit = getWallet().getBudget(category);
        if (limit == null) {
            throw new IllegalArgumentException("Для категории \"" + category + "\" бюджет не установлен");
        }
        return Money.round(limit - getExpenseInCategory(category));
    }

    public List<String> findMissingCategories(List<String> categories) {
        Wallet wallet = getWallet();
        List<String> missing = new ArrayList<>();
        for (String category : categories) {
            if (!wallet.hasIncomeCategory(category) && !wallet.hasExpenseCategory(category)) {
                missing.add(category);
            }
        }
        return missing;
    }

    public double getIncomeForCategories(List<String> categories) {
        double sum = 0;
        for (String category : categories) {
            sum += getIncomeInCategory(category);
        }
        return Money.round(sum);
    }

    public double getExpenseForCategories(List<String> categories) {
        double sum = 0;
        for (String category : categories) {
            sum += getExpenseInCategory(category);
        }
        return Money.round(sum);
    }
}