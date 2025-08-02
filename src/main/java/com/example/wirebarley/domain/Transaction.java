package com.example.wirebarley.domain;

import com.example.wirebarley.dto.TransactionDTO;
import com.example.wirebarley.enumeration.TransactionType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "transaction", indexes = {
        @Index(name = "idx__account_id__type__date", columnList = "accountId,type,date")
})
@Getter
@Setter
@NoArgsConstructor
public class Transaction extends AbstractDomain {

    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    private TransactionType type; // 거래 종류(deposit, withdraw, transfer)

    @Column(nullable = false)
    private Long grossAmount; // 수수료 포함된 사용자 이체한 총 금액

    @Column(nullable = false)
    private Long netAmount; // 수수료를 제외한 사용자가 받은 총 금액

    @Column(nullable = false)
    private Long prevBalance; // 거래 전 잔액

    @Column(nullable = false)
    private Long postBalance; // 거래 후 잔액

    @Column(nullable = false)
    private Long fee = 0L;

    @Column(precision = 5, scale = 4)
    private BigDecimal feeRate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column
    private Long relatedTransactionId;

    @Column(length = 1024)
    private String memo; // 메모

    @Column
    private String counterpartyName; // 상대방 이름

    @Column
    private String counterpartyAccountNumber; // 상태방 계좌 번호

    @Column(length = 10)
    private String date;

    @Builder
    public Transaction(Long grossAmount, TransactionType type, Long netAmount, Long prevBalance, Long postBalance,
                       Long fee, BigDecimal feeRate, Long accountId, Long relatedTransactionId, String counterpartyName, String counterpartyAccountNumber, String date) {
        this.grossAmount = grossAmount;
        this.type = type;
        this.netAmount = netAmount;
        this.prevBalance = prevBalance;
        this.postBalance = postBalance;
        this.fee = fee;
        this.feeRate = feeRate;
        this.account = new Account(accountId);
        this.relatedTransactionId = relatedTransactionId;
        this.counterpartyName = counterpartyName;
        this.counterpartyAccountNumber = counterpartyAccountNumber;
        this.date = date;
    }

    public TransactionDTO toDTO() {
        return TransactionDTO.builder()
                .id(this.getId())
                .type(this.type)
                .netAmount(this.netAmount)
                .prevBalance(this.prevBalance)
                .postBalance(this.postBalance)
                .fee(this.fee)
                .feeRate(this.feeRate.toString())
                .relatedTransactionId(this.relatedTransactionId)
                .memo(this.memo)
                .counterpartyName(this.counterpartyName)
                .counterpartyAccountNumber(this.counterpartyName)
                .build();
    }
}
