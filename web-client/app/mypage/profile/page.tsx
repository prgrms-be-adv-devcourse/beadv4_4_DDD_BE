// app/mypage/profile

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

  const [realName, setRealName] = useState('')
  const [email, setEmail] = useState('')

  const [nickname, setNickname] = useState('')
  const [profileImageUrl, setProfileImageUrl] = useState('')
  const [heightCm, setHeightCm] = useState<number | ''>('')
  const [weightKg, setWeightKg] = useState<number | ''>('')
  const [skinType, setSkinType] = useState('')

  useEffect(() => {
    fetchData()
  }, [])

  const fetchData = async () => {
    try {
      setLoading(true)
      const basicInfoResponse = await api.get('/api/v1/members/me/basic-info')
      const basicInfo: MemberBasicInfo = basicInfoResponse.data.result
      setRealName(basicInfo.realName || '')
      setEmail(basicInfo.email || '')

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
        if (error.response?.status === 404) {
          setHasProfile(false)
        } else {
          console.error('프로필 조회 실패:', error)
        }
      }
    } catch (error) {
      console.error('데이터 조회 실패:', error)
    } finally {
      setLoading(false)
    }
  }

  // 프로필 저장 전 검증 및 실행
  const handleSave = async () => {
    const trimmedNickname = nickname.trim();

    // 1. 필수 값 검증 (백엔드 @NotBlank 대응)
    if (!trimmedNickname) {
      alert('닉네임은 필수 입력 항목입니다.');
      return;
    }
    if (!skinType) {
      alert('피부 타입을 선택해 주세요.');
      return;
    }

    try {
      setSaving(true)

      const profileData = {
        nickname: trimmedNickname, // null 대신 유효한 문자열 전송
        profileImageUrl: profileImageUrl.trim() || null,
        heightCm: heightCm === '' ? null : Number(heightCm),
        weightKg: weightKg === '' ? null : Number(weightKg),
        skinType: skinType, // 필수 값이므로 선택된 값 전송
      }

      if (hasProfile) {
        await api.put('/api/v1/members/me/profile', profileData)
        alert('프로필이 수정되었습니다.')
      } else {
        await api.post('/api/v1/members/me/profile', profileData)
        setHasProfile(true)
        alert('프로필이 생성되었습니다.')
      }

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

  const handleImageUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    // 이벤트 타겟을 미리 저장해둡니다 (비동기 처리 후 접근 및 초기화를 위해)
    const fileInput = e.target;

    if (!hasProfile) {
      alert('프로필 정보를 먼저 저장(생성)한 후 이미지를 업로드할 수 있습니다.');
      fileInput.value = ''; // 초기화
      return;
    }

    const file = fileInput.files?.[0];
    if (!file) {
      fileInput.value = ''; // 초기화
      return;
    }

    try {
      setUploading(true);

      let ext = '';
      const nameParts = file.name.split('.');

      // 1. 파일명에 점(.)이 존재하고 분리된 배열 길이가 2 이상인 경우 확장자 추출
      if (nameParts.length > 1) {
        ext = nameParts.pop() || '';
      }

      // 2. 확장자가 없거나 빈 문자열인 경우 MIME Type 기반 매핑
      if (!ext) {
        const mimeToExt: { [key: string]: string } = {
          'image/jpeg': 'jpg', 'image/jpg': 'jpg', 'image/png': 'png',
          'image/gif': 'gif', 'image/webp': 'webp',
          'image/svg+xml': 'svg', 'image/bmp': 'bmp'
        };
        // 매핑된 타입이 없으면 기본값 'jpg' 사용
        ext = mimeToExt[file.type] || 'jpg';
      }

      // 3. 소문자 정규화
      ext = ext.toLowerCase();

      // 개발 환경에서만 로그 출력
      if (process.env.NODE_ENV === 'development') {
        console.log('1. 파일 선택됨:', file.name, file.type);
      }

      // 1. URL 발급
      const presignedResponse = await api.post('/api/v1/files/presigned-url', {
        domainType: 'MEMBER',
        contentType: file.type,
        ext: ext,
      });

      if (process.env.NODE_ENV === 'development') {
        console.log('2. Presigned Response:', presignedResponse.data);
      }

      const { presignedUrl, key } = presignedResponse.data.result;

      if (!presignedUrl) throw new Error('Presigned URL이 없습니다!');

      // 2. S3 업로드
      if (process.env.NODE_ENV === 'development') {
        console.log('3. S3 업로드 시작');
      }

      const s3Response = await fetch(presignedUrl, {
        method: 'PUT',
        headers: { 'Content-Type': file.type },
        body: file,
      });

      console.log('4. S3 응답 상태:', s3Response.status);

      if (!s3Response.ok) {
        const errorText = await s3Response.text(); // AWS 에러 메시지 확인
        console.error(`S3 업로드 실패: ${s3Response.status}`, errorText);
        throw new Error(`S3 업로드 실패: ${s3Response.status}`);
      }

      // 3. Public URL 전환
      const publicUrlResponse = await api.post('/api/v1/files/public-url', {
        rawKey: key,
        domainType: 'MEMBER',
        contentType: file.type
      });

      const finalImageUrl = publicUrlResponse.data.result.imageUrl;

      console.log('최종 이미지 URL 획득 완료');

      // 4. 최종 URL을 Member 도메인에 저장
      await api.patch('/api/v1/members/me/profile/image', {
        imageUrl: finalImageUrl
      });

      setProfileImageUrl(finalImageUrl);
      alert('프로필 이미지가 업로드되었습니다.');
      await fetchData();
      window.dispatchEvent(new Event('loginStatusChanged'));

    } catch (error) {
      console.error('업로드 실패:', error);
      alert('이미지 업로드에 실패했습니다.');
    } finally {
      setUploading(false);
      // 성공/실패 여부와 상관없이 input 값 초기화 (재업로드 가능하도록)
      if (fileInput) {
        fileInput.value = '';
      }
    }
  };

  if (loading) {
    return (
        <MypageLayout>
          <div style={{ maxWidth: '600px', textAlign: 'center', padding: '40px' }}>로딩 중...</div>
        </MypageLayout>
    )
  }

  const avatarLetter = realName ? realName.charAt(0).toUpperCase() : 'U'

  return (
      <MypageLayout>
        <div style={{ maxWidth: '600px' }}>
          <h1 style={{ fontSize: '28px', fontWeight: 700, marginBottom: '24px' }}>프로필</h1>

          <div style={{ background: 'white', borderRadius: '12px', padding: '24px', boxShadow: '0 2px 8px rgba(0,0,0,0.1)', marginBottom: '24px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
              <input type="file" id="profile-image-upload" accept="image/*" style={{ display: 'none' }} onChange={handleImageUpload} disabled={uploading || !hasProfile} />
              <label
                  htmlFor="profile-image-upload"
                  style={{
                    width: '64px', height: '64px', borderRadius: '50%',
                    background: profileImageUrl ? `url(${profileImageUrl}) center/cover` : 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                    color: 'white', fontSize: '26px', cursor: (uploading || !hasProfile) ? 'not-allowed' : 'pointer',
                    position: 'relative', opacity: (uploading || !hasProfile) ? 0.6 : 1,
                  }}
                  onClick={(e) => {
                    if (!hasProfile) {
                      e.preventDefault();
                      alert('프로필 정보를 먼저 생성(저장)해 주세요.');
                    }
                  }}
              >
                {uploading ? '...' : (!profileImageUrl && avatarLetter)}
              </label>
              <div>
                <div style={{ fontSize: '14px', color: '#666' }}>이름</div>
                <div style={{ fontSize: '16px', fontWeight: 600 }}>{realName || '사용자'}</div>
                <div style={{ fontSize: '13px', color: '#666' }}>{email}</div>
              </div>
            </div>
            {!hasProfile && <div style={{ fontSize: '12px', color: '#ff4d4f', marginTop: '12px' }}>* 프로필 정보를 먼저 저장해야 이미지를 등록할 수 있습니다.</div>}
          </div>

          <div style={{ background: 'white', borderRadius: '12px', padding: '24px', boxShadow: '0 2px 8px rgba(0,0,0,0.1)' }}>
            <div style={{ marginBottom: '16px' }}>
              <label style={{ display: 'block', fontSize: '14px', fontWeight: 500, marginBottom: '6px' }}>닉네임 *</label>
              <input
                  type="text"
                  value={nickname}
                  onChange={(e) => setNickname(e.target.value)}
                  placeholder="닉네임을 입력해주세요"
                  style={{ width: '100%', padding: '10px', borderRadius: '8px', border: '1px solid #e0e0e0' }}
              />
            </div>

            <div style={{ marginBottom: '16px', display: 'flex', gap: '12px' }}>
              <div style={{ flex: 1 }}>
                <label style={{ display: 'block', fontSize: '14px', fontWeight: 500, marginBottom: '6px' }}>키 (cm)</label>
                <input
                    type="number"
                    value={heightCm}
                    onChange={(e) => setHeightCm(e.target.value === '' ? '' : Number(e.target.value))}
                    min="50" max="300"
                    style={{ width: '100%', padding: '10px', borderRadius: '8px', border: '1px solid #e0e0e0' }}
                />
              </div>
              <div style={{ flex: 1 }}>
                <label style={{ display: 'block', fontSize: '14px', fontWeight: 500, marginBottom: '6px' }}>몸무게 (kg)</label>
                <input
                    type="number"
                    value={weightKg}
                    onChange={(e) => setWeightKg(e.target.value === '' ? '' : Number(e.target.value))}
                    min="10" max="300" // 백엔드 기준 일치
                    style={{ width: '100%', padding: '10px', borderRadius: '8px', border: '1px solid #e0e0e0' }}
                />
              </div>
            </div>

            <div style={{ marginBottom: '16px' }}>
              <label style={{ display: 'block', fontSize: '14px', fontWeight: 500, marginBottom: '6px' }}>피부 타입 *</label>
              <select
                  value={skinType}
                  onChange={(e) => setSkinType(e.target.value)}
                  style={{ width: '100%', padding: '10px', borderRadius: '8px', border: '1px solid #e0e0e0', backgroundColor: 'white' }}
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
                  width: '100%', padding: '12px', borderRadius: '8px', border: 'none',
                  background: saving ? '#ccc' : 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                  color: 'white', fontWeight: 600, cursor: saving ? 'not-allowed' : 'pointer'
                }}
            >
              {saving ? '저장 중...' : (hasProfile ? '수정하기' : '생성하기')}
            </button>
          </div>
        </div>
      </MypageLayout>
  )
}