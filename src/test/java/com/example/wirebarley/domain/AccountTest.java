package com.example.wirebarley.domain;

import com.example.wirebarley.dto.AccountDTO;
import com.example.wirebarley.enumeration.AccountStatus;
import com.example.wirebarley.exception.AccountNotActiveException;
import com.example.wirebarley.exception.BalanceRemainingException;
import com.example.wirebarley.exception.InsufficientBalanceException;
import com.example.wirebarley.exception.InvalidAmountException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Account 도메인 로직 테스트")
class AccountTest {

    private Account account;
    private User testUser;

    // 각 테스트가 실행되기 전에, 테스트에 사용할 깨끗한 Account 객체를 생성합니다.
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testUser");

        account = new Account();
        account.setUser(testUser);
        account.setBalance(10000L); // 초기 잔액 10,000원으로 설정
        account.setStatus(AccountStatus.ACTIVE);
    }

    @Test
    @DisplayName("입금 성공: 잔액이 정상적으로 증가한다")
    void testDeposit() throws Throwable {
        // given (준비)
        long depositAmount = 5000L;

        // when (실행)
        account.deposit(depositAmount);

        // then (검증)
        assertThat(account.getBalance()).isEqualTo(15000L);
        assertThat(account.getLastBalanceChangedAt()).isNotNull();
    }

    @Test
    @DisplayName("입금 실패: 0원 이하의 금액을 입금하면 InvalidAmountException 발생")
    void testDepositFailWithNegativeAmount() {
        // given (준비)
        long depositAmount = -100L;

        // when & then (실행 및 검증)
        // 특정 예외가 발생하는지 검증합니다.
        InvalidAmountException exception = assertThrows(InvalidAmountException.class, () -> {
            account.deposit(depositAmount);
        });

        // 예외 메시지가 정확한지 확인합니다.
        assertEquals("입금액은 0보다 커야 합니다.", exception.getMessage());
    }

    @Test
    @DisplayName("출금 성공: 잔액이 정상적으로 감소한다")
    void testWithdraw() throws Throwable {
        // given (준비)
        long withdrawAmount = 3000L;

        // when (실행)
        account.withdraw(withdrawAmount);

        // then (검증)
        assertThat(account.getBalance()).isEqualTo(7000L);
        assertThat(account.getLastBalanceChangedAt()).isNotNull();
    }

    @Test
    @DisplayName("출금 실패: 잔액보다 큰 금액을 출금하면 InsufficientBalanceException 발생")
    void testWithdrawFailWithInsufficientBalance() {
        // given (준비)
        long withdrawAmount = 20000L; // 현재 잔액(10000)보다 많음

        // when & then
        InsufficientBalanceException exception = assertThrows(InsufficientBalanceException.class, () -> {
            account.withdraw(withdrawAmount);
        });

        assertEquals("잔액이 부족합니다.", exception.getMessage());
    }

    @Test
    @DisplayName("계좌 삭제 성공: 활성 상태이고 잔액이 0이면 상태가 DELETED로 변경된다")
    void testDelete() throws Throwable {
        // given (준비)
        account.setBalance(0L); // 잔액을 0으로 만듦

        // when (실행)
        account.delete();

        // then (검증)
        assertThat(account.getStatus()).isEqualTo(AccountStatus.DELETED);
    }

    @Test
    @DisplayName("계좌 삭제 실패: 잔액이 남아있으면 BalanceRemainingException 발생")
    void testDeleteFailWithRemainingBalance() {
        // given - 잔액이 10000원 남아있는 상태

        // when & then
        BalanceRemainingException exception = assertThrows(BalanceRemainingException.class, () -> {
            account.delete();
        });

        assertEquals("잔액이 남아있어 삭제할 수 없습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("계좌 삭제 실패: 계좌가 활성 상태가 아니면 AccountNotActiveException 발생")
    void testDeleteFailWhenNotActive() {
        // given (준비)
        account.setBalance(0L);
        account.setStatus(AccountStatus.DORMANT); // 비활성 상태로 변경

        // when & then
        AccountNotActiveException exception = assertThrows(AccountNotActiveException.class, () -> {
            account.delete();
        });

        assertEquals("활성 상태의 계좌만 삭제할 수 있습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("toDTO: 엔티티 정보를 DTO로 정상적으로 변환한다")
    void testToDTO() {
        // given (준비)
        account.setId(1L);
        account.setAccountNumber("111-222");
        account.setLastBalanceChangedAt(ZonedDateTime.now());

        // when (실행)
        AccountDTO dto = account.toDTO();

        // then (검증)
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getAccountNumber()).isEqualTo("111-222");
        assertThat(dto.getBalance()).isEqualTo(10000L);
        assertThat(dto.getUsername()).isEqualTo("testUser");
        assertThat(dto.getLastBalanceChangedAt()).isEqualTo(account.getLastBalanceChangedAt());
    }
}