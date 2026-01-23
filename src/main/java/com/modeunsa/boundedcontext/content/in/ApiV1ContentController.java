package com.modeunsa.boundedcontext.content.in;

import com.modeunsa.boundedcontext.content.app.ContentFacade;
import com.modeunsa.boundedcontext.content.app.dto.ContentCommentRequest;
import com.modeunsa.boundedcontext.content.app.dto.ContentCommentResponse;
import com.modeunsa.boundedcontext.content.app.dto.ContentRequest;
import com.modeunsa.boundedcontext.content.app.dto.ContentResponse;
import com.modeunsa.boundedcontext.content.domain.entity.ContentMember;
import com.modeunsa.boundedcontext.content.out.search.ContentSearchCondition;
import com.modeunsa.boundedcontext.content.out.search.ContentSearchDocument;
import com.modeunsa.boundedcontext.content.out.search.ContentSearchUseCase;
import com.modeunsa.global.elasticsearch.model.ElasticSearchPage;
import com.modeunsa.global.response.ApiResponse;
import com.modeunsa.global.status.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Content", description = "콘텐츠 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/contents")
public class ApiV1ContentController {

  private final ContentFacade contentFacade;
  private final ContentSearchUseCase contentSearchUseCase;

  @Operation(summary = "콘텐츠 생성", description = "콘텐츠를 생성합니다.")
  @PostMapping
  public ResponseEntity<ApiResponse> createContent(
      @Valid @RequestBody ContentRequest contentRequest, ContentMember author) {
    ContentResponse contentResponse = contentFacade.createContent(contentRequest, author);
    return ApiResponse.onSuccess(SuccessStatus.CREATED, contentResponse);
  }

  @Operation(summary = "콘텐츠 수정", description = "콘텐츠를 수정합니다.")
  @PatchMapping("/{contentId}")
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
  public ResponseEntity<ApiResponse> deleteContent(
      @PathVariable Long contentId, ContentMember author) {
    contentFacade.deleteContent(contentId, author);
    return ApiResponse.onSuccess(SuccessStatus.OK);
  }

  @Operation(summary = "콘텐츠 전체 조회", description = "콘텐츠 전체 목록을 최신순으로 조회합니다.")
  @GetMapping
  public ResponseEntity<ApiResponse> getContents(@RequestParam(defaultValue = "0") int page) {
    Page<ContentResponse> result = contentFacade.getContents(page);

    return ApiResponse.onSuccess(SuccessStatus.CONTENT_LIST_GET_SUCCESS, result);
  }

  @Operation(summary = "콘텐츠 검색 기능", description = "컨텐츠 내에서 글, 태그 검색 조회를 합니다.")
  @GetMapping("/{contentId}/search")
  public ElasticSearchPage<ContentSearchDocument> searchContent(
      @RequestParam String keyword,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    ContentSearchCondition condition = new ContentSearchCondition(keyword, page, size);

    return contentSearchUseCase.search(condition);
  }

  @Operation(summary = "댓글 생성", description = "한 콘텐츠 내 댓글을 생성합니다.")
  @PostMapping("/{contentId}/comments")
  public ResponseEntity<ApiResponse> createContentComment(
      @PathVariable Long contentId,
      @Valid @RequestBody ContentCommentRequest contentCommentRequest,
      ContentMember author) {
    ContentCommentResponse contentCommentResponse =
        contentFacade.createContentComment(contentId, contentCommentRequest, author);
    return ApiResponse.onSuccess(SuccessStatus.CREATED, contentCommentResponse);
  }
}
