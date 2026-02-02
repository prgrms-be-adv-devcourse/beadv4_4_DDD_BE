package com.modeunsa.boundedcontext.inventory.in;

import com.modeunsa.boundedcontext.inventory.app.InventoryFacade;
import com.modeunsa.global.response.ApiResponse;
import com.modeunsa.global.security.CustomUserDetails;
import com.modeunsa.global.status.SuccessStatus;
import com.modeunsa.shared.inventory.dto.InventoryUpdateRequest;
import com.modeunsa.shared.inventory.dto.InventoryUpdateResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ApiV1InventoryController {
  private final InventoryFacade inventoryFacade;

  @Operation(summary = "실재고 수정", description = "판매자가 상품 실재고를 수정합니다.")
  @PatchMapping("/{productId}")
  public ResponseEntity<ApiResponse> updateInventory(
      @AuthenticationPrincipal CustomUserDetails user,
      Long productId,
      @Valid @RequestBody InventoryUpdateRequest inventoryUpdateRequest) {

    // TODO: CustomUserDetails에서 sellerId가져오기
    Long sellerId = 1L;
    InventoryUpdateResponse response =
        inventoryFacade.updateInventory(sellerId, productId, inventoryUpdateRequest);

    return ApiResponse.onSuccess(SuccessStatus.OK, response);
  }
}
