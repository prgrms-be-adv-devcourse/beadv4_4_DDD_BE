'use client'

import Link from 'next/link'
import { useSearchParams, useRouter } from 'next/navigation'
import { useEffect, useRef, useState, Suspense } from 'react'
import Header from '../../../../components/Header'

interface ConfirmPaymentResponse {
  orderNo: string
}

function ChargeSuccessContent() {
  const searchParams = useSearchParams()
  const router = useRouter()
  const [isConfirming, setIsConfirming] = useState(true)
  const [confirmError, setConfirmError] = useState<string | null>(null)
  const [paymentInfo, setPaymentInfo] = useState<ConfirmPaymentResponse | null>(null)
  const hasCalledRef = useRef(false)

  useEffect(() => {
    if (hasCalledRef.current) return
    const confirmPayment = async () => {
      hasCalledRef.current = true
      const orderNo = searchParams.get('orderNo')
      const orderId = searchParams.get('orderId')
      const paymentKey = searchParams.get('paymentKey')
      const amount = searchParams.get('amount')
      const memberId = searchParams.get('memberId')
      const pgCustomerName = searchParams.get('pgCustomerName')
      const pgCustomerEmail = searchParams.get('pgCustomerEmail')

      if (!paymentKey && !orderId) {
        if (!orderNo || !amount) {
          router.push(`/mypage/money/charge/failure?orderNo=${orderNo || ''}&amount=${amount || ''}`)
          return
        }
        setPaymentInfo({ orderNo })
        setConfirmError(null)
        setIsConfirming(false)
        return
      }

      if (!orderNo || !orderId || !paymentKey || !amount || !memberId || !pgCustomerName || !pgCustomerEmail) {
        router.push(`/mypage/money/charge/failure?orderNo=${orderNo || ''}&amount=${amount || ''}`)
        return
      }

      try {
        const apiUrl = process.env.NEXT_PUBLIC_API_URL || ''
        const accessToken = typeof window !== 'undefined' ? localStorage.getItem('accessToken') : null
        if (!apiUrl || !accessToken?.trim()) {
          setConfirmError('API 설정 또는 로그인을 확인해주세요.')
          setIsConfirming(false)
          return
        }
        const res = await fetch(
          `${apiUrl}/api/v1/payments/${encodeURIComponent(orderNo!)}/payment/confirm/by/tossPayments`,
          {
            method: 'POST',
            headers: {
              Authorization: `Bearer ${accessToken}`,
              'Content-Type': 'application/json',
            },
            body: JSON.stringify({
              paymentKey,
              orderId,
              amount: Number(amount),
              pgCustomerName: pgCustomerName || '',
              pgCustomerEmail: pgCustomerEmail || '',
            }),
          }
        )
        const data = await res.json()
        if (!res.ok) {
          setConfirmError(data?.message || '결제 승인에 실패했습니다.')
          setIsConfirming(false)
          return
        }
        if (data?.isSuccess && data?.result) {
          setPaymentInfo({ orderNo: data.result.orderNo ?? orderNo ?? 'N/A' })
          setConfirmError(null)
        } else {
          setConfirmError(data?.message || '결제 승인에 실패했습니다.')
        }
      } catch (error) {
        setConfirmError(error instanceof Error ? error.message : '결제 승인 중 오류가 발생했습니다.')
      } finally {
        setIsConfirming(false)
      }
    }
    confirmPayment()
  }, [searchParams, router])

  if (isConfirming) {
    return (
      <div className="home-page">
        <Header />
        <div style={{ padding: '40px 20px', paddingTop: '12vh', minHeight: 'calc(100vh - 80px)', display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
          <div className="container" style={{ maxWidth: '600px', width: '100%', textAlign: 'center' }}>
          <div
            style={{
              width: '64px',
              height: '64px',
              border: '4px solid #f3f3f3',
              borderTop: '4px solid #4CAF50',
              borderRadius: '50%',
              animation: 'spin 1s linear infinite',
              margin: '0 auto 24px',
            }}
          />
          <h1 style={{ fontSize: '24px', fontWeight: 700, marginBottom: '8px' }}>충전 승인 중...</h1>
          <p style={{ color: '#666' }}>잠시만 기다려주세요.</p>
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

  if (confirmError) {
    return (
      <div className="home-page">
        <Header />
        <div style={{ padding: '40px 20px', paddingTop: '12vh', minHeight: 'calc(100vh - 80px)', display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
          <div className="container" style={{ maxWidth: '600px', width: '100%', textAlign: 'center' }}>
          <div style={{ marginBottom: '24px' }}>
            <svg width="64" height="64" viewBox="0 0 64 64" fill="none" style={{ margin: '0 auto' }}>
              <circle cx="32" cy="32" r="32" fill="#f44336" />
              <path d="M20 20L44 44M44 20L20 44" stroke="white" strokeWidth="4" strokeLinecap="round" />
            </svg>
          </div>
          <h1 style={{ fontSize: '24px', fontWeight: 700, marginBottom: '8px' }}>충전 승인 실패</h1>
          <p style={{ color: '#666', marginBottom: '24px' }}>{confirmError}</p>
          <Link
            href="/mypage/money/charge"
            style={{
              display: 'inline-block',
              padding: '12px 24px',
              borderRadius: '8px',
              background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
              color: 'white',
              fontWeight: 600,
              textDecoration: 'none',
            }}
          >
            충전하기로 돌아가기
          </Link>
          </div>
        </div>
      </div>
    )
  }

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
    second: '2-digit',
  })

  return (
    <div className="home-page">
      <Header />
      <div style={{ padding: '40px 20px', paddingTop: '12vh', minHeight: 'calc(100vh - 80px)', display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
        <div className="container" style={{ maxWidth: '600px', width: '100%' }}>
        <div style={{ textAlign: 'center', marginBottom: '32px' }}>
          <div style={{ marginBottom: '24px' }}>
            <svg width="64" height="64" viewBox="0 0 64 64" fill="none" style={{ margin: '0 auto' }}>
              <circle cx="32" cy="32" r="32" fill="#4CAF50" />
              <path
                d="M20 32L28 40L44 24"
                stroke="white"
                strokeWidth="4"
                strokeLinecap="round"
                strokeLinejoin="round"
              />
            </svg>
          </div>
          <h1 style={{ fontSize: '24px', fontWeight: 700, marginBottom: '8px' }}>충전이 완료되었습니다</h1>
          <p style={{ color: '#666' }}>뭐든사 머니가 정상적으로 충전되었습니다.</p>
        </div>

        <div
          style={{
            background: 'white',
            borderRadius: '12px',
            padding: '24px',
            boxShadow: '0 2px 8px rgba(0, 0, 0, 0.1)',
            marginBottom: '24px',
          }}
        >
          <h2 style={{ fontSize: '16px', fontWeight: 600, marginBottom: '16px' }}>충전 정보</h2>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
              <span style={{ color: '#666' }}>충전번호</span>
              <span>{orderNo}</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
              <span style={{ color: '#666' }}>충전일시</span>
              <span>{paymentDate}</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
              <span style={{ color: '#666' }}>결제수단</span>
              <span>토스페이</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
              <span style={{ color: '#666' }}>충전금액</span>
              <span style={{ fontWeight: 600 }}>{amount}</span>
            </div>
          </div>
        </div>

        <div style={{ display: 'flex', gap: '12px', justifyContent: 'center', flexWrap: 'wrap' }}>
          <Link
            href="/mypage/money/charge"
            style={{
              display: 'inline-block',
              padding: '12px 24px',
              borderRadius: '8px',
              background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
              color: 'white',
              fontWeight: 600,
              textDecoration: 'none',
            }}
          >
            추가 충전하기
          </Link>
          <Link
            href="/mypage"
            style={{
              display: 'inline-block',
              padding: '12px 24px',
              borderRadius: '8px',
              background: '#666',
              color: 'white',
              fontWeight: 600,
              textDecoration: 'none',
            }}
          >
            마이페이지로
          </Link>
        </div>
        </div>
      </div>
    </div>
  )
}

export default function ChargeSuccessPage() {
  return (
    <Suspense
      fallback={
        <div className="home-page">
          <Header />
          <div style={{ padding: '40px 20px', paddingTop: '12vh', minHeight: 'calc(100vh - 80px)', display: 'flex', flexDirection: 'column', alignItems: 'center', textAlign: 'center' }}>
            <div
              style={{
                width: '48px',
                height: '48px',
                border: '4px solid #f3f3f3',
                borderTop: '4px solid #4CAF50',
                borderRadius: '50%',
                animation: 'spin 1s linear infinite',
                margin: '0 auto 16px',
              }}
            />
            <p>로딩 중...</p>
            <style jsx>{`
              @keyframes spin {
                0% { transform: rotate(0deg); }
                100% { transform: rotate(360deg); }
              }
            `}</style>
          </div>
        </div>
      }
    >
      <ChargeSuccessContent />
    </Suspense>
  )
}
