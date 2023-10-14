package com.zerobase.myaccount.domain;

import com.zerobase.myaccount.type.TransactionResultType;
import com.zerobase.myaccount.type.TransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
public class Transaction extends BaseEntity {
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    private TransactionResultType transactionResultType;

    @ManyToOne(fetch = FetchType.LAZY)
    private Account account;

    private Long amount;

    private Long balanceSnapshot;

    private String transactionId;

    private LocalDateTime transactedAt;
}
