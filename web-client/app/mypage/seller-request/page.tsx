'use client'

import Link from 'next/link'
import { useEffect, useState } from 'react'
import MypageLayout from '../../components/MypageLayout'

export default function SellerRequestPage() {
  const [email, setEmail] = useState('test@example.com')

  useEffect(() => {
    if (typeof window === 'undefined') return
    const stored = localStorage.getItem('email')
    if (stored) {
      setEmail(stored)
    }
  }, [])

  return (
    <MypageLayout>
      <div style={{ maxWidth: '600px' }}>
        <h1 style={{ fontSize: '28px', fontWeight: 700, marginBottom: '24px' }}>판매자 전환</h1>

          {/* 상단 프로필 카드 */}
          <div
            style={{
              background: 'white',
              borderRadius: '12px',
              padding: '24px',
              boxShadow: '0 2px 8px rgba(0, 0, 0, 0.1)',
              marginBottom: '24px',
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: '16px', marginBottom: '8px' }}>
              <div
                style={{
                  width: '64px',
                  height: '64px',
                  borderRadius: '50%',
                  background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  color: 'white',
                  fontWeight: '600',
                  fontSize: '26px',
                }}
              >
                T
              </div>
              <div>
                <div style={{ fontSize: '14px', color: '#666', marginBottom: '4px' }}>이름</div>
                <div style={{ fontSize: '16px', fontWeight: 600 }}>테스트 사용자</div>
                <div style={{ fontSize: '13px', color: '#666', marginTop: '2px' }}>{email}</div>
              </div>
            </div>
            <p style={{ fontSize: '13px', color: '#666', marginTop: '8px', lineHeight: 1.5 }}>
              판매자 전환을 신청하면 상품 관리, 주문 관리 기능을 사용할 수 있어요. 아래 정보를 입력하고 신청해 주세요.
            </p>
          </div>

          {/* 판매자 전환 신청 카드 */}
          <div
            style={{
              background: 'white',
              borderRadius: '12px',
              padding: '24px',
              boxShadow: '0 2px 8px rgba(0, 0, 0, 0.1)',
            }}
          >
            {/* 사업자 정보 */}
            <div style={{ marginBottom: '16px' }}>
              <label style={{ display: 'block', fontSize: '14px', fontWeight: 500, marginBottom: '6px' }}>
                상호명
              </label>
              <input
                type="text"
                placeholder="예: 뭐든사 주식회사"
                style={{
                  width: '100%',
                  padding: '10px 12px',
                  borderRadius: '8px',
                  border: '1px solid #e0e0e0',
                  fontSize: '14px',
                }}
              />
            </div>

            <div style={{ marginBottom: '16px' }}>
              <label style={{ display: 'block', fontSize: '14px', fontWeight: 500, marginBottom: '6px' }}>
                대표자명
              </label>
              <input
                type="text"
                placeholder="예: 홍길동"
                style={{
                  width: '100%',
                  padding: '10px 12px',
                  borderRadius: '8px',
                  border: '1px solid #e0e0e0',
                  fontSize: '14px',
                }}
              />
            </div>

            {/* 정산 계좌 정보 */}
            <div style={{ marginBottom: '16px' }}>
              <label style={{ display: 'block', fontSize: '14px', fontWeight: 500, marginBottom: '6px' }}>
                정산 은행명
              </label>
              <input
                type="text"
                placeholder="예: 국민은행"
                style={{
                  width: '100%',
                  padding: '10px 12px',
                  borderRadius: '8px',
                  border: '1px solid #e0e0e0',
                  fontSize: '14px',
                }}
              />
            </div>

            <div style={{ marginBottom: '16px' }}>
              <label style={{ display: 'block', fontSize: '14px', fontWeight: 500, marginBottom: '6px' }}>
                정산 계좌번호
              </label>
              <input
                type="text"
                placeholder="예: 123456-01-123456"
                style={{
                  width: '100%',
                  padding: '10px 12px',
                  borderRadius: '8px',
                  border: '1px solid #e0e0e0',
                  fontSize: '14px',
                }}
              />
            </div>

            {/* 사업자등록증 업로드 */}
            <div style={{ marginBottom: '16px' }}>
              <label style={{ display: 'block', fontSize: '14px', fontWeight: 500, marginBottom: '6px' }}>
                사업자등록증
              </label>
              <input
                type="file"
                accept="image/*,.pdf"
                style={{ fontSize: '13px' }}
              />
              <p style={{ marginTop: '6px', fontSize: '12px', color: '#777' }}>
                사업자등록증 사본 파일을 업로드해 주세요. (이미지 또는 PDF)
              </p>
            </div>

            <button
              type="button"
              onClick={() => alert('판매자 전환 기능은 Mock 화면입니다.')}
              style={{
                width: '100%',
                marginTop: '8px',
                padding: '10px 0',
                borderRadius: '8px',
                border: 'none',
                background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                color: 'white',
                cursor: 'pointer',
                fontSize: '14px',
                fontWeight: 600,
              }}
            >
              판매자 전환 신청하기
            </button>
          </div>

          {/* 상품 관리 카드 */}
          <div
            style={{
              marginTop: '24px',
              background: 'white',
              borderRadius: '12px',
              padding: '24px',
              boxShadow: '0 2px 8px rgba(0, 0, 0, 0.08)',
            }}
          >
            <h2 style={{ fontSize: '18px', fontWeight: 600, marginBottom: '8px' }}>상품 관리</h2>
            <p style={{ fontSize: '13px', color: '#666', marginBottom: '16px' }}>
              판매자 전환이 완료되면 바로 상품을 관리해 보세요.
            </p>
            <Link
              href="/products/create"
              style={{
                display: 'inline-block',
                padding: '10px 18px',
                borderRadius: '8px',
                border: 'none',
                background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                color: 'white',
                fontSize: '14px',
                fontWeight: 600,
                textDecoration: 'none',
              }}
            >
              상품 관리하러 가기
            </Link>
          </div>

          <div style={{ marginTop: '24px', textAlign: 'center' }}>
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
    </MypageLayout>
  )
}

