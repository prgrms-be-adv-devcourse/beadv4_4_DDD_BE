-- 인덱스 추가: 계정별 입출금 목록 조회(account_id + created_at 기간 + ORDER BY created_at DESC)용
CREATE INDEX idx_payment_account_id_created_at
    ON payment_account_log (account_id, created_at DESC)
;