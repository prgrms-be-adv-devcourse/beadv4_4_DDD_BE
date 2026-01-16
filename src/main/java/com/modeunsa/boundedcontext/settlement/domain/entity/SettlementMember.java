package com.modeunsa.boundedcontext.settlement.domain.entity;

import com.modeunsa.boundedcontext.settlement.app.dto.SettlementMemberDto;
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
  @Column(nullable = false, length = 50)
  private String name;

  public static SettlementMember create(Long memberId, String name) {
    return SettlementMember.builder().id(memberId).name(name).build();
  }

  public SettlementMemberDto toDto() {
    return SettlementMemberDto.builder().memberId(getId()).name(getName()).build();
  }
}
