package com.example.wirebarley.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountRequestDTO {
    private String username;
    private String email;
    private String phone;
    private String password;
}
