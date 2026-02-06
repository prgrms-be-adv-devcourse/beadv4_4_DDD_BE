package com.modeunsa.global.security;

import com.modeunsa.boundedcontext.auth.app.facade.AuthFacade;
import com.modeunsa.shared.member.dto.request.MemberRoleUpdateRequest;
import com.modeunsa.shared.member.dto.response.MemberRoleUpdateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "role", description = "role 관련 API")
@RestController
@RequestMapping("/api/v1/test/roles")
@RequiredArgsConstructor
public class RoleTestController {

  private final AuthFacade authFacade;

  @Operation(summary = "내 권한 강제 변경", description = "현재 로그인한 사용자의 Role을 변경하고 새 토큰을 발급받습니다.")
  @PatchMapping("/change")
  public ResponseEntity<MemberRoleUpdateResponse> changeMyRole(
      @AuthenticationPrincipal CustomUserDetails user,
      @RequestBody MemberRoleUpdateRequest request) {
    // Facade를 통해 로직 처리 및 토큰 생성 결과 수신
    MemberRoleUpdateResponse response =
        authFacade.updateMemberRole(user.getMemberId(), request.role());

    return ResponseEntity.ok(response);
  }

  @Operation(summary = "공개 API", description = "인증 여부와 관계없이 누구나 접근 가능한 API입니다.")
  @GetMapping("/public")
  public Map<String, String> publicApi() {
    return Map.of("message", "Public API 접근 성공 - 누구나 접근 가능한 API");
  }

  @Operation(summary = "인증 확인 API", description = "최소한 로그인(인증)이 필요한 API입니다.")
  @GetMapping("/authenticated")
  public Map<String, String> authenticatedApi() {
    return Map.of("message", "Authenticated API 접근 성공 - 로그인 필요한 API");
  }

  @Operation(
      summary = "회원 전용 API",
      description = "MEMBER 이상의 권한(SELLER, ADMIN, SYSTEM, HOLDER 포함)이 필요합니다.")
  @GetMapping("/member")
  @PreAuthorize("hasRole('MEMBER')")
  public Map<String, String> memberApi() {
    return Map.of("message", "Member API 접근 성공 - MEMBER 권한 필요한 API");
  }

  @Operation(
      summary = "판매자 전용 API",
      description = "SELLER 이상의 권한(ADMIN, SYSTEM, HOLDER 포함)이 필요합니다.")
  @GetMapping("/seller")
  @PreAuthorize("hasRole('SELLER')")
  public Map<String, String> sellerApi() {
    return Map.of("message", "Seller API 접근 성공 - SELLER 권한 필요한 API");
  }

  @Operation(summary = "관리자 전용 API", description = "ADMIN 이상의 권한(SYSTEM, HOLDER 포함)이 필요합니다.")
  @GetMapping("/admin")
  @PreAuthorize("hasRole('ADMIN')")
  public Map<String, String> adminApi() {
    return Map.of("message", "Admin API 접근 성공 - ADMIN 권한 필요한 API");
  }

  @Operation(summary = "시스템 전용 API", description = "SYSTEM 권한만 접근 가능한 API입니다.")
  @GetMapping("/system")
  @PreAuthorize("hasRole('SYSTEM')")
  public Map<String, String> systemApi() {
    return Map.of("message", "System API 접근 성공 - SYSTEM 권한 필요한 API");
  }

  @Operation(summary = "홀더 전용 API", description = "HOLDER 권한만 접근 가능한 API입니다.")
  @GetMapping("/holder")
  @PreAuthorize("hasRole('HOLDER')")
  public Map<String, String> holderApi() {
    return Map.of("message", "Holder API 접근 성공 - HOLDER 권한 필요한 API");
  }

  @Operation(
      summary = "내 상세 권한 정보 조회",
      description = "현재 로그인된 사용자의 ID, 주요 Role, 그리고 Role Hierarchy가 적용된 모든 권한 목록을 확인합니다.")
  @GetMapping("/me")
  public Map<String, Object> myInfo(@AuthenticationPrincipal CustomUserDetails user) {
    if (user == null) {
      return Map.of("error", "로그인 필요");
    }
    return Map.of(
        "memberId", user.getMemberId(),
        "role", user.getRole().name(),
        "authorities", user.getAuthorities().toString() // 계층 구조가 적용된 권한 목록 확인 가능
        );
  }
}
