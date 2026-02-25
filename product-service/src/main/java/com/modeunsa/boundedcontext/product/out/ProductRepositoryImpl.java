package com.modeunsa.boundedcontext.product.out;

import static com.modeunsa.boundedcontext.product.domain.QProduct.product;
import static com.modeunsa.boundedcontext.product.domain.QProductMemberSeller.productMemberSeller;

import com.modeunsa.api.pagination.KeywordCursorDto;
import com.modeunsa.boundedcontext.product.domain.Product;
import com.modeunsa.boundedcontext.product.domain.ProductCategory;
import com.modeunsa.boundedcontext.product.domain.ProductPolicy;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
  public Slice<Product> searchByKeyword(String keyword, KeywordCursorDto cursor, int size) {
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

  @Override
  public Page<Product> searchByConditions(ProductCategory category, Pageable pageable) {

    List<Product> content =
        queryFactory
            .selectFrom(product)
            .where(categoryEq(category), saleStatusIn(), productStatusIn())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

    Long total =
        queryFactory
            .select(product.count())
            .from(product)
            .where(categoryEq(category), saleStatusIn(), productStatusIn())
            .fetchOne();

    return new PageImpl<>(content, pageable, total == null ? 0 : total);
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

  private BooleanExpression cursorCondition(KeywordCursorDto cursor) {
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

  private BooleanExpression categoryEq(ProductCategory category) {
    // as-is: 패션 화면에서 뷰티 카테고리 노출 안되게 설정
    // TODO: 카테고리 세분화하고 수정 필요
    return category != null
        ? product.category.eq(category)
        : product.category.notIn(ProductPolicy.BEAUTY_CATEGORIES);
  }
}
