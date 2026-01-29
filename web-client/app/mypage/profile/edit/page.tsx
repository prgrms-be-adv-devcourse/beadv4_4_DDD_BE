'use client'

import Link from 'next/link'
import { useEffect, useState } from 'react'
import MypageLayout from '../../../components/MypageLayout'

export default function ProfileEditPage() {
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
        <h1 style={{ fontSize: '28px', fontWeight: 700, marginBottom: '24px' }}>프로필 수정</h1>

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
                onClick={() => alert('프로필 이미지 등록 기능은 Mock 화면입니다.')}
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
                  cursor: 'pointer',
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

          {/* 프로필(닉네임/신체/피부) 정보 수정 */}
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
                닉네임
              </label>
              <input
                type="text"
                placeholder="예: 뭐든사_사용자"
                style={{
                  width: '100%',
                  padding: '10px 12px',
                  borderRadius: '8px',
                  border: '1px solid #e0e0e0',
                  fontSize: '14px',
                }}
              />
            </div>

            <div style={{ marginBottom: '16px', display: 'flex', gap: '12px' }}>
              <div style={{ flex: 1 }}>
                <label style={{ display: 'block', fontSize: '14px', fontWeight: 500, marginBottom: '6px' }}>
                  키 (cm)
                </label>
                <input
                  type="number"
                  placeholder="예: 175"
                  style={{
                    width: '100%',
                    padding: '10px 12px',
                    borderRadius: '8px',
                    border: '1px solid #e0e0e0',
                    fontSize: '14px',
                  }}
                />
              </div>
              <div style={{ flex: 1 }}>
                <label style={{ display: 'block', fontSize: '14px', fontWeight: 500, marginBottom: '6px' }}>
                  몸무게 (kg)
                </label>
                <input
                  type="number"
                  placeholder="예: 65"
                  style={{
                    width: '100%',
                    padding: '10px 12px',
                    borderRadius: '8px',
                    border: '1px solid #e0e0e0',
                    fontSize: '14px',
                  }}
                />
              </div>
            </div>

            <div style={{ marginBottom: '16px' }}>
              <label style={{ display: 'block', fontSize: '14px', fontWeight: 500, marginBottom: '6px' }}>
                피부 타입
              </label>
              <select
                defaultValue=""
                style={{
                  width: '100%',
                  padding: '10px 12px',
                  borderRadius: '8px',
                  border: '1px solid #e0e0e0',
                  fontSize: '14px',
                  backgroundColor: 'white',
                }}
              >
                <option value="" disabled>
                  선택해주세요
                </option>
                <option value="normal">중성</option>
                <option value="dry">건성</option>
                <option value="oily">지성</option>
                <option value="combination">복합성</option>
                <option value="sensitive">민감성</option>
              </select>
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

