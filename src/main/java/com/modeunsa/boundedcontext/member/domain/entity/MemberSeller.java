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
import lombok.ToString;

/**
 * 판매자(MemberSeller) 정보를 나타내는 엔티티.
 *
 * <p>
 * 회원(Member) 중 판매자 전환을 신청한 사용자의 사업자 정보와
 * 판매자 상태를 관리한다.
 * </p>
 *
 * <h3>판매자 상태 전이 규칙</h3>
 * <ul>
 *   <li>PENDING → ACTIVE : 승인</li>
 *   <li>PENDING → REJECTED : 거절</li>
 *   <li>ACTIVE → SUSPENDED : 판매 정지</li>
 * </ul>
 *
 * <p>
 * 상태 변경은 비즈니스 규칙을 보장하기 위해
 * 엔티티 내부 메서드를 통해서만 수행된다.
 * </p>
 */
@Entity
@Getter
@Builder
@ToString(exclude = "member")
@Table(name = "member_seller")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberSeller extends GeneratedIdAndAuditedEntity {

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false, unique = true)
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
  @Builder.Default
  private SellerStatus status = SellerStatus.PENDING;

  @Column(name = "requested_at", nullable = false)
  private LocalDateTime requestedAt;

  @Column(name = "activated_at")
  private LocalDateTime activatedAt;

  /**
   * 판매자 프로필 정보를 수정한다.
   *
   * <p>
   * 전달된 파라미터 중 {@code null} 값은 무시되며,
   * {@code null} 이 아닌 값만 기존 값에 반영된다.
   * </p>
   *
   * <p>
   * 이 메서드는 부분 수정(Partial Update)을 지원한다.
   * </p>
   */
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

  /**
   * 판매자 신청을 승인한다.
   *
   * <p>
   * 현재 상태가 PENDING인 경우에만 승인할 수 있으며,
   * 승인 시 상태는 ACTIVE로 변경되고 활성화 시간이 기록된다.
   * </p>
   *
   * @throws IllegalStateException 상태가 PENDING이 아닌 경우
   */
  public void approve() {
    if (this.status != SellerStatus.PENDING) {
      throw new IllegalStateException(
          "Cannot approve seller with status " + this.status + ". Only PENDING sellers can be approved.");
    }
    this.status = SellerStatus.ACTIVE;
    this.activatedAt = LocalDateTime.now();
  }

  /**
   * 판매자 신청을 거절한다.
   *
   * <p>
   * PENDING 상태의 판매자만 거절할 수 있다.
   * </p>
   */
  public void reject() {
    if (this.status != SellerStatus.PENDING) {
      throw new IllegalStateException(
          "Cannot approve seller with status " + this.status + ". Only PENDING sellers can be approved.");
    }
    this.status = SellerStatus.REJECTED;
  }

  /**
   * 활성화된 판매자를 판매 정지 상태로 변경한다.
   */
  public void suspend() {
    this.status = SellerStatus.SUSPENDED;
  }
}
