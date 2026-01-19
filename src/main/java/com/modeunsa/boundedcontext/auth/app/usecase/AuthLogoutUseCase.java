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
    // 1. Access Token에서 정보 추출
    Long memberId = jwtTokenProvider.getMemberIdFromToken(accessToken);
    long remainingExpiration = jwtTokenProvider.getRemainingExpiration(accessToken);

    // 2. Refresh Token 삭제
    refreshTokenRepository.deleteById(memberId);
    log.info("Refresh Token 삭제 완료 - memberId: {}", memberId);

    // 3. Access Token 블랙리스트 등록 (남은 만료시간만큼 TTL 설정)
    if (remainingExpiration > 0) {
      AuthAccessTokenBlacklist blacklist =
          AuthAccessTokenBlacklist.of(accessToken, memberId, remainingExpiration);
      blacklistRepository.save(blacklist);
      log.info("Access Token 블랙리스트 등록 완료 - memberId: {}, TTL: {}ms", memberId, remainingExpiration);
    }
  }
}
