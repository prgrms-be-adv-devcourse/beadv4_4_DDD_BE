'use client'

import Link from 'next/link'
import { useSearchParams } from 'next/navigation'

export default function FailurePage() {
  const searchParams = useSearchParams()
  const orderNo = searchParams.get('orderNo') || 'N/A'
  const amount = searchParams.get('amount')
  
  // 시도일시는 현재 시간으로 표시
  const attemptDate = new Date().toLocaleString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
  
  // 금액 포맷팅
  const formattedAmount = amount
    ? new Intl.NumberFormat('ko-KR').format(parseFloat(amount)) + '원'
    : 'N/A'

  return (
    <div className="home-page">
      <header className="header">
        <div className="header-container">
          <div className="logo">
            <Link href="/">뭐든사</Link>
          </div>
          <nav className="nav">
            <Link href="/fashion">패션</Link>
            <Link href="/beauty">뷰티</Link>
            <Link href="/sale">세일</Link>
          </nav>
          <div className="header-actions">
            <button className="search-btn">검색</button>
            <button className="cart-btn">장바구니</button>
            <button className="user-btn">로그인</button>
          </div>
        </div>
      </header>

      <div className="order-page-container">
        <div className="container" style={{ maxWidth: '600px' }}>
        <div style={{ textAlign: 'center', marginBottom: '40px' }}>
          <div className="failure-icon" style={{ marginBottom: '24px' }}>
            <svg width="64" height="64" viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg" style={{ margin: '0 auto' }}>
              <circle cx="32" cy="32" r="32" fill="#F44336"/>
              <path d="M20 20L44 44M44 20L20 44" stroke="white" strokeWidth="4" strokeLinecap="round" strokeLinejoin="round"/>
            </svg>
          </div>
          <div className="failure-messages">
            <h1 className="failure-title">결제에 실패했습니다</h1>
            <p className="failure-subtitle">결제 처리 중 오류가 발생했습니다. 다시 시도해주세요.</p>
          </div>
        </div>

        <div className="order-card">
          <h2 className="card-title">오류 정보</h2>
          <div className="error-content">
            <div className="error-message">결제 승인 처리 중 오류가 발생했습니다.</div>
            <div className="error-instruction">결제 수단을 확인하고 다시 시도해주세요.</div>
          </div>
        </div>

        <div className="order-card">
          <h2 className="card-title">주문 정보</h2>
          <div className="order-details">
            <div className="detail-row">
              <span className="detail-label">주문번호</span>
              <span className="detail-value">{orderNo}</span>
            </div>
            <div className="detail-row">
              <span className="detail-label">시도일시</span>
              <span className="detail-value">{attemptDate}</span>
            </div>
            <div className="detail-row">
              <span className="detail-label">결제수단</span>
              <span className="detail-value">토스페이</span>
            </div>
            <div className="detail-row">
              <span className="detail-label">주문금액</span>
              <span className="detail-value">{formattedAmount}</span>
            </div>
          </div>
        </div>

        <div style={{ textAlign: 'center', marginTop: '32px', display: 'flex', gap: '12px', justifyContent: 'center' }}>
          <Link href="/order" className="order-payment-button" style={{ display: 'inline-block', maxWidth: '200px', textAlign: 'center' }}>
            다시 시도
          </Link>
          <Link href="/" className="order-payment-button" style={{ display: 'inline-block', maxWidth: '200px', textAlign: 'center', background: '#666' }}>
            홈으로
          </Link>
        </div>
        </div>
      </div>

      <footer className="footer">
        <div className="container">
          <div className="footer-content">
            <div className="footer-section">
              <h3>고객센터</h3>
              <p>1588-0000</p>
              <p>평일 09:00 - 18:00</p>
            </div>
            <div className="footer-section">
              <h3>회사정보</h3>
              <p>주소: 서울시 강남구</p>
              <p>사업자등록번호: 000-00-00000</p>
            </div>
            <div className="footer-section">
              <h3>이용안내</h3>
              <Link href="/terms">이용약관</Link>
              <Link href="/privacy">개인정보처리방침</Link>
            </div>
          </div>
          <div className="footer-bottom">
            <p>&copy; 2024 뭐든사. All rights reserved.</p>
          </div>
        </div>
      </footer>
    </div>
  )
}
