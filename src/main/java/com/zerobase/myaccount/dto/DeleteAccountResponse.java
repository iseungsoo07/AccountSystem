package com.zerobase.myaccount.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeleteAccountResponse {
    private Long userId;
    private String accountNumber;
    private LocalDateTime unRegisteredAt;

    public static DeleteAccountResponse from(AccountDto accountDto) {
        return DeleteAccountResponse.builder()
                .userId(accountDto.getUserId())
                .accountNumber(accountDto.getAccountNumber())
                .unRegisteredAt(accountDto.getUnRegisteredAt())
                .build();

    }
}
