package com.account.springboot.controllers;

import com.account.springboot.dto.ConversionRateInDto;
import com.account.springboot.dto.ConversionRateOutDto;
import com.account.springboot.services.RatesService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/api/rates")
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class ConversionRatesController {

    private final RatesService ratesService;

    @GetMapping
    public ResponseEntity<ConversionRateOutDto> getConversionRate(ConversionRateInDto conversionRateInDto) {
        log.info("Getting conversion rate for: {}", conversionRateInDto);
        ConversionRateOutDto out = ratesService.getConversionRate(conversionRateInDto);
        return ResponseEntity.ok(out);
    }
}
