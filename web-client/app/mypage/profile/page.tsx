'use client'

import { useEffect, useState } from 'react'
import MypageLayout from '../../components/MypageLayout'
import api from "@/app/lib/axios";

interface MemberProfile {
  nickname: string
  profileImageUrl?: string
  heightCm?: number
  weightKg?: number
  skinType?: string
}

interface MemberBasicInfo {
  realName: string
  email: string
}

export default function ProfilePage() {
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [hasProfile, setHasProfile] = useState(false)
  const [uploading, setUploading] = useState(false)

  // 기본정보 (상단 카드용)
  const [realName, setRealName] = useState('')
  const [email, setEmail] = useState('')

  // 프로필 정보
  const [nickname, setNickname] = useState('')
  const [profileImageUrl, setProfileImageUrl] = useState('')
  const [heightCm, setHeightCm] = useState<number | ''>('')
  const [weightKg, setWeightKg] = useState<number | ''>('')
  const [skinType, setSkinType] = useState('')

  // 페이지 로드 시 기본정보와 프로필 조회
  useEffect(() => {
    fetchData()
  }, [])

  const fetchData = async () => {
    try {
      setLoading(true)

      // 기본정보 조회
      const basicInfoResponse = await api.get('/api/v1/members/me/basic-info')
      const basicInfo: MemberBasicInfo = basicInfoResponse.data.result
      setRealName(basicInfo.realName || '')
      setEmail(basicInfo.email || '')

      // 프로필 조회
      try {
        const profileResponse = await api.get('/api/v1/members/me/profile')
        const profile: MemberProfile = profileResponse.data.result

        setHasProfile(true)
        setNickname(profile.nickname || '')
        setProfileImageUrl(profile.profileImageUrl || '')
        setHeightCm(profile.heightCm ?? '')
        setWeightKg(profile.weightKg ?? '')
        setSkinType(profile.skinType || '')
      } catch (error: any) {
        // 404: 프로필이 아직 없음
        if (error.response?.status === 404) {
          setHasProfile(false)
        } else {
          console.error('프로필 조회 실패:', error)
          alert('프로필을 불러오는데 실패했습니다.')
        }
      }
    } catch (error) {
      console.error('데이터 조회 실패:', error)
      alert('정보를 불러오는데 실패했습니다.')
    } finally {
      setLoading(false)
    }
  }

  // 프로필 저장 (생성 또는 수정)
  const handleSave = async () => {
    try {
      setSaving(true)

      const profileData = {
        nickname: nickname.trim() || null,
        profileImageUrl: profileImageUrl.trim() || null,
        heightCm: heightCm === '' ? null : Number(heightCm),
        weightKg: weightKg === '' ? null : Number(weightKg),
        skinType: skinType || null,
      }

      if (hasProfile) {
        // 프로필 수정 (PUT)
        await api.put('/api/v1/members/me/profile', profileData)
        alert('프로필이 수정되었습니다.')
      } else {
        // 프로필 생성 (POST)
        await api.post('/api/v1/members/me/profile', profileData)
        setHasProfile(true)
        alert('프로필이 생성되었습니다.')
      }

      // 저장 후 다시 조회
      await fetchData()
    } catch (error: any) {
      console.error('프로필 저장 실패:', error)
      const errorMessage = error.response?.data?.message || '저장에 실패했습니다.'
      alert(errorMessage)
    } finally {
      setSaving(false)
    }
    window.dispatchEvent(new Event('loginStatusChanged'));
  }

  // 프로필 이미지 업로드
  const handleImageUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (!file) return

    // 이미지 파일만 허용
    const allowedTypes = ['image/png', 'image/jpeg', 'image/webp']
    if (!allowedTypes.includes(file.type)) {
      alert('PNG, JPEG, WEBP 형식의 이미지만 업로드 가능합니다.')
      return
    }

    // 파일 크기 제한 (10MB)
    if (file.size > 10 * 1024 * 1024) {
      alert('파일 크기는 10MB 이하여야 합니다.')
      return
    }

    try {
      setUploading(true)

      // 1. Presigned URL 발급
      const ext = file.type.split('/')[1] // png, jpeg, webp
      console.log('1. Presigned URL 요청:', { domainType: 'MEMBER', contentType: file.type, ext })

      const presignedResponse = await api.post('/api/v1/files/presigned-url', {
        domainType: 'MEMBER',
        contentType: file.type,
        ext: ext,
      })

      // 백엔드 응답: { presignedUrl, key }
      const result = presignedResponse.data.result
      const presignedUrl = result.presignedUrl || result.url
      const rawKey = result.key || result.rawKey || result.objectKey

      if (!presignedUrl || !rawKey) {
        console.error('❌ Presigned URL 또는 rawKey가 없습니다:', result)
        throw new Error('Presigned URL 발급 응답이 올바르지 않습니다.')
      }

      console.log('2. Presigned URL 발급 완료:', { presignedUrl, rawKey })

      // 2. S3에 직접 업로드
      const s3Response = await fetch(presignedUrl, {
        method: 'PUT',
        headers: {
          'Content-Type': file.type,
        },
        body: file,
      })

      if (!s3Response.ok) {
        throw new Error(`S3 업로드 실패: ${s3Response.status} ${s3Response.statusText}`)
      }

      console.log('3. S3 업로드 완료')

      // S3 업로드 후 약간의 지연 (S3 eventual consistency)
      await new Promise(resolve => setTimeout(resolve, 1000))

      // 3. 프로필 이미지 업데이트 (이 API가 내부적으로 public-url 변환도 처리함)
      console.log('4. 프로필 이미지 업데이트 요청:', { rawKey, domainType: 'MEMBER', contentType: file.type })

      const updateResponse = await api.patch('/api/v1/members/me/profile/image', {
        rawKey: rawKey,
        domainType: 'MEMBER',
        contentType: file.type,
      })

      console.log('5. 프로필 이미지 업데이트 완료:', updateResponse.data)

      // 응답에서 imageUrl 추출 (백엔드 응답: { imageUrl, key })
      const imageUrl = updateResponse.data.result?.imageUrl || updateResponse.data.result?.publicUrl

      if (imageUrl) {
        setProfileImageUrl(imageUrl)
      }

      alert('프로필 이미지가 업로드되었습니다.')

      // 프로필 재조회
      await fetchData()
    } catch (error: any) {
      console.error('❌ 이미지 업로드 실패:', error)

      // 상세 에러 메시지
      let errorMessage = '이미지 업로드에 실패했습니다.'

      if (error.response) {
        console.error('에러 응답:', {
          status: error.response.status,
          data: error.response.data,
          headers: error.response.headers,
        })
        errorMessage = error.response.data?.message || `서버 에러 (${error.response.status})`
      } else if (error.message) {
        errorMessage = error.message
      }

      alert(errorMessage)
    } finally {
      setUploading(false)
      // input 초기화 (같은 파일 재선택 가능하도록)
      e.target.value = ''
    }
    window.dispatchEvent(new Event('loginStatusChanged'));
  }

  if (loading) {
    return (
        <MypageLayout>
          <div style={{ maxWidth: '600px', textAlign: 'center', padding: '40px' }}>
            로딩 중...
          </div>
        </MypageLayout>
    )
  }

  const avatarLetter = realName ? realName.charAt(0).toUpperCase() : 'U'

  return (
      <MypageLayout>
        <div style={{ maxWidth: '600px' }}>
          <h1 style={{ fontSize: '28px', fontWeight: 700, marginBottom: '24px' }}>프로필</h1>

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
              <input
                  type="file"
                  id="profile-image-upload"
                  accept="image/png,image/jpeg,image/webp"
                  style={{ display: 'none' }}
                  onChange={handleImageUpload}
                  disabled={uploading}
              />
              <label
                  htmlFor="profile-image-upload"
                  style={{
                    width: '64px',
                    height: '64px',
                    borderRadius: '50%',
                    background: profileImageUrl
                        ? `url(${profileImageUrl}) center/cover`
                        : 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    color: 'white',
                    fontWeight: '600',
                    fontSize: '26px',
                    cursor: uploading ? 'not-allowed' : 'pointer',
                    position: 'relative',
                    opacity: uploading ? 0.6 : 1,
                  }}
                  title={uploading ? '업로드 중...' : '클릭하여 이미지 변경'}
              >
                {uploading ? (
                    <div style={{ fontSize: '12px' }}>...</div>
                ) : !profileImageUrl ? (
                    avatarLetter
                ) : null}
                {/* 호버 시 오버레이 */}
                <div
                    style={{
                      position: 'absolute',
                      top: 0,
                      left: 0,
                      width: '100%',
                      height: '100%',
                      borderRadius: '50%',
                      background: 'rgba(0, 0, 0, 0.3)',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      opacity: 0,
                      transition: 'opacity 0.2s',
                    }}
                    onMouseEnter={(e) => (e.currentTarget.style.opacity = '1')}
                    onMouseLeave={(e) => (e.currentTarget.style.opacity = '0')}
                >
                  <span style={{ fontSize: '12px', color: 'white' }}>변경</span>
                </div>
              </label>
              <div>
                <div style={{ fontSize: '14px', color: '#666', marginBottom: '4px' }}>이름</div>
                <div style={{ fontSize: '16px', fontWeight: 600 }}>{realName || '사용자'}</div>
                <div style={{ fontSize: '13px', color: '#666', marginTop: '2px' }}>{email}</div>
              </div>
            </div>
            {uploading && (
                <div style={{ fontSize: '13px', color: '#667eea', marginTop: '8px' }}>
                  이미지 업로드 중...
                </div>
            )}
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
                  value={nickname}
                  onChange={(e) => setNickname(e.target.value)}
                  placeholder="예: 뭐든사_사용자"
                  maxLength={50}
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
                    value={heightCm}
                    onChange={(e) => setHeightCm(e.target.value === '' ? '' : Number(e.target.value))}
                    placeholder="예: 175"
                    min="0"
                    max="300"
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
                    value={weightKg}
                    onChange={(e) => setWeightKg(e.target.value === '' ? '' : Number(e.target.value))}
                    placeholder="예: 65"
                    min="0"
                    max="500"
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
                  value={skinType}
                  onChange={(e) => setSkinType(e.target.value)}
                  style={{
                    width: '100%',
                    padding: '10px 12px',
                    borderRadius: '8px',
                    border: '1px solid #e0e0e0',
                    fontSize: '14px',
                    backgroundColor: 'white',
                  }}
              >
                <option value="">선택해주세요</option>
                <option value="normal">중성</option>
                <option value="dry">건성</option>
                <option value="oily">지성</option>
                <option value="combination">복합성</option>
                <option value="sensitive">민감성</option>
              </select>
            </div>

            <button
                type="button"
                onClick={handleSave}
                disabled={saving}
                style={{
                  width: '100%',
                  marginTop: '8px',
                  padding: '10px 0',
                  borderRadius: '8px',
                  border: 'none',
                  background: saving ? '#ccc' : 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                  color: 'white',
                  cursor: saving ? 'not-allowed' : 'pointer',
                  fontSize: '14px',
                  fontWeight: 600,
                }}
            >
              {saving ? '저장 중...' : hasProfile ? '수정하기' : '생성하기'}
            </button>
          </div>
        </div>
      </MypageLayout>
  )
}