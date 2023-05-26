package com.account.springboot.services;

import com.account.springboot.dto.ConversionRateInDto;
import com.account.springboot.dto.ConversionRateOutDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class RatesServiceImpl implements RatesService {

    /**
     * When there is no entry for particular day the method checks what is actual rate with date before the requested date
     *@param conversionRateInDto  requested conversion rate dto with desired currency
     * @return returns conversion rate with current date
     */
    @Override
    public ConversionRateOutDto getConversionRate(ConversionRateInDto conversionRateInDto) {
        Random random = new Random();
//        Map<String, String> exchangeRates = new HashMap<>();
//        exchangeRates.put("CADUSD", "0.73");
//        exchangeRates.put("CADEUR", "0.68");
//        exchangeRates.put("CADGBP", "0.6");
//        exchangeRates.put("USDCAD", "1.36");
//        exchangeRates.put("USDEUR", "0.93");
//        exchangeRates.put("USDGBP", "0.81");
//        exchangeRates.put("EURCAD", "1.46");
//        exchangeRates.put("EURUSD", "1.07");
//        exchangeRates.put("EURGBP", "0.87");
//        exchangeRates.put("GBPCAD", "1.68");
//        exchangeRates.put("GBPUSD", "1.23");
//        exchangeRates.put("GBPEUR", "1.15");

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
        String rate = exchangeRates.get(conversionRateInDto.getSourceCurrency().toString()+conversionRateInDto.getTargetCurrency().toString());



        return ConversionRateOutDto
                .builder()
                .rate(rate)
                .targetCurrency(conversionRateInDto.getTargetCurrency())
                .date(new Date().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate())
                .build();
    }


}