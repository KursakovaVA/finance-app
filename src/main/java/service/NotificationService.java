package service;

import model.Money;
import model.Wallet;

import java.util.ArrayList;
import java.util.List;

public class NotificationService {

    private static final double WARNING_THRESHOLD = 0.2;

    private AuthService authService;
    private StatisticsService statisticsService;

    public NotificationService(AuthService authService, StatisticsService statisticsService) {
        this.authService = authService;
        this.statisticsService = statisticsService;
    }

    private Wallet getWallet() {
        return authService.getCurrentUser().getWallet();
    }

    public String checkBalance() {
        double income = statisticsService.getTotalIncome();
        double expense = statisticsService.getTotalExpense();
        if (expense > income) {
            return "ВНИМАНИЕ: расходы (" + Money.format(expense) + ") превышают доходы ("
                    + Money.format(income) + ")";
        }
        return null;
    }

    public String checkBudget(String category) {
        Wallet wallet = getWallet();
        if (!wallet.hasBudget(category)) {
            return null;
        }
        double limit = wallet.getBudget(category);
        double spent = statisticsService.getExpenseInCategory(category);
        double remaining = statisticsService.getRemainingBudget(category);

        if (remaining < 0) {
            return "ВНИМАНИЕ: превышен лимит по категории \"" + category + "\": потрачено "
                    + Money.format(spent) + " из " + Money.format(limit)
                    + ", перерасход " + Money.format(-remaining);
        }
        if (remaining == 0) {
            return "ВНИМАНИЕ: лимит по категории \"" + category + "\" исчерпан полностью";
        }
        if (remaining <= limit * WARNING_THRESHOLD) {
            return "Предупреждение: по категории \"" + category + "\" осталось "
                    + Money.format(remaining) + " из " + Money.format(limit);
        }
        return null;
    }

    public List<String> checkAfterExpense(String category) {
        List<String> warnings = new ArrayList<>();
        String budgetWarning = checkBudget(category);
        if (budgetWarning != null) {
            warnings.add(budgetWarning);
        }
        String balanceWarning = checkBalance();
        if (balanceWarning != null) {
            warnings.add(balanceWarning);
        }
        return warnings;
    }

    public List<String> checkAll() {
        List<String> warnings = new ArrayList<>();
        for (String category : getWallet().getBudgets().keySet()) {
            String budgetWarning = checkBudget(category);
            if (budgetWarning != null) {
                warnings.add(budgetWarning);
            }
        }
        String balanceWarning = checkBalance();
        if (balanceWarning != null) {
            warnings.add(balanceWarning);
        }
        return warnings;
    }
}