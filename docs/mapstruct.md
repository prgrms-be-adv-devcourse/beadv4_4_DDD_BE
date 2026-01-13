# mapstruct
* mapper 를 자동으로 생성해주는 라이브러리

### 장점
* 컴파일 시점에 구현 클래스(mapperImpl)를 생성하여 빠름
* 매핑 누락 컴파일 에러로 잡을 수 있음

### 동작 방식
* @Mapper 인터페이스 생성
* 구현 클래스 컴파일 시점에 생성 (ex. ProductMapperImpl)
* 필드명 같으면 자동 매핑
* 필드명 다른 경우 @Mapping으로 규칙 지정 필요

### 의존성 추가
```groovy
    // mapper
    implementation 'org.modelmapper:modelmapper:3.1.1'

    // mapstruct
    implementation 'org.mapstruct:mapstruct:1.5.5.Final'
    annotationProcessor "org.projectlombok:lombok-mapstruct-binding:0.2.0"
    annotationProcessor 'org.mapstruct:mapstruct-processor:1.5.5.Final'
    testAnnotationProcessor 'org.mapstruct:mapstruct-processor:1.5.5.Final'

```

### 클래스
* mapper 클래스/인터페이스에 규칙 지정
```java
@Mapper(componentModel = "spring")
public interface ProductMapper {
  @Mapping(target = "productStatus", defaultValue = "DRAFT")
  @Mapping(target = "saleStatus", defaultValue = "NOT_SALE")
  @Mapping(target = "currency", defaultValue = "KRW")
  Product toEntity(ProductDto productDto);
}
```

* 구현 클래스 자동 생성
```java
@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-13T14:17:54+0900",
    comments = "version: 1.5.5.Final, compiler: IncrementalProcessingEnvironment from gradle-language-java-9.2.1.jar, environment: Java 21.0.9 (Amazon.com Inc.)"
)
@Component
public class ProductMapperImpl implements ProductMapper {

    @Override
    public Product toEntity(ProductDto productDto) {
        if ( productDto == null ) {
            return null;
        }

        Product.ProductBuilder product = Product.builder();

        if ( productDto.getProductStatus() != null ) {
            product.productStatus( productDto.getProductStatus() );
        }
        else {
            product.productStatus( ProductStatus.DRAFT );
        }
        if ( productDto.getSaleStatus() != null ) {
            product.saleStatus( productDto.getSaleStatus() );
        }
        else {
            product.saleStatus( SaleStatus.NOT_SALE );
        }
        if ( productDto.getCurrency() != null ) {
            product.currency( productDto.getCurrency() );
        }
        else {
            product.currency( ProductCurrency.KRW );
        }
        product.sellerId( productDto.getSellerId() );
        product.name( productDto.getName() );
        product.category( productDto.getCategory() );
        product.description( productDto.getDescription() );
        product.salePrice( productDto.getSalePrice() );
        product.price( productDto.getPrice() );
        product.qty( productDto.getQty() );

        return product.build();
    }
}
```
* dto <-> entity 변환 필요한 곳에 다음과 같이 사용
```java
  public Product createProduct(ProductDto productDto) {
    Product product = productMapper.toEntity(productDto);
    return productRepository.save(product);
  }
```
