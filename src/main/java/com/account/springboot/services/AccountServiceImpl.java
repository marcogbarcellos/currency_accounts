package com.account.springboot.services;

import com.account.springboot.dto.*;
import com.account.springboot.exceptions.CustomException;
import com.account.springboot.exceptions.ErrorCode;
import com.account.springboot.models.Account;
import com.account.springboot.models.Transaction;
import com.account.springboot.models.TransactionTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class AccountServiceImpl implements AccountService {

    // this service would eventually be replaced by an actual exchange rates api/third party
    @Autowired
    private RatesService ratesService;

    @Autowired
    private InMemoryService inMemoryService;

    // Service fee to be charged when the user is exchanging funds through its balances
    @Value("${service.fee}")
    private String SERVICE_FEE;

    // Yearly interested rates to be paid to the customer on all balances (for simplicity's sake we will consider always the same for all currencies)
    @Value("${yearly.interest}")
    private String YEARLY_INTEREST;

    @Override
    public AccountResponseDto create(AccountRequestDto accountDto) {
        Account newAccount = new Account(accountDto);
        inMemoryService.addAccount(newAccount.getEmail(), newAccount);
        return newAccount.toDTO();

    }

    @Override
    public Transaction deposit(DepositDto depositDto) {
        Account account = inMemoryService.getAccount(depositDto.getEmail());
        BigDecimal bgAmount = new BigDecimal(depositDto.getAmount());
        account.updateBalance(depositDto.getCurrency(), bgAmount);
        inMemoryService.upsertAccount(account.getEmail(), account);
        // creating a transaction and persisting it to the "in-memory" storage
        Transaction newTransaction = Transaction.builder()
                .fromAccount(account)
                .toAccount(account)
                .fromCurrency(depositDto.getCurrency())
                .toCurrency(depositDto.getCurrency())
                .serviceCurrency(depositDto.getCurrency())
                .fromAmount(bgAmount)
                .toAmount(bgAmount)
                .serviceFeeAmount(new BigDecimal("0")) // let's consider the fee is zero for transfers between users
                .type(TransactionTypeEnum.DEPOSIT)
                .createdAt(LocalDate.now())
                .build();
        inMemoryService.addTransaction(newTransaction);
        return newTransaction;
    }

    @Override
    public AccountResponseDto createBalance(CreateBalanceDto createBalanceDTO) {
        Account account = inMemoryService.getAccount(createBalanceDTO.getEmail());
        if (account.getBalances().containsKey(createBalanceDTO.getCurrency())) {
            throw new CustomException(ErrorCode.BALANCE_ALREADY_EXISTS);
        }
        account.addBalance(createBalanceDTO.getCurrency(), new BigDecimal(YEARLY_INTEREST));
        inMemoryService.upsertAccount(account.getEmail(), account);
        return account.toDTO();
    }


    @Override
    public AccountResponseDto find(String email) {
        Account account = inMemoryService.getAccount(email);
        return account.toDTO();
    }

    @Override
    public Transaction send(SendDto sendDTO) {
        Account sendingAccount = inMemoryService.getAccount(sendDTO.getFromEmail());
        Account receivingAccount = inMemoryService.getAccount(sendDTO.getToEmail());
        BigDecimal bgAmount = new BigDecimal(sendDTO.getAmount());
        // decrease balance from the customer that's sending the funds
        sendingAccount.updateBalance(sendDTO.getCurrency(), bgAmount.multiply(new BigDecimal("-1")));
        // increase balance from the customer that's receiving the funds
        receivingAccount.updateBalance(sendDTO.getCurrency(), bgAmount);
        // persisting changes to "in-memory" storage
        inMemoryService.upsertAccount(sendDTO.getFromEmail(), sendingAccount);
        inMemoryService.upsertAccount(sendDTO.getToEmail(), receivingAccount);
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
        inMemoryService.addTransaction(newTransaction);

        return newTransaction;
    }

    @Override
    public Transaction swap(SwapDto swapDTO) {
        Account account = inMemoryService.getAccount(swapDTO.getEmail());
        BigDecimal bgAmount = new BigDecimal(swapDTO.getAmount());
        // let's consider the service fee for "swaps" as a % defined on the constant SERVICE_FEE
        BigDecimal serviceFeePercentage = new BigDecimal(SERVICE_FEE);
        // thus the receiving percentage would be 100% minus the service fee %.
        BigDecimal receivingPercentage = new BigDecimal(1).subtract(serviceFeePercentage);
        // get the exchange rate given the source and target currencies
        ExchangeRateResponseDto exchangeRateDto = ratesService.getConversionRate(
                ExchangeRateRequestDto.builder()
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
        inMemoryService.upsertAccount(swapDTO.getEmail(), account);
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
        inMemoryService.addTransaction(newTransaction);
        return newTransaction;
    }

    @Override
    public List<Transaction> getTransactions(String email) {
        return inMemoryService.getTransactions(email);
    }

}