-- 인덱스 추가: member_id와 created_at을 기준으로 내림차순 정렬하여 인덱스 생성
CREATE INDEX idx_payment_member_created_at
    ON payment_payment (member_id, created_at DESC)
;