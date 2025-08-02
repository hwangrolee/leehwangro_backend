package com.example.wirebarley.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "member", indexes = {
        @Index(name = "idx__phone", columnList = "phone")
})
@Getter
@Setter
public class User extends AbstractDomain {

    @Column(nullable = false, length = 50)
    private String username;

    @Column(length = 100)
    private String email;

    @Column(length = 50)
    private String phone;

    @Column
    private Long dailyWithdrawalLimit = 1_000_000L;

    @Column
    private Long dailyTransferLimit = 3_000_000L;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Account> accounts = new ArrayList<>();
}
