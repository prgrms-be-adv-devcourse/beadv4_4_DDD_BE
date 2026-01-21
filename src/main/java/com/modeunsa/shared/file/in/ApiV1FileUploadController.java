package com.modeunsa.shared.file.in;

import com.modeunsa.global.s3.S3UploadService;
import com.modeunsa.shared.file.dto.PresignedUrlRequest;
import com.modeunsa.shared.file.dto.PresignedUrlResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "FileUpload", description = "파일 업로드 API")
@RestController
@RequestMapping("/api/v1/file-uploads")
@RequiredArgsConstructor
public class ApiV1FileUploadController {

  private final S3UploadService s3UploadService;

  @PostMapping("/presigned-url")
  public ResponseEntity<PresignedUrlResponse> issuePresignedUrl(
      @RequestBody PresignedUrlRequest request) {
    PresignedUrlResponse response = s3UploadService.getPutPreSignedUrl(request);
    return ResponseEntity.ok(response);
  }
}
