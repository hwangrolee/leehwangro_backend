package com.example.wirebarley.integration;

import com.example.wirebarley.domain.Account;
import com.example.wirebarley.domain.Transaction;
import com.example.wirebarley.dto.CreateAccountRequestDTO;
import com.example.wirebarley.dto.TransferRequestDTO;
import com.example.wirebarley.enumeration.TransactionType;
import com.example.wirebarley.repository.AccountRepository;
import com.example.wirebarley.repository.TransactionRepository;
import com.example.wirebarley.service.AccountService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @SpringBootTest : 실제 애플리케이션처럼 전체 Spring 컨텍스트를 로드합니다.
 * @Transactional : 테스트 메소드가 종료된 후, 모든 데이터 변경사항을 롤백(Rollback)하여 테스트의 독립성을 보장합니다.
 * @ActiveProfiles("test") : 'src/test/resources/application.yml' 설정을 사용하여 테스트를 실행합니다. (H2 DB 사용)
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DisplayName("계좌 통합 테스트")
public class AccountIntegrationTest {

    // Mock 객체가 아닌 실제 Spring 컨텍스트에 등록된 Bean을 주입받습니다.
    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;


    @Test
    @DisplayName("이체 시나리오: 송금, 수수료 계산, 입금, 거래 기록이 모두 정상적으로 처리된다.")
    void transfer_Success_Scenario() throws Throwable {
        // given (준비): 송금인과 수취인의 계좌를 생성하고, 송금인 계좌에 돈을 입금합니다.
        // 이 과정 자체도 통합 테스트의 일부입니다.

        // 1. 송금인 계좌 생성
        CreateAccountRequestDTO fromUserRequest = new CreateAccountRequestDTO("송금인",
                "sender@test.com", "010-1111-1111", "password111");
        Account fromAccount = accountService.createAccount(fromUserRequest);

        // 2. 수취인 계좌 생성
        CreateAccountRequestDTO toUserRequest = new CreateAccountRequestDTO("수취인",
                "receiver@test.com", "010-2222-2222", "password222");
        Account toAccount = accountService.createAccount(toUserRequest);

        // 3. 송금인 계좌에 100,000원 입금
        accountService.deposit(fromAccount.getId(), 100000L);


        // when (실행): 송금인이 수취인에게 30,000원을 이체합니다.
        long transferAmount = 30000L;
        TransferRequestDTO transferRequest = new TransferRequestDTO(toAccount.getAccountNumber(), transferAmount);
        accountService.transfer(fromAccount.getId(), transferRequest);


        // then (검증): 모든 상태가 예상대로 변경되었는지 DB에서 직접 확인합니다.

        // 1. 송금인 계좌 잔액 검증
        // 30,000원(이체금액) + 300원(수수료 1%) = 30,300원 감소해야 함.
        // 100,000 - 30,300 = 69,700원
        Account updatedFromAccount = accountRepository.findById(fromAccount.getId()).orElseThrow();
        assertThat(updatedFromAccount.getBalance()).isEqualTo(69700L);

        // 2. 수취인 계좌 잔액 검증
        // 30,000원(이체금액)이 증가해야 함.
        Account updatedToAccount = accountRepository.findById(toAccount.getId()).orElseThrow();
        assertThat(updatedToAccount.getBalance()).isEqualTo(30000L);

        // 3. 송금인의 거래 내역 검증
        List<Transaction> fromTransactions = transactionRepository.findByAccountIdOrderByIdDesc(fromAccount.getId(),
                PageRequest.of(0, 5)).getContent();
        assertThat(fromTransactions).hasSize(2); // 입금 1건, 이체 1건

        Transaction transferTx = fromTransactions.get(0); // 가장 최근 거래
        assertThat(transferTx.getType()).isEqualTo(TransactionType.TRANSFER);
        assertThat(transferTx.getGrossAmount()).isEqualTo(30300L); // 수수료 포함 금액
        assertThat(transferTx.getNetAmount()).isEqualTo(30000L); // 순수 이체 금액
        assertThat(transferTx.getFee()).isEqualTo(300L);
        assertThat(transferTx.getFeeRate()).isEqualTo(new BigDecimal("0.01"));

        // 4. 수취인의 거래 내역 검증
        List<Transaction> toTransactions = transactionRepository.findByAccountIdOrderByIdDesc(toAccount.getId(),
                PageRequest.of(0, 5)).getContent();
        assertThat(toTransactions).hasSize(1); // 입금 1건

        Transaction depositTx = toTransactions.get(0);
        assertThat(depositTx.getType()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(depositTx.getGrossAmount()).isEqualTo(30000L);
        assertThat(depositTx.getFee()).isZero();

        // 5. 거래 내역 상호 참조 검증
        assertThat(transferTx.getRelatedTransactionId()).isEqualTo(depositTx.getId());
        assertThat(depositTx.getRelatedTransactionId()).isEqualTo(transferTx.getId());
    }
}