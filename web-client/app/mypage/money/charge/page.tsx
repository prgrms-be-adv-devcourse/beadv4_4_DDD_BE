'use client'

import { useEffect, useState } from 'react'
import MypageLayout from '../../../components/MypageLayout'

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

function formatWithComma(value: string): string {
  const digits = value.replace(/\D/g, '')
  if (digits === '') return ''
  return new Intl.NumberFormat('ko-KR').format(Number(digits))
}

export default function MoneyChargePage() {
  const presetAmounts = [10000, 30000, 50000]
  const [balance, setBalance] = useState<number | null>(null)
  const [customerEmail, setCustomerEmail] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [selectedPresetAmount, setSelectedPresetAmount] = useState<number | null>(null)
  const [customAmountDisplay, setCustomAmountDisplay] = useState('')

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
                  onClick={() =>
                    alert('입력하신 금액으로의 충전은 데모 화면입니다.\n실제 결제는 연동되어 있지 않습니다.')
                  }
                  style={{
                    padding: '10px 20px',
                    borderRadius: '8px',
                    border: 'none',
                    background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                    color: 'white',
                    cursor: 'pointer',
                    fontSize: '14px',
                    fontWeight: 600,
                    whiteSpace: 'nowrap',
                  }}
                >
                  충전하기
                </button>
              </div>
            </div>
          </div>

      </div>
    </MypageLayout>
  )
}

