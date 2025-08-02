package com.example.wirebarley.repository;

import com.example.wirebarley.domain.Transaction;
import com.example.wirebarley.enumeration.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findByAccountIdOrderByIdDesc(long accountId, Pageable pageable);

    // 수수료를 제외한 netAmount 를 합산한다.
    @Query("SELECT COALESCE(SUM(t.netAmount), 0L) " + // 결과가 null일 경우 0을 반환
            "FROM Transaction t " +
            "WHERE t.account.user.id = :userId " +
            "AND t.type = :type " +
            "AND t.date = :date ")
    Long sumOfNetAmountBy(long userId, TransactionType type, String date);
}
