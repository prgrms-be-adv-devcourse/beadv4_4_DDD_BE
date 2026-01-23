package com.modeunsa.boundedcontext.member.domain.entity;

import com.modeunsa.global.jpa.converter.EncryptedStringConverter;
import com.modeunsa.global.jpa.entity.GeneratedIdAndAuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
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
public class MemberProfile extends GeneratedIdAndAuditedEntity {

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @Column(unique = true, length = 50)
  private String nickname;

  @Column(length = 1000)
  private String profileImageUrl;

  // TODO: Integer에 대한 암호화 처리
  private Integer heightCm;

  // TODO: Integer에 대한 암호화 처리
  private Integer weightKg;

  // TODO: enum으로 변경 고려
  @Convert(converter = EncryptedStringConverter.class)
  @Column(length = 500)
  private String skinType;

  void setMember(Member member) {
    this.member = member;
  }

  public MemberProfile updateNickname(String nickname) {
    if (nickname != null) {
      this.nickname = nickname;
    }
    return this;
  }

  public MemberProfile updateProfileImageUrl(String profileImageUrl) {
    if (profileImageUrl != null) {
      this.profileImageUrl = profileImageUrl;
    }
    return this;
  }

  public MemberProfile updateHeightCm(Integer heightCm) {
    if (heightCm != null) {
      this.heightCm = heightCm;
    }
    return this;
  }

  public MemberProfile updateWeightKg(Integer weightKg) {
    if (weightKg != null) {
      this.weightKg = weightKg;
    }
    return this;
  }

  public MemberProfile updateSkinType(String skinType) {
    if (skinType != null) {
      this.skinType = skinType;
    }
    return this;
  }
}
