package model;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WalletTest {
    private final Wallet wallet = new Wallet();
    private final LocalDateTime date = LocalDateTime.of(2026, 1, 1, 12, 0);

    @Test
    void balanceIncludesIncomeAndExpense() {
        wallet.addTransaction(new Income(100, date, "Работа", ""));
        wallet.addTransaction(new Expense(25.40, date, "Еда", ""));
        assertEquals(74.60, wallet.getBalance(), 0.00001);
        assertEquals(2, wallet.getTransactions().size());
    }

    @Test
    void duplicateCategoryIsRejected() {
        assertTrue(wallet.addExpenseCategory("Еда"));
        assertFalse(wallet.addExpenseCategory("Еда"));
        assertEquals(List.of("Еда"), wallet.getExpenseCategories());
    }

    @Test
    void updatingBudgetReplacesLimit() {
        wallet.addExpenseCategory("Еда");
        wallet.setBudget("Еда", 50);
        wallet.setBudget("Еда", 80);
        assertEquals(80, wallet.getBudget("Еда"), 0.00001);
    }

    @Test
    void renameMovesBudgetAndOnlyMatchingExpenses() {
        wallet.addIncomeCategory("Общее");
        wallet.addExpenseCategory("Общее");
        wallet.setBudget("Общее", 100);
        wallet.addTransaction(new Income(50, date, "Общее", ""));
        wallet.addTransaction(new Expense(10, date, "Общее", ""));

        wallet.renameCategory(false, "Общее", "Еда");

        assertFalse(wallet.hasExpenseCategory("Общее"));
        assertTrue(wallet.hasIncomeCategory("Общее"));
        assertEquals(100, wallet.getBudget("Еда"), 0.00001);
        assertEquals("Общее", wallet.getTransactions().get(0).getCategory());
        assertEquals("Еда", wallet.getTransactions().get(1).getCategory());
    }

    @Test
    void renameIncomeUpdatesHistory() {
        wallet.addIncomeCategory("Работа");
        wallet.addTransaction(new Income(50, date, "Работа", ""));
        wallet.renameCategory(true, "Работа", "Зарплата");
        assertEquals(List.of("Зарплата"), wallet.getIncomeCategories());
        assertEquals("Зарплата", wallet.getTransactions().getFirst().getCategory());
    }

    @Test
    void renameConflictPreservesBothBudgets() {
        wallet.addExpenseCategory("Еда");
        wallet.addExpenseCategory("Кафе");
        wallet.setBudget("Еда", 100);
        wallet.setBudget("Кафе", 30);
        assertThrows(IllegalArgumentException.class,
                () -> wallet.renameCategory(false, "Еда", "Кафе"));
        assertEquals(100, wallet.getBudget("Еда"), 0.00001);
        assertEquals(30, wallet.getBudget("Кафе"), 0.00001);
    }
}
