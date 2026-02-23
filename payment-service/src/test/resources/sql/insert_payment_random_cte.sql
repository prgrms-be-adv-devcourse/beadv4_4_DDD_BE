-- MySQL 8+: WITH RECURSIVE로 결제 목록 테스트용 N건 INSERT
-- order_no, order_id UNIQUE 전제: 매 실행마다 run_id를 섞어서 중복 방지

SET @member_id = 10;     -- 조회할 회원 ID
SET @max_n = 900;        -- 삽입 건수

-- 실행 배치 식별자(마이크로초 포함). 매 실행마다 값이 달라짐
SET @run_id = DATE_FORMAT(NOW(6), '%Y%m%d%H%i%s%f');  -- 예: 20260223112345123456 (20자리)

INSERT INTO payment_payment (
    member_id,
    order_no,
    status,
    payment_purpose,
    order_id,
    total_amount,
    need_pg_payment,
    payment_deadline_at,
    payment_provider,
    pg_order_name,
    created_at,
    updated_at
)
WITH RECURSIVE numbers AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM numbers WHERE n < @max_n
)
SELECT
    @member_id,

    CONCAT('ORD-RND-', @member_id, '-', @run_id, '-', n) AS order_no,

    ELT(1 + MOD(n, 6), 'SUCCESS', 'SUCCESS', 'CANCELED', 'REFUNDED', 'PENDING', 'FAILED') AS status,

    CASE MOD(n, 5) WHEN 0 THEN 'DEPOSIT_CHARGE' ELSE 'PRODUCT_PURCHASE' END AS payment_purpose,

    -- 예: run_id가 20자리면 뒤 12자리만 쓰고 n을 붙여서 최대 18~19자리로 유지
    CAST(CONCAT(RIGHT(@run_id, 12), LPAD(n, 6, '0')) AS UNSIGNED) AS order_id,

    FLOOR(1000 + (RAND() * 99000)) AS total_amount,

    0 AS need_pg_payment,

    DATE_ADD(NOW(), INTERVAL 1 DAY) AS payment_deadline_at,

    CASE MOD(n, 2) WHEN 0 THEN 'MODEUNSA_PAY' ELSE 'TOSS_PAYMENTS' END AS payment_provider,

    ELT(1 + MOD(n, 8),
        CONCAT('테스트상품 ', n),
        CONCAT('상품A 외 ', 1 + MOD(n, 5), '건'),
        '예치금 충전',
        CONCAT('주문번호 ORD-RND-', @member_id, '-', @run_id, '-', n),
        '단일 상품 결제',
        CONCAT('뭐든사 상품 패키지 ', n),
        CONCAT('토스 결제 테스트 ', MOD(n, 100)),
        '기타 결제'
    ) AS pg_order_name,

    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 90) DAY) AS created_at,

    NOW() AS updated_at
FROM numbers;