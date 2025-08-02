package com.example.wirebarley.repository;

import com.example.wirebarley.domain.Account;
import com.example.wirebarley.domain.User;
import com.example.wirebarley.enumeration.AccountStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    private User pTestUser;
    private Account pTestAccount;
    private final String password = "password";
    private final String accountNumber = "accountNumber";
    private final long balance = 1000L;

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
    @DisplayName("계좌 생성")
    void testCreateAccount() {
        assertThat(pTestAccount.getId()).isNotNull();
        assertThat(pTestAccount.getAccountNumber()).isEqualTo(accountNumber);
        assertThat(pTestAccount.getPassword()).isEqualTo(password);
        assertThat(pTestAccount.getBalance()).isEqualTo(balance);
        assertThat(pTestAccount.getUser().getId()).isEqualTo(pTestUser.getId());
    }

    @Test
    @DisplayName("계좌 검색")
    void testFindByAccountNumber() {
        Optional<Account> oAccount = accountRepository.findByAccountNumber(accountNumber);

        assertThat(oAccount.isPresent()).isTrue();

        assertThat(oAccount.get().getAccountNumber()).isEqualTo(accountNumber);
    }
}
