package com.pechatnikov.numbermnemocardsgeneratorbot.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Money Class Comprehensive Tests")
class MoneyTest {

    private final Currency USD = Currency.getInstance("USD");
    private final Currency EUR = Currency.getInstance("EUR");
    private final Currency RUB = Currency.getInstance("RUB");
    private final Currency JPY = Currency.getInstance("JPY"); // 0 fractional digits

    @Test
    @DisplayName("Should create Money with different constructors")
    void testConstructors() {
        Money money1 = new Money(new BigDecimal("100.50"), USD);
        Money money2 = new Money(100.50, USD);
        Money money3 = new Money("100.50", USD);
        Money money4 = Money.of(new BigDecimal("100.50"), USD);
        Money money5 = Money.of(100.50, USD);
        Money money6 = Money.of("100.50", USD);

        assertEquals(new BigDecimal("100.50"), money1.getAmount());
        assertEquals(USD, money1.getCurrency());
        assertEquals(money1, money2);
        assertEquals(money1, money3);
        assertEquals(money1, money4);
        assertEquals(money1, money5);
        assertEquals(money1, money6);
    }

    @Test
    @DisplayName("Should throw exception for null parameters")
    void testNullValidation() {
        assertThrows(NullPointerException.class, () -> new Money((BigDecimal) null, USD));
        assertThrows(NullPointerException.class, () -> new Money(BigDecimal.ONE, null));
    }

    @Test
    @DisplayName("Should create Money from minor units")
    void testFromMinorUnits() {
        Money fromCents = Money.fromMinorUnits(1999, USD);
        assertEquals(new BigDecimal("19.99"), fromCents.getAmount());
        assertEquals(USD, fromCents.getCurrency());

        Money fromKopeks = Money.fromMinorUnits(15050, RUB);
        assertEquals(new BigDecimal("150.50"), fromKopeks.getAmount());

        Money fromYen = Money.fromMinorUnits(2500, JPY);
        assertEquals(new BigDecimal("2500"), fromYen.getAmount());

        Money fromBigDecimal = Money.fromMinorUnits(new BigDecimal("1999"), USD);
        assertEquals(new BigDecimal("19.99"), fromBigDecimal.getAmount());
    }

    @Test
    @DisplayName("Should convert to minor units correctly")
    void testToMinorUnits() {
        Money money = new Money("123.45", USD);
        assertEquals(12345L, money.toMinorUnits());
        assertEquals(new BigDecimal("12345"), money.toMinorUnitsBigDecimal());

        Money jpyMoney = new Money("5000", JPY);
        assertEquals(5000L, jpyMoney.toMinorUnits());
    }

    @Test
    @DisplayName("Should handle fractional parts correctly")
    void testFractionalParts() {
        Money money = new Money("123.45", USD);

        assertEquals(new BigDecimal("123"), money.getIntegerPart());
        assertEquals(new BigDecimal("0.45"), money.getFractionalPart());
        assertEquals(45L, money.getFractionalPartInMinorUnits());

        Money wholeMoney = new Money("100.00", USD);
        assertEquals(0L, wholeMoney.getFractionalPartInMinorUnits());
    }

    @Test
    @DisplayName("Should perform arithmetic operations correctly")
    void testArithmeticOperations() {
        Money money1 = new Money("100.50", USD);
        Money money2 = new Money("50.25", USD);

        // Addition
        Money sum = money1.add(money2);
        assertEquals(new BigDecimal("150.75"), sum.getAmount());

        // Subtraction
        Money difference = money1.subtract(money2);
        assertEquals(new BigDecimal("50.25"), difference.getAmount());

        // Multiplication
        Money multiplied = money1.multiply(new BigDecimal("2"));
        assertEquals(new BigDecimal("201.00"), multiplied.getAmount());

        Money multipliedDouble = money1.multiply(2.0);
        assertEquals(new BigDecimal("201.00"), multipliedDouble.getAmount());

        // Division
        Money divided = money1.divide(new BigDecimal("2"), RoundingMode.HALF_UP);
        assertEquals(new BigDecimal("50.25"), divided.getAmount());

        Money dividedDouble = money1.divide(2.0, RoundingMode.HALF_UP);
        assertEquals(new BigDecimal("50.25"), dividedDouble.getAmount());
    }

    @Test
    @DisplayName("Should throw exception for currency mismatch in operations")
    void testCurrencyMismatch() {
        Money usdMoney = new Money("100.00", USD);
        Money eurMoney = new Money("100.00", EUR);

        assertThrows(IllegalArgumentException.class, () -> usdMoney.add(eurMoney));
        assertThrows(IllegalArgumentException.class, () -> usdMoney.subtract(eurMoney));
        assertThrows(IllegalArgumentException.class, () -> usdMoney.isGreaterThan(eurMoney));
    }

    @Test
    @DisplayName("Should compare money amounts correctly")
    void testComparisons() {
        Money money1 = new Money("100.00", USD);
        Money money2 = new Money("50.00", USD);
        Money money3 = new Money("100.00", USD);

        assertTrue(money1.isGreaterThan(money2));
        assertTrue(money2.isLessThan(money1));
        assertTrue(money1.isEqualTo(money3));
        assertTrue(money1.isGreaterThanOrEqual(money2));
        assertTrue(money2.isLessThanOrEqual(money1));
        assertTrue(money1.isGreaterThanOrEqual(money3));
        assertTrue(money1.isLessThanOrEqual(money3));
    }

    @Test
    @DisplayName("Should check money state correctly")
    void testStateChecks() {
        Money positive = new Money("100.00", USD);
        Money negative = new Money("-100.00", USD);
        Money zero = new Money("0.00", USD);

        assertTrue(positive.isPositive());
        assertFalse(positive.isNegative());
        assertFalse(positive.isZero());
        assertTrue(positive.isPositiveOrZero());
        assertFalse(positive.isNegativeOrZero());

        assertTrue(negative.isNegative());
        assertFalse(negative.isPositive());
        assertFalse(negative.isZero());
        assertFalse(negative.isPositiveOrZero());
        assertTrue(negative.isNegativeOrZero());

        assertTrue(zero.isZero());
        assertFalse(zero.isPositive());
        assertFalse(zero.isNegative());
        assertTrue(zero.isPositiveOrZero());
        assertTrue(zero.isNegativeOrZero());
    }

    @Test
    @DisplayName("Should handle absolute and negative values")
    void testAbsoluteAndNegative() {
        Money positive = new Money("100.00", USD);
        Money negative = new Money("-100.00", USD);

        assertEquals(positive, negative.abs());
        assertEquals(negative, positive.negate());
        assertEquals(positive, negative.negate().abs());
    }

    @ParameterizedTest
    @DisplayName("Should format money correctly")
    @CsvSource({
        "100.50, USD, '$100.50', '100.50 USD', '100.50 USD'",
        "99.99, EUR, '€99.99', '99.99 EUR', '99.99 EUR'"
    })
    void testFormatting(String amount, String currencyCode, String expectedSymbol,
                        String expectedCode, String expectedToString) {
        Currency currency = Currency.getInstance(currencyCode);
        Money money = new Money(amount, currency);

        // Note: Symbol formatting might vary by locale, so we test carefully
        String formattedWithSymbol = money.formatWithSymbol();
        String formattedWithCode = money.formatWithCode();
        String toString = money.toString();

        assertTrue(formattedWithSymbol.contains(amount));
        assertEquals(expectedCode, formattedWithCode);
        assertTrue(toString.contains(amount) && toString.contains(currencyCode));
    }

    @Test
    @DisplayName("Should maintain immutability")
    void testImmutability() {
        BigDecimal originalAmount = new BigDecimal("100.00");
        Money money = new Money(originalAmount, USD);

        // Original objects should not be modified
        Money added = money.add(new Money("50.00", USD));
        assertNotSame(money.getAmount(), added.getAmount());
        assertEquals(originalAmount, money.getAmount()); // Original unchanged
    }

    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    void testEqualsAndHashCode() {
        Money money1 = new Money("100.00", USD);
        Money money2 = new Money("100.00", USD);
        Money money3 = new Money("200.00", USD);
        Money money4 = new Money("100.00", EUR);

        // Reflexivity
        assertEquals(money1, money1);

        // Symmetry
        assertEquals(money1, money2);
        assertEquals(money2, money1);

        // Transitivity
        Money money5 = new Money("100.00", USD);
        assertEquals(money1, money2);
        assertEquals(money2, money5);
        assertEquals(money1, money5);

        // Different amounts
        assertNotEquals(money1, money3);

        // Different currencies
        assertNotEquals(money1, money4);

        // Null comparison
        assertNotEquals(null, money1);

        // Different class
        assertNotEquals("100.00 USD", money1);

        // HashCode consistency
        assertEquals(money1.hashCode(), money2.hashCode());
        assertEquals(money1.hashCode(), money1.hashCode());
    }

    @Test
    @DisplayName("Should handle scale correctly")
    void testScaleHandling() {
        Money defaultScale = new Money("100.123", USD);
        assertEquals(2, defaultScale.getScale()); // USD has 2 fractional digits

        Money customScale = new Money(new BigDecimal("100.123"), USD, 3);
        assertEquals(3, customScale.getScale());
        assertEquals(new BigDecimal("100.123"), customScale.getAmount());
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, 0.01, 1.0, 100.50, -50.25})
    @DisplayName("Should handle various numeric values")
    void testVariousNumericValues(double value) {
        Money money = new Money(value, USD);
        assertEquals(BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_EVEN), money.getAmount());
    }

    @Test
    @DisplayName("Should handle edge cases with zero fractional digits currency")
    void testZeroFractionDigitsCurrency() {
        Money jpyMoney = new Money("5000", JPY);

        assertEquals(0, jpyMoney.getScale());
        assertEquals(0, jpyMoney.getDefaultFractionDigits());
        assertEquals(5000L, jpyMoney.toMinorUnits());
        assertEquals(0L, jpyMoney.getFractionalPartInMinorUnits());
        assertEquals(new BigDecimal("5000"), jpyMoney.getIntegerPart());
        assertEquals(BigDecimal.ZERO, jpyMoney.getFractionalPart());
    }

    @Test
    @DisplayName("Should handle division by zero")
    void testDivisionByZero() {
        Money money = new Money("100.00", USD);

        assertThrows(ArithmeticException.class,
            () -> money.divide(BigDecimal.ZERO, RoundingMode.HALF_UP));
    }

    @Test
    @DisplayName("Should maintain precision in complex operations")
    void testPrecisionInComplexOperations() {
        Money initial = new Money("100.00", USD);
        Money result = initial
            .multiply(1.1)      // 110.00
            .divide(3, RoundingMode.HALF_UP)  // 36.67
            .add(new Money("10.00", USD))     // 46.67
            .multiply(2);        // 93.34

        assertEquals(new BigDecimal("93.34"), result.getAmount());
    }
}
