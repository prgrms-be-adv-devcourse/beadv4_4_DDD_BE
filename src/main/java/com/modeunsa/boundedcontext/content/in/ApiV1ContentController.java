package com.modeunsa.boundedcontext.content.in;

import com.modeunsa.boundedcontext.content.app.ContentFacade;
import com.modeunsa.boundedcontext.content.app.ContentWriteUseCase.ImagePayload;
import com.modeunsa.boundedcontext.content.domain.entity.Content;
import com.modeunsa.global.response.ApiResponse;
import com.modeunsa.global.status.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Content", description = "콘텐츠 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/contents")
public class ApiV1ContentController {

  private final ContentFacade contentFacade;

  @Operation(summary = "콘텐츠 생성")
  @PostMapping
  public ResponseEntity<ApiResponse> create(
    @RequestParam Long authorUserId,
    @RequestParam String text
  ) {
    Content content = contentFacade.create(authorUserId, text, null, null);
    return ApiResponse.onSuccess(SuccessStatus._CREATED, content.getId());
  }

  @Operation(summary = "콘텐츠 수정")
  @PatchMapping
  public ResponseEntity<ApiResponse> update(
    @RequestParam Long contentId,
    @RequestParam Long requesterId,
    @RequestParam String text
  ) {
    Content content =
      contentFacade.update(contentId, requesterId, text, null, null);
    return ApiResponse.onSuccess(SuccessStatus._OK, content.getId());
  }

  @Operation(summary = "콘텐츠 삭제")
  @DeleteMapping
  public ResponseEntity<ApiResponse> delete(
    @RequestParam Long contentId,
    @RequestParam Long requesterId
  ) {
    contentFacade.delete(contentId, requesterId);
    return ApiResponse.onSuccess(SuccessStatus._OK);
  }

  @Operation(summary = "콘텐츠 전체 조회")
  @GetMapping
  public ResponseEntity<ApiResponse> list(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size
  ) {
    Pageable pageable = PageRequest.of(page, size);
    Page<Content> contents = contentFacade.findLatest(pageable);
    return ApiResponse.onSuccess(SuccessStatus._OK, contents);
  }

  @Operation(summary = "콘텐츠 상세 조회")
  @GetMapping("/{contentId}")
  public ResponseEntity<ApiResponse> detail(@PathVariable Long contentId) {
    Content content = contentFacade.findById(contentId);
    return ApiResponse.onSuccess(SuccessStatus._OK, content);
  }
}
