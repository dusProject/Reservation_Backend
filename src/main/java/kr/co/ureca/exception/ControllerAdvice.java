package kr.co.ureca.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ControllerAdvice {
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleSeatAlreadyReservedException(CustomException e) {
        ErrorResponse response = ErrorResponse.of(e.getErrorCode());
        response.setDetail(e.getMessage());
        return new ResponseEntity<>(response, e.getHttpStatus());    }
}
