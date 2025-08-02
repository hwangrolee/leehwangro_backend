package com.example.wirebarley.repository;

import com.example.wirebarley.domain.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    /**
     * ID로 계좌를 조회할 때, 데이터베이스에 비관적 쓰기 락을 설정합니다.
     * 이 트랜잭션이 끝날 때까지 다른 트랜잭션은 이 로우를 수정하거나 읽을 수 없습니다.
     * @param id must not be {@literal null}.
     * @return
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Account> findById(Long id);

    Optional<Account> findByAccountNumber(String accountNumber);
}
