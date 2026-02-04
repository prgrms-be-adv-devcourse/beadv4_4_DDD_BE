'use client'

import { useRouter } from 'next/navigation'
import { useEffect, useRef, useState } from 'react'
import MypageLayout from '../../../components/MypageLayout'

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

interface PaymentAccountApiResponse {
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

function formatWithComma(value: string): string {
  const digits = value.replace(/\D/g, '')
  if (digits === '') return ''
  return new Intl.NumberFormat('ko-KR').format(Number(digits))
}

function parseAmountFromDisplay(display: string): number {
  const digits = display.replace(/\D/g, '')
  return digits === '' ? 0 : Number(digits)
}

export default function MoneyChargePage() {
  const router = useRouter()
  const presetAmounts = [10000, 30000, 50000]
  const [balance, setBalance] = useState<number | null>(null)
  const [customerEmail, setCustomerEmail] = useState<string | null>(null)
  const [customerName, setCustomerName] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [selectedPresetAmount, setSelectedPresetAmount] = useState<number | null>(null)
  const [customAmountDisplay, setCustomAmountDisplay] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  useEffect(() => {
    const fetchAccount = async () => {
      if (typeof window === 'undefined') return
      const accessToken = localStorage.getItem('accessToken')
      if (!accessToken?.trim()) {
        setLoading(false)
        return
      }
      const apiUrl = process.env.NEXT_PUBLIC_API_URL || ''
      if (!apiUrl) {
        setLoading(false)
        return
      }
      try {
        const res = await fetch(`${apiUrl}/api/v1/payments/accounts`, {
          headers: { Authorization: `Bearer ${accessToken}` },
        })
        const data: PaymentAccountApiResponse = await res.json()
        if (res.ok && data.isSuccess && data.result != null) {
          setBalance(Number(data.result.balance))
          setCustomerEmail(data.result.customerEmail ?? null)
          setCustomerName(data.result.customerName ?? null)
        } else {
          setError(data.message || '정보를 불러오지 못했습니다.')
        }
      } catch {
        setError('정보를 불러오지 못했습니다.')
      } finally {
        setLoading(false)
      }
    }
    fetchAccount()
  }, [])

  // 토스페이먼츠 스크립트 로드
  useEffect(() => {
    const clientKey = process.env.NEXT_PUBLIC_TOSS_CLIENT_KEY
    if (!clientKey) return
    const script = document.createElement('script')
    script.src = 'https://js.tosspayments.com/v1/payment-widget'
    script.async = true
    document.body.appendChild(script)
    return () => {
      if (document.body.contains(script)) document.body.removeChild(script)
    }
  }, [])

  const chargeAmount =
    selectedPresetAmount ?? (customAmountDisplay ? parseAmountFromDisplay(customAmountDisplay) : 0)

  const handleCharge = async () => {
    if (chargeAmount < 100) {
      alert('충전 금액은 100원 이상 입력해주세요.')
      return
    }
    const accessToken = typeof window !== 'undefined' ? localStorage.getItem('accessToken') : null
    const apiUrl = process.env.NEXT_PUBLIC_API_URL || ''
    if (!accessToken?.trim() || !apiUrl) {
      alert('로그인 또는 API 설정을 확인해주세요.')
      return
    }
    const clientKey = process.env.NEXT_PUBLIC_TOSS_CLIENT_KEY
    if (!clientKey) {
      alert('토스페이먼츠 설정이 없습니다. 충전을 진행할 수 없습니다.')
      return
    }

    const orderNo = `CHG-${Date.now()}`
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
          totalAmount: chargeAmount,
          paymentDeadlineAt,
          providerType: 'TOSS_PAYMENTS',
          paymentPurpose: 'DEPOSIT_CHARGE',
        }),
      })
      const data: RequestPaymentApiResponse = await res.json()

      if (!res.ok) {
        const message = data.message || '충전 요청에 실패했습니다.'
        router.replace(
          `/mypage/money/charge/failure?orderNo=${encodeURIComponent(orderNo)}&amount=${chargeAmount}&message=${encodeURIComponent(message)}`
        )
        setIsSubmitting(false)
        return
      }
      if (!data.isSuccess || !data.result) {
        const message = data.message || '충전 요청에 실패했습니다.'
        router.replace(
          `/mypage/money/charge/failure?orderNo=${encodeURIComponent(orderNo)}&amount=${chargeAmount}&message=${encodeURIComponent(message)}`
        )
        setIsSubmitting(false)
        return
      }

      const result = data.result

      if (!result.needsPgPayment) {
        router.push(
          `/mypage/money/charge/success?orderNo=${encodeURIComponent(result.orderNo)}&amount=${result.totalAmount}`
        )
        setIsSubmitting(false)
        return
      }

      const amount = result.requestPgAmount
      const origin = typeof window !== 'undefined' ? window.location.origin : ''
      const successUrl = `${origin}/mypage/money/charge/success?orderNo=${encodeURIComponent(result.orderNo)}&amount=${amount}&memberId=${result.buyerId}&pgCustomerName=${encodeURIComponent(customerName || '')}&pgCustomerEmail=${encodeURIComponent(customerEmail || '')}`
      const failUrl = `${origin}/mypage/money/charge/failure?orderNo=${encodeURIComponent(result.orderNo)}&amount=${amount}`

      const tossClient = window.TossPayments?.(clientKey)
      if (tossClient?.requestPayment) {
        await tossClient.requestPayment('카드', {
          amount,
          orderId: String(result.orderId),
          orderName: '뭐든사 머니 충전',
          successUrl,
          failUrl,
          customerName: customerName || '',
          customerEmail: customerEmail || '',
        })
      } else {
        alert('토스 페이먼츠 결제 창을 열 수 없습니다. 결제 SDK를 확인해주세요.')
      }
    } catch (err) {
      console.error('충전 요청 실패:', err)
      alert(err instanceof Error ? err.message : '충전 요청 중 오류가 발생했습니다.')
    } finally {
      setIsSubmitting(false)
    }
  }

  const balanceDisplay =
    loading && balance === null
      ? '조회 중...'
      : error
        ? error
        : balance != null
          ? `${new Intl.NumberFormat('ko-KR').format(balance)}원`
          : '-'

  return (
    <MypageLayout>
      <div style={{ maxWidth: '600px' }}>
        <h1 style={{ fontSize: '24px', fontWeight: 700, marginBottom: '24px' }}>뭐든사 머니 충전</h1>

          <div
            style={{
              background: 'white',
              borderRadius: '12px',
              padding: '24px',
              boxShadow: '0 2px 8px rgba(0, 0, 0, 0.1)',
              marginBottom: '24px',
            }}
          >
            <div
              style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                marginBottom: '20px',
              }}
            >
              <div>
                <div style={{ fontSize: '14px', color: '#666', marginBottom: '4px' }}>현재 보유 머니</div>
                <div
                  style={{
                    fontSize: '22px',
                    fontWeight: 700,
                    color: error ? '#dc3545' : undefined,
                  }}
                >
                  {balanceDisplay}
                </div>
              </div>
              <div style={{ fontSize: '12px', color: '#999' }}>
                {loading && !customerEmail ? '조회 중...' : customerEmail ?? '-'}
              </div>
            </div>

            <div style={{ marginBottom: '16px' }}>
              <div style={{ fontSize: '14px', fontWeight: 500, marginBottom: '8px' }}>충전 금액 선택</div>
              <div style={{ display: 'flex', gap: '8px', marginBottom: '12px' }}>
                {presetAmounts.map((amount) => {
                  const isSelected = selectedPresetAmount === amount
                  return (
                    <button
                      key={amount}
                      type="button"
                      onClick={() => {
                        setSelectedPresetAmount(amount)
                        setCustomAmountDisplay('')
                      }}
                      style={{
                        flex: 1,
                        padding: '10px 0',
                        borderRadius: '8px',
                        border: isSelected ? '2px solid #667eea' : '1px solid #e0e0e0',
                        background: isSelected ? '#f0f0ff' : '#f9f9f9',
                        cursor: 'pointer',
                        fontSize: '14px',
                        fontWeight: isSelected ? 600 : 500,
                        color: isSelected ? '#667eea' : undefined,
                      }}
                    >
                      {amount.toLocaleString()}원
                    </button>
                  )
                })}
              </div>
              <div style={{ display: 'flex', gap: '8px' }}>
                <input
                  type="text"
                  inputMode="numeric"
                  placeholder="직접 입력 (원)"
                  value={customAmountDisplay}
                  onChange={(e) => {
                    const formatted = formatWithComma(e.target.value)
                    setCustomAmountDisplay(formatted)
                    if (formatted !== '') setSelectedPresetAmount(null)
                  }}
                  onFocus={() => setSelectedPresetAmount(null)}
                  style={{
                    flex: 1,
                    padding: '10px 12px',
                    borderRadius: '8px',
                    border: '1px solid #e0e0e0',
                    fontSize: '14px',
                  }}
                />
                <button
                  type="button"
                  onClick={handleCharge}
                  disabled={loading || chargeAmount < 100 || isSubmitting}
                  style={{
                    padding: '10px 20px',
                    borderRadius: '8px',
                    border: 'none',
                    background:
                      chargeAmount >= 100 && !isSubmitting
                        ? 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
                        : '#ccc',
                    color: 'white',
                    cursor: chargeAmount >= 100 && !isSubmitting ? 'pointer' : 'not-allowed',
                    fontSize: '14px',
                    fontWeight: 600,
                    whiteSpace: 'nowrap',
                  }}
                >
                  {isSubmitting ? '처리 중...' : `${chargeAmount >= 100 ? `${chargeAmount.toLocaleString()}원 ` : ''}충전하기`}
                </button>
              </div>
            </div>
          </div>

      </div>
    </MypageLayout>
  )
}

