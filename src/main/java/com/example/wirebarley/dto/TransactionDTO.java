package com.example.wirebarley.dto;

import com.example.wirebarley.enumeration.TransactionType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TransactionDTO {

    private long id;
    private TransactionType type;
    private long netAmount;
    private long prevBalance;
    private long postBalance;
    private long fee;
    private String feeRate;
    private Long relatedTransactionId;
    private String memo;
    private String counterpartyName;
    private String counterpartyAccountNumber;
}
