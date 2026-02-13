package com.modeunsa.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.global.status.SuccessStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;

@Getter
@RequiredArgsConstructor
@JsonPropertyOrder({"isSuccess", "code", "message", "pageInfo", "cursorInfo", "result"})
public class ApiResponse {

  private final Boolean isSuccess;
  private final String code;
  private final String message;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private final PageInfo pageInfo;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private final CursorInfo cursorInfo;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private final Object result;

  // 성공한 경우 응답 생성
  public static ResponseEntity<ApiResponse> onSuccess(
      SuccessStatus status, PageInfo pageInfo, CursorInfo cursorInfo, Object result) {
    return new ResponseEntity<>(
        new ApiResponse(true, status.getCode(), status.getMessage(), pageInfo, cursorInfo, result),
        status.getHttpStatus());
  }

  // 성공 - 기본 응답
  public static ResponseEntity<ApiResponse> onSuccess(SuccessStatus status) {
    return onSuccess(status, null, null, null);
  }

  // 성공 - 데이터 포함
  public static ResponseEntity<ApiResponse> onSuccess(SuccessStatus status, Object result) {
    return onSuccess(status, null, null, result);
  }

  // 성공 - 페이지네이션 포함
  public static ResponseEntity<ApiResponse> onSuccess(SuccessStatus status, Page<?> page) {
    PageInfo pageInfo =
        new PageInfo(
            page.getNumber(),
            page.getSize(),
            page.hasNext(),
            page.getTotalElements(),
            page.getTotalPages());
    return onSuccess(status, pageInfo, null, page.getContent());
  }

  public static ResponseEntity<ApiResponse> onSuccess(
      SuccessStatus status, Slice<?> slice, String nextCursor) {
    CursorInfo cursorInfo = new CursorInfo(slice.hasNext(), nextCursor);
    return onSuccess(status, null, cursorInfo, slice.getContent());
  }

  // 실패한 경우 응답 생성
  public static ResponseEntity<ApiResponse> onFailure(ErrorStatus error) {
    return new ResponseEntity<>(
        new ApiResponse(false, error.getCode(), error.getMessage(), null, null, null),
        error.getHttpStatus());
  }

  public static ResponseEntity<ApiResponse> onFailure(ErrorStatus error, String message) {
    return new ResponseEntity<>(
        new ApiResponse(false, error.getCode(), error.getMessage(message), null, null, null),
        error.getHttpStatus());
  }

  public static ResponseEntity<ApiResponse> onFailure(ErrorStatus error, Object data) {
    return new ResponseEntity<>(
        new ApiResponse(false, error.getCode(), error.getMessage(), null, null, data),
        error.getHttpStatus());
  }
}
