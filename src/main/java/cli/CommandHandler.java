package cli;

import model.Money;
import model.Wallet;
import service.AuthService;
import service.ConsoleReportWriter;
import service.FileReportWriter;
import service.NotificationService;
import service.ReportService;
import service.ReportWriter;
import service.WalletService;
import util.InputValidator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandHandler {

    private final AuthService authService;
    private final WalletService walletService;
    private final NotificationService notificationService;
    private final ReportService reportService;
    private final ReportWriter console;

    public CommandHandler(AuthService authService, WalletService walletService,
                          NotificationService notificationService, ReportService reportService) {
        this.authService = authService;
        this.walletService = walletService;
        this.notificationService = notificationService;
        this.reportService = reportService;
        this.console = new ConsoleReportWriter();
    }

    public boolean handle(String line) {
        String[] parts = line.trim().split("\\s+");  // делим по пробелам
        String command = parts[0].toLowerCase();

        switch (command) {
            case "" -> { }
            case "help" -> printHelp();
            case "register" -> register(parts);
            case "login" -> login(parts);
            case "logout" -> logout();
            case "category" -> addCategory(parts);
            case "rename" -> renameCategory(parts);
            case "categories" -> printCategories();
            case "income" -> addIncome(parts);
            case "expense" -> addExpense(parts);
            case "budget" -> setBudget(parts);
            case "balance" -> printBalance();
            case "stats" -> printStats(parts);
            case "history" -> console.write(reportService.buildHistory());
            case "report" -> saveReport(parts);
            case "exit" -> {
                exit();
                return false;
            }
            default -> System.out.println("Неизвестная команда: " + command + ". Введите help");
        }
        return true;
    }

    public String getPrompt() {
        if (authService.isLoggedIn()) {
            return authService.getCurrentUser().getLogin() + "> ";
        }
        return "> ";
    }

    private void register(String[] parts) {
        InputValidator.requireArgs(parts, 3, "register <логин> <пароль>");
        authService.register(parts[1], parts[2]);
        System.out.println("Пользователь " + parts[1] + " зарегистрирован. Войдите: login " + parts[1] + " <пароль>");
    }

    private void login(String[] parts) {
        InputValidator.requireArgs(parts, 3, "login <логин> <пароль>");
        authService.login(parts[1], parts[2]);
        System.out.println("Добро пожаловать, " + parts[1] + "!");
        printWarnings(notificationService.checkAll());
    }

    private void logout() {
        authService.logout();
        System.out.println("Данные сохранены. Вы вышли из аккаунта");
    }

    private void exit() {
        if (authService.isLoggedIn()) {
            authService.logout();
            System.out.println("Данные сохранены.");
        }
        System.out.println("До свидания!");
    }

    private void addCategory(String[] parts) {
        InputValidator.requireArgs(parts, 3, "category income|expense <название>");
        String type = parts[1].toLowerCase();
        String name = parts[2];
        if (type.equals("income")) {
            walletService.addIncomeCategory(name);
            System.out.println("Категория доходов \"" + name + "\" создана");
        } else if (type.equals("expense")) {
            walletService.addExpenseCategory(name);
            System.out.println("Категория расходов \"" + name + "\" создана");
        } else {
            throw new IllegalArgumentException("Тип категории должен быть income или expense");
        }
    }

    private void renameCategory(String[] parts) {
        InputValidator.requireArgs(parts, 4, "rename income|expense <старое> <новое>");

        String type = parts[1].toLowerCase();
        String oldName = parts[2];
        String newName = parts[3];

        walletService.renameCategory(type, oldName, newName);
        System.out.println("Категория переименована: " + oldName + " → " + newName);
    }

    private void printCategories() {
        Wallet wallet = authService.getCurrentUser().getWallet();
        System.out.println("Категории доходов: " + listOrDash(wallet.getIncomeCategories()));
        System.out.println("Категории расходов: " + listOrDash(wallet.getExpenseCategories()));
    }

    private void addIncome(String[] parts) {
        InputValidator.requireArgs(parts, 3, "income <сумма> <категория> [описание]");
        double amount = InputValidator.parseAmount(parts[1]);
        walletService.addIncome(amount, parts[2], joinFrom(parts, 3));
        System.out.println("Доход добавлен. " + balanceText());
    }

    private void addExpense(String[] parts) {
        InputValidator.requireArgs(parts, 3, "expense <сумма> <категория> [описание]");
        double amount = InputValidator.parseAmount(parts[1]);
        String category = parts[2];
        walletService.addExpense(amount, category, joinFrom(parts, 3));
        System.out.println("Расход добавлен. " + balanceText());
        printWarnings(notificationService.checkAfterExpense(category));
    }

    private void setBudget(String[] parts) {
        InputValidator.requireArgs(parts, 3, "budget <категория> <лимит>");
        String category = parts[1];
        double limit = InputValidator.parseAmount(parts[2]);
        walletService.setBudget(category, limit);
        System.out.println("Лимит для \"" + category + "\" установлен: " + Money.format(limit));
        String warning = notificationService.checkBudget(category);
        if (warning != null) {
            System.out.println(warning);
        }
    }

    private void printBalance() {
        System.out.println(balanceText());
    }

    private void printStats(String[] parts) {
        if (parts.length == 1) {
            console.write(reportService.buildFullReport());
        } else {
            console.write(reportService.buildCategoriesReport(categoriesFrom(parts, 1)));
        }
    }

    private void saveReport(String[] parts) {
        InputValidator.requireArgs(parts, 2, "report <имя файла> [категории...]");
        String report;
        if (parts.length == 2) {
            report = reportService.buildFullReport();
        } else {
            report = reportService.buildCategoriesReport(categoriesFrom(parts, 2));
        }
        FileReportWriter writer = new FileReportWriter(parts[1]);
        writer.write(report);
        System.out.println("Отчёт сохранён в " + writer.getFile().toAbsolutePath());
    }

    private void printHelp() {
        System.out.println("""
                Аккаунт:
                  register <логин> <пароль>                 регистрация
                  login <логин> <пароль>                    вход
                  logout                                    выход с сохранением данных
                Категории (название — одно слово, регистр важен):
                  category income <название>                создать категорию доходов
                  category expense <название>               создать категорию расходов
                  rename income|expense <старое> <новое>    переименовать категорию
                  categories                                список категорий
                Операции:
                  income <сумма> <категория> [описание]
                  expense <сумма> <категория> [описание]
                  budget <категория> <лимит>                лимит на категорию расходов
                Информация:
                  balance                                   текущий баланс
                  stats                                     полный отчёт
                  stats <кат1> <кат2> ...                   отчёт по выбранным категориям
                  history                                   история операций
                  report <файл> [категории...]              сохранить отчёт в файл
                  help                                      эта справка
                  exit                                      выход из программы""");
    }

    private String balanceText() {
        return "Баланс: " + Money.format(authService.getCurrentUser().getWallet().getBalance());
    }

    private void printWarnings(List<String> warnings) {
        for (String warning : warnings) {
            System.out.println(warning);
        }
    }

    private String joinFrom(String[] parts, int start) {
        if (parts.length <= start) {
            return "";
        }
        return String.join(" ", Arrays.copyOfRange(parts, start, parts.length));
    }

    private List<String> categoriesFrom(String[] parts, int start) {
        List<String> result = new ArrayList<>();
        for (int i = start; i < parts.length; i++) {
            if (!result.contains(parts[i])) {
                result.add(parts[i]);
            }
        }
        return result;
    }

    private String listOrDash(List<String> list) {
        if (list.isEmpty()) {
            return "—";
        }
        return String.join(", ", list);
    }
}