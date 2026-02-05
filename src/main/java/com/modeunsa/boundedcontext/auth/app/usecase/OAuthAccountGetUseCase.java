package com.modeunsa.boundedcontext.auth.app.usecase;

import com.modeunsa.boundedcontext.auth.domain.entity.OAuthAccount;
import com.modeunsa.boundedcontext.auth.domain.types.OAuthProvider;
import com.modeunsa.boundedcontext.auth.out.repository.AuthSocialAccountRepository;
import com.modeunsa.shared.auth.dto.SocialStatusResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OAuthAccountGetUseCase {
  private final AuthSocialAccountRepository socialAccountRepository;

  public SocialStatusResponse execute(Long memberId) {
    // 회원의 연동된 모든 소셜 계정 조회
    List<OAuthAccount> accounts = socialAccountRepository.findAllByMemberId(memberId);

    boolean linkedKakao = accounts.stream()
        .anyMatch(a -> a.getOauthProvider() == OAuthProvider.KAKAO);
    boolean linkedNaver = accounts.stream()
        .anyMatch(a -> a.getOauthProvider() == OAuthProvider.NAVER);

    return new SocialStatusResponse(linkedKakao, linkedNaver);
  }
}
