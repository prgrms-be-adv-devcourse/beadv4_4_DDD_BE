package com.modeunsa.boundedcontext.member.domain.entity;

import com.modeunsa.boundedcontext.member.domain.enums.SellerStatus;
import com.modeunsa.global.jpa.entity.GeneratedIdAndAuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@Table(name = "member_seller")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberSeller extends GeneratedIdAndAuditedEntity {

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @Column(name = "business_name", nullable = false, length = 200)
  private String businessName;

  @Column(name = "representative_name", nullable = false, length = 100)
  private String representativeName;

  @Column(name = "settlement_bank_name", nullable = false, length = 50)
  private String settlementBankName;

  @Column(name = "settlement_bank_account", nullable = false, length = 100)
  private String settlementBankAccount;

  @Column(name = "business_license_url", nullable = false, length = 1000)
  private String businessLicenseUrl;

  @Enumerated(EnumType.STRING)
  @Column(name = "seller_status", nullable = false)
  private SellerStatus status = SellerStatus.PENDING;

  @Column(name = "requested_at", nullable = false)
  private LocalDateTime requestedAt;

  @Column(name = "activated_at")
  private LocalDateTime activatedAt;

  public void updateProfile(
      String businessName,
      String representativeName,
      String settlementBankName,
      String settlementBankAccount,
      String businessLicenseUrl) {
    if (businessName != null) {
      this.businessName = businessName;
    }
    if (representativeName != null) {
      this.representativeName = representativeName;
    }
    if (settlementBankName != null) {
      this.settlementBankName = settlementBankName;
    }
    if (settlementBankAccount != null) {
      this.settlementBankAccount = settlementBankAccount;
    }
    if (businessLicenseUrl != null) {
      this.businessLicenseUrl = businessLicenseUrl;
    }
  }

  public void approve() {
    this.status = SellerStatus.ACTIVE;
    this.activatedAt = LocalDateTime.now();
  }

  public void reject() {
    this.status = SellerStatus.REJECTED;
  }
}
