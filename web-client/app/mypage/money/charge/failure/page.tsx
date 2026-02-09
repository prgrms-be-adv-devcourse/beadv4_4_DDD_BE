'use client'

import Link from 'next/link'
import { useSearchParams } from 'next/navigation'
import { Suspense } from 'react'
import Header from '../../../../components/Header'

function ChargeFailureContent() {
  const searchParams = useSearchParams()
  const orderNo = searchParams.get('orderNo') || 'N/A'
  const amount = searchParams.get('amount')
  const message = searchParams.get('message')

  const attemptDate = new Date().toLocaleString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  })

  const formattedAmount = amount
    ? new Intl.NumberFormat('ko-KR').format(parseFloat(amount)) + '원'
    : 'N/A'

  return (
    <div className="home-page">
      <Header />
      <div style={{ padding: '40px 20px', paddingTop: '12vh', minHeight: 'calc(100vh - 80px)', display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
        <div className="container" style={{ maxWidth: '600px', width: '100%' }}>
        <div style={{ textAlign: 'center', marginBottom: '32px' }}>
          <div style={{ marginBottom: '24px' }}>
            <svg width="64" height="64" viewBox="0 0 64 64" fill="none" style={{ margin: '0 auto' }}>
              <circle cx="32" cy="32" r="32" fill="#F44336" />
              <path
                d="M20 20L44 44M44 20L20 44"
                stroke="white"
                strokeWidth="4"
                strokeLinecap="round"
                strokeLinejoin="round"
              />
            </svg>
          </div>
          <h1 style={{ fontSize: '24px', fontWeight: 700, marginBottom: '8px' }}>충전에 실패했습니다</h1>
          <p style={{ color: '#666' }}>
            결제 처리 중 오류가 발생했습니다. 다시 시도해주세요.
          </p>
          {message && (
            <p style={{ color: '#dc3545', fontSize: '14px', marginTop: '8px' }}>
              {decodeURIComponent(message)}
            </p>
          )}
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
              <span style={{ color: '#666' }}>시도일시</span>
              <span>{attemptDate}</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
              <span style={{ color: '#666' }}>결제수단</span>
              <span>토스페이</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
              <span style={{ color: '#666' }}>충전 금액</span>
              <span>{formattedAmount}</span>
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
            다시 충전하기
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

export default function ChargeFailurePage() {
  return (
    <Suspense
      fallback={
        <div className="home-page">
          <Header />
          <div style={{ padding: '40px 20px', paddingTop: '12vh', minHeight: 'calc(100vh - 80px)', display: 'flex', justifyContent: 'center', alignItems: 'center', textAlign: 'center' }}>로딩 중...</div>
        </div>
      }
    >
      <ChargeFailureContent />
    </Suspense>
  )
}
