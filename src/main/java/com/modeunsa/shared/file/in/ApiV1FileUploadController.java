package com.modeunsa.shared.file.in;

import com.modeunsa.global.response.ApiResponse;
import com.modeunsa.global.s3.S3UploadService;
import com.modeunsa.global.s3.dto.DomainType;
import com.modeunsa.global.s3.dto.PresignedUrlRequest;
import com.modeunsa.global.s3.dto.PresignedUrlResponse;
import com.modeunsa.global.s3.dto.PublicUrlRequest;
import com.modeunsa.global.s3.dto.PublicUrlResponse;
import com.modeunsa.global.status.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "FileUpload", description = "파일 업로드 API")
@RestController
@RequestMapping("/api/v1/file-uploads")
@RequiredArgsConstructor
public class ApiV1FileUploadController {

  private final S3UploadService s3UploadService;

  @Operation(
      summary = "업로드용 presigned url",
      description = "업로드용 presignedUrl을 생성합니다. 반환받은 url을 사용해서 파일 업로드를 진행합니다.")
  @PostMapping("/presigned-url")
  public ResponseEntity<ApiResponse> issuePresignedUrl(@RequestBody PresignedUrlRequest request) {
    PresignedUrlResponse response = s3UploadService.issuePresignedUrl(request);
    return ApiResponse.onSuccess(SuccessStatus.OK, response);
  }

  @Operation(
      summary = "다운로드용 presigned url",
      description = "다운로드용 presignedUrl을 가져옵니다. 해당 url은 만료시간이 존재하므로 계약서, 영수증 등에 사용합니다.")
  @GetMapping("/presigned-url")
  public ResponseEntity<ApiResponse> getPresignedUrl(
      @RequestParam(name = "objectKey") String objectKey) {
    PresignedUrlResponse response = s3UploadService.getPresignedUrl(objectKey);
    return ApiResponse.onSuccess(SuccessStatus.OK, response);
  }

  @Operation(
      summary = "파일 업로드",
      description = "s3에 직접 파일을 업로드하고 public url을 반환합니다. 되도록이면 presigned url 방식을 사용해 주세요.")
  @PostMapping("/upload")
  public ResponseEntity<ApiResponse> upload(
      @RequestPart MultipartFile file,
      @RequestParam DomainType domainType,
      @RequestParam Long domainId,
      @RequestParam String filename)
      throws IOException {

    PublicUrlResponse response = s3UploadService.upload(file, domainType, domainId, filename);
    return ApiResponse.onSuccess(SuccessStatus.OK, response);
  }

  @Operation(
      summary = "public url 반환",
      description = "presigned url 로 업로드한 파일을 public url로 반환합니다. 상품 이미지, 프로필 이미지 등에 사용합니다.")
  @PostMapping("/public-url")
  public ResponseEntity<ApiResponse> getPublicUrl(@RequestBody PublicUrlRequest request) {
    PublicUrlResponse response = s3UploadService.getPublicUrl(request);
    return ApiResponse.onSuccess(SuccessStatus.OK, response);
  }

  @Operation(summary = "파일 삭제", description = "s3에서 파일을 삭제합니다.")
  @DeleteMapping
  public void delete(@RequestParam(name = "objectKey") String objectKey) {
    s3UploadService.delete(objectKey);
  }
}
