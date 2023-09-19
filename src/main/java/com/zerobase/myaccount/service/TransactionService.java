package com.zerobase.myaccount.service;

import com.zerobase.myaccount.domain.Account;
import com.zerobase.myaccount.domain.AccountUser;
import com.zerobase.myaccount.domain.Transaction;
import com.zerobase.myaccount.dto.TransactionDto;
import com.zerobase.myaccount.exception.AccountException;
import com.zerobase.myaccount.repository.AccountRepository;
import com.zerobase.myaccount.repository.AccountUserRepository;
import com.zerobase.myaccount.repository.TransactionRepository;
import com.zerobase.myaccount.type.TransactionResultType;
import com.zerobase.myaccount.type.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import static com.zerobase.myaccount.type.AccountStatus.UNREGISTERED;
import static com.zerobase.myaccount.type.ErrorCode.*;
import static com.zerobase.myaccount.type.TransactionResultType.S;
import static com.zerobase.myaccount.type.TransactionType.USE;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountUserRepository accountUserRepository;
    private final AccountRepository accountRepository;

    public TransactionDto useBalance(Long userId, String accountNumber, Long amount) {
        AccountUser accountUser = accountUserRepository.findById(userId)
                .orElseThrow(() -> new AccountException(USER_NOT_FOUND));

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ACCOUNT_NOT_FOUND));

        validateUseBalance(accountUser, account, amount);

        account.useBalance(amount);

        return TransactionDto.fromEntity(saveAndGetTransaction(amount, account, USE, S));
    }

    private Transaction saveAndGetTransaction(Long amount, Account account, TransactionType transactionType, TransactionResultType transactionResultType) {
        return transactionRepository.save(Transaction.builder()
                .transactionType(transactionType)
                .transactionResultType(transactionResultType)
                .account(account)
                .amount(amount)
                .balanceSnapshot(account.getBalance())
                .transactionId(UUID.randomUUID().toString().replace("-", ""))
                .transactedAt(LocalDateTime.now())
                .build());
    }

    private void validateUseBalance(AccountUser accountUser, Account account, Long amount) {
        if (!Objects.equals(accountUser.getId(), account.getAccountUser().getId())) {
            throw new AccountException(USER_ACCOUNT_UNMATCH);
        }

        if (account.getAccountStatus() == UNREGISTERED) {
            throw new AccountException(ACCOUNT_ALREADY_UNREGISTERED);
        }

        if (amount > account.getBalance()) {
            throw new AccountException(AMOUNT_EXCEED_BALANCE);
        }
    }
}
