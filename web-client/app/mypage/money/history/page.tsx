'use client'

import Link from 'next/link'
import Header from '../../../components/Header'

export default function MoneyHistoryPage() {
  return (
    <div className="home-page">
      <Header />

      <div style={{ padding: '40px 20px', minHeight: '60vh' }}>
        <div className="container" style={{ maxWidth: '600px', margin: '0 auto' }}>
          <h1 style={{ fontSize: '24px', fontWeight: 700, marginBottom: '24px' }}>뭐든사 머니 사용 내역</h1>

          <div
            style={{
              background: 'white',
              borderRadius: '12px',
              padding: '24px',
              boxShadow: '0 2px 8px rgba(0, 0, 0, 0.1)',
              marginBottom: '24px',
            }}
          >
            <div style={{ fontSize: '14px', color: '#666', marginBottom: '8px' }}>현재 보유 머니</div>
            <div style={{ fontSize: '22px', fontWeight: 700, marginBottom: '24px' }}>50,000원</div>

            <div
              style={{
                padding: '32px 16px',
                textAlign: 'center',
                color: '#999',
                fontSize: '14px',
                borderTop: '1px solid #eee',
              }}
            >
              사용 내역이 없습니다.
            </div>
          </div>

          <div style={{ textAlign: 'center', marginTop: '16px' }}>
            <Link
              href="/mypage"
              style={{
                display: 'inline-block',
                padding: '10px 20px',
                borderRadius: '8px',
                border: '1px solid #e0e0ff',
                background: '#f8f8ff',
                color: '#667eea',
                fontSize: '13px',
                fontWeight: 500,
                textDecoration: 'none',
              }}
            >
              마이페이지로 돌아가기
            </Link>
          </div>
        </div>
      </div>
    </div>
  )
}
