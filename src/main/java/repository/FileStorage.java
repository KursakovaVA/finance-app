package repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import model.Expense;
import model.Income;
import model.Transaction;
import model.User;
import model.Wallet;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FileStorage {

    private static final Path DATA_DIR = Paths.get("data");
    private static final Path USERS_FILE = DATA_DIR.resolve("users.json");
    private static final Path WALLETS_DIR = DATA_DIR.resolve("wallets");

    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .disableHtmlEscaping()
            .create();

    public void saveUsers(List<User> users) {
        List<UserData> list = new ArrayList<>();
        for (User user : users) {
            UserData data = new UserData();
            data.login = user.getLogin();
            data.passwordHash = user.getPasswordHash();
            list.add(data);
        }
        writeFile(USERS_FILE, gson.toJson(list));
    }

    public void loadUsers(UserRepository userRepository) {
        if (!Files.exists(USERS_FILE)) {
            return;
        }
        try {
            UserData[] list = gson.fromJson(readFile(USERS_FILE), UserData[].class);
            if (list == null) {
                return;
            }
            for (UserData data : list) {
                userRepository.add(new User(data.login, data.passwordHash));
            }
        } catch (JsonParseException e) {
            throw new RuntimeException("Файл " + USERS_FILE + " повреждён: " + e.getMessage());
        }
    }

    public void saveWallet(User user) {
        Wallet wallet = user.getWallet();
        WalletData data = new WalletData();

        data.incomeCategories = wallet.getIncomeCategories();

        for (String category : wallet.getExpenseCategories()) {
            data.expenseCategories.put(category, wallet.getBudget(category));
        }

        for (Transaction transaction : wallet.getTransactions()) {
            TransactionData item = new TransactionData();
            if (transaction instanceof Income) {
                item.type = "INCOME";
            } else {
                item.type = "EXPENSE";
            }
            item.dateTime = transaction.getDateTime().toString();
            item.amount = transaction.getAmount();
            item.category = transaction.getCategory();
            item.description = transaction.getDescription();
            data.transactions.add(item);
        }

        writeFile(walletFile(user), gson.toJson(data));
    }

    public void loadWallet(User user) {
        Path file = walletFile(user);
        if (!Files.exists(file)) {
            return;
        }
        try {
            WalletData data = gson.fromJson(readFile(file), WalletData.class);
            if (data == null) {
                return;  // файл пустой
            }
            List<Transaction> loaded = new ArrayList<>();
            for (TransactionData item : data.transactions) {
                LocalDateTime dateTime = LocalDateTime.parse(item.dateTime);
                if ("INCOME".equals(item.type)) {
                    loaded.add(new Income(item.amount, dateTime, item.category, item.description));
                } else if ("EXPENSE".equals(item.type)) {
                    loaded.add(new Expense(item.amount, dateTime, item.category, item.description));
                } else {
                    throw new RuntimeException("Неизвестный тип операции в файле: " + item.type);
                }
            }

            Wallet wallet = user.getWallet();
            for (String category : data.incomeCategories) {
                wallet.addIncomeCategory(category);
            }
            for (Map.Entry<String, Double> entry : data.expenseCategories.entrySet()) {
                wallet.addExpenseCategory(entry.getKey());
                if (entry.getValue() != null) {
                    wallet.setBudget(entry.getKey(), entry.getValue());
                }
            }
            for (Transaction transaction : loaded) {
                wallet.addTransaction(transaction);
            }
        } catch (DateTimeParseException e) {
            throw new RuntimeException("Файл " + file + " повреждён: неверная дата " + e.getParsedString());
        } catch (JsonParseException e) {
            throw new RuntimeException("Файл " + file + " повреждён: " + e.getMessage());
        }
    }

    private Path walletFile(User user) {
        return WALLETS_DIR.resolve(user.getLogin() + ".json");
    }

    private void writeFile(Path file, String content) {
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось сохранить файл " + file + ": " + e.getMessage());
        }
    }

    private String readFile(Path file) {
        try {
            return Files.readString(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось прочитать файл " + file + ": " + e.getMessage());
        }
    }
}
