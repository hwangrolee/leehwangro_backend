package com.example.wirebarley.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
@Builder
public class AccountDTO {

    private long id;
    private String accountNumber;
    private long balance;
    private String username;
    private ZonedDateTime lastBalanceChangedAt;
}
