package com.example.wirebarley.exception;

/**
 * 출금 또는 이체 시, 계좌의 잔액이 요청된 금액보다 부족할 때 발생하는 예외.
 */
public class InsufficientBalanceException extends BadRequestException {

    /**
     * 지정된 상세 메시지를 사용하여 새로운 InsufficientBalanceException을 생성합니다.
     *
     * @param message 예외에 대한 상세 설명
     */
    public InsufficientBalanceException(String message) {
        super(message);
    }
}