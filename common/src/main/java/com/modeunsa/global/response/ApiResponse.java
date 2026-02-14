package com.modeunsa.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.global.status.SuccessStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;

@Getter
@AllArgsConstructor
@JsonPropertyOrder({"isSuccess", "code", "message", "pagination", "result"})
public class ApiResponse<T> {

  private final boolean isSuccess;
  private final String code;
  private final String message;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private final PaginationInfo pagination;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private final T result;

  // 성공 - 기본 응답
  public static ResponseEntity<ApiResponse> onSuccess(SuccessStatus status) {
    return new ResponseEntity<>(
        new ApiResponse(true, status.getCode(), status.getMessage(), null, null),
        status.getHttpStatus());
  }

  // 성공 - 데이터 포함
  public static <T> ResponseEntity<ApiResponse> onSuccess(SuccessStatus status, T result) {
    return new ResponseEntity<>(
        new ApiResponse(true, status.getCode(), status.getMessage(), null, result),
        status.getHttpStatus());
  }

  // 성공 - 페이지네이션 포함
  public static <T> ResponseEntity<ApiResponse> onSuccess(SuccessStatus status, Page<T> page) {
    PageInfo pageInfo =
        new PageInfo(
            page.getNumber(),
            page.getSize(),
            page.hasNext(),
            page.getTotalElements(),
            page.getTotalPages());
    return new ResponseEntity<>(
        new ApiResponse(true, status.getCode(), status.getMessage(), pageInfo, page.getContent()),
        status.getHttpStatus());
  }

  public static <T> ResponseEntity<ApiResponse> onSuccess(
      SuccessStatus status, Slice<T> slice, String nextCursor) {
    CursorInfo cursorInfo = new CursorInfo(slice.hasNext(), nextCursor);
    return new ResponseEntity<>(
        new ApiResponse(
            true, status.getCode(), status.getMessage(), cursorInfo, slice.getContent()),
        status.getHttpStatus());
  }

  // 실패한 경우 응답 생성
  public static ResponseEntity<ApiResponse> onFailure(ErrorStatus error) {
    return new ResponseEntity<>(
        new ApiResponse(false, error.getCode(), error.getMessage(), null, null),
        error.getHttpStatus());
  }

  public static ResponseEntity<ApiResponse> onFailure(ErrorStatus error, String message) {
    return new ResponseEntity<>(
        new ApiResponse(false, error.getCode(), error.getMessage(message), null, null),
        error.getHttpStatus());
  }

  public static ResponseEntity<ApiResponse> onFailure(ErrorStatus error, Object data) {
    return new ResponseEntity<>(
        new ApiResponse(false, error.getCode(), error.getMessage(), null, data),
        error.getHttpStatus());
  }
}
