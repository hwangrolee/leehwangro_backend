package com.example.wirebarley.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 클라이언트의 잘못된 요청으로 인해 작업을 처리할 수 없을 때 발생하는 최상위 예외.
 * <p>
 * 이 예외가 Controller 계층까지 전파되면 Spring에 의해 HTTP 400 Bad Request 상태 코드가 반환됩니다.
 * {@link InsufficientBalanceException}, {@link InvalidAmountException} 등
 * 비즈니스 규칙 위반과 관련된 구체적인 예외들은 이 클래스를 상속받아 사용합니다.
 * </p>
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {

    /**
     * 지정된 상세 메시지를 사용하여 새로운 BadRequestException을 생성합니다.
     *
     * @param message 예외에 대한 상세 설명
     */
    public BadRequestException(String message) {
        super(message);
    }
}