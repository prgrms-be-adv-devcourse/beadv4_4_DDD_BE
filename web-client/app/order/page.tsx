'use client'

import Link from 'next/link'
import { useRouter } from 'next/navigation'
import { useEffect, useRef, useState } from 'react'

declare global {
  interface Window {
    TossPayments: any
  }
}

interface PaymentMemberResponse {
  customerKey: string
  customerName: string
  customerEmail: string
  balance: number
}

interface PaymentMemberApiResponse {
  isSuccess: boolean
  code: string
  message: string
  result: PaymentMemberResponse
}

interface PaymentResponse {
  buyerId: number
  orderNo: string
  orderId: number
  totalAmount: number
  needsCharge: boolean
  chargeAmount: number
}

interface ApiResponse<T> {
  isSuccess: boolean
  code: string
  message: string
  result: T
}

export default function OrderPage() {
  const router = useRouter()
  const widgetRef = useRef<any>(null)
  const [memberInfo, setMemberInfo] = useState<PaymentMemberResponse | null>(null)
  const [isLoadingMember, setIsLoadingMember] = useState(true)
  const [memberError, setMemberError] = useState<string | null>(null)
  const [selectedMethod, setSelectedMethod] = useState<'modeunsa' | 'toss'>('modeunsa')
  const memberId = 4 // 회원 ID

  // 회원 정보 조회
  useEffect(() => {
    const fetchMemberInfo = async () => {
      try {
        const apiUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'
        const response = await fetch(`${apiUrl}/api/v1/payments/members/${memberId}`)
        
        if (!response.ok) {
          const errorText = await response.text()
          console.error('API 응답 에러:', response.status, errorText)
          throw new Error(`회원 정보 조회 실패 (${response.status})`)
        }
        
        const apiResponse: PaymentMemberApiResponse = await response.json()
        
        if (apiResponse.isSuccess && apiResponse.result) {
          setMemberInfo(apiResponse.result)
          setMemberError(null)
        } else {
          throw new Error('회원 정보를 가져올 수 없습니다.')
        }
      } catch (error) {
        console.error('회원 정보 조회 실패:', error)
        const errorMessage = error instanceof Error ? error.message : '알 수 없는 오류가 발생했습니다.'
        setMemberError(errorMessage)
      } finally {
        setIsLoadingMember(false)
      }
    }

    fetchMemberInfo()
  }, [router, memberId])

  // 토스페이먼츠 위젯 스크립트 로드
  useEffect(() => {
    const clientKey = process.env.NEXT_PUBLIC_TOSS_CLIENT_KEY
    
    if (!clientKey) {
      console.warn('토스페이먼츠 클라이언트 키가 설정되지 않았습니다. 결제 기능을 사용할 수 없습니다.')
      return
    }

    const script = document.createElement('script')
    script.src = 'https://js.tosspayments.com/v1/payment-widget'
    script.async = true
    script.onload = () => {
      if (window.TossPayments) {
        widgetRef.current = window.TossPayments(clientKey)
      }
    }
    script.onerror = () => {
      console.error('토스페이먼츠 스크립트 로드 실패')
    }
    document.body.appendChild(script)

    return () => {
      if (document.body.contains(script)) {
        document.body.removeChild(script)
      }
    }
  }, [])

  const handlePayment = async () => {
    if (!memberInfo) {
      alert('회원 정보를 불러오는 중입니다. 잠시 후 다시 시도해주세요.')
      return
    }

    let payment: PaymentResponse | null = null

    try {
      const amount = 19800 // 19,800원
      const apiUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'

      // 1) 먼저 결제 요청 API 호출
      const paymentRes = await fetch(`${apiUrl}/api/v1/payments`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          buyerId: memberId,
          orderId: Date.now(),
          orderNo: `ORD-${Date.now()}`,
          totalAmount: amount,
        }),
      })

      if (!paymentRes.ok) {
        const errorText = await paymentRes.text()
        console.error('결제 요청 API 에러:', paymentRes.status, errorText)
        router.push(`/order/failure?amount=${amount}`)
        return
      }

      const paymentApiResponse: ApiResponse<PaymentResponse> = await paymentRes.json()

      if (!paymentApiResponse.isSuccess || !paymentApiResponse.result) {
        console.error('결제 요청 응답 실패:', paymentApiResponse.message)
        router.push(`/order/failure?amount=${amount}`)
        return
      }

      payment = paymentApiResponse.result

      // 2) needsCharge가 false이면 토스 결제 모듈 없이 바로 성공 페이지로 이동
      if (!payment.needsCharge) {
        router.push(
          `/order/success?orderNo=${payment.orderNo}&amount=${payment.totalAmount}`
        )
        return
      }

      // 3) needsCharge가 true이면 토스 결제 모듈 호출 (충전 필요 금액 기준)
      const clientKey = process.env.NEXT_PUBLIC_TOSS_CLIENT_KEY
      if (!clientKey) {
        alert('토스페이먼츠 클라이언트 키가 설정되지 않았습니다. 환경 변수를 확인해주세요.')
        return
      }

      if (!widgetRef.current) {
        alert('결제 위젯을 불러오는 중입니다. 잠시 후 다시 시도해주세요.')
        return
      }

      await widgetRef.current.requestPayment('카드', {
        amount: payment.chargeAmount,
        orderId: payment.orderId,
        orderName: '베이직 레더 가방 130004',
        customerName: memberInfo.customerName,
        customerKey: memberInfo.customerKey,
        customerEmail: memberInfo.customerEmail,
        successUrl: `${window.location.origin}/order/success?orderNo=${payment.orderNo}&memberId=${memberId}&pgCustomerName=${encodeURIComponent(memberInfo.customerName)}&pgCustomerEmail=${encodeURIComponent(memberInfo.customerEmail)}`,
        failUrl: `${window.location.origin}/order/failure?orderNo=${payment.orderNo}&amount=${payment.totalAmount}`,
      })
    } catch (error) {
      console.error('결제 요청 실패:', error)
      if (payment) {
        router.push(
          `/order/failure?orderNo=${payment.orderNo}&amount=${payment.totalAmount}`
        )
      } else {
        const fallbackAmount = 19800
        router.push(`/order/failure?amount=${fallbackAmount}`)
      }
    }
  }

  return (
    <div className="home-page">
      {/* Header */}
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
        <div className="container">
          <h1 style={{ fontSize: '32px', fontWeight: '700', marginBottom: '32px', textAlign: 'center' }}>주문하기</h1>

        {/* 주문서 섹션 */}
        <section className="order-card">
          <h2 className="card-title">배송 정보</h2>
          {memberError && (
            <div className="error-alert">
              ⚠️ {memberError}
            </div>
          )}
          <div className="order-info">
            <div className="info-name">{memberInfo?.customerName || (isLoadingMember ? '로딩 중...' : '회원 정보 없음')}</div>
            <div className="info-tag">기본 배송지</div>
            <div className="info-text">서울 강남구 자곡동 123-456</div>
            <div className="info-text">010-1234-5678</div>
          </div>
        </section>

        {/* 주문 상품 섹션 */}
        <section className="order-card">
          <h2 className="card-title">주문 상품</h2>
          <div className="order-product">
            <div className="product-image-wrapper">
              <div className="product-image-placeholder">이미지</div>
            </div>
            <div className="product-details">
              <div className="product-brand">지오다노</div>
              <div className="product-name">베이직 레더 가방 130004</div>
              <div className="product-price">₩19,800</div>
              <div className="product-delivery">01.14(수) 도착 예정</div>
            </div>
          </div>
        </section>

        {/* 결제 금액 섹션 */}
        <section className="order-card">
          <h2 className="card-title">결제 금액</h2>
          <div className="payment-summary">
            <div className="summary-row">
              <span>상품 금액</span>
              <span>₩19,800</span>
            </div>
            <div className="summary-row">
              <span>배송비</span>
              <span>무료배송</span>
            </div>
            <div className="summary-divider"></div>
            <div className="summary-row total">
              <span>총 결제 금액</span>
              <span>₩19,800</span>
            </div>
          </div>
        </section>

        {/* 결제 수단 섹션 */}
        <section className="order-card">
          <h2 className="card-title">결제 수단</h2>
          <div className="payment-method-option">
            <input
              type="radio"
              id="modeunsa"
              name="payment"
              checked={selectedMethod === 'modeunsa'}
              onChange={() => setSelectedMethod('modeunsa')}
            />
            <label htmlFor="modeunsa">
              <div className="payment-logo">뭐든사</div>
              <div className="payment-info">
                <span>뭐든사페이</span>
                <span className="payment-balance">
                  (사용 가능 금액:{' '}
                  {memberInfo
                    ? `${new Intl.NumberFormat('ko-KR').format(Number(memberInfo.balance))}원`
                    : isLoadingMember
                    ? '로딩 중...'
                    : '0원'}
                  )
                </span>
              </div>
            </label>
          </div>
        </section>

        {/* 약관 안내 */}
        <div className="terms-notice">
          <div className="terms-item">
            <span>주문 내용을 확인했으며 결제에 동의합니다.</span>
            <a href="#" className="terms-link">자세히</a>
          </div>
          <div className="terms-item">
            <span>회원님의 개인정보는 안전하게 관리됩니다.</span>
            <a href="#" className="terms-link">자세히</a>
          </div>
          <div className="terms-item">
            <span>뭐든사는 통신판매중개자로, 업체 배송 상품의 상품/상품정보/거래 등에 대한 책임은 뭐든사가 아닌 판매자에게 있습니다.</span>
          </div>
        </div>

        {/* 결제 버튼 */}
        <button 
          className="order-payment-button" 
          onClick={handlePayment}
          disabled={isLoadingMember || !memberInfo}
        >
          {isLoadingMember || !memberInfo
            ? '로딩 중...'
            : (() => {
                const totalAmount = 19800
                if (selectedMethod === 'modeunsa') {
                  const balance = Number(memberInfo.balance ?? 0)
                  const shortage = Math.max(totalAmount - balance, 0)
                  return shortage > 0
                    ? `${new Intl.NumberFormat('ko-KR').format(shortage)}원 결제하기`
                    : '결제하기'
                }
                return `${new Intl.NumberFormat('ko-KR').format(totalAmount)}원 결제하기`
              })()}
        </button>
        </div>
      </div>

      {/* Footer */}
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
