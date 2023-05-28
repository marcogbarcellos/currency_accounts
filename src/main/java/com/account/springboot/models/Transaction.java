package com.account.springboot.models;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@Data
@EqualsAndHashCode
@ToString
public class Transaction {
    private Account fromAccount;
    private Account toAccount;
    private CurrencyEnum fromCurrency;
    private BigDecimal fromAmount;
    private CurrencyEnum toCurrency;
    private BigDecimal toAmount;
    private CurrencyEnum serviceCurrency;
    private BigDecimal serviceFeeAmount;
    private TransactionTypeEnum type;
    private LocalDate createdAt;

}