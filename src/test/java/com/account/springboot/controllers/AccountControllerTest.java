package com.account.springboot.controllers;

import com.account.springboot.dto.*;
import com.account.springboot.exceptions.CustomException;
import com.account.springboot.exceptions.ErrorCode;
import com.account.springboot.models.Transaction;
import com.account.springboot.services.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AccountControllerTest {
    private AccountController accountController;

    @Mock
    private AccountService accountService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        accountController = new AccountController(accountService);
    }

    @Test
    void createAccount_ReturnsCreatedStatus() {

        AccountRequestDto accountRequestDto = new AccountRequestDto();
        when(accountService.create(any(AccountRequestDto.class))).thenReturn(AccountResponseDto.builder().build());


        ResponseEntity response = accountController.createAccount(accountRequestDto);


        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(accountService, times(1)).create(accountRequestDto);
    }

    @Test
    void createAccount_ReturnsErrorResponse_WhenKnownExceptionIsThrown() {

        AccountRequestDto accountRequestDto = new AccountRequestDto();
        when(accountService.create(any(AccountRequestDto.class))).thenThrow(new CustomException(ErrorCode.ACCOUNT_ALREADY_EXISTS));


        ResponseEntity response = accountController.createAccount(accountRequestDto);


        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(accountService, times(1)).create(accountRequestDto);
    }

    @Test
    void createAccount_ReturnsErrorResponse_WhenUnknownExceptionIsThrown() {

        AccountRequestDto accountRequestDto = new AccountRequestDto();
        when(accountService.create(any(AccountRequestDto.class))).thenThrow(new RuntimeException());


        ResponseEntity response = accountController.createAccount(accountRequestDto);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(accountService, times(1)).create(accountRequestDto);
    }

    @Test
    void deposit_ReturnsOkStatus() {

        DepositDto depositDto = new DepositDto();
        when(accountService.deposit(any(DepositDto.class))).thenReturn(Transaction.builder().build());


        ResponseEntity response = accountController.deposit(depositDto);


        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(accountService, times(1)).deposit(depositDto);
    }

    @Test
    void deposit_ReturnsErrorResponse_WhenExceptionIsThrown() {
        DepositDto depositDto = new DepositDto();
        when(accountService.deposit(any(DepositDto.class))).thenThrow(new CustomException(ErrorCode.NO_SUCH_ACCOUNT));
        ResponseEntity response = accountController.deposit(depositDto);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(accountService, times(1)).deposit(depositDto);
    }

    @Test
    void sendFunds_ReturnsOkStatus() {
        SendDto sendDto = new SendDto();
        when(accountService.send(any(SendDto.class))).thenReturn(Transaction.builder().build());
        ResponseEntity response = accountController.sendFunds(sendDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(accountService, times(1)).send(sendDto);
    }

    @Test
    void sendFunds_ReturnsErrorResponse_WhenExceptionIsThrown() {
        SendDto sendDto = new SendDto();
        when(accountService.send(any(SendDto.class))).thenThrow(new CustomException(ErrorCode.INSUFFICIENT_AMOUNT));

        ResponseEntity response = accountController.sendFunds(sendDto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(accountService, times(1)).send(sendDto);
    }

    @Test
    void swapFunds_ReturnsOkStatus() {
        SwapDto swapDto = new SwapDto();
        when(accountService.swap(any(SwapDto.class))).thenReturn(Transaction.builder().build());

        ResponseEntity response = accountController.swapFunds(swapDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(accountService, times(1)).swap(swapDto);
    }

    @Test
    void swapFunds_ReturnsErrorResponse_WhenExceptionIsThrown() {
        SwapDto swapDto = new SwapDto();
        when(accountService.swap(any(SwapDto.class))).thenThrow(new CustomException(ErrorCode.NO_SUCH_CURRENCY));

        ResponseEntity response = accountController.swapFunds(swapDto);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(accountService, times(1)).swap(swapDto);
    }

    @Test
    void createBalance_ReturnsCreatedStatus() {
        CreateBalanceDto createBalanceDto = new CreateBalanceDto();
        when(accountService.createBalance(any(CreateBalanceDto.class))).thenReturn(AccountResponseDto.builder().build());

        ResponseEntity response = accountController.createBalance(createBalanceDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(accountService, times(1)).createBalance(createBalanceDto);
    }

    @Test
    void createBalance_ReturnsErrorResponse_WhenExceptionIsThrown() {
        CreateBalanceDto createBalanceDto = new CreateBalanceDto();
        when(accountService.createBalance(any(CreateBalanceDto.class))).thenThrow(new RuntimeException());

        ResponseEntity response = accountController.createBalance(createBalanceDto);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(accountService, times(1)).createBalance(createBalanceDto);
    }

    @Test
    void getAccount_ReturnsFoundStatus() {
        String email = "test@example.com";
        when(accountService.find(any(String.class))).thenReturn(AccountResponseDto.builder().build());

        ResponseEntity response = accountController.getAccount(email);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(accountService, times(1)).find(email);
    }

    @Test
    void getAccount_ReturnsErrorResponse_WhenExceptionIsThrown() {
        String email = "test@example.com";
        when(accountService.find(any(String.class))).thenThrow(new RuntimeException());

        ResponseEntity response = accountController.getAccount(email);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(accountService, times(1)).find(email);
    }

    @Test
    void getAccountTransactions_ReturnsOkStatus() {
        String email = "test@example.com";
        List<Transaction> transactions = new ArrayList<>();
        when(accountService.getTransactions(anyString())).thenReturn(transactions);

        ResponseEntity response = accountController.getAccountTransactions(email);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(transactions, response.getBody());
        verify(accountService, times(1)).getTransactions(email);
    }

    @Test
    void getAccountTransactions_ReturnsErrorResponse_WhenExceptionIsThrown() {
        String email = "test@example.com";
        when(accountService.getTransactions(anyString())).thenThrow(new RuntimeException());

        ResponseEntity response = accountController.getAccountTransactions(email);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(accountService, times(1)).getTransactions(email);
    }

}
