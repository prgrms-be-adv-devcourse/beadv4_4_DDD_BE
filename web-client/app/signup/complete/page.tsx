// app/signup/complete/page.tsx

'use client'

import { useState, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import api from '@/app/lib/axios' // axios 인스턴스 import

export default function SignupCompletePage() {
  const router = useRouter()
  const [loading, setLoading] = useState(false)

  // 입력 폼 상태
  const [formData, setFormData] = useState({
    realName: '',
    email: '',
    phoneNumber: '',
    nickname: '',
    heightCm: '',
    weightKg: '',
    skinType: '', // 초기값 빈 문자열
  })

  // 페이지 진입 시 기본 정보 불러오기
  useEffect(() => {
    const fetchBasicInfo = async () => {
      try {
        // 내 정보 조회 API
        const response = await api.get('/api/v1/members/me/basic-info')
        if (response.data.isSuccess) {
          const { realName, email, phoneNumber } = response.data.result
          setFormData(prev => ({
            ...prev,
            realName: realName || '',
            email: email || '',
            phoneNumber: phoneNumber || ''
          }))
        }
      } catch (error) {
        console.error('기본 정보 로드 실패', error)
        // 토큰이 없거나 만료되면 로그인 페이지로
        router.replace('/login')
      }
    }
    fetchBasicInfo()
  }, [router])

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target
    setFormData(prev => ({ ...prev, [name]: value }))
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()

    // 간단한 유효성 검사
    if (!formData.nickname || !formData.skinType || !formData.realName || !formData.email) {
      alert('필수 정보를 모두 입력해주세요.')
      return
    }

    try {
      setLoading(true)

      // 백엔드 SignupCompleteRequest DTO 구조에 맞춤
      const requestBody = {
        realName: formData.realName,
        email: formData.email,
        phoneNumber: formData.phoneNumber,
        nickname: formData.nickname,
        heightCm: formData.heightCm ? Number(formData.heightCm) : null,
        weightKg: formData.weightKg ? Number(formData.weightKg) : null,
        skinType: formData.skinType,
        profileImageUrl: null // 필요 시 이미지 업로드 로직 추가
      }

      const response = await api.post('/api/v2/members/signup-complete', requestBody)

      if (response.data.isSuccess) {
        alert('회원가입이 완료되었습니다! 환영합니다.')
        // 로그인 상태 갱신 이벤트 발생
        window.dispatchEvent(new Event('loginStatusChanged'))
        router.replace('/') // 홈으로 이동
      } else {
        alert(response.data.message || '가입 처리에 실패했습니다.')
      }
    } catch (error: any) {
      console.error('가입 완료 요청 실패:', error)
      alert(error.response?.data?.message || '서버 오류가 발생했습니다.')
    } finally {
      setLoading(false)
    }
  }

  return (
      <div style={{ maxWidth: '500px', margin: '60px auto', padding: '20px' }}>
        <h1 style={{ textAlign: 'center', marginBottom: '30px' }}>회원가입 마무리</h1>
        <p style={{ textAlign: 'center', color: '#666', marginBottom: '30px' }}>
          서비스 이용을 위해 필수 정보를 입력해주세요.
        </p>

        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>

          {/* 기본 정보 섹션 */}
          <div style={sectionStyle}>
            <h3>기본 정보</h3>
            <input
                type="text"
                name="realName"
                placeholder="실명 *"
                value={formData.realName}
                onChange={handleChange}
                style={inputStyle}
                required
            />
            <input
                type="email"
                name="email"
                placeholder="이메일 *"
                value={formData.email}
                onChange={handleChange}
                style={inputStyle}
                required
            />
            <input
                type="tel"
                name="phoneNumber"
                placeholder="전화번호 *"
                value={formData.phoneNumber}
                onChange={handleChange}
                style={inputStyle}
                required
            />
          </div>

          {/* 프로필 정보 섹션 */}
          <div style={sectionStyle}>
            <h3>프로필 정보</h3>
            <input
                type="text"
                name="nickname"
                placeholder="닉네임 (필수) *"
                value={formData.nickname}
                onChange={handleChange}
                style={inputStyle}
                required
            />

            <div style={{ display: 'flex', gap: '10px' }}>
              <input
                  type="number"
                  name="heightCm"
                  placeholder="키 (cm)"
                  value={formData.heightCm}
                  onChange={handleChange}
                  style={{ ...inputStyle, flex: 1 }}
              />
              <input
                  type="number"
                  name="weightKg"
                  placeholder="몸무게 (kg)"
                  value={formData.weightKg}
                  onChange={handleChange}
                  style={{ ...inputStyle, flex: 1 }}
              />
            </div>

            <select
                name="skinType"
                value={formData.skinType}
                onChange={handleChange}
                style={inputStyle}
            >
              <option value="" disabled>피부 타입 선택</option>
              <option value="dry">건성 (Dry)</option>
              <option value="oily">지성 (Oily)</option>
              <option value="combination">복합성 (Combination)</option>
              <option value="sensitive">민감성 (Sensitive)</option>
              <option value="normal">중성 (Normal)</option>
            </select>
          </div>

          <button
              type="submit"
              disabled={loading}
              style={{
                padding: '15px',
                backgroundColor: loading ? '#ccc' : '#007bff',
                color: 'white',
                border: 'none',
                borderRadius: '8px',
                fontSize: '16px',
                fontWeight: 'bold',
                cursor: loading ? 'not-allowed' : 'pointer',
                marginTop: '10px'
              }}
          >
            {loading ? '처리 중...' : '가입 완료하고 시작하기'}
          </button>
        </form>
      </div>
  )
}

const inputStyle = {
  width: '100%',
  padding: '12px',
  borderRadius: '8px',
  border: '1px solid #ddd',
  fontSize: '14px'
}

const sectionStyle = {
  background: '#f9f9f9',
  padding: '20px',
  borderRadius: '12px',
  display: 'flex',
  flexDirection: 'column' as const,
  gap: '12px'
}