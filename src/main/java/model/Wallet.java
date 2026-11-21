package model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Wallet {

    private List<Transaction> transactions;
    private List<String> incomeCategories;
    private Map<String, Double> expenseCategories;

    public Wallet() {
        transactions = new ArrayList<>();
        incomeCategories = new ArrayList<>();
        expenseCategories = new LinkedHashMap<>();
    }

    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
    }

    public boolean addIncomeCategory(String category) {
        if (incomeCategories.contains(category)) {
            return false;
        }
        incomeCategories.add(category);
        return true;
    }

    public boolean addExpenseCategory(String category) {
        if (expenseCategories.containsKey(category)) {
            return false;
        }
        expenseCategories.put(category, null);
        return true;
    }

    public void renameCategory(boolean income, String oldName, String newName) {
        boolean oldExists = income ? hasIncomeCategory(oldName) : hasExpenseCategory(oldName);
        boolean newExists = income ? hasIncomeCategory(newName) : hasExpenseCategory(newName);

        if (!oldExists) {
            throw new IllegalArgumentException("Категория не найдена: " + oldName);
        }
        if (newExists) {
            throw new IllegalArgumentException("Имя категории уже занято: " + newName);
        }

        if (income) {
            int index = incomeCategories.indexOf(oldName);
            incomeCategories.set(index, newName);
        } else {
            Double limit = expenseCategories.remove(oldName);
            expenseCategories.put(newName, limit);
        }

        for (Transaction transaction : transactions) {
            boolean sameType = income ? transaction instanceof Income : transaction instanceof Expense;

            if (sameType && oldName.equals(transaction.getCategory())) {
                transaction.renameCategory(newName);
            }
        }
    }

    public boolean hasIncomeCategory(String category) {
        return incomeCategories.contains(category);
    }

    public boolean hasExpenseCategory(String category) {
        return expenseCategories.containsKey(category);
    }

    public void setBudget(String category, double limit) {
        double checkedLimit = Money.checkPositive(limit);
        expenseCategories.put(category, checkedLimit);
    }

    public boolean hasBudget(String category) {
        return expenseCategories.get(category) != null;
    }

    public Double getBudget(String category) {
        return expenseCategories.get(category);
    }

    public Map<String, Double> getBudgets() {
        Map<String, Double> result = new LinkedHashMap<>();
        for (String category : expenseCategories.keySet()) {
            Double limit = expenseCategories.get(category);
            if (limit != null) {
                result.put(category, limit);
            }
        }
        return result;
    }

    public List<Transaction> getTransactions() {
        return new ArrayList<>(transactions);
    }

    public List<String> getIncomeCategories() {
        return new ArrayList<>(incomeCategories);
    }

    public List<String> getExpenseCategories() {
        return new ArrayList<>(expenseCategories.keySet());
    }

    public double getBalance() {
        double sum = 0;
        for (Transaction transaction : transactions) {
            sum += transaction.signedAmount();
        }
        return Money.round(sum);
    }
}