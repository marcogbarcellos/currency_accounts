package com.account.springboot.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    NO_SUCH_CURRENCY(1001, Constants.NO_SUCH_CURRENCY_MSG),
    NO_SUCH_ACCOUNT(1002, Constants.NO_SUCH_ACCOUNT_MSG),
    INSUFFICIENT_AMOUNT(1003, Constants.INSUFFICIENT_AMOUNT_MSG),
    ACCOUNT_ALREADY_EXISTS(1004, Constants.ACCOUNT_ALREADY_EXISTS_MSG),
    BALANCE_ALREADY_EXISTS(1005, Constants.BALANCE_ALREADY_EXISTS_MSG);

    private final int code;
    private final String msg;

    public static class Constants {
        public final static String NO_SUCH_CURRENCY_MSG = "Currency code is invalid";
        public final static String NO_SUCH_ACCOUNT_MSG = "Account was not found";
        public final static String INSUFFICIENT_AMOUNT_MSG = "Account has not enough funds";

        public final static String ACCOUNT_ALREADY_EXISTS_MSG = "An Account with this email already exists";
        public final static String BALANCE_ALREADY_EXISTS_MSG = "The Account has already a balance open for this currency";
    }
}