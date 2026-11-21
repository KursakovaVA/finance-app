package repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WalletData {
    public List<String> incomeCategories = new ArrayList<>();
    public Map<String, Double> expenseCategories = new LinkedHashMap<>();
    public List<TransactionData> transactions = new ArrayList<>();
}
