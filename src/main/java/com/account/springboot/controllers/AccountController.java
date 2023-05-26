package com.account.springboot.controllers;

import com.account.springboot.dto.AccountInDto;
import com.account.springboot.dto.AccountOutDto;
import com.account.springboot.dto.ConversionRateInDto;
import com.account.springboot.dto.ConversionRateOutDto;
import com.account.springboot.services.AccountService;
import com.account.springboot.services.RatesService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/accounts")
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/create")
    public ResponseEntity<AccountOutDto> getConversionRate(@RequestBody AccountInDto accountInDto) {
        log.info("Getting account created: {}", accountInDto);
        AccountOutDto out = accountService.create(accountInDto);
        return ResponseEntity.ok(out);
    }

}
