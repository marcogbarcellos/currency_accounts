package com.account.springboot.dto;

import com.account.springboot.models.CurrencyEnum;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

@ToString
@Builder
@Getter
@Setter
public class ConversionRateInDto {

    @NotEmpty
    private CurrencyEnum sourceCurrency;

    @NotEmpty
    private CurrencyEnum targetCurrency;


}