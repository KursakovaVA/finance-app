package util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InputValidatorTest {
    @Test
    void acceptsDecimalComma() {
        assertEquals(99.90, InputValidator.parseAmount("99,90"), 0.00001);
    }

    @Test
    void rejectsZero() {
        assertThrows(IllegalArgumentException.class, () -> InputValidator.parseAmount("0"));
    }

    @Test
    void rejectsNegativeAmount() {
        assertThrows(IllegalArgumentException.class, () -> InputValidator.parseAmount("-1"));
    }

    @Test
    void rejectsFractionOfACent() {
        assertThrows(IllegalArgumentException.class, () -> InputValidator.parseAmount("1.001"));
    }

    @Test
    void rejectsAmountAboveMaximum() {
        assertThrows(IllegalArgumentException.class,
                () -> InputValidator.parseAmount("1000000000.01"));
    }
}
