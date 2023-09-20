package com.zerobase.myaccount.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    INTERNAL_SERVER_ERROR("내부 서버 오류"),

    INVALID_REQUEST("잘못된 요청입니다."),

    USER_NOT_FOUND("사용자가 존재하지 않습니다."),

    ACCOUNT_NOT_FOUND("계좌가 존재하지 않습니다."),

    AMOUNT_EXCEED_BALANCE("거래 금액이 계좌 잔액보다 큽니다."),

    USER_ACCOUNT_UNMATCH("사용자와 계좌의 소유주가 다릅니다."),

    TRANSACTION_ACCOUNT_UNMATCH("이 거래는 해당 계좌에서 발생한 거래가 아닙니다."),

    ACCOUNT_ALREADY_UNREGISTERED("해당 계좌는 해지된 상태입니다."),

    BALANCE_NOT_EMPTY("잔액이 있는 계좌는 해지할 수 없습니다."),

    TRANSACTION_NOT_FOUND("해당 거래 내역이 없습니다."),

    CANCEL_MUST_FULLY("거래 금액과 취소 금액이 다릅니다."),

    ACCOUNT_TRANSACTION_LOCK("해당 계좌는 사용 중입니다."),

    ALREADY_CANCELD_TRANSACTION("이미 취소된 거래입니다."),

    TOO_OLD_ORDER_TO_CANCEL("거래 후 1년이 넘은 거래는 취소할 수 없습니다."),

    MAX_ACCOUNT_PER_USER_10("1인당 최대 보유 가능 계좌는 10개 입니다.");

    private final String description;
}
