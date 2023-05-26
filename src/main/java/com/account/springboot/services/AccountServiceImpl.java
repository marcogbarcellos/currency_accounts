package com.account.springboot.services;

import com.account.springboot.dto.AccountInDto;
import com.account.springboot.dto.AccountOutDto;
import com.account.springboot.dto.ConversionRateInDto;
import com.account.springboot.dto.ConversionRateOutDto;
import com.account.springboot.exceptions.CustomException;
import com.account.springboot.exceptions.ErrorCode;
import com.account.springboot.models.Account;
import com.account.springboot.models.CurrencyEnum;
import com.account.springboot.models.Transaction;
import com.account.springboot.models.TransactionTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AccountServiceImpl implements AccountService {

    @Autowired
    private RatesService ratesService;

    // In-memory variable to store accounts by email
    private Map<String, Account> accounts = new ConcurrentHashMap<>();
    private List<Transaction> transactions = new ArrayList<>();

    @Override
    public AccountOutDto create(AccountInDto accountDto) {
        log.info("new Account DTO: {}", accountDto);
        Map<CurrencyEnum, BigDecimal> balances = new ConcurrentHashMap<CurrencyEnum, BigDecimal>();
        Arrays.stream(CurrencyEnum.values()).forEach(currency -> balances.put(currency, new BigDecimal("10")));
        Date now = new Date();
        Account newAccount = Account.builder()
                .email(accountDto.getEmail())
                .createdAt(now)
                .updatedAt(now)
                .balances(balances)
                .build();
        log.info("new Account: {}", newAccount);
        accounts.put(newAccount.getEmail(), newAccount);
        return AccountOutDto.builder()
                .email(newAccount.getEmail())
                .balances(newAccount.getBalances())
                .build();
    }

    @Override
    public Account find(String email) {
        return accounts.get(email);
    }

    @Override
    public boolean send(String fromEmail, String toEmail, CurrencyEnum currency, String amount) {
        if (!accounts.containsKey(fromEmail) || !accounts.containsKey(toEmail)) {
            throw new CustomException(ErrorCode.NO_SUCH_ACCOUNT);
        }
        Account sendingAccount = accounts.get(fromEmail);
        Account receivingAccount = accounts.get(toEmail);
        if (new BigDecimal(amount).compareTo(sendingAccount.getBalances().get(currency)) <= 0) {
            throw new CustomException(ErrorCode.INSUFFICIENT_AMOUNT);
        }
        BigDecimal bgAmount = new BigDecimal(amount);
        // modifying customers balance
        Map<CurrencyEnum, BigDecimal> sendingCustomerBalance = sendingAccount.getBalances();
        Map<CurrencyEnum, BigDecimal> receivingCustomerBalance = receivingAccount.getBalances();
        // decrease balance from the customer that's sending the funds
        sendingCustomerBalance.put(currency, sendingCustomerBalance.get(currency).subtract(bgAmount));
        // increase balance from the customer that's receiving the funds
        receivingCustomerBalance.put(currency, receivingCustomerBalance.get(currency).add(bgAmount));
        sendingAccount.setBalances(sendingCustomerBalance);
        receivingAccount.setBalances(receivingCustomerBalance);
        // persisting changes to "in-memory" storage
        accounts.put(fromEmail, sendingAccount);
        accounts.put(toEmail, receivingAccount);
        // creating a transaction and persisting it to the "in-memory" storage
        Transaction newTransaction = Transaction.builder()
                .fromAccount(sendingAccount)
                .toAccount(receivingAccount)
                .fromCurrency(currency)
                .toCurrency(currency)
                .serviceCurrency(currency)
                .fromAmount(bgAmount)
                .toAmount(bgAmount)
                .serviceFeeAmount(new BigDecimal("0")) // let's consider the fee is zero for transfers between users
                .type(TransactionTypeEnum.TRANSFER)
                .createdAt(new Date())
                .build();
        transactions.add(newTransaction);
        return true;
    }

    @Override
    public boolean swap(String email, CurrencyEnum sourceCurrency, CurrencyEnum targetCurrency, String amount) {
        if (!accounts.containsKey(email)) {
            throw new CustomException(ErrorCode.NO_SUCH_ACCOUNT);
        }
        Account account = accounts.get(email);
        if (new BigDecimal(amount).compareTo(account.getBalances().get(sourceCurrency)) <= 0) {
            throw new CustomException(ErrorCode.INSUFFICIENT_AMOUNT);
        }
        BigDecimal bgAmount = new BigDecimal(amount);
        // modifying customers balance
        Map<CurrencyEnum, BigDecimal> balance = account.getBalances();
        // calculate exchangeRate
        ConversionRateOutDto exchangeRate = ratesService.getConversionRate(
                ConversionRateInDto.builder()
                        .targetCurrency(targetCurrency)
                        .sourceCurrency(sourceCurrency).build()
        );
        // let's consider the service fee for "swaps" at 1% amd this fee will always be charged based on the source currency
        BigDecimal serviceFeeAmount = new BigDecimal(amount).multiply(new BigDecimal("0.01"));
        // then the remaining amount would be the 99% of the sending customer's funds multiplied by the exchange rates
        BigDecimal receivingAmount = new BigDecimal(amount)
                .multiply(new BigDecimal("0.99").multiply(new BigDecimal(exchangeRate.getRate())));
        // decrease balance from the customer sourceCurrency that's sending the funds
        balance.put(sourceCurrency, balance.get(sourceCurrency).subtract(bgAmount));
        // increase balance from the customer that's receiving the funds
        balance.put(targetCurrency, balance.get(sourceCurrency).add(receivingAmount));
        // updating account's balances
        account.setBalances(balance);
        // persisting changes to "in-memory" storage
        accounts.put(email, account);
        // creating a transaction and persisting it to the "in-memory" storage
        Transaction newTransaction = Transaction.builder()
                .fromAccount(account)
                .toAccount(account)
                .fromCurrency(sourceCurrency)
                .toCurrency(targetCurrency)
                .serviceCurrency(sourceCurrency)
                .fromAmount(bgAmount)
                .toAmount(receivingAmount)
                .serviceFeeAmount(serviceFeeAmount)
                .type(TransactionTypeEnum.SWAP)
                .createdAt(new Date())
                .build();
        transactions.add(newTransaction);
        return true;
    }

    @Override
    public List<Transaction> getTransactions(String email) {
        List<Transaction> accountTransactions = transactions.stream()
                .filter(tx -> tx.getFromAccount().getEmail().equals(email) || tx.getToAccount().getEmail().equals(email))
                .collect(Collectors.toList());
        return accountTransactions;
    }
}