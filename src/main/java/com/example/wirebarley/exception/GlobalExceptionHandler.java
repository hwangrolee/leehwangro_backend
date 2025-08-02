package com.example.wirebarley.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * 애플리케이션 전역에서 발생하는 예외를 처리하는 클래스.
 * @RestControllerAdvice 어노테이션을 통해 모든 @RestController에서 발생하는 예외를 감지하고 처리합니다.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 로깅을 위한 Logger 인스턴스 생성
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * BadRequestException 및 그 하위 예외들을 처리합니다. (HTTP 400 Bad Request)
     * - AccountNotActiveException
     * - BalanceRemainingException
     * - InsufficientBalanceException
     * - InvalidAmountException
     * @param ex 발생한 BadRequestException 또는 그 하위 예외
     * @return HTTP 400 상태 코드와 에러 메시지를 담은 응답
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, String>> handleBadRequestException(BadRequestException ex) {
        log.warn("잘못된 요청 처리: {}", ex.getMessage());
        Map<String, String> errorResponse = Map.of("error", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * NotFoundException 및 그 하위 예외들을 처리합니다. (HTTP 404 Not Found)
     * - AccountNotFoundException
     * @param ex 발생한 NotFoundException 또는 그 하위 예외
     * @return HTTP 404 상태 코드와 에러 메시지를 담은 응답
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFoundException(NotFoundException ex) {
        log.warn("리소스를 찾을 수 없음: {}", ex.getMessage());
        Map<String, String> errorResponse = Map.of("error", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * 위에서 처리되지 않은 모든 예외를 처리하는 최후의 핸들러입니다. (HTTP 500 Internal Server Error)
     * 예상치 못한 서버 내부의 오류를 처리하기 위해 사용됩니다.
     * @param ex 발생한 예외
     * @return HTTP 500 상태 코드와 일반적인 에러 메시지를 담은 응답
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGlobalException(Exception ex) {
        // 심각한 오류이므로 error 레벨로 로그를 남깁니다. 스택 트레이스도 함께 기록합니다.
        log.error("처리되지 않은 예외 발생", ex);
        Map<String, String> errorResponse = Map.of("error", "서버 내부 오류가 발생했습니다. 관리자에게 문의하세요.");
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}