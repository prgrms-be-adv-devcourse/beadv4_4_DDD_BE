package com.modeunsa.boundedcontext.order.domain;

import com.modeunsa.shared.order.dto.CreateCartItemRequestDto;
import com.modeunsa.shared.order.dto.CreateCartItemResponseDto;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-13T17:44:51+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.4 (Amazon.com Inc.)"
)
@Component
public class OrderMapperImpl implements OrderMapper {

    @Override
    public CartItem toCartItemEntity(long memberId, CreateCartItemRequestDto createCartItemRequestDto) {
        if ( createCartItemRequestDto == null ) {
            return null;
        }

        CartItem.CartItemBuilder cartItem = CartItem.builder();

        if ( createCartItemRequestDto != null ) {
            cartItem.productId( createCartItemRequestDto.getProductId() );
            cartItem.quantity( createCartItemRequestDto.getQuantity() );
        }
        cartItem.memberId( memberId );

        return cartItem.build();
    }

    @Override
    public CreateCartItemResponseDto toCreateCartItemResponseDto(CartItem cartItem) {
        if ( cartItem == null ) {
            return null;
        }

        CreateCartItemResponseDto.CreateCartItemResponseDtoBuilder createCartItemResponseDto = CreateCartItemResponseDto.builder();

        createCartItemResponseDto.id( cartItem.getId() );
        createCartItemResponseDto.productId( cartItem.getProductId() );
        createCartItemResponseDto.quantity( cartItem.getQuantity() );

        return createCartItemResponseDto.build();
    }
}
