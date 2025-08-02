package com.example.wirebarley.service;

import com.example.wirebarley.domain.Account;
import com.example.wirebarley.domain.User;
import com.example.wirebarley.dto.CreateAccountRequestDTO;
import com.example.wirebarley.enumeration.AccountStatus;
import com.example.wirebarley.enumeration.TransactionType;
import com.example.wirebarley.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Mockito 프레임워크를 사용하기 위한 어노테이션
@DisplayName("AccountService 유닛 테스트")
class AccountServiceTest {

    @InjectMocks // 테스트 대상이 되는 클래스. @Mock으로 생성된 객체들이 자동으로 주입됩니다.
    private AccountService accountService;

    // --- 의존성 Mocking ---
    // 아래 클래스들은 실제 객체가 아닌 가짜(Mock) 객체로 만들어집니다.
    @Mock private UserService userService;
    @Mock private AccountRepository accountRepository;
    @Mock private TransactionService transactionService;

    private User testUser;
    private Account testAccount;

    @BeforeEach
    void setUp() {
        // 모든 테스트에서 공통으로 사용할 객체들을 미리 생성
        testUser = new User();
        testUser.setId(1L);
        testUser.setDailyWithdrawalLimit(1000000L);
        testUser.setDailyTransferLimit(5000000L);

        testAccount = new Account();
        testAccount.setId(100L);
        testAccount.setUser(testUser);
        testAccount.setAccountNumber("111-222-3333");
        testAccount.setBalance(50000L);
        testAccount.setStatus(AccountStatus.ACTIVE);
    }

    @Test
    @DisplayName("계좌 생성 성공")
    void testCreateAccount() {
        // given (준비): Mock 객체들의 행동을 정의
        CreateAccountRequestDTO requestDTO = new CreateAccountRequestDTO("testUser", "email@email.com", "123456789", "password123");

        // userService.findOrCreateUser가 호출되면, 미리 만들어둔 testUser 객체를 반환하도록 설정
        given(userService.findOrCreateUser(any(CreateAccountRequestDTO.class))).willReturn(testUser);
        // accountRepository.save가 호출되면, 파라미터로 받은 객체를 그대로 반환하도록 설정
        given(accountRepository.save(any(Account.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when (실행): 실제 테스트하려는 메소드를 호출
        Account createdAccount = accountService.createAccount(requestDTO);

        // then (검증): 결과 및 Mock 객체의 행위를 검증
        assertThat(createdAccount.getUser()).isEqualTo(testUser);
        assertThat(createdAccount.getBalance()).isZero();
        assertThat(createdAccount.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(createdAccount.getPassword()).isEqualTo("password123");

        // ArgumentCaptor를 사용하여 save 메소드에 전달된 실제 Account 객체를 포착
        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        // accountRepository.save가 1번 호출되었는지, 그리고 그 때 전달된 인자가 무엇인지 검증
        verify(accountRepository, times(1)).save(accountCaptor.capture());

        Account capturedAccount = accountCaptor.getValue();
        assertThat(capturedAccount.getUser().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("계좌 입금 성공: 입금 로직 호출 및 트랜잭션 기록")
    void deposit_Success() throws Throwable {
        // given (준비)
        long depositAmount = 10000L;
        // accountId로 findById를 호출하면, 미리 만들어둔 testAccount를 반환하도록 설정
        given(accountRepository.findById(testAccount.getId())).willReturn(Optional.of(testAccount));

        // when (실행)
        Account resultAccount = accountService.deposit(testAccount.getId(), depositAmount);

        // then (검증)
        // 입금 후 잔액 검증
        assertThat(resultAccount.getBalance()).isEqualTo(60000L);
        // transactionService.createTransaction이 정확한 인자들로 1번 호출되었는지 검증
        verify(transactionService, times(1))
                .createTransaction(resultAccount, TransactionType.DEPOSIT, depositAmount, 50000L);
    }

    @Test
    @DisplayName("계좌 출금 성공: 출금 한도 내")
    void testWithdrawSuccessWithinLimit() throws Throwable {
        // given (준비)
        long withdrawAmount = 20000L;
        given(accountRepository.findById(testAccount.getId())).willReturn(Optional.of(testAccount));
        // 오늘 출금액이 0원이었다고 가정
        given(transactionService.sumOfNetAmountBy(anyLong(), eq(TransactionType.WITHDRAW), anyString())).willReturn(0L);

        // when (실행)
        Account resultAccount = accountService.withdraw(testAccount.getId(), withdrawAmount);

        // then (검증)
        assertThat(resultAccount.getBalance()).isEqualTo(30000L);
        verify(transactionService, times(1))
                .createTransaction(resultAccount, TransactionType.WITHDRAW, withdrawAmount, 50000L);
    }

    @Test
    @DisplayName("계좌 출금 실패: 일일 출금 한도 초과")
    void testWithdrawFailOverLimit() {
        // given (준비)
        long withdrawAmount = 10000L;
        long dailyLimit = testUser.getDailyWithdrawalLimit(); // 1,000,000
        long alreadyWithdrawn = dailyLimit - 5000; // 이미 995,000원 출금한 상태

        given(accountRepository.findById(testAccount.getId())).willReturn(Optional.of(testAccount));
        // 오늘 출금액이 995,000원이었다고 가정
        given(transactionService.sumOfNetAmountBy(anyLong(), eq(TransactionType.WITHDRAW), anyString())).willReturn(alreadyWithdrawn);

        // when & then (실행 및 검증)
        Exception exception = assertThrows(Exception.class, () -> {
            accountService.withdraw(testAccount.getId(), withdrawAmount);
        });

        assertThat(exception.getMessage()).isEqualTo(String.format("일일 출금 한도 %d원을 초과했습니다.", dailyLimit));
        // 출금 한도 초과 시, 실제 출금 로직이나 트랜잭션 기록이 호출되지 않았는지 검증하는 것이 중요
        verify(transactionService, never()).createTransaction(any(), any(), anyLong(), anyLong());
    }

    @Test
    @DisplayName("계좌 삭제 실패: 잔액이 남아있으면 BalanceRemainingException 발생")
    void testDeleteAccountFailWithRemainingBalance() {
        // given (준비)
        // testAccount는 잔액이 50000원 있음
        given(accountRepository.findById(testAccount.getId())).willReturn(Optional.of(testAccount));

        // when & then
        // Account 도메인 객체가 던지는 예외를 그대로 전파하는지 테스트
        assertThrows(com.example.wirebarley.exception.BalanceRemainingException.class, () -> {
            accountService.deleteAccount(testAccount.getId());
        });

        // 계좌 상태가 변경되지 않았으므로 save는 호출되면 안 됨
        verify(accountRepository, never()).save(any(Account.class));
    }
}