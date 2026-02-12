package com.modeunsa.boundedcontext.payment.domain.entity;

import com.modeunsa.boundedcontext.payment.domain.exception.PaymentDomainException;
import com.modeunsa.boundedcontext.payment.domain.exception.PaymentErrorCode;
import com.modeunsa.boundedcontext.payment.domain.types.MemberStatus;
import com.modeunsa.global.jpa.converter.EncryptedStringConverter;
import com.modeunsa.global.jpa.entity.ManualIdAndAuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentMember extends ManualIdAndAuditedEntity {

  private static final String customerKeyPrefix = "CUSTOMER";

  @Convert(converter = EncryptedStringConverter.class)
  private String email;

  @Convert(converter = EncryptedStringConverter.class)
  @Column(nullable = false)
  private String name;

  @Column(nullable = false, unique = true)
  private String customerKey;

  @Builder.Default
  @Column(nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  private MemberStatus status = MemberStatus.ACTIVE;

  public static PaymentMember create(Long id, String email, String name, MemberStatus status) {
    return PaymentMember.builder()
        .id(id)
        .email(email)
        .name(name)
        .customerKey(generateCustomerKey(id))
        .status(status)
        .build();
  }

  public void validateCanOrder() {
    if (!canOrder()) {
      throw new PaymentDomainException(
          PaymentErrorCode.MEMBER_IN_ACTIVE,
          String.format("유효한 회원 상태가 아닙니다. 회원 ID: %d, 회원 상태 : %s", this.getId(), this.status));
    }
  }

  private static String generateCustomerKey(Long id) {
    return String.format("%s_%08d", customerKeyPrefix, id);
  }

  private boolean canOrder() {
    return this.status == MemberStatus.ACTIVE;
  }
}
