package com.modeunsa.boundedcontext.product.domain;

import com.modeunsa.global.jpa.entity.ManualIdAndAuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_member")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductMember extends ManualIdAndAuditedEntity {
  @Column(unique = true, length = 255)
  private String email;

  @Column(length = 30)
  private String realName;

  @Column(length = 20)
  private String phoneNumber;

  public static ProductMember create(
      Long memberId, String email, String realName, String phoneNumber) {
    ProductMember member =
        ProductMember.builder().email(email).realName(realName).phoneNumber(phoneNumber).build();
    member.assignId(memberId);
    return member;
  }
}
