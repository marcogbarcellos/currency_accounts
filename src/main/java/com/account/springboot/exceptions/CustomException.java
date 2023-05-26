package com.account.springboot.exceptions;


import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
    private final ErrorCode code;
    private String details;

    public CustomException(ErrorCode code, String details) {
        super(code.name());
        this.code = code;
        this.details = details;
    }

    public CustomException(ErrorCode code) {
        super(code.name());
        this.code = code;
        this.details = code.getMsg();
    }
}