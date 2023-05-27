package com.account.springboot.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;

public class InterestRateCalculator {
    public BigDecimal getMonthlyInterest(LocalDate accountOpeningDate, BigDecimal yearlyInterestRate) {
        BigDecimal fullMonthlyInterestRate = yearlyInterestRate.divide(new BigDecimal("12"), 6, RoundingMode.HALF_UP);
        // if the account is older than a month, the customer gets the full monthly interest
        if (ChronoUnit.DAYS.between(accountOpeningDate, LocalDate.now()) >= 31) {
            return fullMonthlyInterestRate;
        }
        // otherwise calculates the proportion of the monthly interest the customer will get
        YearMonth currentMonth = YearMonth.now();
        double daysInMonth = currentMonth.lengthOfMonth();
        double daysFromOpening = accountOpeningDate.lengthOfMonth() - accountOpeningDate.getDayOfMonth() + 1;

        return new BigDecimal(daysFromOpening / daysInMonth).multiply(fullMonthlyInterestRate);
    }
}
