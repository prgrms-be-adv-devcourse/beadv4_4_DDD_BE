'use client'

import Link from 'next/link'
import { useSearchParams, useRouter } from 'next/navigation'
import { useEffect, useRef, useState } from 'react'

interface ConfirmPaymentResponse {
  orderNo: string
}

interface ApiResponse {
  isSuccess: boolean
  code: string
  message: string
  result: ConfirmPaymentResponse
}

export default function SuccessPage() {
  const searchParams = useSearchParams()
  const router = useRouter()
  const [isConfirming, setIsConfirming] = useState(true)
  const [confirmError, setConfirmError] = useState<string | null>(null)
  const [paymentInfo, setPaymentInfo] = useState<ConfirmPaymentResponse | null>(null)
  const hasCalledRef = useRef(false)

  useEffect(() => {
    // 중복 호출 방지
    if (hasCalledRef.current) {
      console.warn('[결제 승인] 이미 호출된 요청입니다. 중복 호출을 방지합니다.')
      return
    }

    const confirmPayment = async () => {
      hasCalledRef.current = true
      console.log('[결제 승인] 결제 승인 요청 시작')
      // 1. 쿼리 파라미터 추출
      const orderNo = searchParams.get('orderNo')
      const orderId = searchParams.get('orderId')
      const paymentKey = searchParams.get('paymentKey')
      const amount = searchParams.get('amount')
      const memberId = searchParams.get('memberId')
      const pgCustomerName = searchParams.get('pgCustomerName')
      const pgCustomerEmail = searchParams.get('pgCustomerEmail')

      // 1-1. 토스 결제 모듈을 거치지 않은 내부 결제(잔액 결제 등) 케이스
      if (!paymentKey && !orderId) {
        if (!orderNo || !amount) {
          console.error('[결제 승인] 내부 결제 케이스에서 필수 파라미터 누락', {
            orderNo,
            amount,
          })
          router.push(`/order/failure?orderNo=${orderNo || ''}&amount=${amount || ''}`)
          return
        }

        console.log('[결제 승인] 토스 모듈 없이 내부 결제 성공 케이스로 처리', {
          orderNo,
          amount,
        })
        setPaymentInfo({ orderNo })
        setConfirmError(null)
        setIsConfirming(false)
        return
      }

      // 1-2. 토스 결제 케이스: 필수 파라미터 검증
      if (!orderNo || !orderId || !paymentKey || !amount || !memberId || !pgCustomerName || !pgCustomerEmail) {
        console.error('[결제 승인] 토스 결제 케이스에서 필수 파라미터 누락', {
          orderNo,
          orderId,
          paymentKey,
          amount,
          memberId,
          pgCustomerName,
          pgCustomerEmail,
        })
        router.push(`/order/failure?orderNo=${orderNo || ''}&amount=${amount || ''}`)
        return
      }

      console.log('[결제 승인] 토스 결제 승인 API 호출 시작', {
        orderNo,
        orderId,
        paymentKey,
        amount,
        memberId,
        pgCustomerName,
        pgCustomerEmail,
      })

      try {
        // 2. API 호출
        const apiUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'
        const requestBody = {
          memberId: parseInt(memberId, 10),
          paymentKey: paymentKey,
          orderId: orderId,
          amount: parseInt(amount, 10),
          pgCustomerName: pgCustomerName,
          pgCustomerEmail: pgCustomerEmail,
        }
        console.log('[결제 승인] API 요청', { url: `${apiUrl}/api/v1/payments/${orderNo}/payment/confirm/by/tossPayments`, body: requestBody })
        
        const response = await fetch(
          `${apiUrl}/api/v1/payments/${orderNo}/payment/confirm/by/tossPayments`,
          {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
            },
            body: JSON.stringify(requestBody),
          }
        )

        console.log('[결제 승인] API 응답 상태', { status: response.status, ok: response.ok })

        if (!response.ok) {
          const errorText = await response.text()
          console.error('[결제 승인] API 에러', { status: response.status, errorText, orderNo, orderId, paymentKey })
          router.push(`/order/failure?orderNo=${orderNo}&amount=${amount}`)
          return
        }

        // 3. 응답 처리
        const apiResponse: ApiResponse = await response.json()
        console.log('[결제 승인] API 응답', { apiResponse, orderNo })

        if (apiResponse.isSuccess && apiResponse.result) {
          console.log('[결제 승인] 성공', { orderNo, result: apiResponse.result })
          setPaymentInfo(apiResponse.result)
          setConfirmError(null)
        } else {
          console.error('[결제 승인] 응답 실패', { orderNo, message: apiResponse.message })
          router.push(`/order/failure?orderNo=${orderNo}&amount=${amount}`)
          return
        }
      } catch (error) {
        console.error('[결제 승인] 예외 발생', { error, orderNo, orderId, paymentKey }, error)
        router.push(`/order/failure?orderNo=${orderNo || ''}&amount=${amount || ''}`)
      } finally {
        setIsConfirming(false)
      }
    }

    confirmPayment()
  }, [searchParams, router])

  // 로딩 중
  if (isConfirming) {
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
              <Link href="/search" className="search-btn">검색</Link>
              <button className="cart-btn">장바구니</button>
              <button className="user-btn">로그인</button>
            </div>
          </div>
        </header>
        <div className="order-page-container">
          <div className="container" style={{ textAlign: 'center', maxWidth: '600px' }}>
            <div className="success-icon">
              <div style={{ 
                width: '64px', 
                height: '64px', 
                border: '4px solid #f3f3f3',
                borderTop: '4px solid #4CAF50',
                borderRadius: '50%',
                animation: 'spin 1s linear infinite',
                margin: '0 auto'
              }}></div>
            </div>
            <div className="success-messages">
              <h1 className="success-title">결제 승인 중...</h1>
              <p className="success-subtitle">잠시만 기다려주세요.</p>
            </div>
            <style jsx>{`
              @keyframes spin {
                0% { transform: rotate(0deg); }
                100% { transform: rotate(360deg); }
              }
            `}</style>
          </div>
        </div>
      </div>
    )
  }

  // 에러 발생
  if (confirmError) {
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
              <Link href="/search" className="search-btn">검색</Link>
              <button className="cart-btn">장바구니</button>
              <button className="user-btn">로그인</button>
            </div>
          </div>
        </header>
        <div className="order-page-container">
          <div className="container" style={{ textAlign: 'center', maxWidth: '600px' }}>
            <div className="success-icon">
              <svg width="64" height="64" viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg" style={{ margin: '0 auto' }}>
                <circle cx="32" cy="32" r="32" fill="#f44336"/>
                <path d="M20 20L44 44M44 20L20 44" stroke="white" strokeWidth="4" strokeLinecap="round"/>
              </svg>
            </div>
            <div className="success-messages">
              <h1 className="success-title">결제 승인 실패</h1>
              <p className="success-subtitle">{confirmError}</p>
            </div>
            <button 
              onClick={() => router.push('/')}
              className="order-payment-button"
              style={{ maxWidth: '300px', margin: '0 auto' }}
            >
              홈으로 돌아가기
            </button>
          </div>
        </div>
      </div>
    )
  }

  // 성공
  const orderNo = paymentInfo?.orderNo || searchParams.get('orderNo') || 'N/A'
  const amount = searchParams.get('amount') 
    ? new Intl.NumberFormat('ko-KR').format(parseFloat(searchParams.get('amount')!)) + '원'
    : 'N/A'
  const paymentDate = new Date().toLocaleString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })

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
          <div className="success-icon" style={{ marginBottom: '24px' }}>
            <svg width="64" height="64" viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg" style={{ margin: '0 auto' }}>
              <circle cx="32" cy="32" r="32" fill="#4CAF50"/>
              <path d="M20 32L28 40L44 24" stroke="white" strokeWidth="4" strokeLinecap="round" strokeLinejoin="round"/>
            </svg>
          </div>
          <div className="success-messages">
            <h1 className="success-title">결제가 완료되었습니다</h1>
            <p className="success-subtitle">주문이 정상적으로 처리되었습니다.</p>
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
              <span className="detail-label">결제일시</span>
              <span className="detail-value">{paymentDate}</span>
            </div>
            <div className="detail-row">
              <span className="detail-label">결제수단</span>
              <span className="detail-value">토스페이</span>
            </div>
            <div className="detail-row">
              <span className="detail-label">결제금액</span>
              <span className="detail-value">{amount}</span>
            </div>
          </div>
        </div>

        <div style={{ textAlign: 'center', marginTop: '32px' }}>
          <Link href="/" className="order-payment-button" style={{ display: 'inline-block', maxWidth: '300px' }}>
            쇼핑 계속하기
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
