package com.modeunsa.boundedcontext.recommendation.app;

import com.modeunsa.boundedcontext.product.app.ProductMapper;
import com.modeunsa.boundedcontext.product.app.ProductSupport;
import com.modeunsa.boundedcontext.product.app.search.ProductSearchService;
import com.modeunsa.boundedcontext.product.domain.Product;
import com.modeunsa.boundedcontext.product.domain.search.document.ProductSearch;
import com.modeunsa.boundedcontext.recommendation.domain.MemberProfile;
import com.modeunsa.boundedcontext.recommendation.in.RecommendationDto;
import com.modeunsa.boundedcontext.recommendation.out.OpenAiClient;
import com.modeunsa.shared.product.dto.search.ProductSearchResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@ConditionalOnProperty(name = "app.elasticsearch.enabled", havingValue = "true")
@Service
@RequiredArgsConstructor
@Transactional
public class RecommendationFacade {

  private final BuildRecommendationMemberProfileUseCase buildRecommendationMemberProfileUseCase;
  private final ProductSearchService productSearchService;
  private final ProductSupport productSupport;
  private final ProductMapper productMapper;
  private final OpenAiClient openAiClient;
  private final ProductRecommendationCacheService productRecommendationCacheService;

  public List<ProductSearchResponse> recommend(Long memberId) {
    // 1. redis 조회
    List<ProductSearchResponse> cached = productRecommendationCacheService.get(memberId);
    if (cached != null) {
      return cached;
    }

    // 2. 실제 추천 로직
    // 2-1. 사용자 행동 조회
    MemberProfile memberProfile = buildRecommendationMemberProfileUseCase.execute(memberId);

    // 2-2. es vector search
    List<ProductSearch> candidates =
        productSearchService.searchByVector(memberProfile.toEmbeddingText(), 10);

    // 2-3. LLM re-ranking
    List<RecommendationDto> recommendationDtos = openAiClient.rerank(memberProfile, candidates);

    // 2-4. response용 dto로 변환
    List<ProductSearchResponse> responses =
        recommendationDtos.stream()
            .map(
                item -> {
                  Product product = productSupport.getProduct(item.productId());
                  return productMapper.toProductSearchResponse(product);
                })
            .toList();

    // 3. redis 저장 (ttl: 1시간)
    productRecommendationCacheService.set(memberId, responses);

    return responses;
  }
}
