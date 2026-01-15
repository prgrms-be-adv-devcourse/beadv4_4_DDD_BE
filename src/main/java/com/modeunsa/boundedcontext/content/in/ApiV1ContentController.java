package com.modeunsa.boundedcontext.content.in;

import com.modeunsa.boundedcontext.content.app.ContentFacade;
import com.modeunsa.boundedcontext.content.app.dto.ContentRequest;
import com.modeunsa.boundedcontext.content.app.dto.ContentResponse;
import com.modeunsa.boundedcontext.content.app.usecase.ContentCreateContentUseCase;
import com.modeunsa.boundedcontext.content.domain.entity.Content;
import com.modeunsa.boundedcontext.content.domain.entity.ContentMember;
import com.modeunsa.global.response.ApiResponse;
import com.modeunsa.global.status.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

  @Operation(summary = "콘텐츠 생성", description = "콘텐츠 생성")
  @PostMapping
  public ResponseEntity<ApiResponse> createContent(
    @Valid @RequestBody ContentRequest contentRequest, ContentMember author) {
    ContentResponse contentResponse = contentFacade.createContent(contentRequest, author);
    return ApiResponse.onSuccess(SuccessStatus._CREATED, contentResponse);
  }
}
