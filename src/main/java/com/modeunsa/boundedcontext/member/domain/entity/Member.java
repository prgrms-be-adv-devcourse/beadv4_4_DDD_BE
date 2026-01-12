package com.modeunsa.boundedcontext.member.domain.entity;

import com.modeunsa.boundedcontext.member.domain.enums.MemberRole;
import com.modeunsa.boundedcontext.member.domain.enums.MemberStatus;
import com.modeunsa.global.jpa.entity.GeneratedIdAndAuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Entity
@Getter
@Builder
@AllArgsConstructor
@Table(name = "member")
public class Member extends GeneratedIdAndAuditedEntity {

  @Enumerated(EnumType.STRING)
  @Column(name = "member_role", nullable = false)
  private MemberRole role = MemberRole.MEMBER;

  @Enumerated(EnumType.STRING)
  @Column(name = "member_status", nullable = false)
  private MemberStatus status = MemberStatus.ACTIVE;

  @Column(unique = true, length = 255)
  private String email;

  @Column(nullable = false, length = 50)
  private String nickname;

  @Column(name = "profile_image_url", length = 1000)
  private String profileImageUrl;

  @Column(name = "height_cm")
  private Integer heightCm;

  @Column(name = "weight_kg")
  private Integer weightKg;

  @Column(name = "skin_type", length = 30)
  private String skinType;

  protected Member() {}

  public void updateProfile(
      String nickname,
      String profileImageUrl,
      Integer heightCm,
      Integer weightKg,
      String skinType) {
    if (nickname != null) {
      this.nickname = nickname;
    }
    if (profileImageUrl != null) {
      this.profileImageUrl = profileImageUrl;
    }
    if (heightCm != null) {
      this.heightCm = heightCm;
    }
    if (weightKg != null) {
      this.weightKg = weightKg;
    }
    if (skinType != null) {
      this.skinType = skinType;
    }
  }

  public void registerEmail(String email) {
    this.email = email;
  }

  public void changeRole(MemberRole role) {
    this.role = role;
  }
}
