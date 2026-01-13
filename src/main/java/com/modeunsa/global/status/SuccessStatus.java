package com.modeunsa.global.status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessStatus {
  _OK(HttpStatus.OK, "COMMON200", "성공입니다."),
  _CREATED(HttpStatus.CREATED, "COMMON201", "리소스가 성공적으로 생성되었습니다.");

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;
}
