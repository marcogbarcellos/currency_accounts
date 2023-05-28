package com.account.springboot.services;

import com.account.springboot.models.Account;
import com.account.springboot.models.Transaction;

import java.util.List;
import java.util.Map;

public interface InMemoryService {

    /**
     * Creates an account in to the in-memory "database"
     * @param account - account to be persisted or updated
     */
    void addAccount(String email, Account account);

    /**
     * Persist/Update an account in to the in-memory "database"
     * @param account - account to be persisted or updated
     */
    void upsertAccount(String email, Account account);

    /**
     * Persist a transaction in to the in-memory "database"
     * @param transaction - transaction to be persisted or updated
     */
    void addTransaction(Transaction transaction);

    /**
     * Get Account by email (key)
     * @param email - key to return an account
     @return Account
     */
    Account getAccount(String email);

    /**
     * Get all Transactions from a customer account by email (key)
     * @param email - key to return a list of transactions
     * @return List<Transaction>
     */
    List<Transaction> getTransactions(String email);

    /**
     * Get all accounts
     * @return List<Account>
     */
    Map<String, Account> getAllAccounts();

}