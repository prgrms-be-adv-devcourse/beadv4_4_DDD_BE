# ElasticSearch
* Product 내부에서 검새 기능 구현
* app, in, out, domain 내에 각각 search 패키지 만들어 그 안에서 클래스 구현
`

## 각 클래스별 역할
### ProductSsearchFacade, ProductSearchUseCase
* findAll > 상품 전체 리스트 조회
* findById > 상품 단건 조회
* search > 검색 조회
* 이렇게 분류하여 만들었는데 findById는 굳이 필요없을 것 같습니다.

### ProductSearch
* 검색 조회했을 때 
* id, name, description, category, saleStatus, price, createAt, updateAt
* 가 조회되도록 설정하였습니다.

### ProductSearchController
* create는 상품이 등록되었을 때, ES에도 같이 등록시키기 위해 만든 메서드입니다.
```text
@Operation(summary = "ES 상품 등록", description = "상품을 등록하면 ElasticSearch에도 save된다.")
  @PostMapping
  public ProductSearch create(@RequestBody ProductSearchRequest request) {
    return productSearchUseCase.createproductSearch(
        request.name(),
        request.description(),
        request.category(),
        request.saleStatus(),
        request.price());
  }
```

* 마찬가지로 여기서도 findById는 필요없을 것 같습니다. 추후에 자유롭게 지우셔도 될 것 같습니다.

### productSearchRequest, Response
* ES에서 받기 원하는 정보는 
* id, name, description, category, saleStatus, price
* 입니다. 
* 그러나 ProductDto엔 description, category, saleStatus가 없습니다.
* 그래서 상품이 등록되었을 때 자동으로 ES에도 등록시키는 동기화 과정에 오류가 생겼습니다. (ProductSearchEventListener)
* 이 부분은 ProductDto와 ProductSearchRequest, Response의 객체를 똑같이 맞춰주시면 될 것 같습니다. 

### 현재 검색 기능 상황
* Product와 ProductSearch의 동기화 과정에서 문제가 생기는 바람에 Datainit 8개 중 4개밖에 ES로 들어오지 못했습니다.
* 들어온 4개의 정보는 검색하였을 때 정상적으로 잘 검색 조회가 됩니다. (pr에 캡처본 올려두었습니다.)
* ProductDto와 ProductSearchRequest, Response의 객체를 잘 맞춰주시고,
* ProductSearchEventListener 만 다시 수정해주시면 될 것 같습니다.