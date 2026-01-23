package com.modeunsa.boundedcontext.settlement.domain.entity;

import com.modeunsa.global.jpa.entity.ManualIdAndAuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "settlement_member")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class SettlementMember extends ManualIdAndAuditedEntity {
  @Column(nullable = false, length = 10)
  private String role;

  public static SettlementMember create(Long memberId, String role) {
    return SettlementMember.builder().id(memberId).role(role).build();
  }
}
