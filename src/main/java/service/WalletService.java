package service;

import model.Expense;
import model.Income;
import model.Wallet;

import java.time.LocalDateTime;

public class WalletService {
    private AuthService authService;

    public WalletService(AuthService authService) {
        this.authService = authService;
    }

    private Wallet getWallet() {
        return authService.getCurrentUser().getWallet();
    }

    public void addIncomeCategory(String category) {
        checkCategoryName(category);
        if (!getWallet().addIncomeCategory(category)) {
            throw new IllegalArgumentException("Категория доходов \"" + category + "\" уже существует");
        }
    }

    public void addExpenseCategory(String category) {
        checkCategoryName(category);
        if (!getWallet().addExpenseCategory(category)) {
            throw new IllegalArgumentException("Категория расходов \"" + category + "\" уже существует");
        }
    }

    public void renameCategory(String type, String oldName, String newName) {
        checkCategoryName(oldName);
        checkCategoryName(newName);

        boolean income;
        if (type.equals("income")) {
            income = true;
        }
        else if (type.equals("expense")) {
            income = false;
        }
        else {
            throw new IllegalArgumentException("Тип категории должен быть income или expense");
        }

        getWallet().renameCategory(income, oldName, newName);
    }

    public void addIncome(double amount, String category, String description) {
        Wallet wallet = getWallet();
        if (!wallet.hasIncomeCategory(category)) {
            throw new IllegalArgumentException("Категория доходов \"" + category + "\" не найдена. "
                    + "Создайте её: category income " + category);
        }
        wallet.addTransaction(new Income(amount, LocalDateTime.now(), category, description));
    }

    public void addExpense(double amount, String category, String description) {
        Wallet wallet = getWallet();
        if (!wallet.hasExpenseCategory(category)) {
            throw new IllegalArgumentException("Категория расходов \"" + category + "\" не найдена. "
                    + "Создайте её: category expense " + category);
        }
        wallet.addTransaction(new Expense(amount, LocalDateTime.now(), category, description));
    }

    public void setBudget(String category, double limit) {
        Wallet wallet = getWallet();
        if (!wallet.hasExpenseCategory(category)) {
            throw new IllegalArgumentException("Категория расходов \"" + category + "\" не найдена. "
                    + "Создайте её: category expense " + category);
        }
        wallet.setBudget(category, limit);
    }

    private void checkCategoryName(String category) {
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("Название категории не может быть пустым");
        }
    }
}