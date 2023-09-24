package com.zerobase.myaccount.service;

import com.zerobase.myaccount.domain.Account;
import com.zerobase.myaccount.domain.AccountUser;
import com.zerobase.myaccount.domain.Transaction;
import com.zerobase.myaccount.dto.TransactionDto;
import com.zerobase.myaccount.exception.AccountException;
import com.zerobase.myaccount.repository.AccountRepository;
import com.zerobase.myaccount.repository.AccountUserRepository;
import com.zerobase.myaccount.repository.TransactionRepository;
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
import static com.zerobase.myaccount.type.TransactionResultType.F;
import static com.zerobase.myaccount.type.TransactionResultType.S;
import static com.zerobase.myaccount.type.TransactionType.CANCEL;
import static com.zerobase.myaccount.type.TransactionType.USE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    TransactionRepository transactionRepository;

    @Mock
    AccountUserRepository accountUserRepository;

    @Mock
    AccountRepository accountRepository;

    @InjectMocks
    TransactionService transactionService;

    @Test
    @DisplayName("잔액 사용 - 성공")
    void successUseBalance() {
        // given
        AccountUser user = getAccountUser(12L, "apple");
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        Account account = Account.builder()
                .accountUser(user)
                .accountNumber("1234567890")
                .balance(10000L)
                .accountStatus(IN_USE)
                .build();

        Transaction transaction = Transaction.builder()
                .transactionType(USE)
                .transactionResultType(S)
                .transactionId("transactionId")
                .account(account)
                .amount(1000L)
                .balanceSnapshot(9000L)
                .transactedAt(LocalDateTime.now())
                .build();

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        given(transactionRepository.save(any()))
                .willReturn(transaction);

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

    private static AccountUser getAccountUser(long id, String name) {
        return AccountUser.builder()
                .id(id)
                .name(name)
                .build();
    }

    @Test
    @DisplayName("잔액 사용 실패 - 사용자 없음")
    void failUseBalance_UserNotFound() {
        // given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        // when
        AccountException exception
                = assertThrows(AccountException.class, () -> transactionService.useBalance(1L, "1000000000", 1000L));

        // then
        assertEquals(USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("잔액 사용 실패 - 해당 계좌 없음")
    void failUseBalance_AccountNotFound() {
        // given
        AccountUser user = getAccountUser(12L, "apple");
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.empty());

        // when
        AccountException exception
                = assertThrows(AccountException.class, () -> transactionService.useBalance(1L, "1000000000", 1000L));

        // then
        assertEquals(ACCOUNT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("잔액 사용 실패 - 사용자 아이디와 계좌 소유주가 다름")
    void failUseBalance_UserAccountMismatch() {
        // given
        AccountUser user = getAccountUser(1L, "apple");
        AccountUser user2 = getAccountUser(2L, "kiwi");

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        Account account = Account.builder()
                .accountUser(user2)
                .build();

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));
        // when
        AccountException exception
                = assertThrows(AccountException.class, () -> transactionService.useBalance(1L, "1000000000", 1000L));

        // then
        assertEquals(USER_ACCOUNT_UNMATCH, exception.getErrorCode());
    }

    @Test
    @DisplayName("잔액 사용 실패 - 계좌가 해지 상태")
    void failUseBalance_AccountStatusUnregistered() {
        // given
        AccountUser user = getAccountUser(12L, "apple");

        Account account = Account.builder()
                .accountUser(user)
                .accountStatus(UNREGISTERED)
                .balance(10000L)
                .accountNumber("1000000000")
                .build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        // when
        AccountException exception
                = assertThrows(AccountException.class, () -> transactionService.useBalance(1L, "1000000000", 1000L));

        // then
        assertEquals(ACCOUNT_ALREADY_UNREGISTERED, exception.getErrorCode());
    }

    @Test
    @DisplayName("잔액 사용 실패 - 잔액보다 거래 금액이 큼")
    void failUseBalance_AmountExceedBalance() {
        // given
        AccountUser user = getAccountUser(12L, "apple");

        Account account = Account.builder()
                .accountUser(user)
                .accountStatus(IN_USE)
                .accountNumber("1000000000")
                .balance(100L)
                .build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        // when
        AccountException exception
                = assertThrows(AccountException.class, () -> transactionService.useBalance(1L, "1000000000", 1000L));

        // then
        assertEquals(AMOUNT_EXCEED_BALANCE, exception.getErrorCode());
    }

    @Test
    @DisplayName("잔액 사용 실패 트랜잭션 저장")
    void saveFailedUseTransaction() {
        // given
        AccountUser user = getAccountUser(12L, "apple");

        Account account = Account.builder()
                .accountUser(user)
                .accountNumber("1000000000")
                .balance(10000L)
                .build();

        Transaction transaction = Transaction.builder()
                .account(account)
                .transactionType(USE)
                .transactionResultType(F)
                .transactionId("transactionId")
                .transactedAt(LocalDateTime.now())
                .amount(1000L)
                .balanceSnapshot(10000L)
                .build();

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        given(transactionRepository.save(any()))
                .willReturn(transaction);

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);

        // when
        transactionService.saveFailedUseTransaction("1000000000", 1000L);

        // then
        verify(transactionRepository, times(1)).save(captor.capture());
        assertEquals(F, captor.getValue().getTransactionResultType());
        assertEquals(USE, captor.getValue().getTransactionType());
        assertEquals(1000, captor.getValue().getAmount());
        assertEquals(10000, captor.getValue().getBalanceSnapshot());
    }

    @Test
    @DisplayName("잔액 사용 취소 - 성공")
    void successCancelBalance() {
        // given
        AccountUser user = getAccountUser(12L, "apple");

        Account account = Account.builder()
                .accountUser(user)
                .accountNumber("1000000000")
                .balance(10000L)
                .build();

        Transaction transaction = Transaction.builder()
                .transactionId("transactionId")
                .account(account)
                .transactionType(CANCEL)
                .transactionResultType(S)
                .amount(1000L)
                .transactedAt(LocalDateTime.now())
                .balanceSnapshot(9000L)
                .build();

        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        given(transactionRepository.save(any()))
                .willReturn(Transaction.builder()
                        .account(account)
                        .transactionType(CANCEL)
                        .transactionResultType(S)
                        .transactionId("transactionId")
                        .transactedAt(LocalDateTime.now())
                        .amount(1000L)
                        .balanceSnapshot(10000L)
                        .build());


        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);

        // when
        TransactionDto transactionDto = transactionService.cancelBalance("id", "1234567890", 1000L);

        // then
        verify(transactionRepository, times(1)).save(captor.capture());
        assertEquals(1000, captor.getValue().getAmount());
        assertEquals(11000, captor.getValue().getBalanceSnapshot());
        assertEquals(S, transactionDto.getTransactionResultType());
        assertEquals(CANCEL, transactionDto.getTransactionType());
        assertEquals(10000, transactionDto.getBalanceSnapshot());
        assertEquals(1000, transactionDto.getAmount());
    }

    @Test
    @DisplayName("잔액 사용 취소 실패 - 거래 아이디에 해당하는 거래 없음")
    void failCancelBalance_TransactionNotFound() {
        // given
        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.empty());

        // when
        AccountException exception
                = assertThrows(AccountException.class, () -> transactionService.cancelBalance("transactionId", "1000000000", 1000L));

        // then
        assertEquals(TRANSACTION_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("잔액 사용 취소 실패 - 계좌 없음")
    void failCancelBalance_AccountNotFound() {
        // given
        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(Transaction.builder().build()));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.empty());
        // when
        AccountException exception
                = assertThrows(AccountException.class, () -> transactionService.cancelBalance("transactionId", "1000000000", 1000L));

        // then
        assertEquals(ACCOUNT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("잔액 사용 취소 실패 - 거래와 계좌가 일치하지 않음")
    void failCancelBalance_TransactionAccountMismatch() {
        // given
        AccountUser user = getAccountUser(12L, "apple");

        Account account = Account.builder()
                .id(1L)
                .accountUser(user)
                .accountNumber("1000000000")
                .balance(10000L)
                .build();

        Account anotherAccount = Account.builder()
                .id(2L)
                .accountUser(user)
                .accountNumber("1000000012")
                .balance(10000L)
                .build();

        Transaction transaction = Transaction.builder()
                .transactionId("transactionId")
                .account(account)
                .transactionType(CANCEL)
                .transactionResultType(S)
                .amount(1000L)
                .transactedAt(LocalDateTime.now())
                .balanceSnapshot(9000L)
                .build();

        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(anotherAccount));

        // when
        AccountException exception
                = assertThrows(AccountException.class, () -> transactionService.cancelBalance("transactionId", "1000000000", 1000L));

        // then
        assertEquals(TRANSACTION_ACCOUNT_UNMATCH, exception.getErrorCode());
    }

    @Test
    @DisplayName("잔액 사용 취소 실패 - 거래 금액과 거래 취소 금액이 다름")
    void failCancelBalance_CancelMustFully() {
        // given
        AccountUser user = getAccountUser(12L, "apple");

        Account account = Account.builder()
                .id(1L)
                .accountUser(user)
                .accountNumber("1000000000")
                .balance(10000L)
                .build();

        Transaction transaction = Transaction.builder()
                .transactionId("transactionId")
                .account(account)
                .transactionType(CANCEL)
                .transactionResultType(S)
                .amount(1200L)
                .transactedAt(LocalDateTime.now())
                .balanceSnapshot(9000L)
                .build();

        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        // when
        AccountException exception
                = assertThrows(AccountException.class, () -> transactionService.cancelBalance("transactionId", "1000000000", 1000L));

        // then
        assertEquals(CANCEL_MUST_FULLY, exception.getErrorCode());
    }

    @Test
    @DisplayName("잔액 사용 취소 실패 - 1년 넘은 거래는 취소 불가")
    void failCancelBalance_TooOldOrderToCancel() {
        // given
        AccountUser user = getAccountUser(12L, "apple");

        Account account = Account.builder()
                .id(1L)
                .accountUser(user)
                .accountNumber("1000000000")
                .balance(10000L)
                .build();

        Transaction transaction = Transaction.builder()
                .transactionId("transactionId")
                .account(account)
                .transactionType(CANCEL)
                .transactionResultType(S)
                .amount(1000L)
                .transactedAt(LocalDateTime.now().minusYears(1).minusDays(1))
                .balanceSnapshot(9000L)
                .build();

        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));
        // when
        AccountException exception
                = assertThrows(AccountException.class, () -> transactionService.cancelBalance("transactionId", "1000000000", 1000L));

        // then
        assertEquals(TOO_OLD_ORDER_TO_CANCEL, exception.getErrorCode());
    }

    @Test
    @DisplayName("잔액 취소 실패 트랜잭션 저장 ")
    void saveFailedCancelTransaction() {
        // given
        AccountUser user = getAccountUser(12L, "apple");

        Account account = Account.builder()
                .id(1L)
                .accountUser(user)
                .accountNumber("1000000000")
                .balance(10000L)
                .build();

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        given(transactionRepository.save(any()))
                .willReturn(Transaction.builder()
                        .account(account)
                        .transactionType(CANCEL)
                        .transactionResultType(F)
                        .amount(1000L)
                        .balanceSnapshot(10000L)
                        .transactedAt(LocalDateTime.now())
                        .build());

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        // when
        transactionService.saveFailedCancelTransaction("1234567890", 1000L);

        // then
        verify(transactionRepository, times(1)).save(captor.capture());
        assertEquals(F, captor.getValue().getTransactionResultType());
        assertEquals(CANCEL, captor.getValue().getTransactionType());
        assertEquals(10000, captor.getValue().getBalanceSnapshot());
    }

    @Test
    @DisplayName("거래 내역 확인 성공")
    void successGetTransction() {
        // given
        AccountUser user = getAccountUser(12L, "apple");

        Account account = Account.builder()
                .id(1L)
                .accountUser(user)
                .accountNumber("1000000000")
                .balance(10000L)
                .build();

        Transaction transaction = Transaction.builder()
                .transactionId("transactionId")
                .account(account)
                .transactionType(CANCEL)
                .transactionResultType(S)
                .amount(1000L)
                .transactedAt(LocalDateTime.now())
                .balanceSnapshot(9000L)
                .build();

        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));

        // when
        TransactionDto transactionDto = transactionService.getTransaction("12345");

        // then
        assertEquals(CANCEL, transactionDto.getTransactionType());
        assertEquals(1000, transactionDto.getAmount());
        assertEquals(S, transactionDto.getTransactionResultType());
        assertEquals("transactionId", transactionDto.getTransactionId());
    }

    @Test
    @DisplayName("거래 내역 확인 실패 - 해당 거래 내역 없음")
    void failGetTransaction_TransactionNotFound() {
        // given
        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.empty());

        // when
        AccountException exception
                = assertThrows(AccountException.class, () -> transactionService.getTransaction("12345"));

        // then
        assertEquals(TRANSACTION_NOT_FOUND, exception.getErrorCode());
    }
}