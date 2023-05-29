package com.account.springboot.dto;

import com.account.springboot.models.CurrencyEnum;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

@ToString
@Builder
@Getter
@Setter
@AllArgsConstructor
public class ExchangeRateRequestDto {

    @NotEmpty
    private CurrencyEnum sourceCurrency;

    @NotEmpty
    private CurrencyEnum targetCurrency;


}