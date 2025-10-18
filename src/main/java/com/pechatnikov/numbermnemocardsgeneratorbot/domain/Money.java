package com.pechatnikov.numbermnemocardsgeneratorbot.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

public class Money {
    private final BigDecimal amount;
    private final Currency currency;
    private final int scale;

    // Конструкторы
    public Money(BigDecimal amount, Currency currency) {
        this(amount, currency, currency.getDefaultFractionDigits());
    }

    public Money(BigDecimal amount, Currency currency, int scale) {
        this.amount = Objects.requireNonNull(amount, "Amount cannot be null")
            .setScale(scale, RoundingMode.HALF_EVEN);
        this.currency = Objects.requireNonNull(currency, "Currency cannot be null");
        this.scale = scale;
    }

    public Money(double amount, Currency currency) {
        this(BigDecimal.valueOf(amount), currency);
    }

    public Money(String amount, Currency currency) {
        this(new BigDecimal(amount), currency);
    }

    // Статические фабричные методы
    public static Money of(BigDecimal amount, Currency currency) {
        return new Money(amount, currency);
    }

    public static Money of(double amount, Currency currency) {
        return new Money(amount, currency);
    }

    public static Money of(String amount, Currency currency) {
        return new Money(amount, currency);
    }

    // Методы для работы с минимальными единицами валюты

    /**
     * Создает Money из минимальных единиц валюты (копейки, центы и т.д.)
     * @param minorUnits количество минимальных единиц
     * @param currency валюта
     * @return объект Money
     */
    public static Money fromMinorUnits(long minorUnits, Currency currency) {
        int fractionDigits = currency.getDefaultFractionDigits();
        BigDecimal amount = BigDecimal.valueOf(minorUnits)
            .divide(BigDecimal.TEN.pow(fractionDigits), fractionDigits, RoundingMode.UNNECESSARY);
        return new Money(amount, currency);
    }

    /**
     * Создает Money из минимальных единиц валюты (копейки, центы и т.д.)
     * @param minorUnits количество минимальных единиц как BigDecimal
     * @param currency валюта
     * @return объект Money
     */
    public static Money fromMinorUnits(BigDecimal minorUnits, Currency currency) {
        int fractionDigits = currency.getDefaultFractionDigits();
        BigDecimal amount = minorUnits.divide(BigDecimal.TEN.pow(fractionDigits),
            fractionDigits, RoundingMode.UNNECESSARY);
        return new Money(amount, currency);
    }

    /**
     * Возвращает количество минимальных единиц валюты
     * @return количество копеек, центов и т.д.
     */
    public long toMinorUnits() {
        return amount.multiply(BigDecimal.TEN.pow(scale)).longValue();
    }

    /**
     * Возвращает количество минимальных единиц валюты как BigDecimal
     * @return количество минимальных единиц
     */
    public BigDecimal toMinorUnitsBigDecimal() {
        BigDecimal minorUnitsAmount = amount.multiply(BigDecimal.TEN.pow(scale));

        return new BigDecimal(minorUnitsAmount.toBigInteger(), 0);
    }

    /**
     * Возвращает дробную часть в минимальных единицах
     * @return дробная часть в копейках, центах и т.д.
     */
    public long getFractionalPartInMinorUnits() {
        long totalMinorUnits = toMinorUnits();
        long majorUnitsInMinor = getIntegerPart().multiply(BigDecimal.TEN.pow(scale)).longValue();
        return totalMinorUnits - majorUnitsInMinor;
    }

    /**
     * Возвращает целую часть суммы
     * @return целая часть (рубли, доллары и т.д.)
     */
    public BigDecimal getIntegerPart() {
        return BigDecimal.valueOf(amount.longValue());
    }

    /**
     * Возвращает дробную часть как BigDecimal
     * @return дробная часть (0.99 для 1.99)
     */
    public BigDecimal getFractionalPart() {
        return amount.remainder(BigDecimal.ONE);
    }

    // Арифметические операции
    public Money add(Money other) {
        validateSameCurrency(other);
        BigDecimal result = this.amount.add(other.amount);
        return new Money(result, this.currency, this.scale);
    }

    public Money subtract(Money other) {
        validateSameCurrency(other);
        BigDecimal result = this.amount.subtract(other.amount);
        return new Money(result, this.currency, this.scale);
    }

    public Money multiply(BigDecimal multiplier) {
        BigDecimal result = this.amount.multiply(multiplier);
        return new Money(result, this.currency, this.scale);
    }

    public Money multiply(double multiplier) {
        return multiply(BigDecimal.valueOf(multiplier));
    }

    public Money divide(BigDecimal divisor, RoundingMode roundingMode) {
        BigDecimal result = this.amount.divide(divisor, roundingMode);
        return new Money(result, this.currency, this.scale);
    }

    public Money divide(double divisor, RoundingMode roundingMode) {
        return divide(BigDecimal.valueOf(divisor), roundingMode);
    }

    public Money divide(BigDecimal divisor) {
        return divide(divisor, RoundingMode.HALF_EVEN);
    }

    public Money divide(double divisor) {
        return divide(BigDecimal.valueOf(divisor), RoundingMode.HALF_EVEN);
    }

    // Операции сравнения
    public boolean isGreaterThan(Money other) {
        validateSameCurrency(other);
        return this.amount.compareTo(other.amount) > 0;
    }

    public boolean isLessThan(Money other) {
        validateSameCurrency(other);
        return this.amount.compareTo(other.amount) < 0;
    }

    public boolean isEqualTo(Money other) {
        validateSameCurrency(other);
        return this.amount.compareTo(other.amount) == 0;
    }

    public boolean isGreaterThanOrEqual(Money other) {
        validateSameCurrency(other);
        return this.amount.compareTo(other.amount) >= 0;
    }

    public boolean isLessThanOrEqual(Money other) {
        validateSameCurrency(other);
        return this.amount.compareTo(other.amount) <= 0;
    }

    // Проверки состояния
    public boolean isPositive() {
        return amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isNegative() {
        return amount.compareTo(BigDecimal.ZERO) < 0;
    }

    public boolean isZero() {
        return amount.compareTo(BigDecimal.ZERO) == 0;
    }

    public boolean isPositiveOrZero() {
        return amount.compareTo(BigDecimal.ZERO) >= 0;
    }

    public boolean isNegativeOrZero() {
        return amount.compareTo(BigDecimal.ZERO) <= 0;
    }

    // Абсолютное значение
    public Money abs() {
        return new Money(amount.abs(), currency, scale);
    }

    // Отрицательное значение
    public Money negate() {
        return new Money(amount.negate(), currency, scale);
    }

    // Валидация
    private void validateSameCurrency(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                String.format("Currency mismatch: %s vs %s", this.currency, other.currency));
        }
    }

    // Геттеры
    public BigDecimal getAmount() {
        return amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public int getScale() {
        return scale;
    }

    public String getCurrencyCode() {
        return currency.getCurrencyCode();
    }

    public int getDefaultFractionDigits() {
        return currency.getDefaultFractionDigits();
    }

    // Форматирование
    public String format() {
        return String.format("%s %s", amount, currency.getSymbol());
    }

    public String formatWithCode() {
        return String.format("%s %s", amount, currency.getCurrencyCode());
    }

    public String formatWithSymbol() {
        return String.format("%s%s", currency.getSymbol(), amount);
    }

    // Object methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return amount.compareTo(money.amount) == 0 &&
            Objects.equals(currency, money.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }

    @Override
    public String toString() {
        return String.format("%s %s", amount, currency.getCurrencyCode());
    }
}