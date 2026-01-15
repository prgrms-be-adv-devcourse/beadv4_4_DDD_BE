package com.modeunsa.boundedcontext.settlement.domain.entity;

import com.modeunsa.global.jpa.entity.GeneratedIdAndAuditedEntity;
import com.modeunsa.global.jpa.entity.ManualIdAndAuditedEntity;
import com.modeunsa.shared.settlement.dto.SettlementMemberDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
  @Column(nullable = false, length = 20)
  private String memberRole;

  public static SettlementMember create(Long memberId, String memberRole) {
    return SettlementMember.builder()
        .id(memberId)
        .memberRole(memberRole)
        .build();
  }

  public SettlementMemberDto toDto() {
    return SettlementMemberDto.builder()
        .memberId(getId())
        .build();
  }
}
