package com.modeunsa.boundedcontext.auth.out.repository;

import com.modeunsa.boundedcontext.auth.domain.entity.AuthSocialAccount;
import com.modeunsa.boundedcontext.auth.domain.types.OAuthProvider;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthSocialAccountRepository extends JpaRepository<AuthSocialAccount, Long> {

  Optional<AuthSocialAccount> findByOauthProviderAndProviderAccountId(
      OAuthProvider oauthProvider, String providerAccountId);

  boolean existsByOauthProviderAndProviderAccountId(
      OAuthProvider oauthProvider, String providerAccountId);
}
