package com.account.springboot.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.*;

class InterestRateCalculatorTest {

    private InterestRateCalculator calculator;

    @BeforeEach
    public void setup() {
        calculator = new InterestRateCalculator();
    }

    @Test
    public void testGetMonthlyInterest_WithOlderAccount_ReturnsFullMonthlyInterest() {
        // Get an account older than one month..
        LocalDate accountOpeningDate = LocalDate.now().minusMonths(2);
        BigDecimal yearlyInterestRate = new BigDecimal("0.05");

        // run actual method
        BigDecimal result = calculator.getMonthlyInterest(accountOpeningDate, yearlyInterestRate);

        // Check if customer will get the full interest to be paid
        assertEquals(yearlyInterestRate.divide(new BigDecimal("12"), MathContext.DECIMAL32), result);
    }

    @Test
    public void testGetMonthlyInterest_WithNewAccount_ReturnsProportionalMonthlyInterest() {
        // change opening dates to 15 days ago.
        long daysFromOpening = 15;
        LocalDate accountOpeningDate = LocalDate.now().minusDays(daysFromOpening);
        // getting proportion of expected interest
        double currentMonthLength = YearMonth.now().lengthOfMonth();
        double accountOpeningMonthLength = accountOpeningDate.lengthOfMonth();
        double daysInMonth = currentMonthLength > accountOpeningMonthLength ? currentMonthLength : accountOpeningMonthLength;
        BigDecimal yearlyInterestRate = new BigDecimal("0.05");

        // getting proportion of expected interest
        BigDecimal expectedInterest = yearlyInterestRate.divide(new BigDecimal("12"), MathContext.DECIMAL32)
                .multiply(new BigDecimal(daysFromOpening/daysInMonth));

        // run actual method
        BigDecimal result = calculator.getMonthlyInterest(accountOpeningDate, yearlyInterestRate);

        // Check if the user will get just a proportional interest
        assertEquals(expectedInterest, result);
    }

    @Test
    public void testGetMonthlyInterest_WithAccountOpenedToday_ReturnsZeroInterest() {
        // Get "fresh" account
        LocalDate accountOpeningDate = LocalDate.now();
        BigDecimal yearlyInterestRate = new BigDecimal("0.05");
        BigDecimal expectedInterest = BigDecimal.ZERO;

        // run actual method
        BigDecimal result = calculator.getMonthlyInterest(accountOpeningDate, yearlyInterestRate);

        // expected interest should be zero
        assertEquals(expectedInterest, result);
    }

    @Test
    public void testGetMonthlyInterest_WithAccountOpenedInFuture_ReturnsZeroInterest() {
        // If a weird scenario like account created in the future comes to this method
        // we should not pay any interest
        LocalDate accountOpeningDate = LocalDate.now().plusDays(7);
        BigDecimal yearlyInterestRate = new BigDecimal("0.05");
        BigDecimal expectedInterest = BigDecimal.ZERO;

        // run actual method
        BigDecimal result = calculator.getMonthlyInterest(accountOpeningDate, yearlyInterestRate);

        // check if the interest is actually 0
        assertEquals(expectedInterest, result);
    }
}