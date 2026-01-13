package com.modeunsa.boundedcontext.order.domain;

import com.modeunsa.shared.order.dto.CreateCartItemRequestDto;
import com.modeunsa.shared.order.dto.CreateCartItemResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {
  // TODO: SecurityContext에서 memberId 추출해서 Auditing으로 createdBy필드 채우기
  @Mapping(target = "isAvailable", ignore = true)
  CartItem toCartItemEntity(long memberId, CreateCartItemRequestDto createCartItemRequestDto);

  CreateCartItemResponseDto toCreateCartItemResponseDto(CartItem cartItem);
}
