package com.modeunsa.boundedcontext.settlement.domain;

import static jakarta.persistence.FetchType.LAZY;

import com.modeunsa.global.jpa.entity.GeneratedIdAndAuditedEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "settlement_settlement")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class Settlement extends GeneratedIdAndAuditedEntity {
  private long amount;

  @Column(nullable = false)
  private Long sellerUserId;

  @OneToMany(mappedBy = "settlement", cascade = CascadeType.PERSIST, fetch = LAZY)
  @Builder.Default
  private List<SettlementItem> items = new ArrayList<>();

  private LocalDateTime payoutAt;
}
