package com.account.springboot.services;

import com.account.springboot.dto.ExchangeRateRequestDto;
import com.account.springboot.dto.ExchangeRateResponseDto;

public interface RatesService {

    /**
     * Searches for a reference rate within rates list
     * @param exchangeRateRequestDto - request with targetCurrency
     * @return current conversion rate for target currency using data from ECB
     */
    ExchangeRateResponseDto getConversionRate(ExchangeRateRequestDto exchangeRateRequestDto);

}