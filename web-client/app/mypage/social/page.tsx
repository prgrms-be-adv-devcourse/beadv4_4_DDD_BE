'use client'

import { useEffect, useState } from 'react'
import MypageLayout from '../../components/MypageLayout'

export default function SocialPage() {
  const [linkedNaver, setLinkedNaver] = useState(false)
  const [linkedKakao, setLinkedKakao] = useState(false)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    const fetchStatus = async () => {
      try {
        const response = await fetch('/mypage/social/status');
        const data = await response.json();
        if (data.isSuccess) {
          setLinkedNaver(data.result.linkedNaver);
          setLinkedKakao(data.result.linkedKakao);
        }
      } catch (error) {
        console.error("상태 로드 실패:", error);
      } finally {
        setIsLoading(false);
      }
    };
    fetchStatus()
  }, [])

  const handleToggle = async (type: 'naver' | 'kakao') => {
    const isLinked = type === 'naver' ? linkedNaver : linkedKakao
    if (isLinked) {
      alert('연동 해제 기능은 준비 중입니다.')
      return
    }

    try {
      const redirectUri = `${window.location.origin}/mypage/social/callback/${type}`
      // 폴더 구조에 맞춰 경로 수정
      const response = await fetch(`/mypage/social/link-url?type=${type}&redirectUri=${encodeURIComponent(redirectUri)}`)
      const data = await response.json()

      if (data.isSuccess) {
        window.location.href = data.result
      } else {
        alert(data.message || '인증 URL을 가져오지 못했습니다.')
      }
    } catch (error) {
      alert('연동 요청 중 오류가 발생했습니다.')
    }
  }

  if (isLoading) return <MypageLayout><div>로딩 중...</div></MypageLayout>

  return (
      <MypageLayout>
        <div style={{ maxWidth: '600px' }}>
          <h1 style={{ fontSize: '28px', fontWeight: 700, marginBottom: '24px' }}>소셜 연동</h1>
          <div style={{ background: 'white', borderRadius: '12px', padding: '24px', boxShadow: '0 2px 8px rgba(0, 0, 0, 0.1)' }}>
            <p style={{ fontSize: '14px', color: '#666', marginBottom: '20px' }}>연동된 계정으로 간편 로그인할 수 있어요.</p>
            <SocialItem label="네이버" color="#03C75A" icon="N" isActive={linkedNaver} onToggle={() => handleToggle('naver')} />
            <div style={{ height: '1px', background: '#f0f0f0', margin: '0' }} />
            <SocialItem label="카카오" color="#FEE500" icon="K" textColor="#191919" isActive={linkedKakao} onToggle={() => handleToggle('kakao')} />
          </div>
        </div>
      </MypageLayout>
  )
}

function SocialItem({ label, color, icon, textColor = 'white', isActive, onToggle }: any) {
  return (
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '16px 0' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <span style={{ display: 'inline-flex', alignItems: 'center', justifyContent: 'center', width: '36px', height: '36px', borderRadius: '8px', background: color, color: textColor, fontSize: '14px', fontWeight: 700 }}>{icon}</span>
          <span style={{ fontSize: '15px', fontWeight: 500 }}>{label}</span>
        </div>
        <button onClick={onToggle} style={{ width: '48px', height: '28px', borderRadius: '14px', border: 'none', background: isActive ? 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' : '#e0e0e0', cursor: 'pointer', position: 'relative' }}>
          <span style={{ position: 'absolute', top: '3px', left: isActive ? '23px' : '3px', width: '22px', height: '22px', borderRadius: '50%', background: 'white', transition: 'left 0.2s' }} />
        </button>
      </div>
  )
}