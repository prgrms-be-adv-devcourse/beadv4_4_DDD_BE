package com.modeunsa.shared.member.dto.response;

import com.modeunsa.boundedcontext.member.domain.entity.MemberSeller;
import com.modeunsa.boundedcontext.member.domain.types.SellerStatus;

public record SellerInfoResponse(
    Long sellerId,
    String businessName,
    String representativeName,
    String settlementBankName,
    String settlementBankAccount,
    String businessLicenseUrl,
    SellerStatus status) {
  public static SellerInfoResponse from(MemberSeller seller) {
    return new SellerInfoResponse(
        seller.getId(),
        seller.getBusinessName(),
        seller.getRepresentativeName(),
        seller.getSettlementBankName(),
        seller.getSettlementBankAccount(),
        seller.getBusinessLicenseUrl(),
        seller.getStatus());
  }
}
