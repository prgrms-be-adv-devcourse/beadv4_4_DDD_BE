package com.modeunsa.boundedcontext.recommendation.out;

import com.modeunsa.boundedcontext.product.domain.search.document.ProductSearch;
import com.modeunsa.boundedcontext.recommendation.domain.MemberProfile;
import com.modeunsa.boundedcontext.recommendation.in.RecommendationDto;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OpenAiClient {

  private final ChatClient chatClient;

  public List<RecommendationDto> rerank(MemberProfile profile, List<ProductSearch> candidates) {

    String prompt = this.build(profile, candidates);

    List<RecommendationDto> response =
        chatClient.prompt().user(prompt).call().entity(new ParameterizedTypeReference<>() {});

    return response;
  }

  public String build(MemberProfile profile, List<ProductSearch> candidates) {

    String candidateText =
        candidates.stream()
            .map(
                p ->
                    """
                    {
                      "id": %s,
                      "상품명": "%s",
                      "카테고리": "%s",
                      "브랜드명": "%s",
                      "가격": %s
                    }
                    """
                        .formatted(
                            p.getId(),
                            p.getName(),
                            p.getCategory(),
                            p.getSellerBusinessName(),
                            p.getSalePrice()))
            .collect(Collectors.joining(",\n"));

    return """
        당신은 이커머스 추천 시스템의 랭킹 엔진입니다.
        당신의 역할은 사용자 활동내역을 기반으로 가장 적합한 상품을 재정렬하는 것입니다.
        새로운 상품을 생성하거나 후보 목록에 없는 상품을 추가하면 안 됩니다.

        [사용자 프로필]
        - 선호 상품명: %s
        - 선호 카테고리: %s
        - 선호 브랜드: %s
        - 선호 가격대: %s

        [후보 상품 목록]
        [
        %s
        ]

        [랭킹 판단 기준]
        아래 기준을 종합적으로 고려하여 적합도를 판단하세요.

        1. 카테고리 일치 여부 (가장 중요)
        2. 브랜드 선호 일치 여부
        3. 가격대 적합성
          - 가격대 기준
            - LOW: 0원 ~ 50,000원
            - MID: 50,000원 ~ 150,000원
            - HIGH: 150,000원 이상
        4. 사용자 선호 상품명 키워드와의 유사성
        5. 전반적인 사용자 관심 패턴과의 연관성

        위 항목을 종합적으로 평가하여 가장 적합한 순서대로 정렬하세요.

        [작업 지시]
        1. 사용자에게 가장 적합한 상품 10개를 선택하세요.
        2. 적합도 순으로 1위부터 정렬하세요.
        3. rank는 1부터 시작하는 정수여야 합니다.
        4. 각 상품마다 1줄의 간결한 추천 이유를 작성하세요.
        5. 반드시 아래 JSON 형식으로만 응답하세요.

        [응답 형식]
        [
          {
            "productId": 123,
            "rank": 1,
            "reason": "사용자의 관심 카테고리와 일치하며 선호 가격대에 적합합니다."
          }
        ]
        """
        .formatted(
            profile.getTopNames(),
            profile.getTopCategories(),
            profile.getTopBrands(),
            profile.getPriceRange(),
            candidateText);
  }
}
