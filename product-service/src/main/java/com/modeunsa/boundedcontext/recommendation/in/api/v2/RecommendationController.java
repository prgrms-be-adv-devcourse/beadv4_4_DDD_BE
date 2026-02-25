package com.modeunsa.boundedcontext.recommendation.in.api.v2;

import com.modeunsa.boundedcontext.recommendation.app.RecommendationFacade;
import com.modeunsa.global.response.ApiResponse;
import com.modeunsa.global.security.CustomUserDetails;
import com.modeunsa.global.status.SuccessStatus;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@ConditionalOnProperty(name = "app.elasticsearch.enabled", havingValue = "true")
@Tag(name = "Recommendation", description = "상품 추천 API")
@RestController("RecommendationV2Controller")
@RequestMapping("/api/v2/products/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

  private final RecommendationFacade recommendationFacade;

  @GetMapping
  public ResponseEntity<ApiResponse> recommend(
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    if (userDetails == null) {
      return null;
    }
    return ApiResponse.onSuccess(
        SuccessStatus.OK, recommendationFacade.recommend(userDetails.getMemberId()));
  }
}
