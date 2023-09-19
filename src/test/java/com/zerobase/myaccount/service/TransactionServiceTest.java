package com.zerobase.myaccount.service;

import com.zerobase.myaccount.domain.Account;
import com.zerobase.myaccount.domain.AccountUser;
import com.zerobase.myaccount.domain.Transaction;
import com.zerobase.myaccount.dto.TransactionDto;
import com.zerobase.myaccount.repository.AccountRepository;
import com.zerobase.myaccount.repository.AccountUserRepository;
import com.zerobase.myaccount.repository.TransactionRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.zerobase.myaccount.type.AccountStatus.IN_USE;
import static com.zerobase.myaccount.type.TransactionResultType.S;
import static com.zerobase.myaccount.type.TransactionType.USE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountUserRepository accountUserRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void successUseBalance() throws Exception {
        // given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("apple")
                .build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        Account account = Account.builder()
                .accountUser(user)
                .accountNumber("1234567890")
                .balance(10000L)
                .accountStatus(IN_USE)
                .build();
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        given(transactionRepository.save(any()))
                .willReturn(Transaction.builder()
                        .transactionType(USE)
                        .transactionResultType(S)
                        .transactionId("transactionId")
                        .account(account)
                        .amount(1000L)
                        .balanceSnapshot(9000L)
                        .transactedAt(LocalDateTime.now())
                        .build());

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        // when

        TransactionDto transactionDto = transactionService.useBalance(1L, "1000000000", 1000L);

        // then
        verify(transactionRepository, times(1)).save(captor.capture());
        assertEquals(9000, captor.getValue().getBalanceSnapshot());
        assertEquals(1000, captor.getValue().getAmount());
        assertEquals(9000, transactionDto.getBalanceSnapshot());
        assertEquals(S, transactionDto.getTransactionResultType());
        assertEquals(USE, transactionDto.getTransactionType());
        assertEquals(1000, transactionDto.getAmount());
    }
}