package com.zerobase.myaccount.controller;

import com.zerobase.myaccount.dto.*;
import com.zerobase.myaccount.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/account")
    public CreateAccountResponse createAccount(@Valid @RequestBody CreateAccountRequest request) {
        return CreateAccountResponse.from(
                accountService.createAccount(request.getUserId(), request.getInitBalance())
        );
    }

    @DeleteMapping("/account")
    public DeleteAccountResponse deleteAccount(@Valid @RequestBody DeleteAccountRequest request) {
        return DeleteAccountResponse.from(
                accountService.deleteAccount(request.getUserId(), request.getAccountNumber())
        );
    }

    @GetMapping("/account")
    public List<AccountInfo> getAccountListByUserId(@RequestParam("user_id") Long userId) {
        return accountService.getAccountListByUserId(userId).stream()
                .map(
                        accountDto -> AccountInfo.builder()
                                .accountNumber(accountDto.getAccountNumber())
                                .balance(accountDto.getBalance())
                                .build()
                ).collect(Collectors.toList());
    }
}
