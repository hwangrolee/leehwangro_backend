package com.example.wirebarley.exception;

/**
 * 입금 또는 출금 금액이 유효하지 않을 때 (예: 0원 또는 음수) 발생하는 예외.
 */
public class InvalidAmountException extends BadRequestException {

    /**
     * 지정된 상세 메시지를 사용하여 새로운 InvalidAmountException을 생성합니다.
     *
     * @param message 예외에 대한 상세 설명
     */
    public InvalidAmountException(String message) {
        super(message);
    }
}