package com.modeunsa.boundedcontext.auth.app.usecase;

import com.modeunsa.boundedcontext.auth.domain.entity.AuthAccessTokenBlacklist;
import com.modeunsa.boundedcontext.auth.out.repository.AuthAccessTokenBlacklistRepository;
import com.modeunsa.boundedcontext.auth.out.repository.AuthRefreshTokenRepository;
import com.modeunsa.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthLogoutUseCase {

  private final JwtTokenProvider jwtTokenProvider;
  private final AuthRefreshTokenRepository refreshTokenRepository;
  private final AuthAccessTokenBlacklistRepository blacklistRepository;

  public void execute(String accessToken) {
    // Access Token에서 정보 추출
    Long memberId = jwtTokenProvider.getMemberIdFromToken(accessToken);

    // 이미 블랙리스트에 있으면 중복 로그아웃 → 무시
    if (blacklistRepository.existsById(accessToken)) {
      log.info("이미 로그아웃된 토큰 - memberId: {}", memberId);
      return;
    }

    // Refresh Token 삭제
    refreshTokenRepository.deleteById(memberId);
    log.info("Refresh Token 삭제 완료 - memberId: {}", memberId);

    long remainingExpiration = jwtTokenProvider.getRemainingExpiration(accessToken);

    // 3. Access Token 블랙리스트 등록 (남은 만료시간만큼 TTL 설정)
    if (remainingExpiration > 0) {
      AuthAccessTokenBlacklist blacklist =
          AuthAccessTokenBlacklist.of(accessToken, memberId, remainingExpiration);
      blacklistRepository.save(blacklist);
      log.info("Access Token 블랙리스트 등록 완료 - memberId: {}, TTL: {}ms", memberId, remainingExpiration);
    }
  }
}
