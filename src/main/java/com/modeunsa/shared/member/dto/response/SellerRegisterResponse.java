package com.modeunsa.shared.member.dto.response;

public record SellerRegisterResponse(
    String accessToken,
    String refreshToken
) {}