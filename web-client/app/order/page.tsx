'use client'

import api from '@/app/lib/axios'
import Link from 'next/link'
import { useRouter, useSearchParams } from 'next/navigation'
import {Suspense, useEffect, useRef, useState} from 'react'
import Header from '../components/Header'

declare global {
  interface Window {
    TossPayments: any
  }
}

interface OrderItem {
  productId: number
  productName: string
  quantity: number
  salePrice: number
}

interface OrderResult {
  memberId: number
  orderId: number
  orderNo: string
  totalAmount: number
  orderItems: OrderItem[]
  recipientName: string
  recipientPhone: string
  zipCode: string
  address: string
  addressDetail: string
  paymentDeadlineAt: string
  createdAt?: string
}

interface OrderApiResponse {
  isSuccess: boolean
  code: string
  message: string
  result: OrderResult
}

interface ProductDetail {
  id: number
  name: string
  sellerBusinessName: string // 브랜드명으로 사용
  images: { imageUrl: string; isPrimary: boolean }[] // 이미지
  category: string
  salePrice: number
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
  needsPgPayment: boolean
  requestPgAmount: number
}

interface RequestPaymentApiResponse {
  isSuccess: boolean
  code: string
  message: string
  result: RequestPaymentResponse
}

const getCookie = (name: string) => {
  if (typeof document === 'undefined') return null
  const value = `; ${document.cookie}`
  const parts = value.split(`; ${name}=`)
  if (parts.length === 2) return parts.pop()?.split(';').shift()
  return null
}

export default function OrderPage() {
  return (
    <Suspense>
      <OrderContent />
    </Suspense>
  )
}

function OrderContent() {
  const router = useRouter()
  const searchParams = useSearchParams() // URL 파라미터 읽기용
  const orderId = searchParams.get('orderId') // URL에서 orderId 가져오기

  const widgetRef = useRef<any>(null)

  const [orderInfo, setOrderInfo] = useState<OrderResult | null>(null)
  const [isLoadingOrder, setIsLoadingOrder] = useState(true)
  const [productDetails, setProductDetails] = useState<Record<number, ProductDetail>>({})

  const [memberInfo, setMemberInfo] = useState<PaymentMemberResponse | null>(null)
  const [isLoadingMember, setIsLoadingMember] = useState(true)
  const [memberError, setMemberError] = useState<string | null>(null)

  const [selectedMethod, setSelectedMethod] = useState<'modeunsa' | 'toss'>('modeunsa')
  const [isAuthChecked, setIsAuthChecked] = useState(false)
  const [isSubmitting, setIsSubmitting] = useState(false)

// 쿠키 기반 인증: 결제 회원 정보 조회 (401/403 시 로그인 페이지로 이동)
  useEffect(() => {
    let redirecting = false
    const fetchMemberInfo = async () => {
      try {
        const res = await api.get<PaymentMemberApiResponse>('/api/v1/payments/members')

        const data = res.data
        if (data.isSuccess && data.result != null) {
          setMemberInfo(data.result)
          setMemberError(null)
        } else {
          setMemberError(data.message || '회원 정보를 불러오지 못했습니다.')
          setMemberInfo(null)
        }
      } catch (err: unknown) {
        const status = (err as { response?: { status?: number } })?.response?.status
        if (status === 401 || status === 403) {
          redirecting = true
          router.replace('/login')
          return
        }
        setMemberError('회원 정보를 불러오지 못했습니다.')
        setMemberInfo(null)
      } finally {
        setIsLoadingMember(false)
        if (!redirecting) setIsAuthChecked(true)
      }
    }
    fetchMemberInfo()
  }, [router])

  const [recipientName, setRecipientName] = useState('')
  const [recipientPhone, setRecipientPhone] = useState('')
  const [zipCode, setZipCode] = useState('')
  const [address, setAddress] = useState('')
  const [addressDetail, setAddressDetail] = useState('')

  // [추가] 주문 정보가 로드되면, 입력창에도 초기값을 채워넣기
  useEffect(() => {
    if (orderInfo) {
      setRecipientName(orderInfo.recipientName || '')
      setRecipientPhone(orderInfo.recipientPhone || '')
      setZipCode(orderInfo.zipCode || '')
      setAddress(orderInfo.address || '')
      setAddressDetail(orderInfo.addressDetail || '')
    }
  }, [orderInfo])

  // 주문 정보 조회 (orderId 기준)
  useEffect(() => {
    if (!isAuthChecked || !orderId) return // orderId 없으면 실행 안 함

    const fetchOrderInfo = async () => {
      try {
        // fetch 대신 api.get 사용
        const res = await api.get<OrderApiResponse>(`/api/v1/orders/${orderId}`)

        const data = res.data

        console.log('서버 응답 데이터:', data)

        if (data.isSuccess && data.result) {
          setOrderInfo(data.result)

          // 2. [핵심] 상품 상세 정보 추가 조회 (병렬 처리)
          // 주문 항목에서 productId만 뽑아냄
          const productIds = data.result.orderItems.map((item: any) => item.productId)

          // Promise.all로 모든 상품 API를 동시에 호출
          const productPromises = productIds.map((pid: number) =>
              api.get(`/api/v1/products/${pid}`)
          )

          const productResponses = await Promise.all(productPromises)

          // 3. 받아온 상품 정보를 객체(Map) 형태로 정리
          const newDetails: Record<number, ProductDetail> = {}

          productResponses.forEach((res) => {
            const pData = res.data
            if (pData.isSuccess && pData.result) {
              newDetails[pData.result.id] = pData.result
            }
          })

          setProductDetails(newDetails) // state 업데이트
        } else {
          alert(data.message || '주문 정보를 찾을 수 없습니다.')
          router.back()
        }
      } catch (e: any) {
        console.error(e)
        // 401 에러 처리
        if (e.response?.status === 401) {
          alert('로그인 세션이 만료되었습니다.')
          router.replace('/login')
          return
        }
        alert('주문 정보를 불러오는 중 오류가 발생했습니다.')
      } finally {
        setIsLoadingOrder(false)
      }
    }
    fetchOrderInfo()
  }, [isAuthChecked, orderId])


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

  useEffect(() => {
    const clientKey = process.env.NEXT_PUBLIC_TOSS_CLIENT_KEY

    if (!clientKey) {
      console.warn('토스페이먼츠 클라이언트 키가 설정되지 않았습니다. 결제 기능을 사용할 수 없습니다.')
      return
    }

    const script = document.createElement('script')
    // 주의: 일반 결제창을 띄우려면 v1/payment 여야 하지만, 요청하신 대로 v1/payment-widget 유지합니다.
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
    if (!memberInfo || !orderInfo) {
      alert('회원 정보를 불러오는 중입니다. 잠시 후 다시 시도해주세요.')
      return
    }

    // 필수 입력값 체크
    if (!recipientName || !recipientPhone || !address) {
      alert('배송 정보를 모두 입력해주세요.')
      return
    }

    setIsSubmitting(true)

    try {
      // 배송지 정보 업데이트 API 호출
      await api.patch(`/api/v2/orders/delivery-info`, {
        recipientName,
        recipientPhone,
        zipCode,
        address,
        addressDetail
      }, {
        // Query Parameter로 orderId 전달
        params: { orderId: orderInfo.orderId }
      })

      const { orderId, orderNo, totalAmount, paymentDeadlineAt } = orderInfo

      const providerType = selectedMethod === 'modeunsa' ? 'MODEUNSA_PAY' : 'TOSS_PAYMENTS'

      setIsSubmitting(true)
      try {
        const res = await api.post<RequestPaymentApiResponse>('/api/v1/payments', {
          orderId,
          orderNo,
          totalAmount,
          paymentDeadlineAt,
          providerType,
          paymentPurpose: 'PRODUCT_PURCHASE',
        })
        const data = res.data

        if (!data.isSuccess || !data.result) {
          const code = data.code || ''
          const message = data.message || '결제 요청에 실패했습니다.'
          router.replace(
              `/order/failure?orderNo=${encodeURIComponent(orderNo)}&amount=${totalAmount}&code=${encodeURIComponent(code)}&message=${encodeURIComponent(message)}`
          )
          setIsSubmitting(false)
          return
        }

        const result = data.result

        if (!result.needsPgPayment) {
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

        // 뭐든사페이: 부족한 금액만 PG 결제(requestPgAmount). 토스페이먼츠: 전체 금액(requestPgAmount === totalAmount)
        const amount = result.requestPgAmount
        const origin = typeof window !== 'undefined' ? window.location.origin : ''
        const successUrl = `${origin}/order/success?orderNo=${encodeURIComponent(result.orderNo)}&amount=${amount}&orderId=${encodeURIComponent(String(result.orderNo))}&memberId=${result.buyerId}&pgCustomerName=${encodeURIComponent(memberInfo.customerName || '')}&pgCustomerEmail=${encodeURIComponent(memberInfo.customerEmail || '')}`
        const failUrl = `${origin}/order/failure?orderNo=${encodeURIComponent(result.orderNo)}&amount=${amount}`

        const tossClient = window.TossPayments?.(clientKey)
        if (tossClient?.requestPayment) {
          const firstItem = orderInfo.orderItems[0]
          const orderName =
            firstItem?.productName || '뭐든사 주문 결제'

          await tossClient.requestPayment('카드', {
            amount,
            orderId: String(result.orderNo),
            orderName,
            successUrl,
            failUrl,
            customerName: memberInfo.customerName,
            customerEmail: memberInfo.customerEmail,
          })
        } else {
          alert('토스 페이먼츠 결제 창을 열 수 없습니다. 결제 SDK를 확인해주세요.')
        }
      } catch (err: unknown) {
        console.error('결제 요청 실패:', err)
        const ax = err as { response?: { data?: RequestPaymentApiResponse; status?: number } }
        const code = ax.response?.data?.code || ''
        const message = ax.response?.data?.message || (err instanceof Error ? err.message : '결제 요청 중 오류가 발생했습니다.')
        router.replace(
            `/order/failure?orderNo=${encodeURIComponent(orderNo)}&amount=${totalAmount}&code=${encodeURIComponent(code)}&message=${encodeURIComponent(message)}`
        )
      } finally {
        setIsSubmitting(false)
      }
    } catch (err) {
      console.error('배송지 업데이트 실패', err)
      alert('배송지 정보 업데이트 중 오류가 발생했습니다.')
      setIsSubmitting(false)
    }
  }


  // --- 렌더링 ---
  if (!isAuthChecked) return null


  return (
        <div className="home-page">
          {/* Header */}
          <Header/>

          <div className="order-page-container">
            <div className="container">
              <h1 style={{
                fontSize: '32px',
                fontWeight: '700',
                marginBottom: '32px',
                textAlign: 'center'
              }}>주문하기</h1>

              {/* 주문 번호 / 생성 일시 섹션 */}
              {orderInfo && (
                <section className="order-card" style={{ marginBottom: '16px' }}>
                  <h2 className="card-title">주문 정보</h2>
                  <div className="payment-summary">
                    <div className="summary-row">
                      <span style={{ fontSize: '14px', color: '#666' }}>주문번호</span>
                      <span style={{ fontWeight: 600 }}>
                        {orderInfo.orderNo || `#${orderInfo.orderId}`}
                      </span>
                    </div>
                    <div className="summary-row">
                      <span style={{ fontSize: '14px', color: '#666' }}>주문일시</span>
                      <span style={{ fontSize: '14px' }}>
                        {orderInfo.createdAt
                          ? new Date(orderInfo.createdAt).toLocaleString('ko-KR', {
                              year: 'numeric',
                              month: '2-digit',
                              day: '2-digit',
                              hour: '2-digit',
                              minute: '2-digit',
                              second: '2-digit',
                            })
                          : '-'}
                      </span>
                    </div>
                  </div>
                </section>
              )}

              {/* 주문서 섹션 */}
              <section className="order-card">
                <h2 className="card-title">배송 정보</h2>
                <div className="shipping-form"
                     style={{display: 'flex', flexDirection: 'column', gap: '12px'}}>
                  {/* 받는 사람 */}
                  <div className="form-group">
                    <label style={{
                      fontSize: '13px',
                      color: '#666',
                      marginBottom: '4px',
                      display: 'block'
                    }}>받는 분</label>
                    <input
                        type="text"
                        value={recipientName}
                        onChange={(e) => setRecipientName(e.target.value)}
                        placeholder="이름을 입력하세요"
                        style={{
                          width: '100%',
                          padding: '10px',
                          border: '1px solid #ddd',
                          borderRadius: '4px'
                        }}
                    />
                  </div>

                  {/* 연락처 */}
                  <div className="form-group">
                    <label style={{
                      fontSize: '13px',
                      color: '#666',
                      marginBottom: '4px',
                      display: 'block'
                    }}>연락처</label>
                    <input
                        type="text"
                        value={recipientPhone}
                        onChange={(e) => setRecipientPhone(e.target.value)}
                        placeholder="010-0000-0000"
                        style={{
                          width: '100%',
                          padding: '10px',
                          border: '1px solid #ddd',
                          borderRadius: '4px'
                        }}
                    />
                  </div>

                  {/* 주소 (우편번호 + 주소) */}
                  <div className="form-group">
                    <label style={{
                      fontSize: '13px',
                      color: '#666',
                      marginBottom: '4px',
                      display: 'block'
                    }}>주소</label>
                    <div style={{display: 'flex', gap: '8px', marginBottom: '8px'}}>
                      <input
                          type="text"
                          value={zipCode}
                          onChange={(e) => setZipCode(e.target.value)}
                          placeholder="우편번호"
                          style={{
                            width: '100px',
                            padding: '10px',
                            border: '1px solid #ddd',
                            borderRadius: '4px'
                          }}
                      />
                      {/* (선택사항) 여기에 '주소 찾기' 버튼을 넣으면 더 좋습니다 */}
                      <input
                          type="text"
                          value={address}
                          onChange={(e) => setAddress(e.target.value)}
                          placeholder="기본 주소"
                          style={{
                            flex: 1,
                            padding: '10px',
                            border: '1px solid #ddd',
                            borderRadius: '4px'
                          }}
                      />
                    </div>
                    <input
                        type="text"
                        value={addressDetail}
                        onChange={(e) => setAddressDetail(e.target.value)}
                        placeholder="상세 주소 (아파트 동/호수 등)"
                        style={{
                          width: '100%',
                          padding: '10px',
                          border: '1px solid #ddd',
                          borderRadius: '4px'
                        }}
                    />
                  </div>
                </div>
              </section>

              {/* 주문 상품 섹션 */}
              <section className="order-card">
                <h2 className="card-title">주문 상품</h2>
                {orderInfo?.orderItems.map((item, index) => {
                  // 해당 상품의 상세 정보를 찾음
                  const detail = productDetails[item.productId]
                  // 대표 이미지 찾기 (없으면 첫 번째 이미지)
                  const mainImage = detail?.images.find(img => img.isPrimary) || detail?.images[0]

                  return (
                      <div key={index} className="order-product">
                        <div className="product-image-wrapper">
                          {/* 이미지가 있으면 보여주고, 없으면 가방 아이콘 */}
                          {mainImage ? (
                              <img
                                  src={mainImage.imageUrl}
                                  alt={detail?.name}
                                  style={{width: '100%', height: '100%', objectFit: 'cover'}}
                              />
                          ) : (
                              <div className="product-image-bag">
                                <div className="product-image-bag-handle"/>
                                <div className="product-image-bag-body"/>
                              </div>
                          )}
                        </div>
                        <div className="product-details">
                          <div
                              className="product-brand">{detail ? detail.sellerBusinessName : '로딩 중...'}</div>
                          <div
                              className="product-name">{detail ? detail.name : item.productName || `상품 ${item.productId}`}</div>
                          <div
                              className="product-price">{new Intl.NumberFormat('ko-KR').format(item.salePrice * item.quantity)}원
                            <span style={{
                              fontSize: '12px',
                              color: '#666',
                              marginLeft: '5px',
                              fontWeight: 'normal'
                            }}>
                      ({item.quantity}개)
                    </span></div>
                          <div className="product-delivery">01.14(수) 도착 예정</div>
                        </div>
                      </div>
                  )
                })}
              </section>

              {/* 결제 금액 섹션 */}
              <section className="order-card">
                <h2 className="card-title">결제 금액</h2>
                <div className="payment-summary">
                  <div className="summary-row">
                    {orderInfo ? new Intl.NumberFormat('ko-KR').format(orderInfo.totalAmount) : '0'}원
                    <span>₩{orderInfo ? new Intl.NumberFormat('ko-KR').format(orderInfo.totalAmount) : '0'}</span>
                  </div>
                  <div className="summary-row">
                    <span>배송비</span>
                    <span style={{color: '#667eea'}}>무료배송</span>
                  </div>
                  <div className="summary-divider"></div>
                  <div className="summary-row total">
                    <span>총 결제 금액</span>
                    <span>{orderInfo ? new Intl.NumberFormat('ko-KR').format(orderInfo.totalAmount) : '0'}원</span>
                  </div>
                </div>
              </section>

              {/* 결제 수단 섹션 */}
              <section className="order-card">
                <h2 className="card-title">결제 수단</h2>
                <div style={{display: 'grid', gridTemplateColumns: '1fr', gap: '16px'}}>
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
                      <div className="payment-logo payment-logo-fixed"
                           style={{background: '#0064FF', color: 'white'}}>Toss
                      </div>
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
                  disabled={isLoadingMember || !memberInfo || isSubmitting || !orderInfo}
              >
                {isSubmitting
                    ? '처리 중...'
                    : isLoadingMember || !memberInfo || !orderInfo
                        ? '로딩 중...'
                        : (() => {
                          const totalAmount = Number(orderInfo.totalAmount ?? 0)
                          // 총 결제 금액 기준 버튼 문구
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