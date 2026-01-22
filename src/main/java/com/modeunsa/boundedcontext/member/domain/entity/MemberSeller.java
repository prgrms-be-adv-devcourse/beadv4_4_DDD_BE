package com.modeunsa.boundedcontext.member.domain.entity;

import com.modeunsa.boundedcontext.member.domain.types.MemberRole;
import com.modeunsa.boundedcontext.member.domain.types.SellerStatus;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.jpa.converter.EncryptedStringConverter;
import com.modeunsa.global.jpa.entity.GeneratedIdAndAuditedEntity;
import com.modeunsa.global.status.ErrorStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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

  @Convert(converter = EncryptedStringConverter.class)
  @Column(name = "business_name", nullable = false, length = 500)
  private String businessName;

  @Convert(converter = EncryptedStringConverter.class)
  @Column(name = "representative_name", nullable = false, length = 500)
  private String representativeName;

  @Convert(converter = EncryptedStringConverter.class)
  @Column(name = "settlement_bank_name", nullable = false, length = 500)
  private String settlementBankName;

  @Convert(converter = EncryptedStringConverter.class)
  @Column(name = "settlement_bank_account", nullable = false, length = 500)
  private String settlementBankAccount;

  @Convert(converter = EncryptedStringConverter.class)
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

  public void approve() {
    if (this.status != SellerStatus.PENDING) {
      throw new GeneralException(ErrorStatus.SELLER_CANNOT_APPROVE);
    }
    this.status = SellerStatus.ACTIVE;
    this.activatedAt = LocalDateTime.now();
    this.member.changeRole(MemberRole.SELLER);
  }

  public static void validateBankAccount(String account) {
    if (!account.matches(BANK_ACCOUNT_PATTERN)) {
      throw new GeneralException(ErrorStatus.SELLER_INVALID_BANK_ACCOUNT);
    }
  }

  public void reapply(
      String businessName,
      String representativeName,
      String settlementBankName,
      String settlementBankAccount,
      String businessLicenseUrl) {

    // 거절된 상태가 아니면 재신청 불가
    // TODO: 심사 로직 구현 후 원복 예정
    //    if (this.status != SellerStatus.REJECTED) {
    //      throw new GeneralException(ErrorStatus.SELLER_ALREADY_REQUESTED);
    //    }

    this.businessName = businessName;
    this.representativeName = representativeName;
    this.settlementBankName = settlementBankName;
    // 계좌번호 검증 로직 재사용
    validateBankAccount(settlementBankAccount);
    this.settlementBankAccount = settlementBankAccount;

    this.businessLicenseUrl = businessLicenseUrl;

    // 상태 초기화
    // TODO: 기본 상태를 PENDING으로 변경 예정
    this.status = SellerStatus.ACTIVE;
    this.requestedAt = LocalDateTime.now();
  }
}
