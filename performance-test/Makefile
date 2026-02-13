# Makefile
# 인프라 기동: docker-compose up -d influxdb grafana
# 대시보드: http://localhost:3000 (Grafana → Explore 또는 k6 대시보드 임포트)

PAYMENT_SCRIPT=scripts/payment/payment_request.js
PAYMENT_MEMBER_SCRIPT=scripts/payment/payment_member.js
PRODUCT_SEARCH_SCRIPT=scripts/product/product_search.js
ORDER_SCRIPT=scripts/order/product_detail_and_order.js
ORDER_NOT_RETRY_SCRIPT=scripts/order/not_retry_optimistic_product_detail_and_order.js

# .env 파일에서 TARGET_URL 읽기 (없으면 기본값)
-include .env
TARGET_URL?=http://host.docker.internal:8080

# arrival 모드 기본 값 (원하면 make 호출 시 ARRIVAL_RATE=... 로 override)
ARRIVAL_RATE?=100
ARRIVAL_DURATION?=5m
# step 모드에서 목표 RPS (대략)
TARGET_RPS?=200

K6_OUT = --out influxdb=http://influxdb:8086/k6

.PHONY: test-payment-request test-payment-member test-product-search test-product-search-arrival test-product-search-step test-product-detail-and-order test-order-not-retry up down
up:
	docker-compose up -d influxdb grafana

down:
	docker-compose down

test-payment-request:
	docker-compose run --rm k6 run $(K6_OUT) /$(PAYMENT_SCRIPT)

test-payment-member:
	docker-compose run --rm k6 run $(K6_OUT) /$(PAYMENT_MEMBER_SCRIPT)

# order: 상품 상세 조회 → 주문 생성 (점차 동시 주문 증가)
test-product-detail-and-order:
	docker-compose run --rm \
		-e TARGET_URL=$(TARGET_URL) \
		-e MEMBER_ID=$(MEMBER_ID) \
		-e PRODUCT_ID=$(PRODUCT_ID) \
		-e PEAK_VUS=$(PEAK_VUS) \
		k6 run $(K6_OUT) /$(ORDER_SCRIPT)

# order: 낙관적 락 미재시도 버전
test-order-not-retry:
	docker-compose run --rm \
		-e TARGET_URL=$(TARGET_URL) \
		-e MEMBER_ID=$(MEMBER_ID) \
		-e PRODUCT_ID=$(PRODUCT_ID) \
		-e PEAK_VUS=$(PEAK_VUS) \
		k6 run $(K6_OUT) /$(ORDER_NOT_RETRY_SCRIPT)

test-product-search:
	docker-compose run --rm k6 run $(K6_OUT) /$(PRODUCT_SEARCH_SCRIPT)

test-product-search-arrival:
	docker-compose run --rm \
		-e SCENARIO=arrival \
		-e ARRIVAL_RATE=$(ARRIVAL_RATE) \
		-e ARRIVAL_DURATION=$(ARRIVAL_DURATION) \
		k6 run $(K6_OUT) /$(PRODUCT_SEARCH_SCRIPT)

test-product-search-step:
	docker-compose run --rm \
		-e SCENARIO=step \
		-e TARGET_RPS=$(TARGET_RPS) \
		k6 run $(K6_OUT) /$(PRODUCT_SEARCH_SCRIPT)

# product_search.js - 고정 VU(closed-loop) 시나리오
test-product-search-constant:
	docker-compose run --rm \
		-e SCENARIO=constant \
		k6 run $(K6_OUT) /$(PRODUCT_SEARCH_SCRIPT)

test-product-search-constant-150:
	docker-compose run --rm \
		-e SCENARIO=constant \
		-e CONSTANT_VUS=180 \
		-e CONSTANT_DURATION=5m \
		k6 run $(K6_OUT) /$(PRODUCT_SEARCH_SCRIPT)

test-product-search-constant-50:
	docker-compose run --rm \
		-e SCENARIO=constant \
		-e CONSTANT_VUS=60 \
		-e CONSTANT_DURATION=5m \
		k6 run $(K6_OUT) /$(PRODUCT_SEARCH_SCRIPT)

test-product-search-constant-200:
	docker-compose run --rm \
		-e SCENARIO=constant \
		-e CONSTANT_VUS=240 \
		-e CONSTANT_DURATION=5m \
		k6 run $(K6_OUT) /$(PRODUCT_SEARCH_SCRIPT)