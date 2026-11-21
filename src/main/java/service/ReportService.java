package service;

import model.Money;
import model.Transaction;
import model.Wallet;

import java.util.List;
import java.util.Map;

public class ReportService {

    private final AuthService authService;
    private final StatisticsService statisticsService;

    public ReportService(AuthService authService, StatisticsService statisticsService) {
        this.authService = authService;
        this.statisticsService = statisticsService;
    }

    private Wallet getWallet() {
        return authService.getCurrentUser().getWallet();
    }

    public String buildFullReport() {
        Wallet wallet = getWallet();
        StringBuilder sb = new StringBuilder();

        sb.append("===== Отчёт пользователя ")
                .append(authService.getCurrentUser().getLogin()).append(" =====\n");
        sb.append("Баланс: ").append(Money.format(wallet.getBalance())).append("\n\n");

        sb.append("Общий доход: ").append(Money.format(statisticsService.getTotalIncome())).append("\n");
        Map<String, Double> incomes = statisticsService.getIncomeByCategory();
        if (incomes.isEmpty()) {
            sb.append("  категорий доходов пока нет\n");
        } else {
            sb.append("Доходы по категориям:\n");
            for (String category : incomes.keySet()) {
                sb.append("  ").append(category).append(": ")
                        .append(Money.format(incomes.get(category))).append("\n");
            }
        }
        sb.append("\n");

        sb.append("Общие расходы: ").append(Money.format(statisticsService.getTotalExpense())).append("\n");
        Map<String, Double> expenses = statisticsService.getExpenseByCategory();
        if (expenses.isEmpty()) {
            sb.append("  категорий расходов пока нет\n");
        } else {
            sb.append("Расходы по категориям:\n");
            for (String category : expenses.keySet()) {
                sb.append("  ").append(category).append(": ")
                        .append(Money.format(expenses.get(category))).append("\n");
            }
        }
        sb.append("\n");

        Map<String, Double> budgets = wallet.getBudgets();
        if (budgets.isEmpty()) {
            sb.append("Бюджеты не установлены\n");
        } else {
            sb.append("Бюджет по категориям:\n");
            for (String category : budgets.keySet()) {
                sb.append("  ").append(budgetLine(category)).append("\n");
            }
        }
        return sb.toString();
    }

    public String buildCategoriesReport(List<String> categories) {
        Wallet wallet = getWallet();
        StringBuilder sb = new StringBuilder();

        List<String> missing = statisticsService.findMissingCategories(categories);
        for (String category : missing) {
            sb.append("Категория не найдена: ").append(category).append("\n");
        }
        if (missing.size() == categories.size()) {
            sb.append("Ни одна из указанных категорий не найдена\n");
            return sb.toString();
        }

        sb.append("===== Статистика по выбранным категориям =====\n");
        for (String category : categories) {
            if (wallet.hasIncomeCategory(category)) {
                sb.append("  Доход   | ").append(category).append(": ")
                        .append(Money.format(statisticsService.getIncomeInCategory(category))).append("\n");
            }
            if (wallet.hasExpenseCategory(category)) {
                sb.append("  Расход  | ").append(category).append(": ")
                        .append(Money.format(statisticsService.getExpenseInCategory(category)));
                if (wallet.hasBudget(category)) {
                    sb.append(" (").append(budgetLine(category)).append(")");
                }
                sb.append("\n");
            }
        }
        sb.append("Итого доходов: ")
                .append(Money.format(statisticsService.getIncomeForCategories(categories))).append("\n");
        sb.append("Итого расходов: ")
                .append(Money.format(statisticsService.getExpenseForCategories(categories))).append("\n");
        return sb.toString();
    }

    public String buildHistory() {
        List<Transaction> transactions = getWallet().getTransactions();
        if (transactions.isEmpty()) {
            return "Операций пока нет";
        }
        StringBuilder sb = new StringBuilder("===== История операций =====\n");
        for (Transaction transaction : transactions) {
            sb.append("  ").append(transaction).append("\n");
        }
        return sb.toString();
    }

    private String budgetLine(String category) {
        double limit = getWallet().getBudget(category);
        double spent = statisticsService.getExpenseInCategory(category);
        double remaining = statisticsService.getRemainingBudget(category);

        String line = category + ": лимит " + Money.format(limit)
                + ", потрачено " + Money.format(spent);
        if (remaining < 0) {
            return line + ", ПРЕВЫШЕН на " + Money.format(-remaining);
        }
        return line + ", осталось " + Money.format(remaining);
    }
}