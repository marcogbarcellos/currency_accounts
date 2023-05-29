package com.account.springboot.util;

import com.account.springboot.exceptions.CustomException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ControllerExceptionsHandler {

    public static ResponseEntity setResponseEntity(Exception exception) {
        if (exception instanceof CustomException) {
            return switch (((CustomException) exception).getCode()) {
                case INSUFFICIENT_AMOUNT,ACCOUNT_ALREADY_EXISTS, BALANCE_ALREADY_EXISTS -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(((CustomException) exception).getDetails());
                case NO_SUCH_ACCOUNT,NO_SUCH_CURRENCY  -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(((CustomException) exception).getDetails());
                default -> ResponseEntity.internalServerError().build();
            };
        }
        return ResponseEntity.internalServerError().build();
    }
}
