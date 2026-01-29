'use client'

import Link from 'next/link'
import { usePathname } from 'next/navigation'

const linkStyle = {
  padding: '8px 10px',
  borderRadius: '8px',
  textDecoration: 'none' as const,
  color: '#333',
  fontSize: '13px',
}

function NavLink({
  href,
  children,
}: {
  href: string
  children: React.ReactNode
}) {
  const pathname = usePathname()
  const isActive = pathname === href
  return (
    <Link
      href={href}
      style={{
        ...linkStyle,
        ...(isActive
          ? { background: '#f1f3ff', color: '#667eea', fontWeight: 600 }
          : {}),
      }}
    >
      {children}
    </Link>
  )
}

const cardStyle = {
  width: '220px',
  flexShrink: 0 as const,
  background: 'white',
  borderRadius: '12px',
  padding: '20px 16px',
  boxShadow: '0 2px 8px rgba(0, 0, 0, 0.08)',
}

export default function MypageNav() {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px', width: '220px', flexShrink: 0 }}>
      <aside style={cardStyle}>
        <div style={{ marginBottom: '16px' }}>
          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '12px',
              marginBottom: '8px',
            }}
          >
            <div
              style={{
                width: '40px',
                height: '40px',
                borderRadius: '50%',
                background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: 'white',
                fontWeight: 600,
                fontSize: '18px',
              }}
            >
              T
            </div>
            <div>
              <div style={{ fontSize: '13px', fontWeight: 600 }}>test@example.com</div>
              <div style={{ fontSize: '12px', color: '#777' }}>뭐든사 회원</div>
            </div>
          </div>
        </div>

        <nav style={{ display: 'flex', flexDirection: 'column', gap: '4px', fontSize: '14px' }}>
          <div style={{ fontSize: '12px', color: '#999', margin: '8px 0 4px' }}>내 정보</div>
          <NavLink href="/mypage">마이페이지 홈</NavLink>
          <NavLink href="/mypage/profile">기본 정보 수정</NavLink>
          <NavLink href="/mypage/profile/edit">프로필 수정</NavLink>
          <NavLink href="/mypage/address">배송지</NavLink>

          <div style={{ fontSize: '12px', color: '#999', margin: '12px 0 4px' }}>주문</div>
          <NavLink href="/mypage/orders">주문 내역</NavLink>
          <NavLink href="/mypage/cancel">취소/반품 내역</NavLink>

          <div style={{ fontSize: '12px', color: '#999', margin: '12px 0 4px' }}>뭐든사 머니</div>
          <NavLink href="/mypage/money/charge">충전하기</NavLink>
          <NavLink href="/mypage/money/history">사용 내역</NavLink>

          <div style={{ fontSize: '12px', color: '#999', margin: '12px 0 4px' }}>저장</div>
          <NavLink href="/mypage/favorites">좋아요</NavLink>
        </nav>
      </aside>

      <aside style={cardStyle}>
        <nav style={{ display: 'flex', flexDirection: 'column', gap: '4px', fontSize: '14px' }}>
          <div style={{ fontSize: '12px', color: '#999', margin: '8px 0 4px' }}>판매</div>
          <NavLink href="/mypage/seller-request">판매자정보</NavLink>
          <NavLink href="/products/create">상품 관리</NavLink>
        </nav>
      </aside>
    </div>
  )
}
