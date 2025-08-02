package com.example.wirebarley.service;

import com.example.wirebarley.domain.Account;
import com.example.wirebarley.domain.Transaction;
import com.example.wirebarley.domain.User;
import com.example.wirebarley.dto.CreateAccountRequestDTO;
import com.example.wirebarley.dto.TransferRequestDTO;
import com.example.wirebarley.enumeration.AccountStatus;
import com.example.wirebarley.enumeration.TransactionType;
import com.example.wirebarley.exception.AccountNotFoundException;
import com.example.wirebarley.repository.AccountRepository;
import com.example.wirebarley.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * 계좌 관련 비즈니스 로직을 처리하는 서비스 클래스.
 * 계좌 생성, 조회, 삭제, 입출금, 이체 등의 기능을 담당합니다.
 */
@Service
public class AccountService {

    @Autowired
    private UserService userService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionService transactionService;

    /**
     * 계좌 ID로 계좌 정보를 조회합니다. 결과는 Optional로 반환됩니다.
     * 계좌 존재 여부를 확인하고 싶을 때 예외 없이 안전하게 사용합니다.
     *
     * @param accountId 조회할 계좌의 ID
     * @return 계좌 정보가 담긴 Optional 객체. 계좌가 없으면 비어있는 Optional을 반환합니다.
     */
    @Transactional
    public Optional<Account> oFindById(Long accountId) {
        return accountRepository.findById(accountId);
    }

    /**
     * 계좌 ID로 계좌 정보를 조회합니다.
     * 해당 ID의 계좌가 반드시 존재한다고 가정할 때 사용하며, 없으면 NoSuchElementException이 발생합니다.
     *
     * @param accountId 조회할 계좌의 ID
     * @return 조회된 Account 엔티티
     * @throws com.example.wirebarley.exception.AccountNotFoundException 계좌가 존재하지 않을 경우
     */
    @Transactional
    public Account findById(long accountId) throws Throwable {
        return this.oFindById(accountId).orElseThrow(AccountNotFoundException::new);
    }

    /**
     * 새로운 계좌를 생성합니다.
     * 요청 정보(DTO)를 바탕으로 사용자를 찾거나 새로 생성한 후, 해당 사용자에게 새 계좌를 할당합니다.
     *
     * @param requestDTO 계좌 생성을 위한 사용자 정보 및 패스워드가 담긴 DTO
     * @return 생성되고 데이터베이스에 저장된 Account 엔티티
     */
    @Transactional
    public Account createAccount(CreateAccountRequestDTO requestDTO) {
        // DTO 정보로 사용자를 찾거나 새로 생성합니다.
        User user = userService.findOrCreateUser(requestDTO);
        // 충돌 가능성이 매우 낮은 고유한 계좌번호를 생성합니다.
        String accountNumber = this.generateAccountNumber();

        Account account = new Account();
        account.setUser(user);
        account.setBalance(0L); // 초기 잔액은 0원
        account.setAccountNumber(accountNumber);
        account.setPassword(requestDTO.getPassword());
        account.setStatus(AccountStatus.ACTIVE); // 기본 상태는 활성

        return accountRepository.save(account);
    }

    /**
     * 특정 계좌를 삭제 처리합니다.
     * 실제 데이터를 지우는 것이 아니라, 계좌의 상태를 'DELETED'로 변경합니다.
     * 도메인 객체의 delete() 메소드를 통해 삭제 가능 여부를 검증합니다.
     *
     * @param accountId 삭제할 계좌의 ID
     * @throws Throwable 도메인 객체의 삭제 검증 로직(잔액 존재, 비활성 상태)에서 예외 발생 시
     */
    @Transactional
    public void deleteAccount(long accountId) throws Throwable {
        Account account = this.findById(accountId);
        // 도메인 로직에 삭제를 위임. 잔액이 남아있거나 비활성 상태면 예외 발생.
        account.delete();
        accountRepository.save(account);
    }

    /**
     * 특정 계좌에 금액을 입금하고, 입금 거래 기록을 생성합니다.
     *
     * @param accountId 입금할 계좌의 ID
     * @param amount 입금할 금액
     * @return 입금 처리 후의 Account 엔티티
     * @throws Throwable 도메인 객체의 입금 로직(0원 이하 입금 시도 등)에서 예외 발생 시
     */
    @Transactional
    public Account deposit(long accountId, long amount) throws Throwable {
        Account account = this.findById(accountId);
        long prevBalance = account.getBalance(); // 거래 전 잔액 기록
        account.deposit(amount); // 도메인 객체에 입금을 위임

        // 입금 거래 내역 생성
        transactionService.createTransaction(account, TransactionType.DEPOSIT, amount, prevBalance);
        return account;
    }

    /**
     * 특정 계좌에서 금액을 출금하고, 출금 거래 기록을 생성합니다.
     * 사용자의 일일 출금 한도를 확인하여 초과 시 예외를 발생시킵니다.
     *
     * @param accountId 출금할 계좌의 ID
     * @param amount 출금할 금액
     * @return 출금 처리 후의 Account 엔티티
     * @throws Throwable 잔액 부족, 한도 초과 등 출금 로직에서 예외 발생 시
     */
    @Transactional
    public Account withdraw(Long accountId, Long amount) throws Throwable {
        Account account = this.findById(accountId);
        long prevBalance = account.getBalance();

        User user = account.getUser();
        long userId = user.getId();
        long dailyWithdrawalLimit = user.getDailyWithdrawalLimit();

        // 일일 출금 한도를 초과하는지 확인
        long todayRemainingWithdrawalLimit = this.getTodayRemainingWithdrawalLimit(userId, dailyWithdrawalLimit, amount);
        if (todayRemainingWithdrawalLimit < 0) {
            String message = String.format("일일 출금 한도 %d원을 초과했습니다.", dailyWithdrawalLimit);
            throw new Exception(message);
        }

        account.withdraw(amount); // 도메인 객체에 출금을 위임
        // 출금 거래 내역 생성
        transactionService.createTransaction(account, TransactionType.WITHDRAW, amount, prevBalance);
        return account;
    }

    /**
     * 특정 계좌에서 다른 계좌로 금액을 이체합니다.
     * 이체 한도 확인, 수수료(1%) 계산, 양쪽 계좌의 입출금 처리, 거래 기록 2건 생성을 모두 처리합니다.
     *
     * @param accountId 송금인 계좌 ID
     * @param requestDTO 수취인 계좌번호와 이체 금액이 담긴 DTO
     * @return 이체 처리 후의 송금인 Account 엔티티
     * @throws Throwable 한도 초과, 잔액 부족, 수취인 계좌 없음 등 이체 로직에서 예외 발생 시
     */
    @Transactional
    public Account transfer (Long accountId, TransferRequestDTO requestDTO) throws Throwable {
        Account fromAccount = this.findById(accountId);
        User user = fromAccount.getUser();
        long userId = user.getId();
        long requestedAmount = requestDTO.getAmount();

        long dailyTransferLimit = user.getDailyTransferLimit();

        // 일일 이체 한도를 초과하는지 확인
        long todayRemainingTransferLimit = this.getTodayRemainingTransferLimit(userId, dailyTransferLimit, requestedAmount);
        if (todayRemainingTransferLimit < 0) {
            String message = String.format("일일 이체 한도 %d원을 초과했습니다.", dailyTransferLimit);
            throw new Exception(message);
        }

        // 수취인 계좌를 조회
        String counterpartyAccountNumber = requestDTO.getCounterpartyAccountNumber();
        Optional<Account> oTargetAccount = accountRepository.findByAccountNumber(counterpartyAccountNumber);
        if (oTargetAccount.isEmpty()) {
            throw new Exception("존재하지 않는 계좌번호입니다.");
        }
        Account toAccount = oTargetAccount.get();

        // 이체 수수료(1%, 소수점 버림)를 계산
        final BigDecimal feeRate = new BigDecimal("0.01");
        long feeAmount = new BigDecimal(requestedAmount)
                .multiply(feeRate)
                .setScale(0, RoundingMode.DOWN)
                .longValue();

        long grossAmount = requestedAmount + feeAmount; // 수수료를 포함한 총 출금액

        // 1. 송금인 계좌에서 총 출금액(이체액+수수료)만큼 출금
        long fromAccountPrevBalance = fromAccount.getBalance();
        fromAccount.withdraw(grossAmount);

        ZonedDateTime now = DateUtil.now();
        String yyyymmdd = DateUtil.yyyymmdd(now);

        // 2. 송금인의 '이체' 거래 기록 생성
        Transaction withdrawalTx = transactionService.createTransferTransaction(fromAccount,
                TransactionType.TRANSFER, grossAmount, requestedAmount, feeRate, feeAmount,
                fromAccountPrevBalance, toAccount, yyyymmdd);

        // 3. 수취인 계좌에 순수 이체액만큼 입금
        long toAccountPrevBalance = toAccount.getBalance();
        toAccount.deposit(requestedAmount);

        // 4. 수취인의 '입금' 거래 기록 생성
        Transaction depositTx = transactionService.createTransferTransaction(toAccount,
                TransactionType.DEPOSIT, requestedAmount, requestedAmount, BigDecimal.ZERO, 0L,
                toAccountPrevBalance, fromAccount, yyyymmdd);

        // 5. 두 거래 기록을 서로 연결
        withdrawalTx.setRelatedTransactionId(depositTx.getId());
        depositTx.setRelatedTransactionId(withdrawalTx.getId());

        return fromAccount;
    }

    /**
     * 오늘의 남은 이체 한도를 계산하여 반환합니다.
     * @param userId 사용자 ID
     * @param dailyTransferLimit 사용자의 일일 이체 한도
     * @param amount 현재 이체하려는 금액
     * @return 남은 이체 가능 금액 (음수일 경우 한도 초과)
     */
    @Transactional
    public long getTodayRemainingTransferLimit(long userId, long dailyTransferLimit, long amount) {
        return this.getTodayRemainingBalance(userId, dailyTransferLimit, amount, TransactionType.TRANSFER);
    }

    /**
     * 오늘의 남은 출금 한도를 계산하여 반환합니다.
     * @param userId 사용자 ID
     * @param dailyWithdrawalLimit 사용자의 일일 출금 한도
     * @param amount 현재 출금하려는 금액
     * @return 남은 출금 가능 금액 (음수일 경우 한도 초과)
     */
    @Transactional
    public long getTodayRemainingWithdrawalLimit(long userId, long dailyWithdrawalLimit, long amount) {
        return this.getTodayRemainingBalance(userId, dailyWithdrawalLimit, amount, TransactionType.WITHDRAW);
    }

    // =============================================
    // ================== private ==================
    // =============================================

    /**
     * 특정 거래 유형에 대한 오늘의 남은 한도를 계산하는 내부 메소드.
     * @param userId 사용자 ID
     * @param limit 일일 한도 금액
     * @param amount 현재 거래하려는 금액
     * @param type 거래 유형 (TRANSFER 또는 WITHDRAW)
     * @return 남은 한도 금액
     */
    private long getTodayRemainingBalance(Long userId, Long limit, Long amount, TransactionType type) {
        ZonedDateTime now = DateUtil.now();
        String yyyymmdd = DateUtil.yyyymmdd(now);
        // 오늘 해당 유형으로 거래한 총액을 조회
        long sumOfNetAmount = transactionService.sumOfNetAmountBy(userId, type, yyyymmdd);
        // (일일 한도) - (오늘 이미 쓴 돈 + 지금 쓰려는 돈)
        return limit - (amount + sumOfNetAmount);
    }

    /**
     * 고유한 16자리 계좌번호를 생성합니다.
     * UUID를 기반으로 하여 충돌 확률이 매우 낮습니다.
     * @return 생성된 16자리 영문 대문자+숫자 조합의 계좌번호
     */
    private String generateAccountNumber() {
        // 1. UUID 생성 (예: 550e8400-e29b-41d4-a716-446655440000)
        String uuid = UUID.randomUUID().toString();

        // 2. 하이픈 제거 (예: 550e8400e29b41d4a716446655440000)
        String noHyphenUuid = uuid.replaceAll("-", "");

        // 3. 원하는 길이만큼 잘라내서 반환 (예: 앞 16자리)
        return noHyphenUuid.substring(0, 16).toUpperCase();
    }
}