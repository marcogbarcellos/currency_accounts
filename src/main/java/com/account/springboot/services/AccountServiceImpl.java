package com.account.springboot.services;

import com.account.springboot.dto.*;
import com.account.springboot.exceptions.CustomException;
import com.account.springboot.exceptions.ErrorCode;
import com.account.springboot.models.Account;
import com.account.springboot.models.Transaction;
import com.account.springboot.models.TransactionTypeEnum;
import com.account.springboot.util.InterestRateCalculator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AccountServiceImpl implements AccountService {

    // this service would eventually be replaced by an actual exchange rates api/third party
    @Autowired
    private RatesService ratesService;

    // Service fee to be charged when the user is exchanging funds through its balances
    @Value("${service.fee}")
    private String SERVICE_FEE;

    // Yearly interested rates to be paid to the customer on all balances (for simplicity sake we will consider always the same for all currencies)
    @Value("${yearly.interest}")
    private String YEARLY_INTEREST;

    private InterestRateCalculator interestRateCalculator;

    // In-memory variable to store accounts by email
    private Map<String, Account> accounts = new ConcurrentHashMap<>();
    private List<Transaction> transactions = new ArrayList<>();

    @Override
    public AccountOutDto create(AccountInDto accountDto) {
        log.info("new Account DTO: {}", accountDto);
        Account newAccount = new Account(accountDto);
        log.info("new Account: {}", newAccount);
        accounts.put(newAccount.getEmail(), newAccount);
        return newAccount.toDTO();
    }

    @Override
    public AccountOutDto createBalance(CreateBalanceDTO createBalanceDTO) {
        log.info("new Balance DTO: {}", createBalanceDTO);
        if (!accounts.containsKey(createBalanceDTO.getEmail())) {
            throw new CustomException(ErrorCode.NO_SUCH_ACCOUNT);
        }
        Account account = accounts.get(createBalanceDTO.getEmail());
        log.info("found Account: {}", account);
        account.addBalance(createBalanceDTO.getCurrency(), new BigDecimal(YEARLY_INTEREST));
        accounts.put(account.getEmail(), account);
        return account.toDTO();
    }


    @Override
    public AccountOutDto find(String email) {
        if (!accounts.containsKey(email)) {
            throw new CustomException(ErrorCode.NO_SUCH_ACCOUNT);
        }
        Account account = accounts.get(email);
        return account.toDTO();
    }

    @Override
    public Transaction send(SendDTO sendDTO) {
        if (!accounts.containsKey(sendDTO.getFromEmail()) || !accounts.containsKey(sendDTO.getToEmail())) {
            throw new CustomException(ErrorCode.NO_SUCH_ACCOUNT);
        }
        Account sendingAccount = accounts.get(sendDTO.getFromEmail());
        Account receivingAccount = accounts.get(sendDTO.getToEmail());
        log.info("sendingAccount: {}", sendingAccount);
        log.info("receivingAccount: {}", receivingAccount);
        log.info("sendDto: {}", sendDTO);
        BigDecimal bgAmount = new BigDecimal(sendDTO.getAmount());
        // decrease balance from the customer that's sending the funds
        sendingAccount.updateBalance(sendDTO.getCurrency(), bgAmount.multiply(new BigDecimal("-1")));
        // increase balance from the customer that's receiving the funds
        receivingAccount.updateBalance(sendDTO.getCurrency(), bgAmount);
        // persisting changes to "in-memory" storage
        accounts.put(sendDTO.getFromEmail(), sendingAccount);
        accounts.put(sendDTO.getToEmail(), receivingAccount);
        // creating a transaction and persisting it to the "in-memory" storage
        Transaction newTransaction = Transaction.builder()
                .fromAccount(sendingAccount)
                .toAccount(receivingAccount)
                .fromCurrency(sendDTO.getCurrency())
                .toCurrency(sendDTO.getCurrency())
                .serviceCurrency(sendDTO.getCurrency())
                .fromAmount(bgAmount)
                .toAmount(bgAmount)
                .serviceFeeAmount(new BigDecimal("0")) // let's consider the fee is zero for transfers between users
                .type(TransactionTypeEnum.TRANSFER)
                .createdAt(LocalDate.now())
                .build();
        transactions.add(newTransaction);

        return newTransaction;
    }

    @Override
    public Transaction swap(SwapDTO swapDTO) {
        if (!accounts.containsKey(swapDTO.getEmail())) {
            throw new CustomException(ErrorCode.NO_SUCH_ACCOUNT);
        }
        Account account = accounts.get(swapDTO.getEmail());
        BigDecimal bgAmount = new BigDecimal(swapDTO.getAmount());
        // let's consider the service fee for "swaps" as a % defined on the constant SERVICE_FEE
        BigDecimal serviceFeePercentage = new BigDecimal(SERVICE_FEE);
        // thus the receiving percentage would be 100% minus the service fee %.
        BigDecimal receivingPercentage = new BigDecimal(1).subtract(serviceFeePercentage);
        // get the exchange rate given the source and target currencies
        ConversionRateOutDto exchangeRateDto = ratesService.getConversionRate(
                ConversionRateInDto.builder()
                        .targetCurrency(swapDTO.getTargetCurrency())
                        .sourceCurrency(swapDTO.getSourceCurrency()).build()
        );
        BigDecimal exchangeRate = new BigDecimal(exchangeRateDto.getRate());
        // calculate the fee based on the source currency + sending amount
        BigDecimal serviceFeeAmount = bgAmount.multiply(serviceFeePercentage);
        // then the actual amount to be received
        BigDecimal receivingAmount = bgAmount
                .multiply(receivingPercentage)
                .multiply(exchangeRate);
        // decrease balance from the customer sourceCurrency that's sending the funds
        account.updateBalance(swapDTO.getSourceCurrency(), bgAmount.multiply(new BigDecimal("-1")));
        // increase balance from the customer that's receiving the funds
        account.updateBalance(swapDTO.getTargetCurrency(), receivingAmount);
        // persisting changes to "in-memory" storage
        accounts.put(swapDTO.getEmail(), account);
        // creating a transaction and persisting it to the "in-memory" storage
        Transaction newTransaction = Transaction.builder()
                .fromAccount(account)
                .toAccount(account)
                .fromCurrency(swapDTO.getSourceCurrency())
                .toCurrency(swapDTO.getTargetCurrency())
                .serviceCurrency(swapDTO.getSourceCurrency())
                .fromAmount(bgAmount)
                .toAmount(receivingAmount)
                .serviceFeeAmount(serviceFeeAmount)
                .type(TransactionTypeEnum.SWAP)
                .createdAt(LocalDate.now())
                .build();
        transactions.add(newTransaction);
        return newTransaction;
    }

    @Override
    public List<Transaction> getTransactions(String email) {
        log.info("accounts {}", accounts);
        if (!accounts.containsKey(email)) {
            throw new CustomException(ErrorCode.NO_SUCH_ACCOUNT);
        }
        List<Transaction> accountTransactions = transactions.stream()
                .filter(tx -> tx.getFromAccount().getEmail().equals(email) || tx.getToAccount().getEmail().equals(email))
                .collect(Collectors.toList());
        return accountTransactions;
    }

//        @Scheduled(cron = "0 * * * * *") // Run every minute

    @Scheduled(cron = "0 0 0 1 * *") // Run at midnight on the first day of each month
    public void payoutInterestRates() {
        log.info("Running interest rates job");
        accounts.forEach((email, account) -> {
            account.getBalances().forEach((currency, balance) -> {
                log.info("payoutInterestRates balance: {}", balance);
                // calculates the monthly interest rate to be paid considering
                // if the account was created for more than a 1 month (full interest) or less (proportional to the days that were open over that month)
                BigDecimal monthlyInterestRate = interestRateCalculator.getMonthlyInterest(balance.getCreatedAt(), balance.getYearlyInterestRate());
                BigDecimal monthlyPayout = balance.getAmount().multiply(monthlyInterestRate);
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
                accounts.put(account.getEmail(), account);
                transactions.add(newTransaction);
            });
        });

    }
}