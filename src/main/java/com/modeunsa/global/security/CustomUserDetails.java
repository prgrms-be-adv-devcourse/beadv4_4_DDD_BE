package com.modeunsa.global.security;

import com.modeunsa.boundedcontext.member.domain.types.MemberRole;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
public class CustomUserDetails implements UserDetails {

  private final Long memberId;
  private final MemberRole role;
  private final Long sellerId; // nullable

  public CustomUserDetails(Long memberId, MemberRole role, Long sellerId) {
    this.memberId = memberId;
    this.role = role;
    this.sellerId = sellerId;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
  }

  @Override
  public String getPassword() {
    return null;
  }

  @Override
  public String getUsername() {
    return memberId.toString();
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  public boolean isSeller() {
    return MemberRole.SELLER.equals(this.role) && sellerId != null;
  }
}
