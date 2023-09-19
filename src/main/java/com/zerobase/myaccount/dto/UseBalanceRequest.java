package com.zerobase.myaccount.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UseBalanceRequest {
    private Long userId;
    private String accountNumber;
    private Long amount;
}
