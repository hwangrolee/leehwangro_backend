package com.example.wirebarley.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 입금 API DTO
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DepositRequestDTO {

    private Long amount;
}
