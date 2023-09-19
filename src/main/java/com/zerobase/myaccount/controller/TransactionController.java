package com.zerobase.myaccount.controller;

import com.zerobase.myaccount.dto.UseBalanceRequest;
import com.zerobase.myaccount.dto.UseBalanceResponse;
import com.zerobase.myaccount.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    @PostMapping("/transaction/use")
    public UseBalanceResponse useBalance(@RequestBody @Valid UseBalanceRequest request) {
        return UseBalanceResponse.from(transactionService.useBalance(request.getUserId(), request.getAccountNumber(), request.getAmount()));
    }


}
