package com.zerobase.myaccount.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAccountResponse {
    private Long userId;
    private String accountNumber;
    private LocalDateTime registeredAt;

    public static CreateAccountResponse from(AccountDto accountDto) {
        return CreateAccountResponse.builder()
                .userId(accountDto.getUserId())
                .accountNumber(accountDto.getAccountNumber())
                .registeredAt(accountDto.getRegisteredAt())
                .build();
    }
}
