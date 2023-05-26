package com.account.springboot.services;

import com.account.springboot.dto.ConversionRateInDto;
import com.account.springboot.dto.ConversionRateOutDto;

public interface RatesService {

    /**
     * Searches for a reference rate within rates list
     * @param conversionRateInDto - request with targetCurrency
     * @return current conversion rate for target currency using data from ECB
     */
    ConversionRateOutDto getConversionRate(ConversionRateInDto conversionRateInDto);

}