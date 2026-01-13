package com.modeunsa.boundedcontext.member.domain.entity;

import com.modeunsa.global.jpa.entity.GeneratedIdAndAuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
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

  private Integer heightCm;

  private Integer weightKg;

  @Column(length = 30)
  private String skinType;

  // 연관관계 편의 메서드
  void setMember(Member member) {
    this.member = member;
  }

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
}