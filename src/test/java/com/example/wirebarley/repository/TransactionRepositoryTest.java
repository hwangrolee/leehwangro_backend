package com.example.wirebarley.repository;

import com.example.wirebarley.domain.Account;
import com.example.wirebarley.domain.Transaction;
import com.example.wirebarley.domain.User;
import com.example.wirebarley.enumeration.AccountStatus;
import com.example.wirebarley.enumeration.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class TransactionRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private User pTestUser;
    private Account pTestAccount;
    private final String password = "password";
    private final String accountNumber = "accountNumber";
    private final long balance = 1000L;

    private final String date = "20250802";

    @BeforeEach
    void setup() {
        User testUser = new User();
        testUser.setUsername("Test User");
        testUser.setEmail("test@test.com");
        testUser.setPhone("123456789");
        pTestUser = userRepository.save(testUser);

        Account account = new Account();
        account.setUser(pTestUser);
        account.setAccountNumber(accountNumber);
        account.setStatus(AccountStatus.ACTIVE);
        account.setPassword(password);
        account.setBalance(balance);
        pTestAccount = accountRepository.save(account);
    }

    @Test
    @DisplayName("이력 없을 때 조회")
    void testFindBy() {
        Pageable pageable = Pageable.ofSize(1000);
        Page<Transaction> transactionPage = transactionRepository.findByAccountIdOrderByIdDesc(pTestUser.getId(), pageable);

        assertThat(transactionPage.getTotalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("데이터 추가 및 이력 조회")
    void testCreateAndFindBy() {
        TransactionType type = TransactionType.DEPOSIT;

        Transaction tx = Transaction.builder()
                .type(type)
                .grossAmount(100L)
                .netAmount(100L)
                .prevBalance(0L)
                .postBalance(100L)
                .fee(0L)
                .feeRate(new BigDecimal("0.0"))
                .accountId(pTestAccount.getId())
                .build();

        Transaction pTx = transactionRepository.save(tx);

        assertThat(pTx.getId()).isNotNull();
        assertThat(pTx.getType()).isEqualTo(type);
        assertThat(pTx.getAccount().getId()).isEqualTo(pTestAccount.getId());

        Pageable pageable = Pageable.ofSize(1000);
        Page<Transaction> transactionPage = transactionRepository.findByAccountIdOrderByIdDesc(pTestUser.getId(), pageable);
        assertThat(transactionPage.getTotalElements()).isEqualTo(1);
    }
}
