'use client'

import Link from 'next/link'
import { useRouter } from 'next/navigation'
import { useState } from 'react'
import MypageLayout from '../../components/MypageLayout'

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
    // API 통신 제거됨 - 로컬 미리보기 URL 반환
    return new Promise((resolve) => {
      const reader = new FileReader()
      reader.onloadend = () => {
        resolve(reader.result as string)
      }
      reader.readAsDataURL(file)
    })
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
      // API 통신 제거됨
      alert('상품 관리 기능이 비활성화되었습니다.')
      router.push('/mypage/products')
    } catch (error) {
      console.error('상품 관리 실패:', error)
      const errorMessage = error instanceof Error ? error.message : '상품 관리 중 오류가 발생했습니다.'
      alert(errorMessage)
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <MypageLayout>
      <div className="product-create-container" style={{ maxWidth: '900px' }}>
        <div className="create-header">
          <h1 className="create-title">상품 등록</h1>
          <Link href="/mypage/products" className="create-cancel-btn">
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
    </MypageLayout>
  )
}
