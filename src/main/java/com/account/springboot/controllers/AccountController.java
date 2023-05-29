package com.account.springboot.controllers;

import com.account.springboot.dto.*;
import com.account.springboot.models.Transaction;
import com.account.springboot.services.AccountService;
import com.account.springboot.util.ControllerExceptionsHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity createAccount(@RequestBody AccountRequestDto accountRequestDto) {
        log.info("Getting account created: {}", accountRequestDto);
        try {
            AccountResponseDto out = accountService.create(accountRequestDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(out);
        } catch (Exception exception) {
            return ControllerExceptionsHandler.setResponseEntity(exception);
        }

    }

    @PostMapping("/create-balance")
    public ResponseEntity createBalance(@RequestBody CreateBalanceDto createBalanceDTO) {
        log.info("Create balance: {}", createBalanceDTO);

        try {
            AccountResponseDto out = accountService.createBalance(createBalanceDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(out);
        } catch (Exception exception) {
            return ControllerExceptionsHandler.setResponseEntity(exception);
        }
    }

    @PostMapping("/deposit")
    public ResponseEntity deposit(@RequestBody DepositDto depositDto) {
        log.info("Depositing into account: {}", depositDto);
        try {
            AccountResponseDto out = accountService.deposit(depositDto);
            return ResponseEntity.ok(out);
        } catch (Exception exception) {
            return ControllerExceptionsHandler.setResponseEntity(exception);
        }
    }

    @PostMapping("/send")
    public ResponseEntity sendFunds(@RequestBody SendDto sendDTO) {
        log.info("Sending money: {}", sendDTO);
        try {
            Transaction out = accountService.send(sendDTO);
            return ResponseEntity.ok(out);
        } catch (Exception exception) {
            return ControllerExceptionsHandler.setResponseEntity(exception);
        }
    }

    @PostMapping("/swap")
    public ResponseEntity swapFunds(@RequestBody SwapDto swapDTO) {
        log.info("Swapping funds: {}", swapDTO);
        try {
            Transaction out = accountService.swap(swapDTO);
            return ResponseEntity.ok(out);
        } catch (Exception exception) {
            return ControllerExceptionsHandler.setResponseEntity(exception);
        }
    }

    @GetMapping("/{email}")
    @ResponseBody
    public ResponseEntity getAccount(@PathVariable String email) {
        try {
            AccountResponseDto out = accountService.find(email);
            return ResponseEntity.status(HttpStatus.FOUND).body(out);
        } catch (Exception exception) {
            return ControllerExceptionsHandler.setResponseEntity(exception);
        }
    }

    @GetMapping("/{email}/transactions")
    @ResponseBody
    public ResponseEntity getAccountTransactions(@PathVariable String email) {
        log.info("Getting customer transactions: {}", email);
        try {
            List<Transaction> out = accountService.getTransactions(email);
            return ResponseEntity.ok(out);
        } catch (Exception exception) {
            return ControllerExceptionsHandler.setResponseEntity(exception);
        }
    }

}
