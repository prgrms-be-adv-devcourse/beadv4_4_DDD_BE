package com.modeunsa.global.exception;

import com.modeunsa.global.response.ApiResponse;
import com.modeunsa.global.status.ErrorStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class ExceptionAdvice {

  private static final String EXCEPTION_ATTRIBUTE = "handledException";

  @ExceptionHandler
  public ResponseEntity<ApiResponse> validation(ConstraintViolationException e) {
    storeException(e);

    String errorMessage =
        e.getConstraintViolations().stream()
            .map(constraintViolation -> constraintViolation.getMessage())
            .findFirst()
            .orElseThrow(() -> new RuntimeException("ConstraintViolationException 추출 도중 에러 발생"));

    return ApiResponse.onFailure(ErrorStatus.VALIDATION_ERROR, errorMessage);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException e) {
    storeException(e);

    String errorMessage = e.getBindingResult().getFieldError().getDefaultMessage();
    return ApiResponse.onFailure(ErrorStatus.VALIDATION_ERROR, errorMessage);
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ApiResponse> handleNoResourceFoundException(NoResourceFoundException e) {
    storeException(e);

    return ApiResponse.onFailure(ErrorStatus.NOT_FOUND);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse> handleException(Exception e) {
    storeException(e);

    return ApiResponse.onFailure((ErrorStatus.INTERNAL_SERVER_ERROR));
  }

  @ExceptionHandler(GeneralException.class)
  public ResponseEntity<ApiResponse> handleGeneralException(GeneralException e) {
    storeException(e);
    System.out.println(">>> 예외 핸들러 도착! data 값: " + e.getData());
    if (e.getData() != null) {
      System.out.println(">>> 데이터가 있어서 onFailure(..., data) 호출함");
      // 데이터가 있는 경우
      return ApiResponse.onFailure(e.getErrorStatus(), e.getData());
    }

    System.out.println(">>> 데이터가 없어서 그냥 에러 메시지만 보냄");
    return ApiResponse.onFailure(e.getErrorStatus(), e.getMessage());
  }

  @ExceptionHandler({MethodArgumentTypeMismatchException.class, ConversionFailedException.class})
  public ResponseEntity<ApiResponse> handleConversionFailedException(Exception e) {
    storeException(e);

    return ApiResponse.onFailure((ErrorStatus.BAD_REQUEST));
  }

  private void storeException(Exception e) {
    try {
      ServletRequestAttributes attributes =
          (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

      if (attributes != null) {
        HttpServletRequest request = attributes.getRequest();
        request.setAttribute(EXCEPTION_ATTRIBUTE, e);
      }
    } catch (Exception ex) {
      log.error("Failed to save exception in ExceptionAdvice", ex);
    }
  }
}
