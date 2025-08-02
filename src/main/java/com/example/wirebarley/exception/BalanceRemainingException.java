package com.example.wirebarley.exception;

/**
 * 잔액이 0원이 아닌 상태에서 계좌 삭제 등 잔액이 없어야 하는 작업을 시도할 때 발생하는 예외.
 */
public class BalanceRemainingException extends BadRequestException {

    /**
     * 지정된 상세 메시지를 사용하여 새로운 BalanceRemainingException을 생성합니다.
     *
     * @param message 예외에 대한 상세 설명
     */
    public BalanceRemainingException(String message) {
        super(message);
    }
}