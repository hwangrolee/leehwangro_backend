package com.example.wirebarley.service;

import com.example.wirebarley.domain.Account;
import com.example.wirebarley.domain.Transaction;
import com.example.wirebarley.enumeration.TransactionType;
import com.example.wirebarley.repository.TransactionRepository;
import com.example.wirebarley.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Transactional
    public Page<Transaction> findBy(long accountId, Pageable pageable) {
        return transactionRepository.findByAccountIdOrderByIdDesc(accountId, pageable);
    }

    @Transactional
    public void createTransaction(Account account, TransactionType type, Long amount, Long prevBalance) {
        // 이체용 메서드를 호출하되, 이체 관련 정보는 모두 null로 전달
        ZonedDateTime now = DateUtil.now();
        String yyyymmdd = DateUtil.yyyymmdd(now);
        createTransferTransaction(account, type, amount, amount, BigDecimal.ZERO, 0L, prevBalance, null, yyyymmdd);
    }

    /**
     *
     * @param account 송금인
     * @param type 이체 유형 (출금/입금)
     * @param grossAmount 수수료가 포함된 이체 금액
     * @param netAmount 수수료가 미포함된 이체금액(실제 수취인 계좌에 입금되는 금액)
     * @param feeRate 수수료율
     * @param feeAmount 수수료
     * @param prevBalance 이체 전 금액
     * @param counterparty 수취인
     * @param yyyymmdd 이체날짜(yyyyMMdd)
     * @return
     */
    @Transactional
    public Transaction createTransferTransaction(Account account, TransactionType type, Long grossAmount, Long netAmount,
                                                 BigDecimal feeRate, Long feeAmount, Long prevBalance,
                                                 Account counterparty, String yyyymmdd) {

        Long postBalance = account.getBalance(); // 상태 변경이 끝난 후의 잔액을 가져옴

        Transaction.TransactionBuilder builder = Transaction.builder()
                .accountId(account.getId())
                .type(type)
                .grossAmount(grossAmount)
                .netAmount(netAmount)
                .fee(feeAmount)
                .feeRate(feeRate)
                .prevBalance(prevBalance)
                .postBalance(postBalance)
                .date(yyyymmdd);

        // 이체 거래인 경우 (상대방 정보가 있을 때) 상대방 정보를 추가
        if (counterparty != null) {
            builder.counterpartyName(counterparty.getUser().getUsername())
                    .counterpartyAccountNumber(counterparty.getAccountNumber());
        }

        Transaction transaction = builder.build();

        return transactionRepository.save(transaction);
    }

    @Transactional
    public long sumOfNetAmountBy(long userId, TransactionType type, String date) {
        return transactionRepository.sumOfNetAmountBy(userId, type, date);
    }
}
