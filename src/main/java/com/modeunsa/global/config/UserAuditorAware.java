package com.modeunsa.global.config;

import com.modeunsa.global.security.CustomUserDetails;
import java.util.Optional;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class UserAuditorAware implements AuditorAware<Long> {

  @Override
  public Optional<Long> getCurrentAuditor() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    // 인증 정보가 없거나, 익명 사용자인 경우 처리
    if (authentication == null
        || !authentication.isAuthenticated()
        || authentication.getPrincipal().equals("anonymousUser")) {
      return Optional.empty();
    }

    Object principal = authentication.getPrincipal();
    if (principal instanceof CustomUserDetails) {
      return Optional.ofNullable(((CustomUserDetails) principal).getMemberId());
    }

    return Optional.empty();
  }
}
