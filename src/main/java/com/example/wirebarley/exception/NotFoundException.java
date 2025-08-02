package com.example.wirebarley.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 요청한 리소스를 찾을 수 없을 때 발생하는 최상위 예외.
 * <p>
 * 이 예외가 Controller 계층까지 전파되면 Spring에 의해 HTTP 404 Not Found 상태 코드가 반환됩니다.
 * {@link AccountNotFoundException} 과 같이 특정 리소스를 찾지 못했을 때 발생하는
 * 구체적인 예외들은 이 클래스를 상속받아 사용합니다.
 * </p>
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class NotFoundException extends RuntimeException {

    /**
     * 지정된 상세 메시지를 사용하여 새로운 NotFoundException을 생성합니다.
     *
     * @param message 예외에 대한 상세 설명
     */
    public NotFoundException(String message) {
        super(message);
    }
}