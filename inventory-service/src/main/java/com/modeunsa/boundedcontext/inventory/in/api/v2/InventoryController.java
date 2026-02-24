package com.modeunsa.boundedcontext.inventory.in.api.v2;

import com.modeunsa.boundedcontext.inventory.app.common.InventoryFacade;
import com.modeunsa.global.response.ApiResponse;
import com.modeunsa.global.security.CustomUserDetails;
import com.modeunsa.global.status.SuccessStatus;
import com.modeunsa.shared.inventory.dto.InventoryDto;
import com.modeunsa.shared.inventory.dto.InventoryInitializeRequest;
import com.modeunsa.shared.inventory.dto.InventoryListRequest;
import com.modeunsa.shared.inventory.dto.InventoryListResponse;
import com.modeunsa.shared.inventory.dto.InventoryReserveRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/inventories")
@Tag(name = "Inventory", description = "재고 도메인 API")
public class InventoryController {
  private final InventoryFacade inventoryFacade;

  /* TODO: 재고 수정 로직 분리
  @Operation(summary = "실재고 수정", description = "판매자가 상품 실재고를 수정합니다.")
  @PatchMapping("/{productId}")
  public ResponseEntity<ApiResponse> updateInventory(
      @AuthenticationPrincipal CustomUserDetails user,
      Long productId,
      @Valid @RequestBody InventoryUpdateRequest inventoryUpdateRequest) {

    InventoryUpdateResponse response =
        inventoryFacade.updateInventory(user.getSellerId(), productId, inventoryUpdateRequest);

    return ApiResponse.onSuccess(SuccessStatus.OK, response);
  }
   */

  @Operation(summary = "실재고 조회", description = "내부모듈에서 사용하는 상품별 실재고 조회 기능입니다.")
  @GetMapping("/internal/{productId}")
  public InventoryDto getInternalInventory(@PathVariable Long productId) {
    return inventoryFacade.getInventory(productId);
  }

  @Operation(summary = "예약재고 수정", description = "(내부 모듈) 회원이 주문한 상품의 예약재고를 수정합니다.")
  @PostMapping("/internal/reserve")
  public void reserveInventory(@Valid @RequestBody InventoryReserveRequest request) {
    inventoryFacade.reserveInventory(request);
  }

  @Operation(summary = "실재고 리스트 조회", description = "판매자가 판매하는 모든 상품 실재고 조회 기능입니다.")
  @PostMapping()
  public ResponseEntity<ApiResponse> getInventories(
      @AuthenticationPrincipal CustomUserDetails user,
      @Valid @RequestBody InventoryListRequest request) {
    InventoryListResponse dto = inventoryFacade.getInventories(user.getSellerId(), request);
    return ApiResponse.onSuccess(SuccessStatus.OK, dto);
  }

  @Operation(summary = "실재고 등록", description = "판매자가 상품의 실재고를 등록합니다.")
  @PostMapping("/{productId}")
  public ResponseEntity<ApiResponse> initializeInventory(
      @AuthenticationPrincipal CustomUserDetails user,
      @PathVariable Long productId,
      @Valid @RequestBody InventoryInitializeRequest request) {
    inventoryFacade.initializeInventory(user.getSellerId(), productId, request);
    return ApiResponse.onSuccess(SuccessStatus.OK);
  }
}
