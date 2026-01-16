package com.modeunsa.boundedcontext.content.in;

import com.modeunsa.boundedcontext.content.app.ContentFacade;
import com.modeunsa.boundedcontext.content.app.dto.ContentRequest;
import com.modeunsa.boundedcontext.content.app.dto.ContentResponse;
import com.modeunsa.boundedcontext.content.domain.entity.ContentMember;
import com.modeunsa.global.response.ApiResponse;
import com.modeunsa.global.status.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Content", description = "콘텐츠 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/contents")
public class ApiV1ContentController {

  private final ContentFacade contentFacade;

  @Operation(summary = "콘텐츠 생성", description = "콘텐츠를 생성합니다.")
  @PostMapping
  public ResponseEntity<ApiResponse> createContent(
      @Valid @RequestBody ContentRequest contentRequest, ContentMember author) {
    ContentResponse contentResponse = contentFacade.createContent(contentRequest, author);
    return ApiResponse.onSuccess(SuccessStatus.CREATED, contentResponse);
  }

  @Operation(summary = "콘텐츠 수정", description = "콘텐츠를 수정합니다.")
  @PatchMapping("/{content_Id")
  public ResponseEntity<ApiResponse> updateContent(
      @PathVariable Long contentId,
      @Valid @RequestBody ContentRequest contentRequest,
      ContentMember author) {
    ContentResponse contentResponse =
        contentFacade.updateContent(contentId, contentRequest, author);
    return ApiResponse.onSuccess(SuccessStatus.OK, contentResponse);
  }

  @Operation(summary = "콘텐츠 삭제", description = "콘텐츠를 삭제합니다.")
  @DeleteMapping
  public ResponseEntity<ApiResponse> deleteContent(Long contentId, ContentMember author) {
    contentFacade.deleteContent(contentId, author);
    return ApiResponse.onSuccess(SuccessStatus.OK);
  }
}
