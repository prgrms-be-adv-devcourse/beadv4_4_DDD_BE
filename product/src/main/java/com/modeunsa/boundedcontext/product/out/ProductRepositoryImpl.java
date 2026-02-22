package com.modeunsa.boundedcontext.product.out;

import static com.modeunsa.boundedcontext.product.domain.QProduct.product;
import static com.modeunsa.boundedcontext.product.domain.QProductMemberSeller.productMemberSeller;

import com.modeunsa.api.pagination.CursorDto;
import com.modeunsa.boundedcontext.product.domain.Product;
import com.modeunsa.boundedcontext.product.domain.ProductCategory;
import com.modeunsa.boundedcontext.product.domain.ProductPolicy;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public Slice<Product> searchByKeyword(String keyword, CursorDto cursor, int size) {
    List<Product> content =
        queryFactory
            .selectFrom(product)
            .leftJoin(product.seller, productMemberSeller)
            .where(
                keywordCondition(keyword),
                saleStatusIn(),
                productStatusIn(),
                cursorCondition(cursor))
            .orderBy(product.createdAt.desc(), product.id.desc()) // cursor 기반으로 정렬 고정
            .limit(size + 1) // 다음 페이지 존재 여부 확인
            .fetch();

    boolean hasNext = content.size() > size;
    if (hasNext) {
      content = content.subList(0, size);
    }

    return new SliceImpl<>(content, Pageable.unpaged(), hasNext);
  }

  private BooleanExpression keywordCondition(String keyword) {
    if (!StringUtils.hasText(keyword)) {
      return null;
    }

    // 상품 기본 검색
    BooleanExpression nameOrDescriptionCondition =
        product
            .name
            .containsIgnoreCase(keyword)
            .or(product.description.containsIgnoreCase(keyword));

    List<ProductCategory> matchedCategories = ProductCategory.fromDescriptionKeyword(keyword);
    BooleanExpression categoryCondition =
        matchedCategories.isEmpty() ? null : product.category.in(matchedCategories);

    // 판매자명 검색
    BooleanExpression sellerCondition =
        productMemberSeller.businessName.containsIgnoreCase(keyword);

    return nameOrDescriptionCondition.or(categoryCondition).or(sellerCondition);
  }

  private BooleanExpression saleStatusIn() {
    return product.saleStatus.in(ProductPolicy.ORDERABLE_SALE_STATUES);
  }

  private BooleanExpression productStatusIn() {
    return product.productStatus.in(ProductPolicy.ORDERABLE_PRODUCT_STATUES);
  }

  private BooleanExpression cursorCondition(CursorDto cursor) {
    if (cursor == null) {
      // 첫 조회 시 null
      return null;
    }
    return product
        .createdAt
        .lt((LocalDateTime) cursor.createdAt())
        .or(
            product
                .createdAt
                .eq((LocalDateTime) cursor.createdAt())
                .and(product.id.lt(cursor.id())));
  }
}
