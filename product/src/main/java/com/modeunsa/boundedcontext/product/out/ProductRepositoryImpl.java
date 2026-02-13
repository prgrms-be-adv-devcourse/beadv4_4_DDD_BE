package com.modeunsa.boundedcontext.product.out;

import static com.modeunsa.boundedcontext.product.domain.QProduct.product;
import static com.modeunsa.boundedcontext.product.domain.QProductMemberSeller.productMemberSeller;

import com.modeunsa.boundedcontext.product.domain.Product;
import com.modeunsa.boundedcontext.product.domain.ProductCategory;
import com.modeunsa.boundedcontext.product.domain.ProductPolicy;
import com.modeunsa.boundedcontext.product.in.dto.ProductCursorDto;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {

  private static final PathBuilder<Product> PRODUCT = new PathBuilder<>(Product.class, "product");
  private final JPAQueryFactory queryFactory;

  @Override
  public Slice<Product> searchByKeyword(String keyword, ProductCursorDto cursor, int size) {
    List<Product> content =
        queryFactory
            .selectFrom(product)
            .leftJoin(product.seller, productMemberSeller)
            .where(
                keywordCondition(keyword),
                saleStatusIn(),
                productStatusIn(),
                cursorCondition(cursor))
            .orderBy(product.createdAt.desc(), product.id.desc()) // 복합 정렬
            .limit(size + 1) // 다음 페이지 존재 여부 확인
            .fetch();

    boolean hasNext = false;
    if (content.size() > size) {
      hasNext = true;
      content.remove(size);
    }

    return new SliceImpl<>(content, PageRequest.of(0, size), hasNext);
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

  private BooleanExpression cursorCondition(ProductCursorDto cursor) {
    if (cursor == null) {
      return null;
    }
    return product
        .createdAt
        .lt(cursor.createdAt())
        .or(product.createdAt.eq(cursor.createdAt()).and(product.id.lt(cursor.id())));
  }

  private OrderSpecifier<?>[] getOrderSpecifiers(Pageable pageable) {
    return pageable.getSort().stream()
        .map(
            order -> {
              return new OrderSpecifier(
                  order.isAscending() ? Order.ASC : Order.DESC, PRODUCT.get(order.getProperty()));
            })
        .toArray(OrderSpecifier[]::new);
  }
}
