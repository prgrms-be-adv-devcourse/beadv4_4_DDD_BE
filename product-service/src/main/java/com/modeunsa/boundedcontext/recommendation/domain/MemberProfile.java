package com.modeunsa.boundedcontext.recommendation.domain;

import com.modeunsa.boundedcontext.product.domain.Product;
import com.modeunsa.boundedcontext.product.domain.ProductFavorite;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/** 사용자의 활동내역 기반 데이터 모델입니다. */
@Getter
@Setter
@RequiredArgsConstructor
public class MemberProfile {
  private final Long memberId;
  private final List<String> topNames;
  private final List<String> topCategories;
  private final List<String> topBrands;
  private final String priceRange;

  public String toEmbeddingText() {
    return """
        사용자가 관심있는 정보:
        상품명: %s
        상품 카테고리: %s
        브랜드: %s
        가격대: %s
        """
        .formatted(topNames, topCategories, topBrands, priceRange);
  }

  public static MemberProfile aggregate(
      List<Product> cartItems, List<ProductFavorite> favoriteProducts, int size) {
    Map<String, Double> nameScore = new HashMap<>();
    Map<String, Double> categoryScore = new HashMap<>();
    Map<String, Double> brandScore = new HashMap<>();
    List<BigDecimal> salePrices = new ArrayList<>();

    // 장바구니 (가중치 2.0)
    //    for (Product p : cartItems) {
    //    nameScore.merge(p.getProduct().getName(), 2.0, Double::sum);
    //      categoryScore.merge(p.getCategory().getDescription(), 2.0, Double::sum);
    //      brandScore.merge(p.getSeller().getBusinessName(), 2.0, Double::sum);
    //      salePrices.add(p.getSalePrice());
    //    }

    // 관심상품 (가중치 1.5)
    for (ProductFavorite p : favoriteProducts) {
      nameScore.merge(p.getProduct().getName(), 1.5, Double::sum);
      categoryScore.merge(p.getProduct().getCategory().name(), 1.5, Double::sum);
      brandScore.merge(p.getProduct().getSeller().getBusinessName(), 1.5, Double::sum);
      salePrices.add(p.getProduct().getSalePrice());
    }

    List<String> topNames = topN(nameScore, size);
    List<String> topCategories = topN(categoryScore, size);
    List<String> topBrands = topN(brandScore, size);

    String priceRange = calculatePriceRange(salePrices);

    return new MemberProfile(null, topNames, topCategories, topBrands, priceRange);
  }

  private static List<String> topN(Map<String, Double> scoreMap, int n) {
    return scoreMap.entrySet().stream()
        .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
        .limit(n)
        .map(Map.Entry::getKey)
        .toList();
  }

  private static String calculatePriceRange(List<BigDecimal> prices) {

    if (prices.isEmpty()) {
      return "UNKNOWN";
    }

    BigDecimal avg =
        prices.stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(prices.size()), RoundingMode.HALF_UP);

    if (avg.compareTo(BigDecimal.valueOf(50000)) < 0) {
      return "LOW";
    }
    if (avg.compareTo(BigDecimal.valueOf(150000)) < 0) {
      return "MID";
    }
    return "HIGH";
  }
}
