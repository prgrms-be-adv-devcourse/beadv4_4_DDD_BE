package com.modeunsa.shared.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SellerRegisterRequest(
    @NotBlank(message = "상호명은 필수입니다.") String businessName,
    @NotBlank(message = "대표자명은 필수입니다.") String representativeName,
    @NotBlank(message = "은행명은 필수입니다.") String settlementBankName,
    @NotBlank(message = "계좌번호는 필수입니다.")
        @Pattern(regexp = BANK_ACCOUNT_REGEX, message = "계좌번호 형식이 올바르지 않습니다")
        String settlementBankAccount,
    @NotBlank(message = "사업자등록증 이미지 키는 필수입니다.") String licenseImageRawKey,
    @NotBlank(message = "콘텐츠 타입은 필수입니다.") String licenseContentType) {
  public static final String BANK_ACCOUNT_REGEX = "^[0-9-]{10,20}$";
}
