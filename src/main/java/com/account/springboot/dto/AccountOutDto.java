package com.account.springboot.dto;

import com.account.springboot.models.CurrencyEnum;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@ToString
@Builder
@Getter
@Setter
public class AccountOutDto {

    private String email;
    private Map<CurrencyEnum, BigDecimal> balances;
    private LocalDate createdAt;
    private LocalDate updatedAt;

}