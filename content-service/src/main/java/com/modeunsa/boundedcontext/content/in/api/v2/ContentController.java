package com.modeunsa.boundedcontext.content.in.api.v2;

import com.modeunsa.boundedcontext.content.app.ContentFacade;
import com.modeunsa.boundedcontext.content.app.dto.content.ContentDetailDto;
import com.modeunsa.global.response.ApiResponse;
import com.modeunsa.global.status.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Content", description = "콘텐츠 API")
@RestController("ContentV2Controller")
@RequiredArgsConstructor
@RequestMapping("/api/v2/contents")
public class ContentController {

  private final ContentFacade contentFacade;

  @Operation(summary = "콘텐츠 상세 조회", description = "콘텐츠를 상세 조회합니다.")
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse> getContent(@Valid @PathVariable(name = "id") Long contentId) {
    ContentDetailDto response = contentFacade.getContent(contentId);
    return ApiResponse.onSuccess(SuccessStatus.CREATED, response);
  }
}
