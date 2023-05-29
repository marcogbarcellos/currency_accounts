package com.account.springboot.jobs;

import com.account.springboot.models.Account;
import com.account.springboot.models.Transaction;
import com.account.springboot.models.TransactionTypeEnum;
import com.account.springboot.services.InMemoryService;
import com.account.springboot.util.InterestRateCalculator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.Map;

@Component
@Slf4j
public class ScheduledTasks {

    @Autowired
    private InMemoryService inMemoryService;

    @Autowired
    private InterestRateCalculator interestRateCalculator;

    @Scheduled(cron = "0 0 0 1 * *") // Run at midnight on the first day of each month
    public void payoutInterestRates() {
        Map<String, Account> accounts  = inMemoryService.getAllAccounts();
        log.info("Running interest rates job");
        accounts.forEach((email, account) -> {
            account.getBalances().forEach((currency, balance) -> {
                log.info("payoutInterestRates balance: {}", balance);
                // calculates the monthly interest rate to be paid considering
                // if the account was created for more than a 1 month (full interest) or less (proportional to the days that were open over that month)
                BigDecimal monthlyInterestRate = interestRateCalculator.getMonthlyInterest(balance.getCreatedAt(), balance.getYearlyInterestRate());
                BigDecimal monthlyPayout = balance.getAmount().multiply(monthlyInterestRate, MathContext.DECIMAL32);
                log.info("monthlyPayout: {} monthlyPayout.compareTo(BigDecimal.ZERO): {}", monthlyPayout, monthlyPayout.compareTo(BigDecimal.ZERO));

                if (monthlyPayout.compareTo(BigDecimal.ZERO) > 0) {
                    account.updateBalance(currency, monthlyPayout);
                    Transaction newTransaction = Transaction.builder()
                            .fromAccount(account)
                            .toAccount(account)
                            .fromCurrency(currency)
                            .toCurrency(currency)
                            .serviceCurrency(currency)
                            .fromAmount(monthlyPayout)
                            .toAmount(monthlyPayout)
                            .serviceFeeAmount(new BigDecimal("0"))
                            .type(TransactionTypeEnum.INTEREST_PAYOUT)
                            .createdAt(LocalDate.now())
                            .build();
                    inMemoryService.upsertAccount(email, account);
                    inMemoryService.addTransaction(newTransaction);
                }
            });
        });

    }
}
