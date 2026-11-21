package util;

public class InputValidator {

    private static final double MAX_AMOUNT = 1_000_000_000;

    public static void requireArgs(String[] parts, int count, String usage) {
        if (parts.length < count) {
            throw new IllegalArgumentException("Не хватает данных. Формат: " + usage);
        }
    }

    public static double parseAmount(String text) {
        String normalized = text.replace(',', '.');
        if (!normalized.matches("\\d+(\\.\\d{1,2})?")) {
            throw new IllegalArgumentException("Некорректная сумма \"" + text
                    + "\": нужно положительное число, например 1500 или 99.90");
        }
        double value = Double.parseDouble(normalized);
        if (value <= 0) {
            throw new IllegalArgumentException("Сумма должна быть больше нуля");
        }
        if (value > MAX_AMOUNT) {
            throw new IllegalArgumentException("Слишком большая сумма, максимум 1 000 000 000");
        }
        return value;
    }
}
