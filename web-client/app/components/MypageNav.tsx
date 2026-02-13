'use client'

import Link from 'next/link'
import { usePathname } from 'next/navigation'
import { useEffect, useState } from 'react'
import api from "@/app/lib/axios";

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

interface MypageNavProps {
  role?: string;
}

export default function MypageNav({ role: externalRole }: MypageNavProps) {
  const [realName, setRealName] = useState('')
  const [email, setEmail] = useState('')
  const [profileImageUrl, setProfileImageUrl] = useState('')
  const [internalRole, setInternalRole] = useState('')
  const [loading, setLoading] = useState(true)

  const fetchInfo = async () => {
    try {
      setLoading(true)
      const [basicRes, profileRes] = await Promise.allSettled([
        api.get('/api/v1/members/me/basic-info'),
        api.get('/api/v1/members/me/profile')
      ])

      if (basicRes.status === 'fulfilled' && basicRes.value.data.isSuccess) {
        const result = basicRes.value.data.result;
        setRealName(result.realName || '')
        setEmail(result.email || '')
        setInternalRole(result.role || 'MEMBER')
      }

      if (profileRes.status === 'fulfilled' && profileRes.value.data.isSuccess) {
        setProfileImageUrl(profileRes.value.data.result.profileImageUrl || '')
      }
    } finally {
      setLoading(false)
    }
  }

  // 기본 정보 + 프로필 조회
  useEffect(() => {
    fetchInfo()
    window.addEventListener('loginStatusChanged', fetchInfo)
    return () => {
      window.removeEventListener('loginStatusChanged', fetchInfo)
    }
  }, [])

  const finalRole = externalRole || internalRole;
  const isSeller = finalRole === 'SELLER'

  // 아바타 글자 (realName의 첫 글자)
  const avatarLetter = realName ? realName.charAt(0).toUpperCase() : 'U'

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
                    background: profileImageUrl
                        ? `url(${profileImageUrl}) center/cover`
                        : 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    color: 'white',
                    fontWeight: 600,
                    fontSize: '18px',
                    overflow: 'hidden'
                  }}
              >
                {loading ? '...' : (!profileImageUrl && avatarLetter)}
              </div>
              <div>
                <div style={{ fontSize: '13px', fontWeight: 600 }}>
                  {loading ? '로딩 중...' : email || '이메일 미등록'}
                </div>
                <div style={{ fontSize: '12px', color: '#777' }}>
                  {loading ? '로딩 중...' : realName ? `${realName} 님` : '이름 미등록'}
                </div>
              </div>
            </div>
          </div>

          <nav style={{ display: 'flex', flexDirection: 'column', gap: '4px', fontSize: '14px' }}>
            <div style={{ fontSize: '12px', color: '#999', margin: '8px 0 4px' }}>내 정보</div>
            <NavLink href="/mypage">마이페이지 홈</NavLink>
            <NavLink href="/mypage/basic-info">기본 정보</NavLink>
            <NavLink href="/mypage/profile">프로필</NavLink>
            <NavLink href="/mypage/address">배송지</NavLink>
            <NavLink href="/mypage/social">소셜 연동</NavLink>

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

        {/* 판매 관련 섹션: 판매자 여부에 따라 내용 분기 */}
        <aside style={cardStyle}>
          <nav style={{ display: 'flex', flexDirection: 'column', gap: '4px', fontSize: '14px' }}>
            <div style={{ fontSize: '12px', color: '#999', margin: '8px 0 4px' }}>판매</div>
            {isSeller ? (
                <>
                  <NavLink href="/mypage/seller-info">판매자 정보</NavLink>
                  <NavLink href="/mypage/products">상품 관리</NavLink>
                  <NavLink href="/mypage/stock">재고 관리</NavLink>
                  <NavLink href="/mypage/settlement">정산 내역</NavLink>
                </>
            ) : (
                // 판매자가 아닐 경우 표시되는 링크
                <NavLink href="/mypage/seller-request">판매자 전환</NavLink>
            )}
          </nav>
        </aside>
      </div>
  )
}