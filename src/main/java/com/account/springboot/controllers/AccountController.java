package com.account.springboot.controllers;

import com.account.springboot.dto.*;
import com.account.springboot.models.Transaction;
import com.account.springboot.services.AccountService;
import com.account.springboot.services.RatesService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/accounts")
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/create")
    public ResponseEntity<AccountOutDto> createAccount(@RequestBody AccountInDto accountInDto) {
        log.info("Getting account created: {}", accountInDto);
        AccountOutDto out = accountService.create(accountInDto);
        return ResponseEntity.ok(out);
    }

    @PostMapping("/send")
    public ResponseEntity<Transaction> sendFunds(@RequestBody SendDTO sendDTO) {
        log.info("Sending money: {}", sendDTO);
        Transaction out = accountService.send(sendDTO);
        return ResponseEntity.ok(out);
    }

    @PostMapping("/swap")
    public ResponseEntity<Transaction> swapFunds(@RequestBody SwapDTO swapDTO) {
        log.info("Swapping funds: {}", swapDTO);
        Transaction out = accountService.swap(swapDTO);
        return ResponseEntity.ok(out);
    }

    @PostMapping("/create-balance")
    public ResponseEntity<AccountOutDto> sendFunds(@RequestBody CreateBalanceDTO createBalanceDTO) {
        log.info("Create balance: {}", createBalanceDTO);
        AccountOutDto out = accountService.createBalance(createBalanceDTO);
        return ResponseEntity.ok(out);
    }

    @GetMapping("/{email}")
    @ResponseBody
    public ResponseEntity<AccountOutDto> getAccount(@PathVariable String email) {
        AccountOutDto out = accountService.find(email);
        return ResponseEntity.ok(out);
    }

    @GetMapping("/{email}/transactions")
    @ResponseBody
    public ResponseEntity<List<Transaction>> getAccountTransactions(@PathVariable String email) {
        log.info("Getting customer transactions: {}", email);
        List<Transaction> out = accountService.getTransactions(email);
        return ResponseEntity.ok(out);
    }

}
