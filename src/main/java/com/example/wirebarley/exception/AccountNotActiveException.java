package com.example.wirebarley.exception;

/**
 * 활성(ACTIVE) 상태가 아닌 계좌에 대해 특정 작업을 시도할 때 발생하는 예외.
 * <p>
 * 예를 들어, 이미 비활성화(INACTIVE)되었거나 휴면(DORMANT) 상태인 계좌에 대해
 * 삭제, 이체 등의 작업을 요청하는 경우 이 예외가 발생할 수 있습니다.
 * </p>
 */
public class AccountNotActiveException extends BadRequestException {

    /**
     * 지정된 상세 메시지를 사용하여 새로운 AccountNotActiveException을 생성합니다.
     *
     * @param message 예외에 대한 상세 설명
     */
    public AccountNotActiveException(String message) {
        super(message);
    }
}