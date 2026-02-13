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

  // Config 500
  CONFIG_INVALID_JWT_SECRET(
      HttpStatus.INTERNAL_SERVER_ERROR, "CONFIG_500_001", "JWT secret은 최소 32바이트 이상이어야 합니다."),

  // Auth 400
  OAUTH_INVALID_PROVIDER(HttpStatus.BAD_REQUEST, "AUTH_400_001", "지원하지 않는 OAuth 제공자입니다."),
  OAUTH_INVALID_REDIRECT_URI(HttpStatus.BAD_REQUEST, "AUTH_400_002", "유효하지 않은 리다이렉트 URI입니다."),
  AUTH_INVALID_TOKEN_FORMAT(HttpStatus.BAD_REQUEST, "AUTH_400_003", "잘못된 토큰 형식입니다."),
  AUTH_NOT_ACCESS_TOKEN(HttpStatus.BAD_REQUEST, "AUTH_400_004", "Access Token이 아닙니다."),
  AUTH_UNAUTHORIZED(HttpStatus.BAD_REQUEST, "AUTH_400_005", "인증되지 않은 사용자입니다."),
  OAUTH_INVALID_STATE(HttpStatus.BAD_REQUEST, "AUTH_400_006", "유효하지 않은 state 값입니다."),
  // Auth 401
  AUTH_INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_401_001", "유효하지 않은 토큰입니다."),
  AUTH_EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_401_002", "만료된 토큰입니다."),
  AUTH_INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_401_003", "유효하지 않은 Refresh Token입니다."),
  AUTH_REFRESH_TOKEN_NOT_FOUND(
      HttpStatus.UNAUTHORIZED, "AUTH_401_004", "Refresh Token이 존재하지 않습니다."),
  AUTH_INVALID_TOKEN_TYPE(HttpStatus.UNAUTHORIZED, "AUTH_401_005", "유효하지 않은 토큰 타입입니다."),
  AUTH_INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_401_006", "유효하지 않은 Access Token입니다."),
  AUTH_BLACKLISTED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_401_007", "로그아웃된 토큰입니다."),
  AUTH_TOKEN_REFRESH_FAILED(HttpStatus.UNAUTHORIZED, "AUTH_401_008", "토큰 재발급에 실패했습니다."),
  // Auth 403
  AUTH_ACCESS_DENIED(HttpStatus.FORBIDDEN, "AUTH_403_001", "접근 권한이 없습니다."),
  // Auth 409
  AUTH_CONFLICT_LOGIN_PROGRESS(
      HttpStatus.CONFLICT, "AUTH_409", "이미 가입 처리가 진행 중입니다. 잠시 후 다시 로그인해 주세요."),
  // Auth 429
  AUTH_TOO_MANY_REQUESTS(
      HttpStatus.TOO_MANY_REQUESTS,
      "AUTH_429_001",
      "현재 로그인 요청이 많아 처리가 지연되고 있습니다. 잠시 후 다시 시도해 주세요."),
  // Auth 502
  OAUTH_TOKEN_REQUEST_FAILED(HttpStatus.BAD_GATEWAY, "AUTH_502_001", "OAuth 토큰 요청에 실패했습니다."),
  OAUTH_USER_INFO_FAILED(HttpStatus.BAD_GATEWAY, "AUTH_502_002", "OAuth 사용자 정보 요청에 실패했습니다."),
  OAUTH_UNLINK_FAILED(HttpStatus.BAD_GATEWAY, "AUTH_502_003", "OAuth 연결 해제에 실패했습니다."),

  // Member 400
  MEMBER_ADDRESS_LIMIT_EXCEEDED(
      HttpStatus.BAD_REQUEST, "MEMBER_400_001", "배송지는 최대 10개까지 등록할 수 있습니다."),
  MEMBER_DEFAULT_ADDRESS_REQUIRED(HttpStatus.BAD_REQUEST, "MEMBER_400_002", "기본 배송지가 필요합니다."),
  SELLER_CANNOT_APPROVE(HttpStatus.BAD_REQUEST, "MEMBER_400_003", "승인 대기 상태가 아닌 판매자는 승인할 수 없습니다."),
  SELLER_CANNOT_REJECT(HttpStatus.BAD_REQUEST, "MEMBER_400_004", "승인 대기 상태가 아닌 판매자는 거절할 수 없습니다."),
  SELLER_CANNOT_SUSPEND(HttpStatus.BAD_REQUEST, "MEMBER_400_005", "활성화 상태가 아닌 판매자는 정지할 수 없습니다."),
  SELLER_INVALID_BANK_ACCOUNT(HttpStatus.BAD_REQUEST, "MEMBER_400_006", "계좌번호 형식이 올바르지 않습니다."),
  MEMBER_INVALID_ID_FORMAT(HttpStatus.BAD_REQUEST, "MEMBER400_007", "회원 ID 형식이 올바르지 않습니다."),
  // Member 403
  MEMBER_SUSPENDED(HttpStatus.FORBIDDEN, "MEMBER_403_001", "정지된 회원입니다."),
  MEMBER_WITHDRAWN(HttpStatus.FORBIDDEN, "MEMBER_403_002", "탈퇴한 회원입니다."),
  ADDRESS_ACCESS_DENIED(HttpStatus.FORBIDDEN, "MEMBER_403_003", "배송지에 대한 접근 권한이 없습니다."),
  MEMBER_NOT_ACTIVATED(HttpStatus.FORBIDDEN, "MEMBER_403_004", "ACTIVE 상태가 아닌 회원입니다."),
  MEMBER_NOT_PREACTIVE(HttpStatus.FORBIDDEN, "MEMBER_403_005", "PRE_ACTIVE 상태가 아닌 회원입니다."),
  // Member 404
  MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_404_001", "회원을 찾을 수 없습니다."),
  SELLER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_404_002", "판매자 정보가 없습니다."),
  ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_404_003", "배송지 정보를 찾을 수 없습니다."),
  MEMBER_PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_404_004", "회원 프로필을 찾을 수 없습니다."),
  MEMBER_DELIVERY_ADDRESS_NOT_FOUND(
      HttpStatus.NOT_FOUND, "MEMBER_404_005", "회원 배송지 정보를 찾을 수 없습니다."),
  // Member 409
  MEMBER_ALREADY_EXISTS(HttpStatus.CONFLICT, "MEMBER_409_001", "이미 존재하는 회원입니다."),
  MEMBER_EMAIL_DUPLICATE(HttpStatus.CONFLICT, "MEMBER_409_002", "이미 사용 중인 이메일입니다."),
  SELLER_ALREADY_REGISTERED(HttpStatus.CONFLICT, "MEMBER_409_003", "이미 판매자 신청을 하셨습니다."),
  SELLER_ALREADY_REQUESTED(HttpStatus.CONFLICT, "MEMBER_409_004", "현재 판매자 승인 심사 대기 중입니다."),
  SOCIAL_ACCOUNT_ALREADY_LINKED(HttpStatus.CONFLICT, "MEMBER_409_005", "이미 연동된 소셜 계정입니다."),
  SOCIAL_ACCOUNT_ALREADY_IN_USE(HttpStatus.CONFLICT, "MEMBER_409_006", "다른 회원이 사용 중인 소셜 계정입니다."),
  MEMBER_PROFILE_ALREADY_EXISTS(HttpStatus.CONFLICT, "MEMBER_409_007", "이미 프로필이 존재합니다."),
  ADDRESS_ALREADY_DEFAULT(HttpStatus.CONFLICT, "MEMBER_409_008", "이미 기본 배송지로 설정되어 있습니다."),
  MEMBER_ALREADY_HAS_DEFAULT_ADDRESS(HttpStatus.CONFLICT, "MEMBER_409_009", "이미 기본 배송지가 존재합니다."),
  MEMBER_ALREADY_ACTIVE(HttpStatus.CONFLICT, "MEMBER_409_010", "이미 이 회원은 ACTIVE 상태입니다."),

  // Order
  ORDER_STOCK_NOT_ENOUGH(HttpStatus.BAD_REQUEST, "ORDER_400_001", "상품의 재고가 부족합니다."),
  ORDER_CANNOT_CANCEL(HttpStatus.BAD_REQUEST, "ORDER_400_002", "현재 주문 상태에서는 주문을 취소할 수 없습니다."),
  ORDER_CART_EMPTY(HttpStatus.BAD_REQUEST, "ORDER_400_003", "장바구니가 비어 있습니다."),
  ORDER_CANNOT_REFUND(HttpStatus.BAD_REQUEST, "ORDER_400_004", "환불 가능한 상태나 기간이 아닙니다."),
  PRODUCT_NOT_ON_SALE(HttpStatus.BAD_REQUEST, "ORDER_400_005", "판매중인 상품이 아닙니다."),

  ORDER_PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER_404_001", "없는 상품입니다."),
  ORDER_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER_404_002", "없는 회원입니다."),
  ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER_404_003", "없는 주문입니다."),
  ORDER_CARTITEM_EMPTY(HttpStatus.NOT_FOUND, "ORDER_404_004", "없는 장바구니 상품입니다."),
  ORDER_ACCESS_DENIED(HttpStatus.FORBIDDEN, "ORDER_403_001", "해당 주문에 대한 접근 권한이 없습니다."),

  // Product 400
  INVALID_PRODUCT_STATUS(HttpStatus.BAD_REQUEST, "PRODUCT_400_001", "잘못된 상품 등록 상태입니다."),
  INVALID_PRODUCT_UPDATE_FIELD(HttpStatus.BAD_REQUEST, "PRODUCT_400_002", "수정 불가한 필드입니다."),
  INVALID_PRODUCT_MEMBER(HttpStatus.BAD_REQUEST, "PRODUCT_400_003", "판매자 정보가 다릅니다."),
  PRODUCT_FIELD_REQUIRED(HttpStatus.BAD_REQUEST, "PRODUCT_400_004", "필수값을 입력해주세요."),
  PRODUCT_SELLER_INCORRECT(HttpStatus.BAD_REQUEST, "PRODUCT_400_005", "해당 판매자의 상품이 아닙니다."),
  // PRODUCT 404
  PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT_404_001", "상품이 존재하지 않습니다."),
  PRODUCT_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT_404_002", "존재하지 않는 회원입니다."),
  PRODUCT_SELLER_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT_404_003", "존재하지 않는 판매자입니다."),

  // Payment
  PAYMENT_MEMBER_DUPLICATE(HttpStatus.BAD_REQUEST, "PAYMENT_400_001", "이미 등록된 결제 회원 정보가 존재합니다."),
  PAYMENT_ACCOUNT_DUPLICATE(HttpStatus.BAD_REQUEST, "PAYMENT_400_002", "이미 등록된 결제 계좌 정보가 존재합니다."),
  PAYMENT_ACCOUNT_INVALID(HttpStatus.BAD_REQUEST, "PAYMENT_400_003", "잘못된 요청입니다."),
  PAYMENT_INSUFFICIENT_BALANCE(
      HttpStatus.BAD_REQUEST, "PAYMENT_400_004", "결제 계좌 잔액이 부족하여 결제를 진행할 수 없습니다."),
  PAYMENT_DUPLICATE(HttpStatus.BAD_REQUEST, "PAYMENT_400_005", "이미 결제된 주문입니다."),
  PAYMENT_MEMBER_IN_ACTIVE(HttpStatus.BAD_REQUEST, "PAYMENT_400_006", "결제 회원이 활성 상태가 아닙니다."),
  PAYMENT_INVALID(HttpStatus.BAD_REQUEST, "PAYMENT_400_007", "결제 요청이 올바르지 않습니다."),
  PAYMENT_INVALID_REQUEST_TOSS_API(
      HttpStatus.BAD_REQUEST, "PAYMENT_400_008", "토스 페이먼츠 API 응답값이 올바르지 않습니다."),
  PAYMENT_REJECT_TOSS_PAYMENT(HttpStatus.BAD_REQUEST, "PAYMENT_400_009", "결제 승인 요청이 거절되었습니다."),
  PAYMENT_INVALID_DATE_REQUEST(HttpStatus.BAD_REQUEST, "PAYMENT_400_010", "유효하지 않은 날짜 요청입니다."),

  PAYMENT_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "PAYMENT_404_001", "결제 회원 정보를 찾을 수 없습니다."),
  PAYMENT_ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "PAYMENT_404_002", "결제 계좌 정보를 찾을 수 없습니다."),
  PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "PAYMENT_404_003", "결제 정보를 찾을 수 없습니다."),

  PAYMENT_FAILED_LOCK_ACQUIRE(
      HttpStatus.INTERNAL_SERVER_ERROR, "PAYMENT_500_001", "결제 처리 중 락을 획득하지 못했습니다."),

  PAYMENT_LOCK_TIMEOUT(
      HttpStatus.INTERNAL_SERVER_ERROR, "PAYMENT_500_002", "결제 처리 중 락 획득이 시간 초과되었습니다."),
  PAYMENT_SYSTEM_ERROR(
      HttpStatus.INTERNAL_SERVER_ERROR, "PAYMENT_500_003", "결제 처리 중 시스템 오류가 발생했습니다."),

  // Content 400
  CONTENT_TAG_REQUIRED(HttpStatus.BAD_REQUEST, "CONTENT_400_001", "TAG는 NULL일 수 없습니다."),
  CONTENT_TAG_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "CONTENT_400_002", "TAG는 최대 5개입니다."),
  CONTENT_TAG_LENGTH_EXCEEDED(HttpStatus.BAD_REQUEST, "CONTENT_400_003", "TAG는 최대 10자입니다."),
  CONTENT_IMAGE_REQUIRED(HttpStatus.BAD_REQUEST, "CONTENT_400_004", "IMAGE는 NULL일 수 없습니다."),
  CONTENT_IMAGE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "CONTENT_400_005", "IMAGE는 최대 5개입니다."),
  CONTENT_TEXT_LENGTH_EXCEEDED(HttpStatus.BAD_REQUEST, "CONTENT_400_006", "TEXT는 최대 500자입니다."),
  CONTENT_TEXT_REQUIRED(HttpStatus.BAD_REQUEST, "CONTENT_400_007", "TEXT는 NULL일 수 없습니다."),
  CONTENT_COMMENT_LENGTH_EXCEEDED(
      HttpStatus.BAD_REQUEST, "CONTENT_400_008", "COMMENT는 100자를 초과할 수 없습니다."),

  // CONTENT 403
  CONTENT_FORBIDDEN(HttpStatus.FORBIDDEN, "CONTENT_403_001", "콘텐츠 내에서 금지된 요청입니다."),

  // CONTENT 404
  CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "CONTENT_404_001", "CONTENT 정보를 찾을 수 없습니다."),
  CONTENT_ALREADY_DELETE(HttpStatus.NOT_FOUND, "CONTENT_404_002", "이 CONTENT는 이미 삭제되었습니다."),
  CONTENT_COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "CONTENT_404_003", "COMMENT를 찾을 수 없습니다."),
  CONTENT_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "CONTENT_404_004", "CONTENT MEMBER를 찾을 수 없습니다."),

  // Settlement 400
  SETTLEMENT_REQUIRED(HttpStatus.BAD_REQUEST, "PRODUCT_400_001", "구매 확정 일자는 필수입니다."),

  // Settlement 404
  SETTLEMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "SETTLEMENT_404_001", "정산서가 존재하지 않습니다."),
  SETTLEMENT_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "SETTLEMENT_404_002", "회원을 찾을 수 없습니다."),

  // S3 Image 400
  IMAGE_FILE_REQUIRED(HttpStatus.BAD_REQUEST, "IMAGE_400_001", "이미지 파일은 필수입니다."),
  IMAGE_FILE_EMPTY(HttpStatus.BAD_REQUEST, "IMAGE_400_002", "이미지 파일이 비어있습니다."),
  IMAGE_FILE_EXTENSION_NOT_SUPPORTED(HttpStatus.BAD_REQUEST, "IMAGE_400_003", "지원하지 않는 파일 형식입니다."),
  S3_FILE_NOT_FOUND(HttpStatus.BAD_REQUEST, "IMAGE_400_004", "S3 파일을 찾을 수 없습니다."),
  S3_OPERATION_FAILED(HttpStatus.BAD_REQUEST, "IMAGE_400_005", "S3 실행에 실패했습니다."),
  IMAGE_RAW_KEY_INVALID(HttpStatus.BAD_REQUEST, "IMAGE_400_006", "key 형식이 잘못되었습니다"),
  // S3 Image 500
  IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "IMAGE_500_001", "이미지 업로드에 실패했습니다."),

  // ElasticSearch 400
  ELASTICSEARCH_SEARCH_FAILED(HttpStatus.NOT_FOUND, "ELASTICSEARCH_400_001", "검색에 실패하였습니다."),
  ELASTICSEARCH_INDEX_FAILED(HttpStatus.NOT_FOUND, "ELASTICSEARCH_400_002", "인덱싱에 실패하였습니다."),
  ELASTICSEARCH_DELETE_FAILED(HttpStatus.NOT_FOUND, "ELASTICSEARCH_400_003", "삭제에 실패하였습니다."),
  ELASTICSEARCH_BULKINDEX_FAILED(HttpStatus.NOT_FOUND, "ELASTICSEARCH_400_004", "대량 인덱싱에 실패하였습니다."),

  // Inventory
  INVENTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "INVENTORY_404_001", "상품 재고가 등록되지 않았습니다."),
  INVENTORY_ACCESS_DENIED(HttpStatus.FORBIDDEN, "INVENTORY_403_001", "해당 재고에 대한 접근 권한이 없습니다."),
  INVALID_STOCK_QUANTITY(HttpStatus.BAD_REQUEST, "INVENTORY_400_001", "현재 예약된 재고 수량보다 적을 수 없습니다."),
  INSUFFICIENT_STOCK(HttpStatus.BAD_REQUEST, "INVENTORY_400_002", "잔여 재고가 부족하여 주문할 수 없습니다.");

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;

  public String getMessage(String message) {
    return Optional.ofNullable(message)
        .filter(Predicate.not(String::isBlank))
        .orElse(this.getMessage());
  }
}
