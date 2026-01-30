'use client'

import Link from 'next/link'
import { useEffect, useState } from 'react'
import MypageLayout from '../../components/MypageLayout'

export default function ProfilePage() {
  const [email, setEmail] = useState('test@example.com')
  const [linkedNaver, setLinkedNaver] = useState(false)
  const [linkedKakao, setLinkedKakao] = useState(false)

  useEffect(() => {
    if (typeof window === 'undefined') return
    const stored = localStorage.getItem('email')
    if (stored) {
      setEmail(stored)
    }
    const naver = localStorage.getItem('social_naver')
    const kakao = localStorage.getItem('social_kakao')
    if (naver !== null) setLinkedNaver(naver === 'true')
    if (kakao !== null) setLinkedKakao(kakao === 'true')
  }, [])

  const handleNaverToggle = () => {
    const next = !linkedNaver
    setLinkedNaver(next)
    if (typeof window !== 'undefined') localStorage.setItem('social_naver', String(next))
    alert(next ? '네이버 계정이 연동되었습니다. (데모)' : '네이버 연동이 해제되었습니다. (데모)')
  }

  const handleKakaoToggle = () => {
    const next = !linkedKakao
    setLinkedKakao(next)
    if (typeof window !== 'undefined') localStorage.setItem('social_kakao', String(next))
    alert(next ? '카카오 계정이 연동되었습니다. (데모)' : '카카오 연동이 해제되었습니다. (데모)')
  }

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
              marginBottom: '24px',
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

          {/* 소셜 연동 */}
          <div
            style={{
              background: 'white',
              borderRadius: '12px',
              padding: '24px',
              boxShadow: '0 2px 8px rgba(0, 0, 0, 0.1)',
            }}
          >
            <div style={{ fontSize: '16px', fontWeight: 600, marginBottom: '16px' }}>소셜 연동</div>
            <p style={{ fontSize: '13px', color: '#666', marginBottom: '16px' }}>
              연동된 계정으로 간편 로그인할 수 있어요.
            </p>

            {/* 네이버 */}
            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                padding: '14px 0',
                borderBottom: '1px solid #f0f0f0',
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                <span
                  style={{
                    display: 'inline-flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    width: '36px',
                    height: '36px',
                    borderRadius: '8px',
                    background: '#03C75A',
                    color: 'white',
                    fontSize: '14px',
                    fontWeight: 700,
                  }}
                >
                  N
                </span>
                <span style={{ fontSize: '15px', fontWeight: 500 }}>네이버</span>
              </div>
              <button
                type="button"
                onClick={handleNaverToggle}
                role="switch"
                aria-checked={linkedNaver}
                style={{
                  width: '48px',
                  height: '28px',
                  borderRadius: '14px',
                  border: 'none',
                  background: linkedNaver ? 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' : '#e0e0e0',
                  cursor: 'pointer',
                  position: 'relative',
                  transition: 'background 0.2s',
                }}
              >
                <span
                  style={{
                    position: 'absolute',
                    top: '3px',
                    left: linkedNaver ? '23px' : '3px',
                    width: '22px',
                    height: '22px',
                    borderRadius: '50%',
                    background: 'white',
                    boxShadow: '0 1px 3px rgba(0,0,0,0.2)',
                    transition: 'left 0.2s',
                  }}
                />
              </button>
            </div>

            {/* 카카오 */}
            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                padding: '14px 0',
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                <span
                  style={{
                    display: 'inline-flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    width: '36px',
                    height: '36px',
                    borderRadius: '8px',
                    background: '#FEE500',
                    color: '#191919',
                    fontSize: '14px',
                    fontWeight: 700,
                  }}
                >
                  K
                </span>
                <span style={{ fontSize: '15px', fontWeight: 500 }}>카카오</span>
              </div>
              <button
                type="button"
                onClick={handleKakaoToggle}
                role="switch"
                aria-checked={linkedKakao}
                style={{
                  width: '48px',
                  height: '28px',
                  borderRadius: '14px',
                  border: 'none',
                  background: linkedKakao ? 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' : '#e0e0e0',
                  cursor: 'pointer',
                  position: 'relative',
                  transition: 'background 0.2s',
                }}
              >
                <span
                  style={{
                    position: 'absolute',
                    top: '3px',
                    left: linkedKakao ? '23px' : '3px',
                    width: '22px',
                    height: '22px',
                    borderRadius: '50%',
                    background: 'white',
                    boxShadow: '0 1px 3px rgba(0,0,0,0.2)',
                    transition: 'left 0.2s',
                  }}
                />
              </button>
            </div>
          </div>

      </div>
    </MypageLayout>
  )
}

