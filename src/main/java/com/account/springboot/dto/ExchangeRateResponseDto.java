package com.account.springboot.dto;

import com.account.springboot.models.CurrencyEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Data
@Builder
@Getter
@Setter
public class ExchangeRateResponseDto {
    private CurrencyEnum targetCurrency;
    private String rate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
}