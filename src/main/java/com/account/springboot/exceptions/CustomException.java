package com.account.springboot.exceptions;


import lombok.Getter;

@Getter
//@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "video not found")
public class CustomException extends RuntimeException {
    private final ErrorCode code;
    private final String details;

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