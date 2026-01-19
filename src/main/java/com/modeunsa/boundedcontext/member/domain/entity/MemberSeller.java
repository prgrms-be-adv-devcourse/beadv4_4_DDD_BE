package com.modeunsa.boundedcontext.member.domain.entity;

import com.modeunsa.boundedcontext.member.domain.types.MemberRole;
import com.modeunsa.boundedcontext.member.domain.types.SellerStatus;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.jpa.entity.GeneratedIdAndAuditedEntity;
import com.modeunsa.global.status.ErrorStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Getter
@Builder
@ToString(exclude = "member")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberSeller extends GeneratedIdAndAuditedEntity {
  private static final String BANK_ACCOUNT_PATTERN = "^[0-9-]{10,20}$";

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false, unique = true)
  private Member member;

  @Column(name = "business_name", nullable = false, length = 200)
  private String businessName;

  @Column(name = "representative_name", nullable = false, length = 100)
  private String representativeName;

  @Column(name = "settlement_bank_name", nullable = false, length = 50)
  private String settlementBankName;

  @Column(name = "settlement_bank_account", nullable = false, length = 20)
  private String settlementBankAccount;

  @Column(name = "business_license_url", nullable = false, length = 1000)
  private String businessLicenseUrl;

  @Enumerated(EnumType.STRING)
  @Column(name = "seller_status", nullable = false)
  @Builder.Default
  private SellerStatus status = SellerStatus.PENDING;

  @Column(name = "requested_at", nullable = false)
  private LocalDateTime requestedAt;

  @Column(name = "activated_at")
  private LocalDateTime activatedAt;

  public MemberSeller updateBusinessName(String businessName) {
    if (businessName != null) {
      this.businessName = businessName;
    }
    return this;
  }

  public MemberSeller updateRepresentativeName(String representativeName) {
    if (representativeName != null) {
      this.representativeName = representativeName;
    }
    return this;
  }

  public MemberSeller updateSettlementBankName(String settlementBankName) {
    if (settlementBankName != null) {
      this.settlementBankName = settlementBankName;
    }
    return this;
  }

  public MemberSeller updateSettlementBankAccount(String settlementBankAccount) {
    if (settlementBankAccount != null) {
      validateBankAccount(settlementBankAccount);
      this.settlementBankAccount = settlementBankAccount;
    }
    return this;
  }

  public MemberSeller updateBusinessLicenseUrl(String businessLicenseUrl) {
    if (businessLicenseUrl != null) {
      this.businessLicenseUrl = businessLicenseUrl;
    }
    return this;
  }

  public void approve() {
    if (this.status != SellerStatus.PENDING) {
      throw new GeneralException(ErrorStatus.SELLER_CANNOT_APPROVE);
    }
    this.status = SellerStatus.ACTIVE;
    this.activatedAt = LocalDateTime.now();
    this.member.changeRole(MemberRole.SELLER);
  }

  public void reject() {
    if (this.status != SellerStatus.PENDING) {
      throw new GeneralException(ErrorStatus.SELLER_CANNOT_REJECT);
    }
    this.status = SellerStatus.REJECTED;
  }

  public void suspend() {
    if (this.status != SellerStatus.ACTIVE) {
      throw new GeneralException(ErrorStatus.SELLER_CANNOT_SUSPEND);
    }
    this.status = SellerStatus.SUSPENDED;
  }

  private void validateBankAccount(String account) {
    if (!account.matches(BANK_ACCOUNT_PATTERN)) {
      throw new GeneralException(ErrorStatus.SELLER_INVALID_BANK_ACCOUNT);
    }
  }
}
