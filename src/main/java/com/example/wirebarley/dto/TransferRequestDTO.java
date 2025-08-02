package com.example.wirebarley.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequestDTO {

    private String counterpartyAccountNumber;
    private Long amount;
    private String memo;

    public TransferRequestDTO(String counterpartyAccountNumber, Long amount) {
        this.amount = amount;
        this.counterpartyAccountNumber = counterpartyAccountNumber;
    }
}
