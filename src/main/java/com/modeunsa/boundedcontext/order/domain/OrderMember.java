package com.modeunsa.boundedcontext.order.domain;

import com.modeunsa.global.jpa.entity.ManualIdAndAuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "order_member")
public class OrderMember extends ManualIdAndAuditedEntity {

  @Column(name = "member_name", nullable = false, length = 20)
  private String memberName;

  @Column(name = "member_phone", nullable = false, length = 20)
  private String memberPhone;

  @Column(name = "zipcode", length = 10)
  private String zipcode;

  @Column(name = "address_detail", length = 200)
  private String addressDetail;
}
