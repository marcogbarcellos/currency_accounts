package com.account.springboot.services;

import com.account.springboot.dto.*;
import com.account.springboot.models.Transaction;

import java.util.List;

public interface AccountService {

    /**
     * Creates a new customer account
     * @param accountDto - dto with basic data
     * @return Account with all "zeroed" balances
     */
    AccountResponseDto create(AccountRequestDto accountDto);

    /**
     * Deposits into an account
     * @param depositDto - dto with deposit information
     * @return Account with all "zeroed" balances
     */
    AccountResponseDto deposit(DepositDto depositDto);

    /**
     * Creates a new currency balance for a customer
     * @param createBalanceDTO - dto with email and currency to open the account
     * @return Account with all "zeroed" balances
     */
    AccountResponseDto createBalance(CreateBalanceDto createBalanceDTO);

    /**
     * find account when provided an email
     * @param email - email (used as the id for that customer)
     * @return Account
     */
    AccountResponseDto find(String email);

    /**
     * send money to a customer through their email
     * @param sendDTO - DTO with the send transaction information
     * @return boolean
     */
    Transaction send(SendDto sendDTO);

    /**
     * send money to a customer through their email
     * @param swapDTO - DTO with the swap transaction information
     * @return boolean
     */
    Transaction swap(SwapDto swapDTO);

    /**
     * gets all transactions given a customer email
     * @param email - email of the customer that would like to see its transactions
     * @return List<Transaction>
     */
    List<Transaction> getTransactions(String email);

}