package com.modeunsa.boundedcontext.auth.app.usecase;

import com.modeunsa.boundedcontext.auth.domain.entity.OAuthAccount;
import com.modeunsa.boundedcontext.auth.domain.types.OAuthProvider;
import com.modeunsa.boundedcontext.auth.out.repository.AuthSocialAccountRepository;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DuplicateResolveUseCase {

  private final AuthSocialAccountRepository repository;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public OAuthAccount findExistingAccount(OAuthProvider provider, String providerId) {
    return repository
        .findByOauthProviderAndProviderId(provider, providerId)
        .orElseThrow(() -> new GeneralException(ErrorStatus.INTERNAL_SERVER_ERROR));
  }
}
