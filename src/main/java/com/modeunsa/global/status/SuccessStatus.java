package com.modeunsa.global.status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessStatus {
  OK(HttpStatus.OK, "COMMON_200", "성공입니다."),
  CREATED(HttpStatus.CREATED, "COMMON_201", "리소스가 성공적으로 생성되었습니다."),

  // Auth 200
  AUTH_LOGIN_SUCCESS(HttpStatus.OK, "AUTH_200_001", "로그인에 성공했습니다."),
  AUTH_LOGOUT_SUCCESS(HttpStatus.OK, "AUTH_200_002", "로그아웃에 성공했습니다."),
  AUTH_TOKEN_REFRESH_SUCCESS(HttpStatus.OK, "AUTH_200_003", "토큰 갱신에 성공했습니다."),
  // Auth 201
  AUTH_SIGNUP_SUCCESS(HttpStatus.CREATED, "AUTH_201_001", "회원가입에 성공했습니다."),

  // Member 200
  MEMBER_BASIC_INFO_GET_SUCCESS(HttpStatus.OK, "MEMBER_200_001", "기본 정보 조회에 성공했습니다."),
  MEMBER_PROFILE_GET_SUCCESS(HttpStatus.OK, "MEMBER_200_002", "프로필 조회에 성공했습니다."),
  MEMBER_ADDRESS_LIST_GET_SUCCESS(HttpStatus.OK, "MEMBER_200_003", "배송지 목록 조회에 성공했습니다."),
  MEMBER_BASIC_INFO_UPDATE_SUCCESS(HttpStatus.OK, "MEMBER_200_004", "기본 정보 수정에 성공했습니다."),
  MEMBER_PROFILE_UPDATE_SUCCESS(HttpStatus.OK, "MEMBER_200_005", "프로필 수정에 성공했습니다."),
  MEMBER_DEFAULT_ADDRESS_UPDATE_SUCCESS(HttpStatus.OK, "MEMBER_200_006", "기본 배송지 변경에 성공했습니다."),
  MEMBER_ADDRESS_DELETE_SUCCESS(HttpStatus.OK, "MEMBER_200_007", "배송지 삭제에 성공했습니다."),
  MEMBER_ADDRESS_UPDATE_SUCCESS(HttpStatus.OK, "MEMBER_200_008", "배송지 수정에 성공했습니다."),
  SELLER_REGISTER_SUCCESS(HttpStatus.OK, "MEMBER_200_009", "판매자 등록 요청이 접수되었습니다."),
  FILE_UPLOAD_URL_GET_SUCCESS(HttpStatus.OK, "FILE_200_001", "파일 업로드 URL 생성에 성공했습니다."),

  // Member 201
  MEMBER_ADDRESS_CREATE_SUCCESS(HttpStatus.CREATED, "MEMBER_201_001", "배송지 등록에 성공했습니다."),
  SOCIAL_ACCOUNT_LINK_SUCCESS(HttpStatus.CREATED, "MEMBER_201_002", "소셜 계정 연동에 성공했습니다."),
  MEMBER_PROFILE_CREATE_SUCCESS(HttpStatus.CREATED, "MEMBER_201_003", "프로필 생성에 성공했습니다."),

  // Content 200
  CONTENT_LIST_GET_SUCCESS(HttpStatus.OK, "CONTENT_200_001", "콘텐츠 전체 조회에 성공했습니다.");

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;
}
