export default function FailurePage() {
  return (
    <main className="failure-page">
      {/* 실패 아이콘 */}
      <div className="failure-icon">
        <svg width="64" height="64" viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
          <circle cx="32" cy="32" r="32" fill="#F44336"/>
          <path d="M20 20L44 44M44 20L20 44" stroke="white" strokeWidth="4" strokeLinecap="round" strokeLinejoin="round"/>
        </svg>
      </div>

      {/* 메인 메시지 */}
      <div className="failure-messages">
        <h1 className="failure-title">결제에 실패했습니다</h1>
        <p className="failure-subtitle">결제 처리 중 오류가 발생했습니다. 다시 시도해주세요.</p>
      </div>

      {/* 오류 정보 카드 */}
      <div className="error-info-card">
        <h2 className="card-title">오류 정보</h2>
        <div className="error-content">
          <div className="error-message">카드 한도 초과 또는 잔액 부족</div>
          <div className="error-instruction">결제 수단을 확인하고 다시 시도해주세요.</div>
        </div>
      </div>

      {/* 주문 정보 카드 */}
      <div className="order-info-card">
        <h2 className="card-title">주문 정보</h2>
        <div className="order-details">
          <div className="detail-row">
            <span className="detail-label">주문번호</span>
            <span className="detail-value">ORD-20240110-001234</span>
          </div>
          <div className="detail-row">
            <span className="detail-label">시도일시</span>
            <span className="detail-value">2024.01.10 16:30:25</span>
          </div>
          <div className="detail-row">
            <span className="detail-label">결제수단</span>
            <span className="detail-value">토스페이</span>
          </div>
          <div className="detail-row">
            <span className="detail-label">주문금액</span>
            <span className="detail-value order-amount">19,800원</span>
          </div>
        </div>
      </div>
    </main>
  )
}
