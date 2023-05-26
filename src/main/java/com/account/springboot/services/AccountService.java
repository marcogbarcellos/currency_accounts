package com.account.springboot.services;

import com.account.springboot.dto.AccountInDto;
import com.account.springboot.dto.AccountOutDto;
import com.account.springboot.dto.ConversionRateInDto;
import com.account.springboot.dto.ConversionRateOutDto;
import com.account.springboot.models.Account;
import com.account.springboot.models.CurrencyEnum;
import com.account.springboot.models.Transaction;

import java.util.List;

public interface AccountService {

    /**
     * Creates a new customer account
     * @param accountDto - dto with basic data
     * @return Account with all "zeroed" balances
     */
    AccountOutDto create(AccountInDto accountDto);

    /**
     * find account when provided an email
     * @param email - email (used as the id for that customer)
     * @return Account
     */
    Account find(String email);

    /**
     * send money to a customer through their email
     * @param fromEmail - email that will be sending the funds
     * @param toEmail - email that will be receiving the funds
     * @param currency - the currency the transaction will be executed
     * @param amount - the amount of money the customer will send
     * @return boolean
     */
    boolean send(String fromEmail, String toEmail, CurrencyEnum currency, String amount);

    /**
     * send money to a customer through their email
     * @param email - email of the customer to execute this transaction
     * @param sourceCurrency - the currency that will have its balance decreased
     * @param targetCurrency - the currency that will have its balance increased
     * @param amount - the amount of money (in the source currency) the customer will exchange
     * @return boolean
     */
    boolean swap(String email, CurrencyEnum sourceCurrency, CurrencyEnum targetCurrency, String amount);

    /**
     * gets all transactions given a customer email
     * @param email - email of the customer that would like to see its transactions
     * @return List<Transaction>
     */
    List<Transaction> getTransactions(String email);

}