package com.zerobase.myaccount.controller;

import com.zerobase.myaccount.aop.AccountLock;
import com.zerobase.myaccount.dto.*;
import com.zerobase.myaccount.exception.AccountException;
import com.zerobase.myaccount.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
public class TransactionController {
    private final TransactionService transactionService;

    @PostMapping("/transaction/use")
    @AccountLock
    public UseBalanceResponse useBalance(@RequestBody @Valid UseBalanceRequest request) throws InterruptedException {
        try {
            Thread.sleep(3000L);
            return UseBalanceResponse.from(transactionService.useBalance(request.getUserId(), request.getAccountNumber(), request.getAmount()));
        } catch (AccountException e) {
            log.error("잔액 사용 실패");

            transactionService.saveFailedUseTransaction(request.getAccountNumber(), request.getAmount());

            throw e;
        }
    }

    @PostMapping("/transaction/cancel")
    @AccountLock
    public CancelBalanceResponse cancelBalance(@RequestBody @Valid CancelBalanceRequest request) throws InterruptedException {
        try {
            Thread.sleep(3000L);
            return CancelBalanceResponse.from(
                    transactionService.cancelBalance(request.getTransactionId(), request.getAccountNumber(), request.getAmount())
            );
        } catch(AccountException e) {
            log.error("잔액 사용 취소 실패");

            transactionService.saveFailedCancelTransaction(request.getAccountNumber(), request.getAmount());

            throw e;
        }
    }

    @GetMapping("/transaction/{transactionId}")
    public TransactionResponse getTrnasactionList(@PathVariable String transactionId) {
        return TransactionResponse.from(
                transactionService.getTransaction(transactionId)
        );
    }
}
