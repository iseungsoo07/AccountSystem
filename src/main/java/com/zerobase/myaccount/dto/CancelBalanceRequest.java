package com.zerobase.myaccount.dto;

import com.zerobase.myaccount.aop.AccountLockIdInterface;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancelBalanceRequest implements AccountLockIdInterface {
    @NotBlank
    private String transactionId;

    @NotBlank
    @Size(min = 10, max = 10)
    private String accountNumber;

    @NotNull
    @Min(10)
    @Max(1_000_000_000)
    private Long amount;
}
