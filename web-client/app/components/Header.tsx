'use client'

import Link from 'next/link'
import { useEffect, useRef, useState } from 'react'
import { useRouter } from 'next/navigation'

export default function Header() {
  const router = useRouter()
  const [isLoggedIn, setIsLoggedIn] = useState(false)
  const [showProfileMenu, setShowProfileMenu] = useState(false)
  const profileRef = useRef<HTMLDivElement | null>(null)

  useEffect(() => {
    // localStorage에서 로그인 상태 확인
    const checkLoginStatus = () => {
      const accessToken = localStorage.getItem('accessToken')
      setIsLoggedIn(!!accessToken)
    }

    checkLoginStatus()

    // storage 이벤트 리스너 추가 (다른 탭에서 로그인/로그아웃 시 동기화)
    window.addEventListener('storage', checkLoginStatus)
    
    // 커스텀 이벤트 리스너 추가 (같은 탭에서 로그인 시)
    window.addEventListener('loginStatusChanged', checkLoginStatus)

    // 프로필 드롭다운 바깥 클릭 시 닫기
    const handleClickOutside = (event: MouseEvent) => {
      if (!profileRef.current) return
      if (!profileRef.current.contains(event.target as Node)) {
        setShowProfileMenu(false)
      }
    }

    document.addEventListener('mousedown', handleClickOutside)

    return () => {
      window.removeEventListener('storage', checkLoginStatus)
      window.removeEventListener('loginStatusChanged', checkLoginStatus)
      document.removeEventListener('mousedown', handleClickOutside)
    }
  }, [])

  const handleLogout = () => {
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('accessTokenExpiresIn')
    localStorage.removeItem('refreshTokenExpiresIn')
    setIsLoggedIn(false)
    setShowProfileMenu(false)
    
    // 로그인 상태 변경 이벤트 발생
    window.dispatchEvent(new Event('loginStatusChanged'))
    
    router.push('/')
  }

  return (
    <header className="header">
      <div className="header-container">
        <div className="logo">
          <Link href="/">뭐든사</Link>
        </div>
        <nav className="nav">
          <Link href="/fashion">패션</Link>
          <Link href="/beauty">뷰티</Link>
          <Link href="/sale">세일</Link>
          <Link href="/magazine">매거진</Link>
        </nav>
        <div className="header-actions">
          <Link href="/search" className="search-btn">검색</Link>
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
                    style={{
                      display: 'block',
                      padding: '12px 16px',
                      textDecoration: 'none',
                      color: '#333',
                      fontSize: '14px',
                      transition: 'background-color 0.2s',
                    }}
                    onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#f5f5f5'}
                    onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'white'}
                  >
                    마이페이지
                  </Link>
                  <Link
                    href="/mypage/orders"
                    style={{
                      display: 'block',
                      padding: '12px 16px',
                      textDecoration: 'none',
                      color: '#333',
                      fontSize: '14px',
                      transition: 'background-color 0.2s',
                    }}
                    onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#f5f5f5'}
                    onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'white'}
                  >
                    주문 내역
                  </Link>
                  <div
                    style={{
                      height: '1px',
                      background: '#e0e0e0',
                      margin: '4px 0',
                    }}
                  />
                  <button
                    onClick={handleLogout}
                    style={{
                      width: '100%',
                      padding: '12px 16px',
                      background: 'none',
                      border: 'none',
                      textAlign: 'left',
                      cursor: 'pointer',
                      color: '#f44336',
                      fontSize: '14px',
                      transition: 'background-color 0.2s',
                    }}
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
