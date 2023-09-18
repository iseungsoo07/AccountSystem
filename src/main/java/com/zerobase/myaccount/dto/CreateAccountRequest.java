package com.zerobase.myaccount.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAccountRequest {
    @NotNull
    @Min(1)
    private Long userId;

    @NotNull
    @Min(0)
    private Long initBalance;
}
