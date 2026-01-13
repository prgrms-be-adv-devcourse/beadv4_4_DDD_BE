package com.modeunsa.boundedcontext.payment.domain;

import com.modeunsa.boundedcontext.payment.domain.types.MemberStatus;
import com.modeunsa.global.jpa.entity.ManualIdAndAuditedEntity;
import jakarta.persistence.Column;
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

/**
 * @author : JAKE
 * @date : 26. 1. 12.
 */
@Entity
@Table
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentMember extends ManualIdAndAuditedEntity {

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false, unique = true)
  private String customerKey;

  @Builder.Default
  @Column(nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  private MemberStatus status = MemberStatus.ACTIVE;

  public static PaymentMember register(
      Long id, String email, String name, String customerKey, MemberStatus status) {
    return PaymentMember.builder()
        .id(id)
        .email(email)
        .name(name)
        .customerKey(customerKey)
        .status(status)
        .build();
  }
}
