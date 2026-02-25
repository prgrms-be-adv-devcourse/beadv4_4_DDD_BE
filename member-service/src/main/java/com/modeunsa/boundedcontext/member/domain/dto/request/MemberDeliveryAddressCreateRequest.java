package com.modeunsa.boundedcontext.member.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record MemberDeliveryAddressCreateRequest(
    @NotBlank(message = "수령인 이름은 필수입니다.") String recipientName,
    @NotBlank(message = "연락처는 필수입니다.")
        @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "올바른 전화번호 형식이 아닙니다.")
        String recipientPhone,
    @NotBlank(message = "우편번호는 필수입니다.") String zipCode,
    @NotBlank(message = "주소는 필수입니다.") String address,
    @NotBlank(message = "상세 주소는 필수입니다.") String addressDetail,
    String addressName,
    Boolean isDefault) {}
