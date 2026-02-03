'use client'

import Link from 'next/link'
import { useEffect, useRef, useState } from 'react'
import { useRouter } from 'next/navigation'

export default function Header() {
  const router = useRouter()
  const [isLoggedIn, setIsLoggedIn] = useState(false)
  const [showProfileMenu, setShowProfileMenu] = useState(false)
  const profileRef = useRef<HTMLDivElement | null>(null)

  // 1. 쿠키에서 accessToken이 존재하는지 확인하는 함수
  const checkLoginStatus = () => {
    if (typeof window === 'undefined') return

    // document.cookie에서 accessToken이라는 글자가 포함되어 있는지 확인
    const cookies = document.cookie;
    const hasToken = cookies.includes('accessToken=');

    console.log("현재 쿠키 상태:", cookies); // 디버깅용 콘솔
    setIsLoggedIn(hasToken);
  }

  useEffect(() => {
    checkLoginStatus();

    // 로그인 완료 시 발생하는 커스텀 이벤트 감지
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
    }
  }, [])

  // 2. 로그아웃 처리 (쿠키 삭제)
  const handleLogout = () => {
    // 쿠키 삭제 (Path=/ 설정을 해주어야 정확히 삭제됩니다)
    document.cookie = "accessToken=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
    setIsLoggedIn(false);
    setShowProfileMenu(false);
    alert('로그아웃 되었습니다.');
    router.push('/');
  }

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
                      T
                    </div>
                    <span style={{ fontSize: '14px', fontWeight: '500' }}>마이페이지</span>
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