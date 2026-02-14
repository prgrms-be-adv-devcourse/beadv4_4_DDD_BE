// web-client/app/mypage/seller-request/page.tsx

'use client'

import { useEffect, useState, ChangeEvent } from 'react'
import MypageLayout from '../../components/MypageLayout'
import api from "@/app/lib/axios"

// 1. API 응답 공통 인터페이스
interface ApiResponse<T> {
  isSuccess: boolean;
  code: string;
  message: string;
  result: T;
}

// 2. 파일 업로드 관련 DTO 정의
interface PresignedUrlResult {
  presignedUrl: string;
  key: string; // rawKey
}

interface PublicUrlResult {
  imageUrl: string;
  key: string; // publicKey
}

interface MemberBasicInfo {
  realName: string;
  email: string;
  phoneNumber?: string;
}

// 3. 도메인 타입 상수
const DOMAIN_TYPE = "SELLER";

// 4. 최대 파일 크기 제한 (10MB)
const MAX_FILE_SIZE = 10 * 1024 * 1024;

export default function SellerRequestPage() {
  const [loading, setLoading] = useState(false)

  const [userInfo, setUserInfo] = useState<MemberBasicInfo | null>(null)
  const [profileImageUrl, setProfileImageUrl] = useState('')

  // 폼 데이터
  const [formData, setFormData] = useState({
    businessName: '',
    representativeName: '',
    settlementBankName: '',
    settlementBankAccount: '',
  })

  const [file, setFile] = useState<File | null>(null)

  // 유저 정보 및 프로필 이미지 불러오기
  useEffect(() => {
    const fetchMemberData = async () => {
      try {
        // 1. 기본 정보 조회
        const basicRes = await api.get<ApiResponse<MemberBasicInfo>>('/api/v1/members/me/basic-info');
        if (basicRes.data.isSuccess) {
          setUserInfo(basicRes.data.result);
        }

        // 2. 프로필 이미지 조회
        try {
          const profileRes = await api.get('/api/v1/members/me/profile');
          if (profileRes.data.isSuccess) {
            setProfileImageUrl(profileRes.data.result.profileImageUrl || '');
          }
        } catch (err: any) {
          if (err.response?.status !== 404) {
            console.error('프로필 조회 오류:', err);
          }
        }
      } catch (error) {
        console.error('유저 정보 조회 실패:', error);
      }
    };

    fetchMemberData();
  }, [])

  const handleInputChange = (e: ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target
    setFormData((prev) => ({ ...prev, [name]: value }))
  }

  // 파일 선택 및 크기 검증 로직
  const handleFileChange = (e: ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      const selectedFile = e.target.files[0];

      // 파일 크기 10MB 제한 검증
      if (selectedFile.size > MAX_FILE_SIZE) {
        alert('파일 크기는 10MB를 초과할 수 없습니다.');
        e.target.value = ''; // input 필드 초기화
        setFile(null);
        return;
      }

      setFile(selectedFile);
    }
  }

  // 파일 업로드 로직
  const uploadImageProcess = async (selectedFile: File): Promise<string> => {
    const FILE_SERVICE_URL = process.env.NEXT_PUBLIC_FILE_API_URL || 'http://localhost:8088';
    let ext = selectedFile.name.split('.').pop()?.toLowerCase() || '';

    // 확장자가 없거나 파일명 전체가 반환된 경우 MIME 타입으로 매핑
    if (!ext || ext === selectedFile.name.toLowerCase()) {
      const mimeType = selectedFile.type;
      switch (mimeType) {
        case 'application/pdf':
          ext = 'pdf';
          break;
        case 'image/jpeg':
          ext = 'jpg';
          break;
        case 'image/png':
          ext = 'png';
          break;
        case 'image/gif':
          ext = 'gif';
          break;
        case 'image/webp':
          ext = 'webp';
          break;
        default:
          ext = 'jpg';
      }
    }
    // jpeg의 경우 통일성을 위해 jpg로 변환
    if (ext === 'jpeg') ext = 'jpg';

    // [Step 1] Presigned URL 발급
    const presignRes = await api.post<ApiResponse<PresignedUrlResult>>(`${FILE_SERVICE_URL}/api/v1/files/presigned-url`, {
      domainType: DOMAIN_TYPE,
      ext: ext,
      contentType: selectedFile.type
    });

    if (!presignRes.data.isSuccess) throw new Error(presignRes.data.message);
    const { presignedUrl, key: rawKey } = presignRes.data.result;

    // [Step 2] S3 업로드
    const uploadRes = await fetch(presignedUrl, {
      method: 'PUT',
      headers: {
        'Content-Type': selectedFile.type
      },
      body: selectedFile
    });

    if (!uploadRes.ok) throw new Error('S3 업로드 실패');

    // [Step 3] Public URL 변환
    const publicUrlRes = await api.post<ApiResponse<PublicUrlResult>>(`${FILE_SERVICE_URL}/api/v1/files/public-url`, {
      rawKey: rawKey,
      domainType: DOMAIN_TYPE,
      contentType: selectedFile.type
    });

    if (!publicUrlRes.data.isSuccess) throw new Error(publicUrlRes.data.message);

    return publicUrlRes.data.result.imageUrl;
  }

  // 제출 버튼 핸들러
  const handleSubmit = async () => {
    if (!formData.businessName || !formData.representativeName || !formData.settlementBankName || !formData.settlementBankAccount) {
      alert('모든 정보를 입력해 주세요.')
      return
    }
    if (!file) {
      alert('사업자등록증 파일을 첨부해 주세요.')
      return
    }

    // 제출 전 한 번 더 파일 크기 방어 로직 추가
    if (file.size > MAX_FILE_SIZE) {
      alert('파일 크기가 10MB를 초과하여 업로드할 수 없습니다.')
      return
    }

    try {
      setLoading(true)

      const finalFileUrl = await uploadImageProcess(file);

      const response = await api.post<ApiResponse<any>>('/api/v1/members/me/seller/register', {
        ...formData,
        businessLicenseUrl: finalFileUrl
      });

      if (response.data.isSuccess) {
        alert('판매자 전환 신청이 완료되었습니다.')

        if (typeof window !== 'undefined') {
          window.dispatchEvent(new Event('loginStatusChanged'));
        }

        window.location.href = '/mypage';
      } else {
        alert(`신청 실패: ${response.data.message}`)
      }
    } catch (error: any) {
      console.error(error)
      const errorMessage = error.response?.data?.message || error.message || '오류가 발생했습니다.';
      alert(errorMessage)
    } finally {
      setLoading(false)
    }
  }

  return (
      <MypageLayout>
        <div style={{ maxWidth: '600px' }}>
          <h1 style={{ fontSize: '28px', fontWeight: 700, marginBottom: '24px' }}>판매자 전환</h1>

          <div style={{ background: 'white', borderRadius: '12px', padding: '24px', boxShadow: '0 2px 8px rgba(0, 0, 0, 0.1)', marginBottom: '24px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '16px', marginBottom: '8px' }}>

              <div style={{
                width: '64px',
                height: '64px',
                borderRadius: '50%',
                background: profileImageUrl
                    ? `url(${profileImageUrl}) center/cover no-repeat`
                    : 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: 'white',
                fontWeight: '600',
                fontSize: '26px',
                border: profileImageUrl ? '1px solid #eee' : 'none'
              }}>
                {!profileImageUrl && (userInfo?.realName ? userInfo.realName.substring(0, 1) : 'M')}
              </div>

              <div>
                <div style={{ fontSize: '14px', color: '#666', marginBottom: '4px' }}>이름</div>
                <div style={{ fontSize: '16px', fontWeight: 600 }}>
                  {userInfo ? userInfo.realName : '불러오는 중...'}
                </div>
                <div style={{ fontSize: '13px', color: '#666', marginTop: '2px' }}>
                  {userInfo ? userInfo.email : ''}
                </div>
              </div>
            </div>
            <p style={{ fontSize: '13px', color: '#666', marginTop: '8px', lineHeight: 1.5 }}>
              판매자 전환을 신청하면 상품 관리, 주문 관리 기능을 사용할 수 있어요. 아래 정보를 입력하고 신청해 주세요.
            </p>
          </div>

          {/* 입력 폼 영역 */}
          <div style={{ background: 'white', borderRadius: '12px', padding: '24px', boxShadow: '0 2px 8px rgba(0, 0, 0, 0.1)' }}>
            <div style={{ marginBottom: '16px' }}>
              <label style={{ display: 'block', fontSize: '14px', fontWeight: 500, marginBottom: '6px' }}>상호명</label>
              <input type="text" name="businessName" value={formData.businessName} onChange={handleInputChange} placeholder="예: 뭐든사 주식회사" style={{ width: '100%', padding: '10px 12px', borderRadius: '8px', border: '1px solid #e0e0e0', fontSize: '14px' }} />
            </div>

            <div style={{ marginBottom: '16px' }}>
              <label style={{ display: 'block', fontSize: '14px', fontWeight: 500, marginBottom: '6px' }}>대표자명</label>
              <input type="text" name="representativeName" value={formData.representativeName} onChange={handleInputChange} placeholder="예: 홍길동" style={{ width: '100%', padding: '10px 12px', borderRadius: '8px', border: '1px solid #e0e0e0', fontSize: '14px' }} />
            </div>

            <div style={{ marginBottom: '16px' }}>
              <label style={{ display: 'block', fontSize: '14px', fontWeight: 500, marginBottom: '6px' }}>정산 은행명</label>
              <input type="text" name="settlementBankName" value={formData.settlementBankName} onChange={handleInputChange} placeholder="예: 국민은행" style={{ width: '100%', padding: '10px 12px', borderRadius: '8px', border: '1px solid #e0e0e0', fontSize: '14px' }} />
            </div>

            <div style={{ marginBottom: '16px' }}>
              <label style={{ display: 'block', fontSize: '14px', fontWeight: 500, marginBottom: '6px' }}>정산 계좌번호</label>
              <input type="text" name="settlementBankAccount" value={formData.settlementBankAccount} onChange={handleInputChange} placeholder="예: 123456-01-123456" style={{ width: '100%', padding: '10px 12px', borderRadius: '8px', border: '1px solid #e0e0e0', fontSize: '14px' }} />
            </div>

            <div style={{ marginBottom: '16px' }}>
              <label style={{ display: 'block', fontSize: '14px', fontWeight: 500, marginBottom: '6px' }}>사업자등록증</label>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                <input
                    type="file"
                    accept="image/*, .pdf"
                    onChange={handleFileChange}
                    style={{ fontSize: '13px' }}
                />
                {file && (
                    <div style={{ fontSize: '13px', color: '#666', padding: '8px', background: '#f8f9fa', borderRadius: '4px' }}>
                      첨부된 파일: <strong>{file.name}</strong>
                      {file.type === 'application/pdf' ? ' (PDF 문서)' : ' (이미지 파일)'}
                    </div>
                )}
              </div>
              <p style={{ marginTop: '6px', fontSize: '12px', color: '#777' }}>
                사업자등록증 사본 파일을 업로드해 주세요. (이미지 또는 PDF, 최대 10MB)
              </p>
            </div>

            <button
                type="button"
                onClick={handleSubmit}
                disabled={loading}
                style={{
                  width: '100%',
                  marginTop: '8px',
                  padding: '10px 0',
                  borderRadius: '8px',
                  border: 'none',
                  background: loading ? '#ccc' : 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                  color: 'white',
                  cursor: loading ? 'not-allowed' : 'pointer',
                  fontSize: '14px',
                  fontWeight: 600,
                }}
            >
              {loading ? '처리 중...' : '판매자 전환 신청하기'}
            </button>
          </div>
        </div>
      </MypageLayout>
  )
}