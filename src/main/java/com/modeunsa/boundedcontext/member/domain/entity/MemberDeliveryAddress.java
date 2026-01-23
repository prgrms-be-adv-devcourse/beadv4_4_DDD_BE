package com.modeunsa.boundedcontext.member.domain.entity;

import com.modeunsa.global.jpa.converter.EncryptedStringConverter;
import com.modeunsa.global.jpa.entity.GeneratedIdAndAuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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

  @Convert(converter = EncryptedStringConverter.class)
  @Column(nullable = false, length = 500)
  private String recipientName;

  @Convert(converter = EncryptedStringConverter.class)
  @Column(nullable = false, length = 500)
  private String recipientPhone;

  @Convert(converter = EncryptedStringConverter.class)
  @Column(nullable = false, length = 500)
  private String zipCode;

  @Convert(converter = EncryptedStringConverter.class)
  @Column(nullable = false, length = 500)
  private String address; // 기본 주소

  @Convert(converter = EncryptedStringConverter.class)
  @Column(nullable = false, length = 500)
  private String addressDetail; // 상세 주소

  @Column(nullable = false)
  @Builder.Default
  private Boolean isDefault = false;

  @Convert(converter = EncryptedStringConverter.class)
  @Column(length = 500)
  private String addressName; // 주소 별칭

  void setMember(Member member) {
    this.member = member;
  }

  public MemberDeliveryAddress updateRecipientName(String recipientName) {
    if (recipientName != null) {
      this.recipientName = recipientName;
    }
    return this;
  }

  public MemberDeliveryAddress updateRecipientPhone(String recipientPhone) {
    if (recipientPhone != null) {
      this.recipientPhone = recipientPhone;
    }
    return this;
  }

  public MemberDeliveryAddress updateZipCode(String zipCode) {
    if (zipCode != null) {
      this.zipCode = zipCode;
    }
    return this;
  }

  public MemberDeliveryAddress updateAddress(String address) {
    if (address != null) {
      this.address = address;
    }
    return this;
  }

  public MemberDeliveryAddress updateAddressDetail(String addressDetail) {
    if (addressDetail != null) {
      this.addressDetail = addressDetail;
    }
    return this;
  }

  public MemberDeliveryAddress updateAddressName(String addressName) {
    if (addressName != null) {
      this.addressName = addressName;
    }
    return this;
  }

  public MemberDeliveryAddress setAsDefault() {
    this.isDefault = true;
    return this;
  }

  public MemberDeliveryAddress unsetAsDefault() {
    this.isDefault = false;
    return this;
  }
}
