package model;

import java.util.Locale;

/**
 * Работа с денежными суммами в double. Все суммы округляются до копеек,
 * чтобы погрешность double не накапливалась (0.1 + 0.2 = 0.30000000000000004).
 */
public final class Money {

    private Money() {
    }

    /** Округление до двух знаков после запятой. */
    public static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    /** Проверяет, что значение — положительное число, и округляет его. */
    public static double checkPositive(double value) {
        double rounded = round(value);
        if (rounded <= 0) {
            throw new IllegalArgumentException("Число должно быть положительным!");
        }
        return rounded;
    }

    /** Формат для вывода: всегда два знака после точки, например 1500.00. */
    public static String format(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }
}