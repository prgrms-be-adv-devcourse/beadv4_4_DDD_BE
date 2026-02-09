'use client'

import Link from 'next/link'
import { useEffect, useRef, useState } from 'react'
import { useRouter } from 'next/navigation'
import api from '../lib/axios'

export default function Header() {
  const router = useRouter()
  const [isLoggedIn, setIsLoggedIn] = useState(false)
  const [showProfileMenu, setShowProfileMenu] = useState(false)
  const profileRef = useRef<HTMLDivElement | null>(null)

  // 기본 정보 상태 추가
  const [realName, setRealName] = useState('')
  const [email, setEmail] = useState('')

  // 1. 서버에 내 정보를 물어봐서 로그인 상태를 확인하는 함수
  const checkLoginStatus = async () => {
    try {
      // HttpOnly 쿠키가 자동으로 전송됩니다.
      const response = await api.get('/api/v1/auths/me');
      if (response.data.isSuccess) {
        setIsLoggedIn(true);
        // 로그인 상태이면 기본정보도 조회
        fetchBasicInfo();
      } else {
        setIsLoggedIn(false);
      }
    } catch (error) {
      // 401 에러 등이 발생하면 비로그인 상태로 간주
      setIsLoggedIn(false);
    }
  }

  // 기본정보 조회
  const fetchBasicInfo = async () => {
    try {
      const response = await api.get('/api/v1/members/me/basic-info');
      const basicInfo = response.data.result;

      setRealName(basicInfo.realName || '');
      setEmail(basicInfo.email || '');
    } catch (error) {
      console.error('기본정보 조회 실패:', error);
    }
  }

  useEffect(() => {
    checkLoginStatus();

    // 로그인 완료 커스텀 이벤트 감지 시에도 API로 재확인
    window.addEventListener('loginStatusChanged', checkLoginStatus);

    const handleClickOutside = (event: MouseEvent) => {
      if (profileRef.current && !profileRef.current.contains(event.target as Node)) {
        setShowProfileMenu(false);
      }
    }

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      window.removeEventListener('loginStatusChanged', checkLoginStatus);
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);

// 2. 서버 로그아웃 API 호출
  const handleLogout = async () => {
    try {
      // HttpOnly 쿠키 제거
      const response = await api.post('/api/v1/auths/logout');
      if (response.data.isSuccess) {
        setIsLoggedIn(false);
        setShowProfileMenu(false);
        // 기본정보 초기화
        setRealName('');
        setEmail('');
        alert('로그아웃 되었습니다.');
        router.push('/');
      }
    } catch (error) {
      console.error("로그아웃 실패:", error);
      alert('로그아웃 처리 중 오류가 발생했습니다.');
    }
  };

  // 아바타 글자 (realName의 첫 글자)
  const avatarLetter = realName ? realName.charAt(0).toUpperCase() : 'U';

  return (
      <header className="header">
        <div className="header-container">
          <div className="logo">
            <Link href="/">뭐든사</Link>
          </div>

          {/* 중앙 내비게이션 메뉴 */}
          <nav className="nav">
            <Link href="/fashion">패션</Link>
            <Link href="/beauty">뷰티</Link>
            <Link href="/magazine">매거진</Link>
          </nav>

          <div className="header-actions">
            <Link href="/cart" className="cart-btn">장바구니</Link>

            {isLoggedIn ? (
                <div
                    className="user-profile-wrapper"
                    style={{ position: 'relative' }}
                    ref={profileRef}
                >
                  <button
                      className="user-profile-btn"
                      style={{
                        background: 'none',
                        border: 'none',
                        cursor: 'pointer',
                        display: 'flex',
                        alignItems: 'center',
                        gap: '8px',
                        padding: '8px 12px',
                        borderRadius: '20px',
                        transition: 'background-color 0.2s',
                      }}
                      onClick={() => setShowProfileMenu((prev) => !prev)}
                  >
                    <div
                        style={{
                          width: '32px',
                          height: '32px',
                          borderRadius: '50%',
                          background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'center',
                          color: 'white',
                          fontWeight: '600',
                          fontSize: '14px',
                        }}
                    >
                      {avatarLetter}
                    </div>
                    <span style={{ fontSize: '14px', fontWeight: '500' }}>
                      {realName ? `${realName} 님` : '마이페이지'}
                    </span>
                  </button>

                  {showProfileMenu && (
                      <div
                          style={{
                            position: 'absolute',
                            top: '100%',
                            right: 0,
                            marginTop: '8px',
                            background: 'white',
                            borderRadius: '8px',
                            boxShadow: '0 4px 12px rgba(0, 0, 0, 0.15)',
                            minWidth: '160px',
                            zIndex: 1000,
                            overflow: 'hidden',
                          }}
                      >
                        <div
                            style={{
                              padding: '12px 16px',
                              borderBottom: '1px solid #e0e0e0',
                              backgroundColor: '#f9f9f9'
                            }}
                        >
                          <div style={{ fontSize: '13px', fontWeight: '600', color: '#333', marginBottom: '2px' }}>
                            {realName || '사용자'}
                          </div>
                          <div style={{ fontSize: '12px', color: '#666' }}>
                            {email || 'test@example.com'}
                          </div>
                        </div>
                        <Link
                            href="/mypage"
                            style={{ display: 'block', padding: '12px 16px', textDecoration: 'none', color: '#333', fontSize: '14px' }}
                            onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#f5f5f5'}
                            onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'white'}
                        >
                          마이페이지
                        </Link>
                        <Link
                            href="/mypage/orders"
                            style={{ display: 'block', padding: '12px 16px', textDecoration: 'none', color: '#333', fontSize: '14px' }}
                            onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#f5f5f5'}
                            onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'white'}
                        >
                          주문 내역
                        </Link>
                        <div style={{ height: '1px', background: '#e0e0e0', margin: '4px 0' }} />
                        <button
                            onClick={handleLogout}
                            style={{ width: '100%', padding: '12px 16px', background: 'none', border: 'none', textAlign: 'left', cursor: 'pointer', color: '#f44336', fontSize: '14px' }}
                            onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#f5f5f5'}
                            onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'white'}
                        >
                          로그아웃
                        </button>
                      </div>
                  )}
                </div>
            ) : (
                <Link href="/login" className="user-btn">로그인</Link>
            )}
          </div>
        </div>
      </header>
  )
}