package com.modeunsa.boundedcontext.order.domain;

import com.modeunsa.global.jpa.converter.EncryptedStringConverter;
import com.modeunsa.global.jpa.entity.ManualIdAndAuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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

  @Convert(converter = EncryptedStringConverter.class)
  @Column(name = "real_name", length = 500)
  String realName;

  @Convert(converter = EncryptedStringConverter.class)
  @Column(name = "phone_number", length = 500)
  String phoneNumber;

  @Convert(converter = EncryptedStringConverter.class)
  @Column(name = "recipient_name", length = 500)
  String recipientName;

  @Convert(converter = EncryptedStringConverter.class)
  @Column(name = "recipient_phone", length = 500)
  String recipientPhone;

  @Convert(converter = EncryptedStringConverter.class)
  @Column(name = "zipcode", length = 500)
  private String zipCode;

  @Convert(converter = EncryptedStringConverter.class)
  @Column(name = "address", length = 500)
  private String address;

  @Convert(converter = EncryptedStringConverter.class)
  @Column(name = "address_detail", length = 500)
  private String addressDetail;

  @Convert(converter = EncryptedStringConverter.class)
  @Column(name = "address_name", length = 500)
  String addressName;

  public void updateInfo(String realName, String phoneNumber) {
    this.realName = realName;
    this.phoneNumber = phoneNumber;
  }

  public void createDeliveryAddress(
      String recipientName,
      String recipientPhone,
      String zipCode,
      String address,
      String addressDetail,
      String addressName) {
    this.recipientName = recipientName;
    this.recipientPhone = recipientPhone;
    this.zipCode = zipCode;
    this.address = address;
    this.addressDetail = addressDetail;
    this.addressName = addressName;
  }
}
