package com.modeunsa.global.status;

import java.util.Optional;
import java.util.function.Predicate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus {
  _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러, 관리자에게 문의 바랍니다."),
  _BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON400", "잘못된 요청입니다."),
  _UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON401", "인증이 필요합니다."),
  _FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다."),
  _NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON404", "페이지를 찾을 수 없습니다."),
  // 입력값 검증 관련 에러
  VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "VALID401", "입력값이 올바르지 않습니다."),

  // Member
  MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER404", "회원을 찾을 수 없습니다."),
  MEMBER_EMAIL_DUPLICATE(HttpStatus.CONFLICT, "MEMBER409", "이미 사용 중인 이메일입니다."),
  MEMBER_ADDRESS_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "MEMBER400", "배송지는 최대 10개까지 등록 가능합니다."),
  MEMBER_DEFAULT_ADDRESS_REQUIRED(HttpStatus.BAD_REQUEST, "MEMBER401", "기본 배송지는 null일 수 없습니다."),

  // Auth
  INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH401", "유효하지 않은 Refresh Token입니다."),
  REFRESH_TOKEN_MISMATCH(HttpStatus.UNAUTHORIZED, "AUTH402", "Refresh Token이 일치하지 않습니다."),

  // Seller
  SELLER_NOT_FOUND(HttpStatus.NOT_FOUND, "SELLER404", "판매자 정보가 없습니다."),
  SELLER_ALREADY_REGISTERED(HttpStatus.CONFLICT, "SELLER409", "이미 판매자 신청을 하셨습니다."),
  SELLER_CANNOT_APPROVE(HttpStatus.BAD_REQUEST, "SELLER400", "PENDING 상태의 판매자만 승인할 수 있습니다."),
  SELLER_CANNOT_REJECT(HttpStatus.BAD_REQUEST, "SELLER401", "PENDING 상태의 판매자만 거절할 수 있습니다."),

  // Order
  ORDERPRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER404", "상품 정보가 없습니다.");

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;

  public String getMessage(String message) {
    return Optional.ofNullable(message)
        .filter(Predicate.not(String::isBlank))
        .orElse(this.getMessage());
  }
}
