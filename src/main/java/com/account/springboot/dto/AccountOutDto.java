package com.account.springboot.dto;

import com.account.springboot.models.CurrencyEnum;
import lombok.*;

import java.math.BigDecimal;
import java.util.Map;

@ToString
@Builder
@Getter
@Setter
public class AccountOutDto {

    private String email;
    private Map<CurrencyEnum, BigDecimal> balances;

}