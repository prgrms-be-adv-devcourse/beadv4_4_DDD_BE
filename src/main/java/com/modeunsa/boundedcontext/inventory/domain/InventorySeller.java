package com.modeunsa.boundedcontext.inventory.domain;

import com.modeunsa.global.jpa.converter.EncryptedStringConverter;
import com.modeunsa.global.jpa.entity.ManualIdAndAuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@Table(name = "inventory_seller")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InventorySeller extends ManualIdAndAuditedEntity {
  @Column(name = "seller_id", nullable = false, unique = true)
  private Long sellerId;

  @Convert(converter = EncryptedStringConverter.class)
  @Column(name = "business_name", nullable = false, length = 500)
  private String businessName;

  @Convert(converter = EncryptedStringConverter.class)
  @Column(name = "representative_name", nullable = false, length = 500)
  private String representativeName;

  @Convert(converter = EncryptedStringConverter.class)
  @Column(name = "settlement_bank_name", nullable = false, length = 500)
  private String settlementBankName;

  @Convert(converter = EncryptedStringConverter.class)
  @Column(name = "settlement_bank_account", nullable = false, length = 500)
  private String settlementBankAccount;
}
