# ElasticSearch
* ElasticSearch의 공통 인프라 구조와 사용 방식
* global 레이어의 기반 구조 정의
* 검색의 의미(무엇을, 어떻게 검색할지)는 각 Bounded Context에서 정의

### 디렉토리 구조
```text
global
└─ elasticsearch
├─ app
│   ├─ ElasticSearchExecutor
│   └─ ElasticSearchExecutorImpl
│
├─ model            ← (기존 domain에서 이름만 변경)
│   ├─ ElasticSearchHit
│   └─ ElasticSearchPage
│
├─ sort
│   ├─ ElasticSearchSort
│   └─ ElasticSearchSortOrder
│
└─ ElasticSearchPageRequest
└─ IndexName
```
* global.elasticsearch는 ElasticSearch와의 연결 및 실행 흐름만을 담당
* Content, Product 등 도메인별 검색 로직은 해당 Bounded Context 내부에서 구현
* Content에서 먼저 구현 후 Product에서 따라 구현하면 될 것 같습니다. 

# 각 클래스별 역할

## app
### ElasticSearchExecutor
* ElasticSearch 사용을 위한 공통 인터페이스
* 도메인 레이어가 ElasticSearch Client에 직접 의존하지 않도록 추상화 역할 수행
* 검색, 색인, 삭제 등 ElasticSearch의 핵심 동작을 포트(Port)로 제공
```java
public interface ElasticSearchExecutor {
    <T> ElasticSearchPage<T> search(
        IndexName indexName,
        Query query,
        ElasticSearchPageRequest pageRequest,
        Class<T> clazz
    );
}
```
 
### ElasticSearchExecutorImpl
* ElasticSearchExecutor의 실제 구현체
* 페이징, 정렬 적용 및 예외 처리 담당
* ElasticSearch Java Client를 사용해 SearchRequest 생성 및 실행

## model
### ElasticSearchHit
* ElasticSearch 검색 결과 단건을 표현하는 모델
* document id, score, 실제 document(source)를 포함

### ElasticSearchPage
* ElasticSearch 검색 결과 목록을 페이지 단위로 감싸는 모델
* 전체 검색 결과 수(total)와 현재 페이지의 hit 목록을 포함
* 도메인별 검색 결과 변환의 기준 모델로 사용

## sort
### ElasticSearchSort
* 검색 결과 정렬 조건을 표현하는 모델
* 정렬 대상 필드와 정렬 방향을 함께 정의

### ElasticSearchSortOrder
* 정렬 방향(ASC, DESC)을 표현하는 enum

## none

### IndexName
* ElasticSearch 인덱스 이름을 중앙에서 관리하기 위한 enum

### ElasticSearchPageRequest
* ElasticSearch 검색 시 사용하는 공통 페이징 요청 객체

