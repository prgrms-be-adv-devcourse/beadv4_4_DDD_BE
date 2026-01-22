'use client'

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
        // 실패 페이지로 이동하지 않고 에러 메시지 표시
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
      // 실패 페이지로 이동하지 않고 경고만 표시
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
          orderId: Date.now(), // 임시 orderId, 실제로는 백엔드 정책에 맞게 조정
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
    <main className="order-page">
      {/* 주문서 섹션 */}
      <section className="order-section card">
        <h2 className="section-title">주문서</h2>
        {memberError && (
          <div style={{ 
            padding: '12px', 
            marginBottom: '12px', 
            backgroundColor: '#fee', 
            color: '#c33', 
            borderRadius: '8px',
            fontSize: '14px'
          }}>
            ⚠️ {memberError}
          </div>
        )}
        <div className="order-info">
          <div className="name">{memberInfo?.customerName || (isLoadingMember ? '로딩 중...' : '회원 정보 없음')}</div>
          <div className="delivery-tag">기본 배송지</div>
          <div className="address">서울 강남구 자곡동 123-456</div>
          <div className="phone">010-1234-5678</div>
        </div>
      </section>

      {/* 결제 금액 섹션 */}
      <section className="payment-section">
        <h2 className="section-title">결제 금액</h2>
        <div className="payment-details">
          <div className="payment-row">
            <span>상품 금액</span>
            <span>19,800원</span>
          </div>
          <div className="payment-row">
            <span>배송비</span>
            <span>무료배송</span>
          </div>
          <div className="payment-divider"></div>
          <div className="payment-row total">
            <span>총 결제 금액</span>
            <span>19,800원</span>
          </div>
        </div>
      </section>

      {/* 주문 상품 섹션 */}
      <section className="product-section">
        <h2 className="section-title">주문 상품 1개</h2>
        <div className="product-item">
          <div className="product-image">
            <svg className="bag-image" viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg">
              {/* 가방 본체 */}
              <rect x="20" y="30" width="60" height="50" rx="4" fill="#d4a574" stroke="#b8945f" strokeWidth="1.5"/>
              {/* 가방 손잡이 */}
              <path d="M 30 30 Q 30 20 40 20 L 60 20 Q 70 20 70 30" stroke="#b8945f" strokeWidth="2" fill="none" strokeLinecap="round"/>
              <path d="M 30 30 Q 30 20 40 20 L 60 20 Q 70 20 70 30" stroke="#b8945f" strokeWidth="2" fill="none" strokeLinecap="round" transform="translate(0, 50)"/>
              {/* 가방 지퍼/라인 */}
              <line x1="30" y1="45" x2="70" y2="45" stroke="#b8945f" strokeWidth="1" opacity="0.6"/>
              <line x1="30" y1="55" x2="70" y2="55" stroke="#b8945f" strokeWidth="1" opacity="0.6"/>
              {/* 가방 장식 라인 */}
              <rect x="25" y="35" width="50" height="40" rx="2" fill="none" stroke="#b8945f" strokeWidth="1" opacity="0.4"/>
            </svg>
          </div>
          <div className="product-info">
            <div className="product-brand">지오다노</div>
            <div className="product-name">베이직 레더 가방 130004</div>
            <div className="product-price">19,800원</div>
            <div className="product-delivery">01.14(수) 도착 예정</div>
          </div>
        </div>
      </section>

      {/* 결제 수단 섹션 */}
      <section className="payment-method-section">
        <h2 className="section-title">결제 수단</h2>
        <div className="payment-method">
          <input
            type="radio"
            id="modeunsa"
            name="payment"
            checked={selectedMethod === 'modeunsa'}
            onChange={() => setSelectedMethod('modeunsa')}
          />
          <label htmlFor="modeunsa">
            <div className="modeunsa-logo">뭐든사</div>
            <span>뭐든사페이</span>
            <span style={{ fontSize: '13px', color: '#666' }}>
              (사용 가능 금액:{' '}
              {memberInfo
                ? `${new Intl.NumberFormat('ko-KR').format(Number(memberInfo.balance))}원`
                : isLoadingMember
                ? '로딩 중...'
                : '0원'}
              )
            </span>
          </label>
        </div>
      </section>

      {/* 약관 안내 (카드 밖) */}
      <div className="terms-outside">
        <div className="terms-item-outside">
          <span>주문 내용을 확인했으며 결제에 동의합니다.</span>
          <a href="#" className="detail-link">자세히</a>
        </div>
        <div className="terms-item-outside">
          <span>회원님의 개인정보는 안전하게 관리됩니다.</span>
          <a href="#" className="detail-link">자세히</a>
        </div>
        <div className="terms-item-outside">
          <span>뭐든사는 통신판매중개자로, 업체 배송 상품의 상품/상품정보/거래 등에 대한 책임은 뭐든사가 아닌 판매자에게 있습니다.</span>
        </div>
      </div>

      {/* 결제 버튼 */}
      <section className="terms-section">
        <button 
          className="payment-button" 
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
                // 토스 선택 시에는 총 결제 금액 표시
                return `${new Intl.NumberFormat('ko-KR').format(totalAmount)}원 결제하기`
              })()}
        </button>
      </section>
    </main>
  )
}
