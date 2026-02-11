# Makefile
# 인프라 기동: docker-compose up -d influxdb grafana
# 대시보드: http://localhost:3000 (Grafana → Explore 또는 k6 대시보드 임포트)

PAYMENT_SCRIPT=scripts/payment/payment_request.js
PAYMENT_MEMBER_SCRIPT=scripts/payment/payment_member.js
PRODUCT_SEARCH_SCRIPT=scripts/product/product_search.js
K6_OUT = --out influxdb=http://influxdb:8086/k6

.PHONY: test-payment-request test-payment-member test-product-search up down
up:
	docker-compose up -d influxdb grafana

down:
	docker-compose down

test-payment-request:
	docker-compose run --rm k6 run $(K6_OUT) /$(PAYMENT_SCRIPT)

test-payment-member:
	docker-compose run --rm k6 run $(K6_OUT) /$(PAYMENT_MEMBER_SCRIPT)

test-product-search:
	docker-compose run --rm k6 run $(K6_OUT) /$(PRODUCT_SEARCH_SCRIPT)

