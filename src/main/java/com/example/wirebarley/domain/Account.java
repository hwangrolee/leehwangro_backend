package com.example.wirebarley.domain;

import com.example.wirebarley.dto.AccountDTO;
import com.example.wirebarley.enumeration.AccountStatus;
import com.example.wirebarley.exception.AccountNotActiveException;
import com.example.wirebarley.exception.BalanceRemainingException;
import com.example.wirebarley.exception.InsufficientBalanceException;
import com.example.wirebarley.exception.InvalidAmountException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;

@Entity
@Table(name = "account", indexes = {
        @Index(name = "idx__account_number", columnList = "accountNumber"),
})
@Getter
@Setter
@NoArgsConstructor
public class Account extends AbstractDomain {

    @Column(nullable = false, unique = true, length = 20)
    private String accountNumber; // 계좌번호

    @Column(nullable = false)
    private String password; // 계좌 패스워드

    @Column(nullable = false)
    private long balance; // 잔액

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status = AccountStatus.ACTIVE; // 계좌상태(ACTIVE, INACTIVE, DORMANT)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(columnDefinition = "timestamp")
    private ZonedDateTime lastBalanceChangedAt;

    public Account(long id) {
        this.setId(id);
    }


    public void deposit(long amount) throws Throwable {
        if (amount <= 0) {
            throw new InvalidAmountException("입금액은 0보다 커야 합니다.");
        }

        this.balance += amount;
        this.lastBalanceChangedAt = ZonedDateTime.now();
    }

    public void withdraw(long amount) throws Throwable {
        if (amount <= 0) {
            throw new InvalidAmountException("출금액은 0보다 커야 합니다.");
        }
        if (this.balance < amount) {
            throw new InsufficientBalanceException("잔액이 부족합니다.");
        }

        this.balance -= amount;
        this.lastBalanceChangedAt = ZonedDateTime.now();
    }

    public void delete() throws Throwable {
        this.validateDeletable();
        this.status = AccountStatus.DELETED;
    }

    private void validateDeletable() throws Throwable {
        if (!this.status.isActive()) {
            throw new AccountNotActiveException("활성 상태의 계좌만 삭제할 수 있습니다.");
        }
        if (this.balance > 0) {
            throw new BalanceRemainingException("잔액이 남아있어 삭제할 수 없습니다.");
        }
    }

    public AccountDTO toDTO() {
        return AccountDTO.builder()
                .id(this.getId())
                .accountNumber(this.accountNumber)
                .balance(this.balance)
                .username(this.user.getUsername())
                .lastBalanceChangedAt(this.lastBalanceChangedAt)
                .build();
    }
}
