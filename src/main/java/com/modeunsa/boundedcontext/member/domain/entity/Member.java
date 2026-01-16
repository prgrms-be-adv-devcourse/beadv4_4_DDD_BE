package com.modeunsa.boundedcontext.member.domain.entity;

import static com.modeunsa.global.status.ErrorStatus.MEMBER_ADDRESS_LIMIT_EXCEEDED;
import static com.modeunsa.global.status.ErrorStatus.MEMBER_DEFAULT_ADDRESS_REQUIRED;

import com.modeunsa.boundedcontext.auth.domain.entity.AuthSocialAccount;
import com.modeunsa.boundedcontext.member.domain.types.MemberRole;
import com.modeunsa.boundedcontext.member.domain.types.MemberStatus;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.jpa.entity.GeneratedIdAndAuditedEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
  @Builder.Default
  private MemberRole role = MemberRole.MEMBER;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @Builder.Default
  private MemberStatus status = MemberStatus.ACTIVE;

  @Column(unique = true, length = 255)
  private String email;

  @Column(length = 30)
  private String realName;

  @Column(length = 20)
  private String phoneNumber;

  @Builder.Default
  @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<AuthSocialAccount> oauthSocialAccounts = new ArrayList<>();

  @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private MemberProfile profile;

  @Builder.Default
  @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<MemberDeliveryAddress> addresses = new ArrayList<>();

  @Column private LocalDateTime withdrawalRequestedAt;

  // 프로필 설정
  public void setProfile(MemberProfile profile) {
    this.profile = profile;
    profile.setMember(this);
  }

  // 배송지 추가
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

    // 주소가 아직 등록되지 않았다면 추가
    if (!addresses.contains(newDefault)) {
      addAddress(newDefault);
    }

    // 이미 기본 배송지라면 불필요한 작업 생략
    if (newDefault.getIsDefault()) {
      return;
    }

    // 기존 기본 배송지 해제
    for (MemberDeliveryAddress address : addresses) {
      if (address.getIsDefault()) {
        address.unsetAsDefault();
      }
    }

    // 새 기본 배송지 설정
    newDefault.setAsDefault();
  }

  // 개인 정보 입력
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

  public Member updateEmail(String email) {
    if (email != null) {
      this.email = email;
    }
    return this;
  }

  public void changeRole(MemberRole role) {
    this.role = role;
  }

  public void changeStatus(MemberStatus status) {
    this.status = status;
  }

  public void addOAuthAccount(AuthSocialAccount oauth) {
    oauthSocialAccounts.add(oauth);
  }
}
