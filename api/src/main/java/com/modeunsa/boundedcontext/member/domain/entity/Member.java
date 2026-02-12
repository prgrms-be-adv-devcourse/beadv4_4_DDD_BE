package com.modeunsa.boundedcontext.member.domain.entity;

import static com.modeunsa.global.status.ErrorStatus.MEMBER_ADDRESS_LIMIT_EXCEEDED;
import static com.modeunsa.global.status.ErrorStatus.MEMBER_DEFAULT_ADDRESS_REQUIRED;

import com.modeunsa.boundedcontext.auth.domain.entity.OAuthAccount;
import com.modeunsa.boundedcontext.member.domain.types.MemberRole;
import com.modeunsa.boundedcontext.member.domain.types.MemberStatus;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.jpa.converter.EncryptedStringConverter;
import com.modeunsa.global.jpa.entity.GeneratedIdAndAuditedEntity;
import com.modeunsa.global.status.ErrorStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "member_member")
public class Member extends GeneratedIdAndAuditedEntity {

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private MemberRole role;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private MemberStatus status;

  @Convert(converter = EncryptedStringConverter.class)
  private String email;

  @Convert(converter = EncryptedStringConverter.class)
  @Column(length = 500)
  private String realName;

  @Convert(converter = EncryptedStringConverter.class)
  @Column(length = 500)
  private String phoneNumber;

  @Getter(AccessLevel.NONE)
  @Builder.Default
  @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<OAuthAccount> oauthAccount = new ArrayList<>();

  @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private MemberProfile profile;

  @Getter(AccessLevel.NONE)
  @Builder.Default
  @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<MemberDeliveryAddress> addresses = new ArrayList<>();

  @Column private LocalDateTime withdrawalRequestedAt;

  public List<OAuthAccount> getOauthAccount() {
    return Collections.unmodifiableList(oauthAccount);
  }

  public List<MemberDeliveryAddress> getAddresses() {
    return Collections.unmodifiableList(addresses);
  }

  public void setProfile(MemberProfile profile) {
    this.profile = profile;
    profile.setMember(this);
  }

  public void addAddress(MemberDeliveryAddress address) {
    if (addresses.size() >= 10) {
      throw new GeneralException(MEMBER_ADDRESS_LIMIT_EXCEEDED);
    }
    addresses.add(address);
    address.setMember(this);
  }

  // 기존 기본 배송지 해제 후 새 기본 배송지 설정
  public void setNewDefaultAddress(MemberDeliveryAddress newDefault) {
    if (newDefault == null) {
      throw new GeneralException(MEMBER_DEFAULT_ADDRESS_REQUIRED);
    }

    if (!addresses.contains(newDefault)) {
      addAddress(newDefault);
    }

    if (newDefault.getIsDefault()) {
      return;
    }

    for (MemberDeliveryAddress address : addresses) {
      if (address.getIsDefault()) {
        address.unsetAsDefault();
      }
    }

    newDefault.setAsDefault();
  }

  public void updateBasicInfo(String realName, String email, String phoneNumber) {
    this.realName = realName;
    this.email = email;
    this.phoneNumber = phoneNumber;
  }

  public Member updateRealName(String realName) {
    if (realName != null) {
      this.realName = realName;
    }
    return this;
  }

  public Member updatePhoneNumber(String phoneNumber) {
    if (phoneNumber != null) {
      this.phoneNumber = phoneNumber;
    }
    return this;
  }

  public void updateEmail(String email) {
    if (email != null) {
      this.email = email;
    }
  }

  public void changeRole(MemberRole role) {
    this.role = role;
  }

  public void changeStatus(MemberStatus status) {
    this.status = status;
  }

  public void activate() {
    if (this.status != MemberStatus.PRE_ACTIVE) {
      throw new GeneralException(ErrorStatus.MEMBER_NOT_PREACTIVE);
    }
    this.status = MemberStatus.ACTIVE;
  }

  public void addOAuthAccount(OAuthAccount oauth) {
    oauthAccount.add(oauth);
    oauth.assignMember(this);
  }

  public void deleteDeliveryAddress(MemberDeliveryAddress deleteAddress) {
    addresses.removeIf(address -> address.getId().equals(deleteAddress.getId()));
  }

  public MemberDeliveryAddress getDefaultDeliveryAddress() {
    return addresses.stream().filter(MemberDeliveryAddress::getIsDefault).findFirst().orElse(null);
  }

  public void validateCanRegisterDefaultAddress(boolean isDefault) {
    if (isDefault && getDefaultDeliveryAddress() != null) {
      throw new GeneralException(ErrorStatus.MEMBER_ALREADY_HAS_DEFAULT_ADDRESS);
    }
  }
}
