package com.account.springboot.jobs;


import com.account.springboot.dto.AccountInDto;
import com.account.springboot.models.Account;
import com.account.springboot.models.Balance;
import com.account.springboot.models.CurrencyEnum;
import com.account.springboot.models.Transaction;
import com.account.springboot.services.InMemoryService;
import com.account.springboot.util.InterestRateCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

public class ScheduledTasksTest {

    @Mock
    private InMemoryService inMemoryService;

    @Mock
    private InterestRateCalculator interestRateCalculator;

    @InjectMocks
    private ScheduledTasks scheduledTasks;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testPayoutInterestRates_WithNoAccounts_NoInteractions() {
        // supposing there is no account created...
        when(inMemoryService.getAllAccounts()).thenReturn(new HashMap<>());

        // running task
        scheduledTasks.payoutInterestRates();

        // check if inMemoryService was called just once and there was no interaction on the interestRateCalculator
        verify(inMemoryService, times(1)).getAllAccounts();
        verifyNoMoreInteractions(inMemoryService);
        verifyNoMoreInteractions(interestRateCalculator);
    }

    @Test
    public void testPayoutInterestRates_WithSingleAccount_CalculatesInterestAndUpdatesAccount() {
        // Creating account with USD balance
        String email = "roger@me.com";
        Account account = new Account(new AccountInDto(email));
        account.addBalance(CurrencyEnum.USD, new BigDecimal("0.05"));
        account.updateBalance(CurrencyEnum.USD, new BigDecimal("15"));
        Map<String, Account> accounts = new HashMap<>();
        accounts.put(email, account);
        Balance balance = accounts.get(email).getBalances().get(CurrencyEnum.USD);
        when(inMemoryService.getAllAccounts()).thenReturn(accounts);
        when(interestRateCalculator.getMonthlyInterest(balance.getCreatedAt(), balance.getYearlyInterestRate()))
                .thenReturn(BigDecimal.valueOf(0.1));

        // running task
        scheduledTasks.payoutInterestRates();

        // checking if the inMemoryService methods were called just once
        verify(inMemoryService, times(1)).upsertAccount(eq(email), any(Account.class));
        verify(inMemoryService, times(1)).addTransaction(any(Transaction.class));
    }

    @Test
    public void testPayoutInterestRates_WithMultipleAccounts_CalculatesInterestForEachAccount() {
        Map<String, Account> accounts = new HashMap<>();
        // Creating account1 with USD balance
        String email1 = "account1@me.com";
        Account account1 = new Account(new AccountInDto(email1));
        account1.addBalance(CurrencyEnum.USD, new BigDecimal("0.05"));
        account1.updateBalance(CurrencyEnum.USD, new BigDecimal("15"));
        // Creating account1 with CAD balance
        String email2 = "account2@me.com";
        Account account2 = new Account(new AccountInDto(email2));
        account2.addBalance(CurrencyEnum.CAD, new BigDecimal("0.05"));
        account2.updateBalance(CurrencyEnum.CAD, new BigDecimal("15"));
        // Adding both accounts to main accounts
        accounts.put(email1, account1);
        accounts.put(email2, account2);

        when(inMemoryService.getAllAccounts()).thenReturn(accounts);
        when(interestRateCalculator.getMonthlyInterest(any(LocalDate.class), any(BigDecimal.class)))
                .thenReturn(BigDecimal.valueOf(0.1));

        // running task
        scheduledTasks.payoutInterestRates();

        // check calls
        verify(inMemoryService, times(2)).upsertAccount(anyString(), any(Account.class));
        verify(inMemoryService, times(2)).addTransaction(any(Transaction.class));
    }

    @Test
    public void testPayoutInterestRates_WithMultipleAccounts_SkipAddingBecausePayoutAmountIsZero() {
        Map<String, Account> accounts = new HashMap<>();
        // Creating account1 with USD balance
        String email1 = "account1@me.com";
        Account account1 = new Account(new AccountInDto(email1));
        account1.addBalance(CurrencyEnum.USD, new BigDecimal("0.05"));
        // Creating account1 with CAD balance
        String email2 = "account2@me.com";
        Account account2 = new Account(new AccountInDto(email2));
        account2.addBalance(CurrencyEnum.CAD, new BigDecimal("0.05"));

        // Adding both accounts to main accounts
        accounts.put(email1, account1);
        accounts.put(email2, account2);

        when(inMemoryService.getAllAccounts()).thenReturn(accounts);
        when(interestRateCalculator.getMonthlyInterest(any(LocalDate.class), any(BigDecimal.class)))
                .thenReturn(BigDecimal.valueOf(0));

        // running task
        scheduledTasks.payoutInterestRates();

        // check if none was called
        verify(inMemoryService, times(0)).upsertAccount(anyString(), any(Account.class));
        verify(inMemoryService, times(0)).addTransaction(any(Transaction.class));
    }
}