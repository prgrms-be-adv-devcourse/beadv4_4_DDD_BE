package com.modeunsa.global.status;

import java.util.Optional;
import java.util.function.Predicate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus {
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500", "서버 에러, 관리자에게 문의 바랍니다."),
  BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON_400", "잘못된 요청입니다."),
  UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON_401", "인증이 필요합니다."),
  FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON_403", "금지된 요청입니다."),
  NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_404", "페이지를 찾을 수 없습니다."),
  // 입력값 검증 관련 에러
  VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "VALID_401", "입력값이 올바르지 않습니다."),

  // Member 400
  MEMBER_ADDRESS_LIMIT_EXCEEDED(
      HttpStatus.BAD_REQUEST, "MEMBER_400_001", "배송지는 최대 10개까지 등록할 수 있습니다."),
  MEMBER_DEFAULT_ADDRESS_REQUIRED(HttpStatus.BAD_REQUEST, "MEMBER_400_002", "기본 배송지가 필요합니다."),
  SELLER_CANNOT_APPROVE(HttpStatus.BAD_REQUEST, "MEMBER_400_003", "승인 대기 상태가 아닌 판매자는 승인할 수 없습니다."),
  SELLER_CANNOT_REJECT(HttpStatus.BAD_REQUEST, "MEMBER_400_004", "승인 대기 상태가 아닌 판매자는 거절할 수 없습니다."),
  // Member 403
  MEMBER_SUSPENDED(HttpStatus.FORBIDDEN, "MEMBER_403_001", "정지된 회원입니다."),
  MEMBER_WITHDRAWN(HttpStatus.FORBIDDEN, "MEMBER_403_002", "탈퇴한 회원입니다."),
  // Member 404
  MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_404_001", "회원을 찾을 수 없습니다."),
  SELLER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_404_002", "판매자 정보가 없습니다."),
  // Member 409
  MEMBER_ALREADY_EXISTS(HttpStatus.CONFLICT, "MEMBER_409_001", "이미 존재하는 회원입니다."),
  MEMBER_EMAIL_DUPLICATE(HttpStatus.CONFLICT, "MEMBER_409_002", "이미 사용 중인 이메일입니다."),
  SELLER_ALREADY_REGISTERED(HttpStatus.CONFLICT, "MEMBER_409_003", "이미 판매자 신청을 하셨습니다."),

  // Auth 400
  OAUTH_INVALID_PROVIDER(HttpStatus.BAD_REQUEST, "AUTH_400_001", "지원하지 않는 OAuth 제공자입니다."),
  // Auth 401
  AUTH_INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_401_001", "유효하지 않은 토큰입니다."),
  AUTH_EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_401_002", "만료된 토큰입니다."),
  AUTH_INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_401_003", "유효하지 않은 Refresh Token입니다."),
  AUTH_REFRESH_TOKEN_NOT_FOUND(
      HttpStatus.UNAUTHORIZED, "AUTH_401_004", "Refresh Token이 존재하지 않습니다."),
  // Auth 403
  AUTH_ACCESS_DENIED(HttpStatus.FORBIDDEN, "AUTH_403_001", "접근 권한이 없습니다."),
  // Auth 502
  OAUTH_TOKEN_REQUEST_FAILED(HttpStatus.BAD_GATEWAY, "AUTH_502_001", "OAuth 토큰 요청에 실패했습니다."),
  OAUTH_USER_INFO_FAILED(HttpStatus.BAD_GATEWAY, "AUTH_502_002", "OAuth 사용자 정보 요청에 실패했습니다."),
  OAUTH_UNLINK_FAILED(HttpStatus.BAD_GATEWAY, "AUTH_502_003", "OAuth 연결 해제에 실패했습니다."),

  // Order
  ORDERPRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER_404_001", "없는 상품입니다."),
  ORDERMEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER_404_002", "없는 회원입니다."),

  // Product 400
  PRODUCT_DESCRIPTION_REQUIRED(HttpStatus.BAD_REQUEST, "PRODUCT_400_001", "상품 설명은 필수입니다."),
  PRODUCT_CATEGORY_REQUIRED(HttpStatus.BAD_REQUEST, "PRODUCT_400_002", "카테고리는 필수입니다."),
  PRODUCT_SALE_PRICE_REQUIRED(HttpStatus.BAD_REQUEST, "PRODUCT_400_003", "판매가는 0원 이상이어야 합니다."),
  PRODUCT_PRICE_REQUIRED(HttpStatus.BAD_REQUEST, "PRODUCT_400_004", "정가는 0원 이상이어야 합니다."),
  PRODUCT_QTY_REQUIRED(HttpStatus.BAD_REQUEST, "PRODUCT_400_005", "재고 수량은 0보다 커야 합니다."),
  // PRODUCT 404
  PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT_404_001", "상품이 존재하지 않습니다."),

  // Payment
  PAYMENT_MEMBER_DUPLICATE(HttpStatus.BAD_REQUEST, "PAYMENT_400_001", "이미 등록된 결제 회원 정보가 존재합니다."),
  PAYMENT_ACCOUNT_DUPLICATE(HttpStatus.BAD_REQUEST, "PAYMENT_400_002", "이미 등록된 결제 계좌 정보가 존재합니다."),
  PAYMENT_ACCOUNT_INVALID(HttpStatus.BAD_REQUEST, "PAYMENT_400_003", "잘못된 요청입니다."),
  PAYMENT_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "PAYMENT_404_001", "결제 회원 정보를 찾을 수 없습니다."),
  PAYMENT_ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "PAYMENT_404_002", "결제 회원 정보를 찾을 수 없습니다.");

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;

  public String getMessage(String message) {
    return Optional.ofNullable(message)
        .filter(Predicate.not(String::isBlank))
        .orElse(this.getMessage());
  }
}
