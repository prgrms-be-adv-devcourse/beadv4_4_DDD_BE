'use client'

import Link from 'next/link'
import { useRouter } from 'next/navigation'
import { useEffect, useRef, useState } from 'react'
import Header from '../components/Header'

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

interface RequestPaymentResponse {
  buyerId: number
  orderNo: string
  orderId: number
  totalAmount: number
  needsCharge: boolean
  chargeAmount: number
}

interface RequestPaymentApiResponse {
  isSuccess: boolean
  code: string
  message: string
  result: RequestPaymentResponse
}

export default function OrderPage() {
  const router = useRouter()
  const widgetRef = useRef<any>(null)
  const [memberInfo, setMemberInfo] = useState<PaymentMemberResponse | null>(null)
  const [isLoadingMember, setIsLoadingMember] = useState(true)
  const [memberError, setMemberError] = useState<string | null>(null)
  const [selectedMethod, setSelectedMethod] = useState<'modeunsa' | 'toss'>('modeunsa')
  const [isAuthChecked, setIsAuthChecked] = useState(false)
  const [isSubmitting, setIsSubmitting] = useState(false)

  // 로그인 여부 확인: 비로그인 시 로그인 페이지로 이동
  useEffect(() => {
    if (typeof window === 'undefined') return
    const accessToken = localStorage.getItem('accessToken')
    if (!accessToken || accessToken.trim() === '') {
      router.replace('/login')
      return
    }
    setIsAuthChecked(true)
  }, [router])

  // 결제 계좌 정보(사용 가능 금액 등) 조회
  useEffect(() => {
    if (!isAuthChecked) return
    const accessToken = localStorage.getItem('accessToken')
    const apiUrl = process.env.NEXT_PUBLIC_API_URL || ''
    if (!accessToken?.trim() || !apiUrl) {
      setMemberError('API 설정을 확인해주세요.')
      setIsLoadingMember(false)
      return
    }
    const fetchMemberInfo = async () => {
      try {
        const res = await fetch(`${apiUrl}/api/v1/payments/accounts`, {
          headers: { Authorization: `Bearer ${accessToken}` },
        })
        const data: PaymentMemberApiResponse = await res.json()
        if (res.ok && data.isSuccess && data.result != null) {
          setMemberInfo(data.result)
          setMemberError(null)
        } else {
          setMemberError(data.message || '회원 정보를 불러오지 못했습니다.')
          setMemberInfo(null)
        }
      } catch {
        setMemberError('회원 정보를 불러오지 못했습니다.')
        setMemberInfo(null)
      } finally {
        setIsLoadingMember(false)
      }
    }
    fetchMemberInfo()
  }, [isAuthChecked])

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
    const accessToken = typeof window !== 'undefined' ? localStorage.getItem('accessToken') : null
    const apiUrl = process.env.NEXT_PUBLIC_API_URL || ''
    if (!accessToken?.trim() || !apiUrl) {
      alert('로그인 또는 API 설정을 확인해주세요.')
      return
    }

    const totalAmount = 19800
    const orderNo = `ORD-${Date.now()}`
    const orderId = Date.now()
    const deadline = new Date(Date.now() + 10 * 60 * 1000)
    const paymentDeadlineAt =
      deadline.getFullYear() +
      '-' +
      String(deadline.getMonth() + 1).padStart(2, '0') +
      '-' +
      String(deadline.getDate()).padStart(2, '0') +
      'T' +
      String(deadline.getHours()).padStart(2, '0') +
      ':' +
      String(deadline.getMinutes()).padStart(2, '0') +
      ':' +
      String(deadline.getSeconds()).padStart(2, '0')

    setIsSubmitting(true)
    try {
      const res = await fetch(`${apiUrl}/api/v1/payments`, {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${accessToken}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          orderId,
          orderNo,
          totalAmount,
          paymentDeadlineAt,
        }),
      })
      const data: RequestPaymentApiResponse = await res.json()

      if (!res.ok) {
        alert(data.message || '결제 요청에 실패했습니다.')
        setIsSubmitting(false)
        return
      }
      if (!data.isSuccess || !data.result) {
        alert(data.message || '결제 요청에 실패했습니다.')
        setIsSubmitting(false)
        return
      }

      const result = data.result

      if (!result.needsCharge) {
        router.push(
          `/order/success?orderNo=${encodeURIComponent(result.orderNo)}&amount=${result.totalAmount}`
        )
        return
      }

      // PG(토스) 결제 필요
      const clientKey = process.env.NEXT_PUBLIC_TOSS_CLIENT_KEY
      if (!clientKey) {
        alert('토스페이먼츠 설정이 없습니다. 결제를 진행할 수 없습니다.')
        setIsSubmitting(false)
        return
      }

      const amount = Number(result.chargeAmount ?? result.totalAmount)
      const origin = typeof window !== 'undefined' ? window.location.origin : ''
      const successUrl = `${origin}/order/success?orderNo=${encodeURIComponent(result.orderNo)}&amount=${result.totalAmount}&memberId=${result.buyerId}&pgCustomerName=${encodeURIComponent(memberInfo.customerName || '')}&pgCustomerEmail=${encodeURIComponent(memberInfo.customerEmail || '')}`
      const failUrl = `${origin}/order/failure?orderNo=${encodeURIComponent(result.orderNo)}&amount=${amount}`

      const tossClient = window.TossPayments?.(clientKey)
      if (tossClient?.requestPayment) {
        await tossClient.requestPayment('카드', {
          amount,
          orderId: String(result.orderId),
          orderName: '뭐든사 주문 결제',
          successUrl,
          failUrl,
          customerName: memberInfo.customerName,
          customerEmail: memberInfo.customerEmail,
        })
      } else {
        alert('토스 페이먼츠 결제 창을 열 수 없습니다. 결제 SDK를 확인해주세요.')
      }
    } catch (err) {
      console.error('결제 요청 실패:', err)
      alert(err instanceof Error ? err.message : '결제 요청 중 오류가 발생했습니다.')
    } finally {
      setIsSubmitting(false)
    }
  }

  if (!isAuthChecked) {
    return null
  }

  return (
    <div className="home-page">
      {/* Header */}
      <Header />

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
              <div className="product-image-bag">
                <div className="product-image-bag-handle" />
                <div className="product-image-bag-body" />
              </div>
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
              <span style={{ color: '#667eea' }}>무료배송</span>
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
          <div style={{ display: 'grid', gridTemplateColumns: '1fr', gap: '16px' }}>
            <div className="payment-method-option">
              <input
                type="radio"
                id="modeunsa"
                name="payment"
                checked={selectedMethod === 'modeunsa'}
                onChange={() => setSelectedMethod('modeunsa')}
              />
              <label htmlFor="modeunsa">
                <div className="payment-logo payment-logo-fixed">뭐든사</div>
                <div className="payment-info payment-info-row">
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
            <div className="payment-method-option">
              <input
                type="radio"
                id="toss"
                name="payment"
                checked={selectedMethod === 'toss'}
                onChange={() => setSelectedMethod('toss')}
              />
              <label htmlFor="toss">
                <div className="payment-logo payment-logo-fixed" style={{ background: '#0064FF', color: 'white' }}>Toss</div>
                <div className="payment-info">
                  <span>토스 페이먼츠</span>
                </div>
              </label>
            </div>
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
          disabled={isLoadingMember || !memberInfo || isSubmitting}
        >
          {isSubmitting
            ? '처리 중...'
            : isLoadingMember || !memberInfo
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
