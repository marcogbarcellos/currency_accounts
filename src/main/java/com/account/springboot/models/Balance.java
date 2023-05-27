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
public class Balance {
    private CurrencyEnum currency;
    private BigDecimal amount;
    private BigDecimal yearlyInterestRate;
    private LocalDate createdAt;
    private LocalDate updatedAt;
}