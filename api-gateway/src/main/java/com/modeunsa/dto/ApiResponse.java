package com.modeunsa.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.modeunsa.status.ErrorStatus;
import com.modeunsa.status.SuccessStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"isSuccess", "code", "message", "pagination", "result"})
public class ApiResponse<T> {

  private Boolean isSuccess;
  private String code;
  private String message;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private T result;

  // 성공 - 기본 응답
  public static ResponseEntity<ApiResponse> onSuccess(SuccessStatus status) {
    return new ResponseEntity<>(
        new ApiResponse(true, status.getCode(), status.getMessage(), null), status.getHttpStatus());
  }

  // 성공 - 데이터 포함
  public static <T> ResponseEntity<ApiResponse> onSuccess(SuccessStatus status, T result) {
    return new ResponseEntity<>(
        new ApiResponse(true, status.getCode(), status.getMessage(), result),
        status.getHttpStatus());
  }

  // 실패한 경우 응답 생성
  public static ResponseEntity<ApiResponse> onFailure(ErrorStatus error) {
    return new ResponseEntity<>(
        new ApiResponse(false, error.getCode(), error.getMessage(), null), error.getHttpStatus());
  }

  public static ResponseEntity<ApiResponse> onFailure(ErrorStatus error, String message) {
    return new ResponseEntity<>(
        new ApiResponse(false, error.getCode(), error.getMessage(message), null),
        error.getHttpStatus());
  }

  public static ResponseEntity<ApiResponse> onFailure(ErrorStatus error, Object data) {
    return new ResponseEntity<>(
        new ApiResponse(false, error.getCode(), error.getMessage(), data), error.getHttpStatus());
  }
}
