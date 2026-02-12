'use client'

import { useRouter } from 'next/navigation'
import { useEffect, useState, ChangeEvent } from 'react'
import MypageLayout from '../../components/MypageLayout'

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

// 3. 도메인 타입 상수
const DOMAIN_TYPE = "SELLER";

export default function SellerRequestPage() {
  const router = useRouter()
  const [email, setEmail] = useState('')
  const [loading, setLoading] = useState(false)

  // 폼 데이터
  const [formData, setFormData] = useState({
    businessName: '',
    representativeName: '',
    settlementBankName: '',
    settlementBankAccount: '',
  })

  // 파일 상태
  const [file, setFile] = useState<File | null>(null)

  useEffect(() => {
    if (typeof window === 'undefined') return
    const stored = localStorage.getItem('email')
    if (stored) setEmail(stored)
  }, [])

  const handleInputChange = (e: ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target
    setFormData((prev) => ({ ...prev, [name]: value }))
  }

  const handleFileChange = (e: ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      setFile(e.target.files[0])
    }
  }

  // --- 3단계 파일 업로드 로직 ---
  const uploadImageProcess = async (selectedFile: File): Promise<string> => {
    const token = localStorage.getItem('accessToken')
    const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'

    // 확장자 추출 (pdf인 경우 pdf로 추출됨)
    const ext = selectedFile.name.split('.').pop() || 'jpg';

    // [Step 1] Presigned URL 발급
    const presignRes = await fetch(`${API_BASE_URL}/api/v1/files/presigned-url`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({
        domainType: DOMAIN_TYPE,
        ext: ext,
        contentType: selectedFile.type // application/pdf 전달됨
      })
    });

    if (!presignRes.ok) throw new Error('Presigned URL 발급 실패');
    const presignData: ApiResponse<PresignedUrlResult> = await presignRes.json();
    if (!presignData.isSuccess) throw new Error(presignData.message);

    const { presignedUrl, key: rawKey } = presignData.result;

    // [Step 2] S3 업로드 (PUT)
    const uploadRes = await fetch(presignedUrl, {
      method: 'PUT',
      headers: {
        'Content-Type': selectedFile.type // application/pdf 전달됨
      },
      body: selectedFile
    });

    if (!uploadRes.ok) throw new Error('S3 업로드 실패');

    // [Step 3] Public URL 변환
    const publicUrlRes = await fetch(`${API_BASE_URL}/api/v1/files/public-url`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({
        rawKey: rawKey,
        domainType: DOMAIN_TYPE,
        contentType: selectedFile.type // application/pdf 전달됨
      })
    });

    if (!publicUrlRes.ok) throw new Error('Public URL 변환 실패');
    const publicUrlData: ApiResponse<PublicUrlResult> = await publicUrlRes.json();
    if (!publicUrlData.isSuccess) throw new Error(publicUrlData.message);

    return publicUrlData.result.imageUrl;
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

    try {
      setLoading(true)
      const finalFileUrl = await uploadImageProcess(file);

      const token = localStorage.getItem('accessToken')
      const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'

      const response = await fetch(`${API_BASE_URL}/api/v1/members/me/seller/register`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({
          ...formData,
          businessLicenseUrl: finalFileUrl
        }),
      })

      const result: ApiResponse<any> = await response.json()

      if (response.ok && result.isSuccess) {
        alert('판매자 전환 신청이 완료되었습니다.')
        router.push('/mypage')
      } else {
        alert(`신청 실패: ${result.message}`)
      }
    } catch (error) {
      console.error(error)
      alert(error instanceof Error ? error.message : '오류가 발생했습니다.')
    } finally {
      setLoading(false)
    }
  }

  return (
      <MypageLayout>
        <div style={{ maxWidth: '600px' }}>
          <h1 style={{ fontSize: '28px', fontWeight: 700, marginBottom: '24px' }}>판매자 전환</h1>

          {/* 상단 프로필 카드 */}
          <div style={{ background: 'white', borderRadius: '12px', padding: '24px', boxShadow: '0 2px 8px rgba(0, 0, 0, 0.1)', marginBottom: '24px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '16px', marginBottom: '8px' }}>
              <div style={{ width: '64px', height: '64px', borderRadius: '50%', background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'white', fontWeight: '600', fontSize: '26px' }}>T</div>
              <div>
                <div style={{ fontSize: '14px', color: '#666', marginBottom: '4px' }}>이름</div>
                <div style={{ fontSize: '16px', fontWeight: 600 }}>테스트 사용자</div>
                <div style={{ fontSize: '13px', color: '#666', marginTop: '2px' }}>{email}</div>
              </div>
            </div>
            <p style={{ fontSize: '13px', color: '#666', marginTop: '8px', lineHeight: 1.5 }}>
              판매자 전환을 신청하면 상품 관리, 주문 관리 기능을 사용할 수 있어요. 아래 정보를 입력하고 신청해 주세요.
            </p>
          </div>

          {/* 입력 폼 */}
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

            {/* 사업자등록증 업로드 (PDF 지원 UI) */}
            <div style={{ marginBottom: '16px' }}>
              <label style={{ display: 'block', fontSize: '14px', fontWeight: 500, marginBottom: '6px' }}>사업자등록증</label>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                <input
                    type="file"
                    // 이미지와 PDF 모두 허용
                    accept="image/*, .pdf, application/pdf"
                    onChange={handleFileChange}
                    style={{ fontSize: '13px' }}
                />

                {/* 선택된 파일 정보 표시 (PDF 대응) */}
                {file && (
                    <div style={{ fontSize: '13px', color: '#666', padding: '8px', background: '#f8f9fa', borderRadius: '4px' }}>
                      첨부된 파일: <strong>{file.name}</strong>
                      {file.type === 'application/pdf' ? ' (PDF 문서)' : ' (이미지 파일)'}
                    </div>
                )}
              </div>

              <p style={{ marginTop: '6px', fontSize: '12px', color: '#777' }}>
                사업자등록증 사본 파일을 업로드해 주세요. (이미지 또는 PDF)
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