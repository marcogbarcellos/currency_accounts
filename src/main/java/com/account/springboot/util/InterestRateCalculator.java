package com.account.springboot.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;

@Component
@Slf4j
public class InterestRateCalculator {
    public BigDecimal getMonthlyInterest(LocalDate accountOpeningDate, BigDecimal yearlyInterestRate) {
        // if account was created today (or even a date in the future) we won't pay interest
        long daysFromOpening = ChronoUnit.DAYS.between(accountOpeningDate, LocalDate.now());
        if (daysFromOpening <= 0) {
            return BigDecimal.ZERO;
        }
        // if the account is older than a month, the customer gets the full monthly interest
        BigDecimal fullMonthlyInterestRate = yearlyInterestRate.divide(new BigDecimal("12"), MathContext.DECIMAL32);
        if (daysFromOpening >= 30) {
            return fullMonthlyInterestRate;
        }
        // otherwise calculates the proportion of the monthly interest the customer will get
        double currentMonthLength = YearMonth.now().lengthOfMonth();
        double accountOpeningMonthLength = accountOpeningDate.lengthOfMonth();
        // we will compare the days of month for the current month and the account creation month, and we'll use whichever is higher
        double daysInMonth = currentMonthLength > accountOpeningMonthLength ? currentMonthLength : accountOpeningMonthLength;

        return new BigDecimal(daysFromOpening / daysInMonth).multiply(fullMonthlyInterestRate);
    }
}
