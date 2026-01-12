package com.modeunsa.boundedcontext.member.domain.entity;

import com.modeunsa.boundedcontext.member.domain.enums.OauthProvider;
import com.modeunsa.global.jpa.entity.GeneratedIdAndAuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Entity
@Getter
@Builder
@AllArgsConstructor
@Table(name = "member_oauth")
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

  protected MemberOauth() {}
}
