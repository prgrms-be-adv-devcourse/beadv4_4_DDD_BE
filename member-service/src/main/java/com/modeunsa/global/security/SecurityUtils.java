package com.modeunsa.global.security;

import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.shared.member.MemberRole;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

  private SecurityUtils() {}

  /** 현재 로그인한 회원 ID 조회 */
  public static Long getCurrentMemberId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      throw new GeneralException(ErrorStatus.AUTH_UNAUTHORIZED);
    }

    Object principal = authentication.getPrincipal();

    if (principal instanceof CustomUserDetails userDetails) {
      return userDetails.getMemberId();
    }

    throw new GeneralException(ErrorStatus.AUTH_UNAUTHORIZED);
  }

  /** 현재 로그인한 회원 Role 조회 */
  public static MemberRole getCurrentMemberRole() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      throw new GeneralException(ErrorStatus.AUTH_UNAUTHORIZED);
    }

    Object principal = authentication.getPrincipal();

    if (principal instanceof CustomUserDetails userDetails) {
      return userDetails.getRole();
    }

    throw new GeneralException(ErrorStatus.AUTH_UNAUTHORIZED);
  }

  /** 현재 사용자가 특정 Role인지 확인 */
  public static boolean hasRole(MemberRole role) {
    try {
      return getCurrentMemberRole() == role;
    } catch (GeneralException e) {
      return false;
    }
  }

  /** 현재 사용자가 관리자인지 확인 */
  public static boolean isAdmin() {
    return hasRole(MemberRole.ADMIN);
  }

  /** 현재 사용자가 판매자인지 확인 (판매자를 포함하는 계층) */
  public static boolean isSeller() {
    MemberRole role = getCurrentMemberRole();
    return role == MemberRole.SELLER
        || role == MemberRole.ADMIN
        || role == MemberRole.SYSTEM
        || role == MemberRole.HOLDER;
  }

  /** 현재 사용자가 회원인지 확인 (회원을 포함하는 계층) */
  public static boolean isMember() {
    MemberRole role = getCurrentMemberRole();
    return role == MemberRole.MEMBER
        || role == MemberRole.SELLER
        || role == MemberRole.ADMIN
        || role == MemberRole.SYSTEM
        || role == MemberRole.HOLDER;
  }
}
