package com.modeunsa.shared.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class SyncCartItemResponseDto {
  private long id;
  private long memberId;
  private long productId;
  private int quantity;
}
