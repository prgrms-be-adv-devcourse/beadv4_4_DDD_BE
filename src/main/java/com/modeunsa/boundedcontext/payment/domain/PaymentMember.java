package com.modeunsa.boundedcontext.payment.domain;

import com.modeunsa.boundedcontext.payment.domain.type.MemberStatus;
import com.modeunsa.global.jpa.entity.ManualIdAndAuditedEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * @author : JAKE
 * @date : 26. 1. 12.
 */
@Entity
@Table(name = "payment_member")
@Getter
@Builder
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
}
