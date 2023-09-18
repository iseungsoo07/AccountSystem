package com.zerobase.myaccount.service;

import com.zerobase.myaccount.domain.Account;
import com.zerobase.myaccount.domain.AccountUser;
import com.zerobase.myaccount.dto.AccountDto;
import com.zerobase.myaccount.exception.AccountException;
import com.zerobase.myaccount.repository.AccountRepository;
import com.zerobase.myaccount.repository.AccountUserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.zerobase.myaccount.type.AccountStatus.IN_USE;
import static com.zerobase.myaccount.type.AccountStatus.UNREGISTERED;
import static com.zerobase.myaccount.type.ErrorCode.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountUserRepository accountUserRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    @DisplayName("계좌 생성 정상 동작")
    void createAccount_success() throws Exception {
        // given
        AccountUser user = getAccountUser();

        given(accountUserRepository.findById(anyLong()))
                .willReturn((Optional.of(user)));

        given(accountRepository.save(any()))
                .willReturn(Account.builder()
                        .accountUser(user)
                        .accountNumber("5432167890")
                        .accountStatus(IN_USE)
                        .build());

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        // when
        AccountDto accountDto = accountService.createAccount(1L, 10000L);

        // then
        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals(12L, accountDto.getUserId());
        assertEquals(10, captor.getValue().getAccountNumber().length());
        assertEquals(IN_USE, captor.getValue().getAccountStatus());
    }

    private static AccountUser getAccountUser() {
        return AccountUser.builder()
                .id(12L)
                .name("apple")
                .build();
    }

    @Test
    @DisplayName("해당 사용자 없음 - 계좌 생성 실패")
    void createAccount_UserNotFound() throws Exception {
        // given
        given(accountUserRepository.findById(anyLong()))
                .willReturn((Optional.of(getAccountUser())));

        // when
        AccountException exception = assertThrows(AccountException.class, () -> accountService.createAccount(1L, 2000L));

        // then
        assertEquals(USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("1명의 사용자가 10개의 계좌를 보유 중 일때 - 계좌 생성 실패")
    void createAccount_Over10Accounts() throws Exception {
        // given
        given(accountUserRepository.findById(anyLong()))
                .willReturn((Optional.of(getAccountUser())));

        given(accountRepository.countByAccountUser(any()))
                .willReturn(10);

        // when
        AccountException exception = assertThrows(AccountException.class, () -> accountService.createAccount(1L, 3000L));

        // then
        assertEquals(MAX_ACCOUNT_PER_USER_10, exception.getErrorCode());
    }

    @Test
    @DisplayName("계좌 해지 정상 동작")
    void deleteAccount_success() throws Exception {
        // given
        given(accountUserRepository.findById(anyLong()))
                .willReturn((Optional.of(getAccountUser())));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .id(1L)
                        .accountUser(getAccountUser())
                        .accountNumber("1000000000")
                        .balance(0L)
                        .unRegisteredAt(LocalDateTime.now())
                        .build()));
        // when
        AccountDto accountDto = accountService.deleteAccount(1L, "1234567890");
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        // then
        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals(12L, accountDto.getUserId());
        assertEquals("1000000000", captor.getValue().getAccountNumber());
        assertEquals(UNREGISTERED, captor.getValue().getAccountStatus());
    }

    @Test
    @DisplayName("사용자 없음 - 계좌 해지 실패")
    void deleteAccount_UserNotFound() throws Exception {
        // given
        given(accountUserRepository.findById(anyLong()))
                .willReturn((Optional.empty()));

        // when
        AccountException exception = assertThrows(AccountException.class, () -> accountService.deleteAccount(1L, "1000000000"));

        // then
        assertEquals(USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("계좌 없음 - 계좌 해지 실패")
    void deleteAccount_AccountNotFound() throws Exception {
        // given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(getAccountUser()));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.empty());

        // when
        AccountException exception = assertThrows(AccountException.class, () -> accountService.deleteAccount(3L, "2222222222"));

        // then
        assertEquals(ACCOUNT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("사용자 아이디와 계좌 소유주가 다름 - 계좌 해지 실패")
    void deleteAccount_AccountUserUnmatch() throws Exception {
        // given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(getAccountUser()));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(AccountUser.builder()
                                .id(2L).name("banana").build())
                        .balance(0L)
                        .accountNumber("1000000000")
                        .build()));

        // when
        AccountException exception = assertThrows(AccountException.class, () -> accountService.deleteAccount(1L, "3333333333"));

        // then
        assertEquals(USER_ACCOUNT_UNMATCH, exception.getErrorCode());
    }

    @Test
    @DisplayName("사용자 계좌가 이미 해지된 상태 - 계좌 해지 실패")
    void deleteAccount_AccountStatusAlreadyUnregistered() throws Exception {
        // given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(getAccountUser()));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(getAccountUser())
                        .accountStatus(UNREGISTERED)
                        .balance(0L)
                        .accountNumber("1000000000")
                        .build()));
        // when
        AccountException exception = assertThrows(AccountException.class, () -> accountService.deleteAccount(1L, "1000000000"));

        // then
        assertEquals(ACCOUNT_ALREADY_UNREGISTERED, exception.getErrorCode());
    }

    @Test
    @DisplayName("계좌에 잔액이 존재하는 경우 - 계좌 해지 실패")
    void deleteAccount_AccountBalanceIsNotEmpty() throws Exception {
        // given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(getAccountUser()));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(getAccountUser())
                        .balance(100L)
                        .accountNumber("1000000000")
                        .build()));

        // when
        AccountException exception = assertThrows(AccountException.class, () -> accountService.deleteAccount(1L, "3333333333"));

        // then
        assertEquals(BALANCE_NOT_EMPTY, exception.getErrorCode());
    }
}