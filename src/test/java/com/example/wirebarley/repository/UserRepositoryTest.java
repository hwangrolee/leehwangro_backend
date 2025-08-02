package com.example.wirebarley.repository;

import com.example.wirebarley.domain.User;
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
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User pTestUser;
    private final String phone = "123456789";

    @BeforeEach
    void setup() {
        User testUser = new User();
        testUser.setUsername("Test User");
        testUser.setEmail("test@test.com");
        testUser.setPhone(phone);
        pTestUser = userRepository.save(testUser);
    }

    @Test
    @DisplayName("phone 필드 검색")
    void testFindByPhone() {
        Optional<User> oUser = userRepository.findByPhone(phone);

        assertThat(oUser.isPresent()).isTrue();
        assertThat(oUser.get().getPhone()).isEqualTo(phone);
        assertThat(oUser.get().getId()).isEqualTo(pTestUser.getId());
    }
}
