package com.account.springboot.services;

import com.account.springboot.dto.*;
import com.account.springboot.exceptions.CustomException;
import com.account.springboot.models.CurrencyEnum;
import com.account.springboot.models.Transaction;
import com.account.springboot.models.TransactionTypeEnum;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// This Test class will NOT mock the services to entertain testing both InMemoryService and AccountService due to a time constraint, but we would ideally also create tests for the InMemoryService and separate the logic here
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
class AccountServiceImplTest {

    @Autowired
    private AccountServiceImpl accountService;

    @Autowired
    private RatesService ratesService;

    @Value("${service.fee}")
    private String SERVICE_FEE;

    @Test
    public void testCreateAccount() {
        AccountRequestDto accountDto = new AccountRequestDto("account1@me.com");
        AccountResponseDto result = accountService.create(accountDto);

        // make sure account is created as expected
        assertNotNull(result);
        assertEquals(accountDto.getEmail(), result.getEmail());
    }

    @Test
    public void testCreateAccount_ExistingAccount() {
        AccountRequestDto accountDto = new AccountRequestDto("john@me.com");
        AccountResponseDto result = accountService.create(accountDto);

        // make sure account is created as expected
        assertNotNull(result);
        assertEquals(accountDto.getEmail(), result.getEmail());

        // error should be thrown if creating an account with existing email
        assertThrows(CustomException.class, () -> accountService.create(accountDto));
    }

    @Test
    public void testCreateBalance() {
        // first create account
        String email = "balance@me.com";
        AccountRequestDto accountDto = new AccountRequestDto(email);
        accountService.create(accountDto);
        // build dto
        CreateBalanceDto createBalanceDTO = new CreateBalanceDto(email, CurrencyEnum.USD);
        // create balance
        AccountResponseDto result = accountService.createBalance(createBalanceDTO);

        // make sure balance is created as expected
        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertEquals(Collections.singletonMap(CurrencyEnum.USD, BigDecimal.ZERO), result.getBalances());
    }

    @Test
    public void testCreateBalance_ExistingBalance() {
        // first create account
        String email = "existingBalance@me.com";
        AccountRequestDto accountDto = new AccountRequestDto(email);
        accountService.create(accountDto);
        // build dto
        CreateBalanceDto createBalanceDTO = new CreateBalanceDto(email, CurrencyEnum.USD);
        // create balance
        AccountResponseDto result = accountService.createBalance(createBalanceDTO);

        // make sure balance is created as expected
        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertEquals(Collections.singletonMap(CurrencyEnum.USD, BigDecimal.ZERO), result.getBalances());

        // error should be thrown if creating an existing balance
        assertThrows(CustomException.class, () -> accountService.createBalance(createBalanceDTO));
    }

    @Test
    public void testFindAccount_ExistingAccount() {
        // first create account
        String email = "existing@me.com";
        AccountRequestDto accountDto = new AccountRequestDto(email);
        accountService.create(accountDto);

        // find created account
        AccountResponseDto result = accountService.find(email);

        // check if account was found
        assertNotNull(result);
        assertEquals(email, result.getEmail());
    }

    @Test
    public void testFindAccount_NonExistingAccount() {
        String email = "random@example.com";
        // try finding nonexistent email
        assertThrows(CustomException.class, () -> accountService.find(email));
    }

    @Test
    public void testGetTransactions_Success() {
        // Creating test data
        String email = "txs@me.com";
        AccountRequestDto accountDto = new AccountRequestDto(email);
        accountService.create(accountDto);
        // Creating USD Balance
        CreateBalanceDto createBalanceDTO = new CreateBalanceDto(email, CurrencyEnum.USD);
        accountService.createBalance(createBalanceDTO);
        accountService.deposit(new DepositDto(email, CurrencyEnum.USD, "10"));
        // Creating CAD Balance
        createBalanceDTO = new CreateBalanceDto(email, CurrencyEnum.CAD);
        accountService.createBalance(createBalanceDTO);
        accountService.swap(new SwapDto(email, CurrencyEnum.USD, CurrencyEnum.CAD, "0.5"));
        // Calling the getTransactions method
        List<Transaction> transactions = accountService.getTransactions(email);

        // Verifying the returned transactions
        assertEquals(transactions.size(), 2);
        assertEquals(transactions.get(0).getType(), TransactionTypeEnum.DEPOSIT);
        assertEquals(transactions.get(1).getType(), TransactionTypeEnum.SWAP);
    }

    @Test
    public void testGetTransactions_InvalidAccount() {
        // nonexistent account should throw a CustomException
        assertThrows(CustomException.class, () -> accountService.getTransactions("nonexistent@example.com"));
    }

    @Test
    void testDeposit_ShouldPerformDepositAndReturnTransaction() {
        // Creating from account with USD Balance a deposit 50usd
        String email = "deposit-customer@example.com";
        String amountStr = "500";
        BigDecimal amount = new BigDecimal(amountStr);
        AccountResponseDto accountResponseDto = accountService.create(new AccountRequestDto(email));
        CurrencyEnum currency = CurrencyEnum.USD;

        accountService.createBalance(new CreateBalanceDto(email, currency));
        Transaction transaction = accountService.deposit(new DepositDto(email, currency, amountStr));
        AccountResponseDto account = accountService.find(email);

        // check created account
        assertEquals(amount, account.getBalances().get(currency));

        // check deposit transaction created
        assertNotNull(transaction);
        assertEquals(TransactionTypeEnum.DEPOSIT, transaction.getType());
        assertEquals(LocalDate.now(), transaction.getCreatedAt());
        assertEquals(accountResponseDto.getEmail(), transaction.getFromAccount().getEmail());
        assertEquals(currency, transaction.getFromCurrency());
        assertEquals(currency, transaction.getToCurrency());
        assertEquals(currency, transaction.getServiceCurrency());
        assertEquals(amount, transaction.getFromAmount());
        assertEquals(amount, transaction.getToAmount());
    }

    @Test
    void testSend_ShouldUpdateBalancesAndReturnTransactionSuccessfully() {
        // Creating from account with USD Balance a deposit 50usd
        String fromEmail = "sender-sucess@example.com";
        CurrencyEnum currency = CurrencyEnum.USD;
        accountService.create(new AccountRequestDto(fromEmail));
        accountService.createBalance(new CreateBalanceDto(fromEmail, currency));
        accountService.deposit(new DepositDto(fromEmail, currency, "500"));
        // Creating to account with USD Balance
        String toEmail = "receiver-sucess@example.com";
        accountService.create(new AccountRequestDto(toEmail));
        accountService.createBalance(new CreateBalanceDto(toEmail, currency));
        accountService.deposit(new DepositDto(toEmail, currency, "500"));
        // Setting up send amount
        BigDecimal amount = new BigDecimal("100.00");

        // Call send method
        SendDto sendDTO = new SendDto(fromEmail, toEmail, currency, amount.toString());
        Transaction transaction = accountService.send(sendDTO);

        AccountResponseDto sendingAccount = accountService.find(fromEmail);
        AccountResponseDto receivingAccount = accountService.find(toEmail);
        // Check if transactions exist
        assertNotNull(transaction);
        assertEquals(TransactionTypeEnum.TRANSFER, transaction.getType());
        assertEquals(LocalDate.now(), transaction.getCreatedAt());
        assertEquals(sendingAccount.getEmail(), transaction.getFromAccount().getEmail());
        assertEquals(receivingAccount.getEmail(), transaction.getToAccount().getEmail());
        assertEquals(currency, transaction.getFromCurrency());
        assertEquals(currency, transaction.getToCurrency());
        assertEquals(amount, transaction.getFromAmount());
        assertEquals(amount, transaction.getToAmount());
        assertEquals(BigDecimal.ZERO, transaction.getServiceFeeAmount());
        // Check if balances were correctly updated
        assertEquals(new BigDecimal("400.00"), sendingAccount.getBalances().get(currency));
        assertEquals(new BigDecimal("600.00"), receivingAccount.getBalances().get(currency));
    }

    @Test
    void testSend_ShouldThrowErrorDueToInsufficientBalance() {
        // Creating from account with USD Balance a deposit 50usd
        String fromEmail = "sender-not-complete@example.com";
        CurrencyEnum currency = CurrencyEnum.USD;
        accountService.create(new AccountRequestDto(fromEmail));
        accountService.createBalance(new CreateBalanceDto(fromEmail, currency));
        accountService.deposit(new DepositDto(fromEmail, currency, "500"));
        // Creating to account with USD Balance
        String toEmail = "receiver-not-complete@example.com";
        accountService.create(new AccountRequestDto(toEmail));
        accountService.createBalance(new CreateBalanceDto(toEmail, currency));
        accountService.deposit(new DepositDto(toEmail, currency, "500"));
        // Setting up send amount higher than sender's balance
        BigDecimal amount = new BigDecimal("500.01");

        // Call send method
        SendDto sendDTO = new SendDto(fromEmail, toEmail, currency, amount.toString());
        assertThrows(CustomException.class, () -> accountService.send(sendDTO));
    }

    @Test
    void testSend_ShouldThrowErrorIfThereIsNoBalanceForCurrency() {
        // Creating from account with USD Balance a deposit 50usd
        String fromEmail = "sender-without-currency-balance@example.com";
        CurrencyEnum currency = CurrencyEnum.USD;
        accountService.create(new AccountRequestDto(fromEmail));
        accountService.createBalance(new CreateBalanceDto(fromEmail, currency));
        accountService.deposit(new DepositDto(fromEmail, currency, "500"));
        // Creating to account with USD Balance
        String toEmail = "receiver-without-currency-balance@example.com";
        CurrencyEnum currencyNotInBalance = CurrencyEnum.CAD;
        accountService.create(new AccountRequestDto(toEmail));
        accountService.createBalance(new CreateBalanceDto(toEmail, currencyNotInBalance));
        // Setting up send amount higher than sender's balance
        BigDecimal amount = new BigDecimal("50.00");

        // Call send method
        SendDto sendDTO = new SendDto(fromEmail, toEmail, currency, amount.toString());
        assertThrows(CustomException.class, () -> accountService.send(sendDTO));
    }

    @Test
    void testSend_ShouldThrowErrorIfReceivingCustomerWasNotFound() {
        // Creating from account with USD Balance a deposit 50usd
        String fromEmail = "not-found@example.com";
        CurrencyEnum currency = CurrencyEnum.USD;
        accountService.create(new AccountRequestDto(fromEmail));
        accountService.createBalance(new CreateBalanceDto(fromEmail, currency));
        accountService.deposit(new DepositDto(fromEmail, currency, "500"));
        // Setting up send amount higher than sender's balance
        BigDecimal amount = new BigDecimal("50.00");

        // Call send method
        SendDto sendDTO = new SendDto(fromEmail, "DOES-NOT-EXIST@me.com", currency, amount.toString());
        assertThrows(CustomException.class, () -> accountService.send(sendDTO));
    }

    @Test
    void testSwap_ShouldPerformSwapAndReturnTransaction() {
        // Creating from account with USD Balance a deposit 50usd
        String email = "sender@example.com";
        AccountResponseDto accountResponseDto = accountService.create(new AccountRequestDto(email));
        CurrencyEnum fromCurrency = CurrencyEnum.USD;
        accountService.createBalance(new CreateBalanceDto(email, fromCurrency));
        accountService.deposit(new DepositDto(email, fromCurrency, "500"));
        CurrencyEnum toCurrency = CurrencyEnum.CAD;
        accountService.createBalance(new CreateBalanceDto(email, toCurrency));
        // Setting up send amount higher than sender's balance
        BigDecimal amount = new BigDecimal("50.00");

        // Call swap method
        Transaction transaction = accountService.swap(new SwapDto(email, fromCurrency, toCurrency, amount.toString()));
        // get rates
        ExchangeRateResponseDto exchangeRateResponseDto = ratesService.getConversionRate(new ExchangeRateRequestDto(fromCurrency, toCurrency));
        // checks

        assertNotNull(transaction);
        assertEquals(TransactionTypeEnum.SWAP, transaction.getType());
        assertEquals(LocalDate.now(), transaction.getCreatedAt());
        assertEquals(accountResponseDto.getEmail(), transaction.getFromAccount().getEmail());
        assertEquals(fromCurrency, transaction.getFromCurrency());
        assertEquals(toCurrency, transaction.getToCurrency());
        assertEquals(fromCurrency, transaction.getServiceCurrency());
        assertEquals(amount, transaction.getFromAmount());
        // fee amount will be a % of the total amount (defined by SERVICE_FEE)
        BigDecimal feeAmount = new BigDecimal(SERVICE_FEE).multiply(amount);
        assertEquals(feeAmount, transaction.getServiceFeeAmount());
        // remaining amount (on FROM currency)
        BigDecimal remainingAmountToBeExchanged = amount.subtract(feeAmount);
        // receivingAmount is the actual amount the receiver will get
        BigDecimal receivingAmount = remainingAmountToBeExchanged.multiply(new BigDecimal(exchangeRateResponseDto.getRate()));
        assertEquals(receivingAmount, transaction.getToAmount());
    }

    @Test
    void testSwap_ShouldThrowErrorAsAccountWasNotFound() {
        String email = "not-found-sender@example.com";
        CurrencyEnum fromCurrency = CurrencyEnum.USD;
        CurrencyEnum toCurrency = CurrencyEnum.CAD;
        BigDecimal amount = new BigDecimal("50.00");

        // Call swap method
        assertThrows(CustomException.class, () -> accountService.swap(new SwapDto(email, fromCurrency, toCurrency, amount.toString())));
    }

    @Test
    void testSwap_ShouldThrowErrorIfFundsAreInsufficient() {
        // Creating from account with USD Balance a deposit 50usd
        String email = "sender-insufficient@example.com";
        accountService.create(new AccountRequestDto(email));
        CurrencyEnum fromCurrency = CurrencyEnum.USD;
        accountService.createBalance(new CreateBalanceDto(email, fromCurrency));
        accountService.deposit(new DepositDto(email, fromCurrency, "500"));
        CurrencyEnum toCurrency = CurrencyEnum.CAD;
        accountService.createBalance(new CreateBalanceDto(email, toCurrency));
        // Setting up send amount higher than sender's balance
        BigDecimal amount = new BigDecimal("501.00");

        // Call swap method
        assertThrows(CustomException.class, () -> accountService.swap(new SwapDto(email, fromCurrency, toCurrency, amount.toString())));
    }

}