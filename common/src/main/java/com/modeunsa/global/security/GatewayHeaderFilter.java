package com.modeunsa.global.security;

import com.modeunsa.shared.member.MemberRole;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class GatewayHeaderFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String userIdStr = request.getHeader("X-User-Id");
    String userRoleStr = request.getHeader("X-User-Role");
    String sellerIdStr = request.getHeader("X-Seller-Id");

    if (userIdStr != null && userRoleStr != null) {
      // 1. SYSTEM 계정 처리 (Internal API Key 요청)
      if ("SYSTEM".equals(userIdStr)) {
        CustomUserDetails systemUser = new CustomUserDetails(0L, MemberRole.SYSTEM, null);
        setAuthentication(systemUser);
      }
      // 2. 일반 유저 처리
      else {
        try {
          Long memberId = Long.parseLong(userIdStr);
          String roleName = userRoleStr.startsWith("ROLE_") ? userRoleStr.substring(5) : userRoleStr;
          MemberRole role = MemberRole.valueOf(roleName);
          Long sellerId = sellerIdStr != null ? Long.parseLong(sellerIdStr) : null;

          CustomUserDetails user = new CustomUserDetails(memberId, role, sellerId);
          setAuthentication(user);
        } catch (Exception e) {
          logger.warn("Gateway 헤더 파싱 실패: " + e.getMessage());
        }
      }
    }

    filterChain.doFilter(request, response);
  }

  private void setAuthentication(CustomUserDetails userDetails) {
    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }
}