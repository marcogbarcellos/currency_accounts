package com.account.springboot.models;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

@Builder
@Data
@EqualsAndHashCode
@ToString
public class Balance {
    private CurrencyEnum currency;
    private BigDecimal amount;
    private Date createdAt;
    private Date updatedAt;
}