'use client'

import Link from 'next/link'
import { useEffect, useState } from 'react'
import MypageLayout from '../../components/MypageLayout'

export default function ProfilePage() {
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
        <h1 style={{ fontSize: '28px', fontWeight: 700, marginBottom: '24px' }}>기본 정보 수정</h1>

          <div
            style={{
              background: 'white',
              borderRadius: '12px',
              padding: '24px',
              boxShadow: '0 2px 8px rgba(0, 0, 0, 0.1)',
              marginBottom: '24px',
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: '16px', marginBottom: '16px' }}>
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
          </div>

          <div
            style={{
              background: 'white',
              borderRadius: '12px',
              padding: '24px',
              boxShadow: '0 2px 8px rgba(0, 0, 0, 0.1)',
            }}
          >
            <div style={{ marginBottom: '16px' }}>
              <label style={{ display: 'block', fontSize: '14px', fontWeight: 500, marginBottom: '6px' }}>
                이름
              </label>
              <input
                type="text"
                defaultValue="테스트 사용자"
                disabled
                style={{
                  width: '100%',
                  padding: '10px 12px',
                  borderRadius: '8px',
                  border: '1px solid #e0e0e0',
                  fontSize: '14px',
                  backgroundColor: '#f9f9f9',
                  color: '#999',
                }}
              />
            </div>

            <div style={{ marginBottom: '16px' }}>
              <label style={{ display: 'block', fontSize: '14px', fontWeight: 500, marginBottom: '6px' }}>
                이메일
              </label>
              <input
                type="email"
                value={email}
                disabled
                style={{
                  width: '100%',
                  padding: '10px 12px',
                  borderRadius: '8px',
                  border: '1px solid #e0e0e0',
                  fontSize: '14px',
                  backgroundColor: '#f9f9f9',
                  color: '#999',
                }}
              />
            </div>

            <div style={{ marginBottom: '16px' }}>
              <label style={{ display: 'block', fontSize: '14px', fontWeight: 500, marginBottom: '6px' }}>
                연락처
              </label>
              <input
                type="text"
                defaultValue="010-1234-5678"
                disabled
                style={{
                  width: '100%',
                  padding: '10px 12px',
                  borderRadius: '8px',
                  border: '1px solid #e0e0e0',
                  fontSize: '14px',
                  backgroundColor: '#f9f9f9',
                  color: '#999',
                }}
              />
            </div>

            <button
              type="button"
              onClick={() => alert('프로필 수정 기능은 Mock 화면입니다.')}
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
              저장하기
            </button>
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

