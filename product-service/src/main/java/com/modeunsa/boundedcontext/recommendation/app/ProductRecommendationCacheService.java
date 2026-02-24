package com.modeunsa.boundedcontext.recommendation.app;

import com.modeunsa.global.json.JsonConverter;
import com.modeunsa.shared.product.dto.search.ProductSearchResponse;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;

/** TODO: 상품 추천 캐시용 서비스 -> 추상화 필요 */
@Component
@RequiredArgsConstructor
public class ProductRecommendationCacheService {

  private final RedisTemplate<String, String> redisTemplate;
  private final JsonConverter jsonConverter;

  private static final String PREFIX = "recommend:member:";
  private static final int TTL = 1;

  private String buildKey(Long memberId) {
    return PREFIX + memberId;
  }

  public void set(Long memberId, List<ProductSearchResponse> value) {
    redisTemplate
        .opsForValue()
        .set(buildKey(memberId), jsonConverter.serialize(value), Duration.ofHours(TTL));
  }

  public List<ProductSearchResponse> get(Long memberId) {
    String cached = redisTemplate.opsForValue().get(buildKey(memberId));
    return cached == null ? null : jsonConverter.deserialize(cached, new TypeReference<>() {});
  }

  public void delete(Long memberId) {
    redisTemplate.delete(buildKey(memberId));
  }
}
