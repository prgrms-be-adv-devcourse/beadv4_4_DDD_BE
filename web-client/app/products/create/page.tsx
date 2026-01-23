'use client'

import Link from 'next/link'
import { useRouter } from 'next/navigation'
import { useState } from 'react'

type ProductCategory = 'OUTER' | 'UPPER' | 'LOWER' | 'CAP' | 'SHOES' | 'BAG' | 'BEAUTY'

interface ProductCreateRequest {
  name: string
  category: ProductCategory
  description: string
  price: number
  salePrice: number
  stock: number
  images: string[]
}

interface ProductResponse {
  id?: number
  productId?: number
  name: string
  category: string
  [key: string]: any
}

interface ApiResponse {
  isSuccess: boolean
  code: string
  message: string
  result: ProductResponse
}

const categoryOptions: { value: ProductCategory; label: string }[] = [
  { value: 'OUTER', label: '아우터' },
  { value: 'UPPER', label: '상의' },
  { value: 'LOWER', label: '하의' },
  { value: 'CAP', label: '모자' },
  { value: 'SHOES', label: '신발' },
  { value: 'BAG', label: '가방' },
  { value: 'BEAUTY', label: '뷰티' },
]

export default function ProductCreatePage() {
  const router = useRouter()
  const [formData, setFormData] = useState<ProductCreateRequest>({
    name: '',
    category: 'UPPER',
    description: '',
    price: 0,
    salePrice: 0,
    stock: 0,
    images: [],
  })
  const [imageFiles, setImageFiles] = useState<File[]>([])
  const [imagePreviews, setImagePreviews] = useState<string[]>([]) // 로컬 미리보기용 (base64)
  const [uploadedImageUrls, setUploadedImageUrls] = useState<string[]>([]) // 업로드된 public URL들
  const [uploadingImageIndex, setUploadingImageIndex] = useState<Set<number>>(new Set()) // 업로드 중인 이미지 인덱스
  const [isSubmitting, setIsSubmitting] = useState(false)

  const handleInputChange = (field: keyof ProductCreateRequest, value: any) => {
    setFormData(prev => ({ ...prev, [field]: value }))
  }

  const handleFileSelect = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files
    if (!files) return

    const newFiles: File[] = []
    const newPreviews: string[] = []
    const startIndex = imageFiles.length

    // 파일 검증 및 미리보기 생성
    Array.from(files).forEach((file) => {
      if (file.type.startsWith('image/')) {
        if (imageFiles.length + newFiles.length < 10) {
          newFiles.push(file)
          const reader = new FileReader()
          reader.onloadend = () => {
            const result = reader.result as string
            setImagePreviews(prev => [...prev, result])
          }
          reader.readAsDataURL(file)
        } else {
          alert('이미지는 최대 10개까지 추가할 수 있습니다.')
        }
      } else {
        alert(`${file.name}은(는) 이미지 파일이 아닙니다.`)
      }
    })

    // 파일 추가
    setImageFiles(prev => [...prev, ...newFiles])

    // 각 파일을 즉시 업로드
    newFiles.forEach(async (file, relativeIndex) => {
      const absoluteIndex = startIndex + relativeIndex
      setUploadingImageIndex(prev => new Set(prev).add(absoluteIndex))

      try {
        console.log(`이미지 ${absoluteIndex + 1} 업로드 시작:`, file.name)
        const publicUrl = await uploadImageToS3(file, 0)
        
        // 업로드된 public URL 저장
        setUploadedImageUrls(prev => {
          const newUrls = [...prev]
          newUrls[absoluteIndex] = publicUrl
          return newUrls
        })
        
        console.log(`이미지 ${absoluteIndex + 1} 업로드 완료:`, publicUrl)
      } catch (error) {
        console.error(`이미지 ${absoluteIndex + 1} 업로드 실패:`, error)
        alert(`이미지 "${file.name}" 업로드에 실패했습니다: ${error instanceof Error ? error.message : '알 수 없는 오류'}`)
        // 업로드 실패 시 해당 이미지 제거
        handleRemoveImage(absoluteIndex)
      } finally {
        setUploadingImageIndex(prev => {
          const newSet = new Set(prev)
          newSet.delete(absoluteIndex)
          return newSet
        })
      }
    })
  }

  const handleRemoveImage = (index: number) => {
    setImageFiles(prev => prev.filter((_, i) => i !== index))
    setImagePreviews(prev => prev.filter((_, i) => i !== index))
    setUploadedImageUrls(prev => prev.filter((_, i) => i !== index))
    setFormData(prev => ({
      ...prev,
      images: prev.images.filter((_, i) => i !== index),
    }))
  }

  // 파일 확장자 추출
  const getFileExtension = (filename: string): string => {
    const parts = filename.split('.')
    return parts.length > 1 ? parts[parts.length - 1].toLowerCase() : 'jpg'
  }

  // Presigned URL 요청 및 S3 업로드
  const uploadImageToS3 = async (file: File, domainId: number = 0): Promise<string> => {
    const apiUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'
    const ext = getFileExtension(file.name)
    const contentType = file.type || `image/${ext === 'jpg' ? 'jpeg' : ext}`

    // 1. Presigned URL 요청
    const presignedUrlResponse = await fetch(`${apiUrl}/api/v1/file-uploads/presigned-url`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        domainId: domainId,
        domainType: 'PRODUCT',
        ext: ext,
        contentType: contentType,
      }),
    })

    if (!presignedUrlResponse.ok) {
      const errorText = await presignedUrlResponse.text()
      console.error('Presigned URL 요청 실패:', presignedUrlResponse.status, errorText)
      throw new Error('이미지 업로드 URL을 가져오는데 실패했습니다.')
    }

    const presignedUrlApiResponse = await presignedUrlResponse.json()
    if (!presignedUrlApiResponse.isSuccess || !presignedUrlApiResponse.result) {
      throw new Error(presignedUrlApiResponse.message || '이미지 업로드 URL을 가져오는데 실패했습니다.')
    }

    const { presignedUrl, key: rawKey } = presignedUrlApiResponse.result

    // 2. S3에 직접 업로드
    const uploadResponse = await fetch(presignedUrl, {
      method: 'PUT',
      headers: {
        'Content-Type': contentType,
      },
      body: file,
    })

    if (!uploadResponse.ok) {
      console.error('S3 업로드 실패:', uploadResponse.status, uploadResponse.statusText)
      throw new Error('이미지 업로드에 실패했습니다.')
    }

    // 3. Public URL 변환
    const publicUrlResponse = await fetch(`${apiUrl}/api/v1/file-uploads/public-url`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        rawKey: rawKey,
        domainType: 'PRODUCT',
        domainId: domainId,
        contentType: contentType,
      }),
    })

    if (!publicUrlResponse.ok) {
      const errorText = await publicUrlResponse.text()
      console.error('Public URL 변환 실패:', publicUrlResponse.status, errorText)
      throw new Error('이미지 URL 변환에 실패했습니다.')
    }

    const publicUrlApiResponse = await publicUrlResponse.json()
    if (!publicUrlApiResponse.isSuccess || !publicUrlApiResponse.result) {
      throw new Error(publicUrlApiResponse.message || '이미지 URL 변환에 실패했습니다.')
    }

    // PublicUrlResponse의 필드명은 imageUrl입니다
    return publicUrlApiResponse.result.imageUrl || publicUrlApiResponse.result.publicUrl
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()

    if (!formData.name.trim()) {
      alert('상품명을 입력해주세요.')
      return
    }

    if (formData.price < 0 || formData.salePrice < 0) {
      alert('가격은 0 이상이어야 합니다.')
      return
    }

    if (formData.stock < 0) {
      alert('재고는 0 이상이어야 합니다.')
      return
    }

    if (formData.salePrice > formData.price) {
      alert('할인가는 정가보다 작거나 같아야 합니다.')
      return
    }

    // 업로드 중인 이미지가 있는지 확인
    if (uploadingImageIndex.size > 0) {
      alert('이미지 업로드가 진행 중입니다. 잠시만 기다려주세요.')
      return
    }

    // 업로드되지 않은 이미지가 있는지 확인
    const missingUrls = imageFiles.length > uploadedImageUrls.filter(url => url).length
    if (missingUrls) {
      alert('일부 이미지가 아직 업로드되지 않았습니다. 잠시만 기다려주세요.')
      return
    }

    setIsSubmitting(true)

    try {
      const apiUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'
      
      // 이미 업로드된 public URL들 사용
      const imageUrls = uploadedImageUrls.filter(url => url)
      console.log('상품 등록에 사용할 이미지 URLs:', imageUrls)
      
      const productRequest: ProductCreateRequest = {
        name: formData.name.trim(),
        category: formData.category,
        description: formData.description.trim() || '',
        price: formData.price,
        salePrice: formData.salePrice,
        stock: formData.stock,
        images: imageUrls,
      }

      console.log('상품 등록 요청:', productRequest)

      const response = await fetch(`${apiUrl}/api/v1/products`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(productRequest),
      })

      if (!response.ok) {
        const errorText = await response.text()
        console.error('API 응답 에러:', response.status, errorText)
        let errorMessage = `상품 등록 실패 (${response.status})`
        try {
          const errorResponse = JSON.parse(errorText)
          if (errorResponse.message) {
            errorMessage = errorResponse.message
          }
        } catch (e) {
          // JSON 파싱 실패 시 기본 메시지 사용
        }
        throw new Error(errorMessage)
      }

      const apiResponse: ApiResponse = await response.json()
      console.log('상품 등록 응답:', apiResponse)

      if (apiResponse.isSuccess && apiResponse.result) {
        alert('상품이 성공적으로 등록되었습니다.')
        const productId = apiResponse.result.id || apiResponse.result.productId
        if (productId) {
          router.push(`/products/${productId}`)
        } else {
          router.push('/')
        }
      } else {
        throw new Error(apiResponse.message || '상품 등록에 실패했습니다.')
      }
    } catch (error) {
      console.error('상품 등록 실패:', error)
      const errorMessage = error instanceof Error ? error.message : '상품 등록 중 오류가 발생했습니다.'
      alert(errorMessage)
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="home-page">
      {/* Header */}
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
            <Link href="/login" className="user-btn">로그인</Link>
          </div>
        </div>
      </header>

      {/* Product Create Section */}
      <section className="product-create-section">
        <div className="container">
          <div className="product-create-container">
            <div className="create-header">
              <h1 className="create-title">상품 등록</h1>
              <Link href="/" className="create-cancel-btn">
                취소
              </Link>
            </div>

            <form className="create-form" onSubmit={handleSubmit}>
              {/* Product Name */}
              <div className="form-group">
                <label htmlFor="name" className="form-label">
                  상품명 <span className="required">*</span>
                </label>
                <input
                  type="text"
                  id="name"
                  className="form-input"
                  placeholder="상품명을 입력하세요"
                  value={formData.name}
                  onChange={(e) => handleInputChange('name', e.target.value)}
                  required
                />
              </div>

              {/* Category */}
              <div className="form-group">
                <label htmlFor="category" className="form-label">
                  카테고리 <span className="required">*</span>
                </label>
                <select
                  id="category"
                  className="form-select"
                  value={formData.category}
                  onChange={(e) => handleInputChange('category', e.target.value as ProductCategory)}
                  required
                >
                  {categoryOptions.map((option) => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </div>

              {/* Description */}
              <div className="form-group">
                <label htmlFor="description" className="form-label">상품 설명</label>
                <textarea
                  id="description"
                  className="form-textarea"
                  placeholder="상품 설명을 입력하세요"
                  value={formData.description}
                  onChange={(e) => handleInputChange('description', e.target.value)}
                  rows={6}
                />
              </div>

              {/* Price */}
              <div className="form-row">
                <div className="form-group">
                  <label htmlFor="price" className="form-label">
                    정가 <span className="required">*</span>
                  </label>
                  <input
                    type="number"
                    id="price"
                    className="form-input"
                    placeholder="0"
                    value={formData.price || ''}
                    onChange={(e) => handleInputChange('price', parseFloat(e.target.value) || 0)}
                    min="0"
                    step="1"
                    required
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="salePrice" className="form-label">
                    할인가 <span className="required">*</span>
                  </label>
                  <input
                    type="number"
                    id="salePrice"
                    className="form-input"
                    placeholder="0"
                    value={formData.salePrice || ''}
                    onChange={(e) => handleInputChange('salePrice', parseFloat(e.target.value) || 0)}
                    min="0"
                    step="1"
                    required
                  />
                </div>
              </div>

              {/* Stock */}
              <div className="form-group">
                <label htmlFor="stock" className="form-label">
                  재고 <span className="required">*</span>
                </label>
                <input
                  type="number"
                  id="stock"
                  className="form-input"
                  placeholder="0"
                  value={formData.stock || ''}
                  onChange={(e) => handleInputChange('stock', parseInt(e.target.value) || 0)}
                  min="0"
                  step="1"
                  required
                />
              </div>

              {/* Images */}
              <div className="form-group">
                <label className="form-label">상품 이미지 (최대 10개)</label>
                <div className="image-upload-wrapper">
                  <label htmlFor="image-upload" className="image-upload-label">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                      <path d="M12 5V19M5 12H19" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                    </svg>
                    <span>이미지 선택</span>
                  </label>
                  <input
                    type="file"
                    id="image-upload"
                    className="image-upload-input"
                    accept="image/*"
                    multiple
                    onChange={handleFileSelect}
                    disabled={imageFiles.length >= 10}
                  />
                  {imageFiles.length > 0 && (
                    <span className="image-count">({imageFiles.length} / 10)</span>
                  )}
                </div>
                <div className="images-preview">
                  {imagePreviews.map((preview, index) => {
                    // 업로드된 public URL이 있으면 그것을 사용, 없으면 로컬 미리보기 사용
                    const imageUrl = uploadedImageUrls[index] || preview
                    const isUploading = uploadingImageIndex.has(index)
                    const isUploaded = !!uploadedImageUrls[index]
                    
                    return (
                      <div key={index} className="image-preview-item">
                        <img src={imageUrl} alt={`상품 이미지 ${index + 1}`} className="preview-image" />
                        {isUploading && (
                          <div style={{ 
                            fontSize: '12px', 
                            color: '#667eea', 
                            marginTop: '4px',
                            textAlign: 'center',
                            fontWeight: '600'
                          }}>
                            업로드 중...
                          </div>
                        )}
                        {isUploaded && !isUploading && (
                          <div style={{ 
                            fontSize: '12px', 
                            color: '#4CAF50', 
                            marginTop: '4px',
                            textAlign: 'center'
                          }}>
                            ✓ 업로드 완료
                          </div>
                        )}
                        <button
                          type="button"
                          className="image-remove-btn"
                          onClick={() => handleRemoveImage(index)}
                          disabled={isUploading}
                        >
                          삭제
                        </button>
                      </div>
                    )
                  })}
                </div>
              </div>

              {/* Submit Button */}
              <div className="create-actions">
                <button
                  type="submit"
                  className="create-submit-btn"
                  disabled={isSubmitting || uploadingImageIndex.size > 0 || !formData.name.trim() || formData.price <= 0 || formData.salePrice <= 0}
                >
                  {uploadingImageIndex.size > 0 ? `이미지 업로드 중... (${uploadingImageIndex.size})` : isSubmitting ? '등록 중...' : '상품 등록'}
                </button>
              </div>
            </form>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="footer">
        <div className="container">
          <div className="footer-content">
            <div className="footer-section">
              <h3>고객센터</h3>
              <p>1588-0000</p>
              <p>평일 09:00 - 18:00</p>
            </div>
            <div className="footer-section">
              <h3>회사정보</h3>
              <p>주소: 서울시 강남구</p>
              <p>사업자등록번호: 000-00-00000</p>
            </div>
            <div className="footer-section">
              <h3>이용안내</h3>
              <Link href="/terms">이용약관</Link>
              <Link href="/privacy">개인정보처리방침</Link>
            </div>
          </div>
          <div className="footer-bottom">
            <p>&copy; 2024 뭐든사. All rights reserved.</p>
          </div>
        </div>
      </footer>
    </div>
  )
}
