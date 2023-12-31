package com.zerobase.myaccount.service;

import com.zerobase.myaccount.exception.AccountException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import static com.zerobase.myaccount.type.ErrorCode.ACCOUNT_TRANSACTION_LOCK;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class LockServiceTest {

    @Mock
    RedissonClient redissonClient;

    @Mock
    RLock rLock;

    @InjectMocks
    LockService lockService;

    @Test
    @DisplayName("락 획득 성공")
    void successsGetLock() throws InterruptedException {
        // given
        given(redissonClient.getLock(anyString()))
                .willReturn(rLock);

        given(rLock.tryLock(anyLong(), anyLong(), any()))
                .willReturn(true);

        // when
        // then
        assertDoesNotThrow(() -> lockService.lock("123"));
    }

    @Test
    @DisplayName("락 획득 실패")
    void failGetLock() throws InterruptedException {
        // given
        given(redissonClient.getLock(anyString()))
                .willReturn(rLock);

        given(rLock.tryLock(anyLong(), anyLong(), any()))
                .willReturn(false);
        // when
        AccountException exception = assertThrows(AccountException.class, () -> lockService.lock("123"));
        // then
        assertEquals(ACCOUNT_TRANSACTION_LOCK, exception.getErrorCode());

    }
}