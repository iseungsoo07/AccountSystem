package com.zerobase.myaccount.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    USER_NOT_FOUND("사용자가 존재하지 않습니다."),
    ACCOUNT_NOT_FOUND("계좌가 존재하지 않습니다."),
    USER_ACCOUNT_UNMATCH("사용자와 계좌의 소유주가 다릅니다."),
    ACCOUNT_ALREADY_UNREGISTERED("해당 계좌는 해지된 상태입니다."),
    BALANCE_NOT_EMPTY("잔액이 있는 계좌는 해지할 수 없습니다."),
    MAX_ACCOUNT_PER_USER_10("1인당 최대 보유 가능 계좌는 10개 입니다.");

    private final String description;
}
