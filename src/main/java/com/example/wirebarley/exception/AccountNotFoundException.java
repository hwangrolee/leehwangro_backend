package com.example.wirebarley.exception;

/**
 * 특정 ID나 계좌번호에 해당하는 계좌 정보를 찾을 수 없을 때 발생하는 예외.
 * <p>
 * 주로 데이터베이스에서 계좌 조회를 시도했으나 결과가 없을 경우 사용됩니다.
 * </p>
 *
 * @see com.example.wirebarley.service.AccountService#findById(long)
 */
public class AccountNotFoundException extends NotFoundException {

    /**
     * 기본 메시지("계좌를 찾을 수 없습니다.")를 사용하여 예외를 생성합니다.
     */
    public AccountNotFoundException() {
        this("계좌를 찾을 수 없습니다.");
    }

    /**
     * 지정된 상세 메시지를 사용하여 새로운 AccountNotFoundException을 생성합니다.
     *
     * @param message 예외에 대한 상세 설명
     */
    public AccountNotFoundException(String message) {
        super(message);
    }
}