package com.modeunsa.boundedcontext.member.domain.entity;

import com.modeunsa.global.jpa.entity.GeneratedIdAndAuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MemberDeliveryAddress extends GeneratedIdAndAuditedEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @Column(nullable = false, length = 30)
  private String recipientName;

  @Column(nullable = false, length = 20)
  private String recipientPhone;

  @Column(nullable = false, length = 10)
  private String zipCode;

  @Column(nullable = false, length = 255)
  private String address;

  @Column(nullable = false, length = 255)
  private String addressDetail;

  @Column(nullable = false)
  @Builder.Default
  private Boolean isDefault = false;

  @Column(length = 30)
  private String addressName;

  // 연관관계 편의 메서드
  void setMember(Member member) {
    this.member = member;
  }

  public void updateAddress(
      String recipientName,
      String recipientPhone,
      String zipCode,
      String address,
      String addressDetail,
      String addressName) {
    this.recipientName = recipientName;
    this.recipientPhone = recipientPhone;
    this.zipCode = zipCode;
    this.address = address;
    this.addressDetail = addressDetail;
    this.addressName = addressName;
  }

  public void setAsDefault() {
    this.isDefault = true;
  }

  public void unsetDefault() {
    this.isDefault = false;
  }
}
