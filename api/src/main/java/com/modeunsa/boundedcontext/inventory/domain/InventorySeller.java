package com.modeunsa.boundedcontext.inventory.domain;

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
@Getter
@SuperBuilder
@Table(name = "inventory_seller")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InventorySeller extends ManualIdAndAuditedEntity {

  @Column(name = "business_name", nullable = false, length = 500)
  private String businessName;

  @Column(name = "representative_name", nullable = false, length = 500)
  private String representativeName;
}
