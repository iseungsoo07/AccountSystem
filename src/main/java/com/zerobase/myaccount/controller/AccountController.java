package com.zerobase.myaccount.controller;

import com.zerobase.myaccount.dto.CreateAccountRequest;
import com.zerobase.myaccount.dto.CreateAccountResponse;
import com.zerobase.myaccount.dto.DeleteAccountRequest;
import com.zerobase.myaccount.dto.DeleteAccountResponse;
import com.zerobase.myaccount.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
    public DeleteAccountResponse deleteAccount(@Valid @RequestBody DeleteAccountRequest request){
        return DeleteAccountResponse.from(
                accountService.deleteAccount(request.getUserId(), request.getAccountNumber())
        );
    }
}
