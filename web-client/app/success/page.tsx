export default function SuccessPage() {
  return (
    <main className="success-page">
      {/* 성공 아이콘 */}
      <div className="success-icon">
        <svg width="64" height="64" viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
          <circle cx="32" cy="32" r="32" fill="#4CAF50"/>
          <path d="M20 32L28 40L44 24" stroke="white" strokeWidth="4" strokeLinecap="round" strokeLinejoin="round"/>
        </svg>
      </div>

      {/* 메인 메시지 */}
      <div className="success-messages">
        <h1 className="success-title">결제가 완료되었습니다</h1>
        <p className="success-subtitle">주문이 정상적으로 처리되었습니다.</p>
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
            <span className="detail-label">결제일시</span>
            <span className="detail-value">2024.01.10 16:30:25</span>
          </div>
          <div className="detail-row">
            <span className="detail-label">결제수단</span>
            <span className="detail-value">토스페이</span>
          </div>
          <div className="detail-row">
            <span className="detail-label">결제금액</span>
            <span className="detail-value">19,800원</span>
          </div>
        </div>
      </div>
    </main>
  )
}
