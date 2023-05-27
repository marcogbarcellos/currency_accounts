package com.account.springboot.services;

import com.account.springboot.dto.AccountInDto;
import com.account.springboot.dto.AccountOutDto;
import com.account.springboot.dto.CreateBalanceDTO;
import com.account.springboot.dto.SwapDTO;
import com.account.springboot.exceptions.CustomException;
import com.account.springboot.exceptions.ErrorCode;
import com.account.springboot.models.Account;
import com.account.springboot.models.CurrencyEnum;
import com.account.springboot.models.Transaction;
import com.account.springboot.models.TransactionTypeEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
class AccountServiceImplTest {

    @Mock
    private RatesService ratesService;

    @Autowired
    private AccountServiceImpl accountService;

    private Map<String, Account> accounts;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        accounts = new HashMap<>();
    }

    @Test
    public void testCreateAccount() {
        AccountInDto accountDto = new AccountInDto("john@example.com");
        AccountOutDto result = accountService.create(accountDto);

        // Assert
        assertNotNull(result);
        assertEquals(accountDto.getEmail(), result.getEmail());
    }

    @Test
    public void testCreateBalance() {
        // first create account
        String email = "john@example.com";
        AccountInDto accountDto = new AccountInDto(email);
        AccountOutDto accountOutDto = accountService.create(accountDto);
        // build dto
        CreateBalanceDTO createBalanceDTO = new CreateBalanceDTO(email, CurrencyEnum.USD);
        // create balance
        AccountOutDto result = accountService.createBalance(createBalanceDTO);

        // Assert
        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertEquals(Collections.singletonMap(CurrencyEnum.USD, new BigDecimal("10")), result.getBalances());
    }

    @Test
    public void testFindAccount_ExistingAccount() {
        // first create account
        String email = "john@example.com";
        AccountInDto accountDto = new AccountInDto(email);
        AccountOutDto accountOutDto = accountService.create(accountDto);

        // find created account
        AccountOutDto result = accountService.find(email);

        // Assert
        assertNotNull(result);
        assertEquals(email, result.getEmail());
    }

    @Test
    public void testFindAccount_NonExistingAccount() {
        // Arrange
        String email = "random@example.com";

        // Act and Assert
        assertThrows(CustomException.class, () -> accountService.find(email));
    }

    @Test
    public void testGetTransactions_Success() {
        // Creating test data
        String email = "john@example.com";
        AccountInDto accountDto = new AccountInDto(email);
        AccountOutDto accountOutDto = accountService.create(accountDto);
        // Creating USD Balance
        CreateBalanceDTO createBalanceDTO = new CreateBalanceDTO(email, CurrencyEnum.USD);
        accountService.createBalance(createBalanceDTO);
        // Creating CAD Balance
        createBalanceDTO = new CreateBalanceDTO(email, CurrencyEnum.CAD);
        accountService.createBalance(createBalanceDTO);
        accountService.swap(new SwapDTO(email, CurrencyEnum.USD, CurrencyEnum.CAD, "0.5"));
        // Calling the getTransactions method
        List<Transaction> transactions = accountService.getTransactions(email);

        // Verifying the returned transactions
        assertEquals(transactions.size(), 1);
        assertEquals(transactions.get(0).getType(), TransactionTypeEnum.SWAP);
    }

    @Test
    public void testGetTransactions_InvalidAccount() {
        // unexistent account should throw a CustomException
        assertThrows(CustomException.class, () -> accountService.getTransactions("nonexistent@example.com"));
    }
}