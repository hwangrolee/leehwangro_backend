package com.example.wirebarley.service;

import com.example.wirebarley.domain.Account;
import com.example.wirebarley.domain.Transaction;
import com.example.wirebarley.domain.User;
import com.example.wirebarley.enumeration.TransactionType;
import com.example.wirebarley.repository.TransactionRepository;
import com.example.wirebarley.util.DateUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionService 유닛 테스트")
class TransactionServiceTest {

    @InjectMocks
    private TransactionService transactionService;

    @Mock
    private TransactionRepository transactionRepository;

    private Account testAccount;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");

        testAccount = new Account();
        testAccount.setId(100L);
        testAccount.setUser(testUser);
        testAccount.setBalance(90000L); // 특정 동작이 끝난 후의 잔액이라고 가정
        testAccount.setAccountNumber("111-222-3333");
    }

    @Test
    @DisplayName("계좌 ID로 거래 내역 페이징 조회 성공")
    void findBy_Success() {
        // given (준비)
        long accountId = 100L;
        Pageable pageable = PageRequest.of(0, 10);
        // transactionRepository가 반환할 가짜 페이지 객체 생성
        Page<Transaction> mockPage = new PageImpl<>(List.of(new Transaction()));

        given(transactionRepository.findByAccountIdOrderByIdDesc(accountId, pageable)).willReturn(mockPage);

        // when (실행)
        Page<Transaction> resultPage = transactionService.findBy(accountId, pageable);

        // then (검증)
        assertThat(resultPage).isNotNull();
        assertThat(resultPage).isEqualTo(mockPage); // repository가 반환한 객체와 동일한지 확인
        verify(transactionRepository, times(1)).findByAccountIdOrderByIdDesc(accountId, pageable);
    }

    @Test
    @DisplayName("일반 거래(입출금) 생성 시, 이체 관련 정보는 비어있는 상태로 저장된다")
    void createTransaction_ShouldCallTransferMethodWithNulls() {
        // given (준비)
        TransactionType type = TransactionType.DEPOSIT;
        long amount = 10000L;
        long prevBalance = 80000L;

        // save 메소드가 호출되면 전달된 인자를 그대로 반환하도록 설정
        given(transactionRepository.save(any(Transaction.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when (실행)
        // 이 메소드는 내부적으로 createTransferTransaction을 호출한다.
        transactionService.createTransaction(testAccount, type, amount, prevBalance);

        // then (검증)
        // transactionRepository.save에 전달된 Transaction 객체를 캡처
        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(transactionCaptor.capture());

        Transaction capturedTx = transactionCaptor.getValue();
        assertThat(capturedTx.getAccount().getId()).isEqualTo(testAccount.getId());
        assertThat(capturedTx.getType()).isEqualTo(type);
        assertThat(capturedTx.getGrossAmount()).isEqualTo(amount);
        assertThat(capturedTx.getNetAmount()).isEqualTo(amount);
        assertThat(capturedTx.getFee()).isZero();
        assertThat(capturedTx.getFeeRate()).isEqualTo(BigDecimal.ZERO);
        assertThat(capturedTx.getPrevBalance()).isEqualTo(prevBalance);
        assertThat(capturedTx.getPostBalance()).isEqualTo(testAccount.getBalance()); // 현재 계좌 잔액
        assertThat(capturedTx.getCounterpartyName()).isNull(); // 핵심 검증 포인트
        assertThat(capturedTx.getCounterpartyAccountNumber()).isNull(); // 핵심 검증 포인트
    }

    @Test
    @DisplayName("이체 거래 생성 시, 모든 정보(상대방 정보 포함)가 정상적으로 저장된다")
    void createTransferTransaction_Success() {
        // given (준비)
        // 상대방 계좌 정보 준비
        User counterpartyUser = new User();
        counterpartyUser.setUsername("receiver");
        Account counterpartyAccount = new Account();
        counterpartyAccount.setUser(counterpartyUser);
        counterpartyAccount.setAccountNumber("999-888-7777");

        long grossAmount = 10100L;
        long netAmount = 10000L;
        BigDecimal feeRate = new BigDecimal("0.01");
        long feeAmount = 100L;
        long prevBalance = 100100L;
        String yyyymmdd = DateUtil.yyyymmdd(ZonedDateTime.now());

        given(transactionRepository.save(any(Transaction.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when (실행)
        Transaction savedTx = transactionService.createTransferTransaction(
                testAccount, TransactionType.TRANSFER, grossAmount, netAmount,
                feeRate, feeAmount, prevBalance, counterpartyAccount, yyyymmdd
        );

        // then (검증)
        // ArgumentCaptor를 사용해도 되고, 반환된 객체를 직접 검증해도 된다.
        assertThat(savedTx.getAccount().getId()).isEqualTo(testAccount.getId());
        assertThat(savedTx.getType()).isEqualTo(TransactionType.TRANSFER);
        assertThat(savedTx.getGrossAmount()).isEqualTo(grossAmount);
        assertThat(savedTx.getNetAmount()).isEqualTo(netAmount);
        assertThat(savedTx.getFee()).isEqualTo(feeAmount);
        assertThat(savedTx.getFeeRate()).isEqualTo(feeRate);
        assertThat(savedTx.getPrevBalance()).isEqualTo(prevBalance);
        assertThat(savedTx.getPostBalance()).isEqualTo(testAccount.getBalance());
        assertThat(savedTx.getDate()).isEqualTo(yyyymmdd);
        // 상대방 정보 검증
        assertThat(savedTx.getCounterpartyName()).isEqualTo("receiver");
        assertThat(savedTx.getCounterpartyAccountNumber()).isEqualTo("999-888-7777");
    }

    @Test
    @DisplayName("사용자/유형/날짜별 순수 거래액 합계 조회를 성공적으로 위임한다")
    void sumOfNetAmountBy_DelegatesCorrectly() {
        // given (준비)
        long userId = 1L;
        TransactionType type = TransactionType.TRANSFER;
        String date = "20250802";
        long expectedSum = 500000L;

        given(transactionRepository.sumOfNetAmountBy(userId, type, date)).willReturn(expectedSum);

        // when (실행)
        long actualSum = transactionService.sumOfNetAmountBy(userId, type, date);

        // then (검증)
        assertThat(actualSum).isEqualTo(expectedSum); // repository가 반환한 값을 그대로 반환했는지 확인
        // repository의 메소드가 정확한 인자들로 호출되었는지 확인
        verify(transactionRepository, times(1)).sumOfNetAmountBy(userId, type, date);
    }
}