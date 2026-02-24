package com.modeunsa.boundedcontext.recommendation.app;

import com.modeunsa.boundedcontext.product.app.ProductSupport;
import com.modeunsa.boundedcontext.product.domain.ProductFavorite;
import com.modeunsa.boundedcontext.recommendation.domain.MemberProfile;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@ConditionalOnProperty(name = "app.elasticsearch.enabled", havingValue = "true")
@Service
@RequiredArgsConstructor
public class BuildRecommendationMemberProfileUseCase {

  private static final int RECOMMENDATION_SIZE = 10;

  //  private final OrderApiClient orderApiClient;
  private final ProductSupport productSupport;

  public MemberProfile execute(Long memberId) {
    // 최근 장바구니 10개
    //    List<Long> cartItemIds = orderApiClient.getRecent10CartItems(memberId);
    //    List<Product> cartItemProducts = productSupport.getProducts(cartItemIds);

    // 최근 관심상품 10개
    List<ProductFavorite> productFavorites =
        productSupport.getRecentFavoriteProducts(memberId, RECOMMENDATION_SIZE);

    // 사용자 활동내역에 추가
    return MemberProfile.aggregate(null, productFavorites, RECOMMENDATION_SIZE);
  }
}
