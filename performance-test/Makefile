# Makefile

# k6 스크립트 경로 설정
PAYMENT_SCRIPT=scripts/payment/payment_request.js

# k6 테스트 실행
.PHONY: test-payment-request
test-payment-request:
	docker-compose run --rm k6 run /$(PAYMENT_SCRIPT)

