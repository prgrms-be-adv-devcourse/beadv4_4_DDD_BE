package com.modeunsa.boundedcontext.order.domain;

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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "order_member")
public class OrderMember extends ManualIdAndAuditedEntity {

  @Column(name = "member_name", nullable = false, length = 20)
  private String memberName;

  @Column(name = "member_phone", nullable = false, length = 20)
  private String memberPhone;

  @Column(name = "zipcode", length = 10)
  private String zipCode;

  @Column(name = "address", length = 200)
  private String address;

  @Column(name = "address_detail", length = 200)
  private String addressDetail;

  public void updateInfo(String memberName, String memberPhone) {
    this.memberName = memberName;
    this.memberPhone = memberPhone;
  }

  public void createDeliveryAddress(String zipCode, String address, String addressDetail) {
    this.zipCode = zipCode;
    this.address = address;
    this.addressDetail = addressDetail;
  }
}
