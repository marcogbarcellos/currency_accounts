package com.account.springboot.services;

import com.account.springboot.dto.ExchangeRateRequestDto;
import com.account.springboot.dto.ExchangeRateResponseDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class RatesServiceImpl implements RatesService {

    /**
     * When there is no entry for particular day the method checks what is actual rate with date before the requested date
     *@param exchangeRateRequestDto  requested conversion rate dto with desired currency
     * @return returns conversion rate with current date
     */
    @Override
    public ExchangeRateResponseDto getConversionRate(ExchangeRateRequestDto exchangeRateRequestDto) {
        Map<String, String> exchangeRates = new HashMap<String,String>(
                Map.of(
                        "CADUSD", "0.73",
                        "CADEUR", "0.68",
                        "USDCAD", "1.36",
                        "USDEUR", "0.93",
                        "EURCAD", "1.46",
                        "EURUSD", "1.07"
                )
        );
        String rate = exchangeRates.get(exchangeRateRequestDto.getSourceCurrency().toString()+ exchangeRateRequestDto.getTargetCurrency().toString());
        return ExchangeRateResponseDto
                .builder()
                .rate(rate)
                .targetCurrency(exchangeRateRequestDto.getTargetCurrency())
                .date(new Date().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate())
                .build();
    }


}