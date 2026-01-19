package com.modeunsa.boundedcontext.auth.in.util;

import com.modeunsa.boundedcontext.auth.domain.types.OAuthProvider;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import java.util.Arrays;
import org.springframework.util.StringUtils;

public class AuthRequestUtils {
  private static final String BEARER_PREFIX = "Bearer ";

  private AuthRequestUtils() {}

  /**
   * Authorization 헤더에서 Bearer 토큰 추출
   */
  public static String resolveToken(String bearerToken) {
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
      return bearerToken.substring(BEARER_PREFIX.length());
    }
    return null;
  }

  /** Provider 문자열 -> Enum 변환 */
  public static OAuthProvider findProvider(String providerName) {
    return Arrays.stream(OAuthProvider.values())
        .filter(p -> p.name().equalsIgnoreCase(providerName))
        .findFirst()
        .orElseThrow(() -> new GeneralException(ErrorStatus.OAUTH_INVALID_PROVIDER));
  }

}
