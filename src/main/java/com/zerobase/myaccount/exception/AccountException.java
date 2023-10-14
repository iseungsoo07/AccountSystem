package com.zerobase.myaccount.exception;

import com.zerobase.myaccount.type.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AccountException extends RuntimeException {
    private final ErrorCode errorCode;
    private final String errorMessage;

    public AccountException(ErrorCode errorCode) {
        this.errorCode = errorCode;
        this.errorMessage = errorCode.getDescription();
    }
}
