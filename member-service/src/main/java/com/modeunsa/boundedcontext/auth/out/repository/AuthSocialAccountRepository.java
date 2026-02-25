package com.modeunsa.boundedcontext.auth.out.repository;

import com.modeunsa.boundedcontext.auth.domain.entity.OAuthAccount;
import com.modeunsa.boundedcontext.auth.domain.types.OAuthProvider;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthSocialAccountRepository extends JpaRepository<OAuthAccount, Long> {

  Optional<OAuthAccount> findByOauthProviderAndProviderId(
      OAuthProvider oauthProvider, String providerId);

  boolean existsByOauthProviderAndProviderId(OAuthProvider oauthProvider, String providerId);

  // 해당 회원이 특정 provider로 이미 연동했는지 확인
  boolean existsByMemberIdAndOauthProvider(Long memberId, OAuthProvider oauthProvider);

  List<OAuthAccount> findAllByMemberId(Long memberId);
}
