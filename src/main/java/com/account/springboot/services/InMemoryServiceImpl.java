package com.account.springboot.services;

import com.account.springboot.exceptions.CustomException;
import com.account.springboot.exceptions.ErrorCode;
import com.account.springboot.models.Account;
import com.account.springboot.models.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class InMemoryServiceImpl implements InMemoryService {

    // Save accounts using email as key
    private final ConcurrentMap<String, Account> accounts = new ConcurrentHashMap<>();
    // Save all transactions
    private final List<Transaction> transactions = new ArrayList<>();

    @Override
    public void addAccount(String email, Account account) {
        if (accounts.containsKey(email)) {
            throw new CustomException(ErrorCode.ACCOUNT_ALREADY_EXISTS);
        }
        accounts.put(email, account);
    }

    @Override
    public void upsertAccount(String email, Account account) {
        accounts.put(email, account);
    }

    @Override
    public void addTransaction(Transaction transaction) { transactions.add(transaction); }

    @Override
    public Account getAccount(String email) {
        if (!accounts.containsKey(email)) {
            throw new CustomException(ErrorCode.NO_SUCH_ACCOUNT);
        }
        return accounts.get(email);
    }

    @Override
    public List<Transaction> getTransactions(String email) {
        if (!accounts.containsKey(email)) {
            throw new CustomException(ErrorCode.NO_SUCH_ACCOUNT);
        }
        log.info("all transactions: {}", transactions);
        List<Transaction> accountTransactions = transactions.stream()
                .filter(tx -> tx.getFromAccount().getEmail().equals(email) || tx.getToAccount().getEmail().equals(email))
                .collect(Collectors.toList());
        return accountTransactions;
    }

    @Override
    public Map<String, Account> getAllAccounts() {
        return accounts;
    }
}