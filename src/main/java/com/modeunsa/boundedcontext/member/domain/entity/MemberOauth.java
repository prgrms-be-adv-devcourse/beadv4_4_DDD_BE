package com.modeunsa.boundedcontext.member.domain.entity;

import com.modeunsa.boundedcontext.member.domain.types.OauthProvider;
import com.modeunsa.global.jpa.entity.GeneratedIdAndAuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = "member")
@Table(
    uniqueConstraints = {
      @UniqueConstraint(columnNames = {"oauth_provider", "provider_member_id"})
    })
public class MemberOauth extends GeneratedIdAndAuditedEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @Enumerated(EnumType.STRING)
  @Column(name = "oauth_provider", nullable = false)
  private OauthProvider oauthProvider;

  @Column(name = "provider_member_id", nullable = false, length = 200)
  private String providerMemberId;

  @Column(name = "provider_nickname", nullable = false, length = 50)
  private String providerNickname;

  void setMember(Member member) {
    this.member = member;
  }
}
