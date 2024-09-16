package kr.co.ureca.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import java.util.function.Supplier;


@AllArgsConstructor
@Getter
public class CustomException extends RuntimeException implements Supplier<CustomException> {
    private ErrorCode errorCode;
    private HttpStatus httpStatus;

    public CustomException(ErrorCode errorCode){
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    }
    @Override
    public String getMessage() {
        return errorCode.getMessage();  // ErrorCode에서 메시지를 가져옴
    }

    @Override
    public CustomException get() {
        return new CustomException(errorCode, httpStatus);
    }
}
