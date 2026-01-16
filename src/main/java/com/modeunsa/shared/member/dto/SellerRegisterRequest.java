package com.modeunsa.shared.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SellerRegisterRequest(
    @NotBlank String businessName,
    @NotBlank String representativeName,
    @NotBlank String settlementBankName,
    @NotBlank @Pattern(regexp = BANK_ACCOUNT_REGEX, message = "계좌번호 형식이 올바르지 않습니다")
    String settlementBankAccount,
    @NotBlank String businessLicenseUrl) {
        public static final String BANK_ACCOUNT_REGEX = "^[0-9-]{10,20}$";
}