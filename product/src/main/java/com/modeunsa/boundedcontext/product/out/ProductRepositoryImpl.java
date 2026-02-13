package com.modeunsa.boundedcontext.product.out;

import static com.modeunsa.boundedcontext.product.domain.QProduct.product;
import static com.modeunsa.boundedcontext.product.domain.QProductMemberSeller.productMemberSeller;

import com.modeunsa.boundedcontext.product.domain.Product;
import com.modeunsa.boundedcontext.product.domain.ProductCategory;
import com.modeunsa.boundedcontext.product.domain.ProductPolicy;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {

  private static final PathBuilder<Product> PRODUCT = new PathBuilder<>(Product.class, "product");
  private final JPAQueryFactory queryFactory;

  @Override
  public Page<Product> searchByKeyword(String keyword, Pageable pageable) {
    List<Product> content =
        queryFactory
            .selectFrom(product)
            .leftJoin(product.seller, productMemberSeller)
            .where(keywordCondition(keyword), saleStatusIn(), productStatusIn())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .orderBy(getOrderSpecifiers(pageable))
            .fetch();

    Long total =
        queryFactory
            .select(product.count())
            .from(product)
            .leftJoin(product.seller, productMemberSeller)
            .where(keywordCondition(keyword), saleStatusIn(), productStatusIn())
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
