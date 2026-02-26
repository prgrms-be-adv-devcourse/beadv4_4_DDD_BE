'use client'

import Link from 'next/link'
import { useRouter } from 'next/navigation'
import { useState } from 'react'
import MypageLayout from '../../components/MypageLayout'
import api from "@/app/lib/axios";

type ProductCategory = 'OUTER' | 'UPPER' | 'LOWER' | 'CAP' | 'SHOES' | 'BAG' | 'BEAUTY'

type PresignedUrlResponse = {
  presignedUrl: string
  key: string
}

type PublicUrlResponse = {
  imageUrl: string
  key: string
}


interface ProductCreateRequest {
  name: string
  category: ProductCategory
  description: string
  price: number
  salePrice: number
  images: string[]
}

interface PresignedUrlApiResponse {
  isSuccess: boolean
  code: string
  message: string
  result: PresignedUrlResponse
}

interface PublicUrlApiResponse {
  isSuccess: boolean
  code: string
  message: string
  result: PublicUrlResponse
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
    category: 'OUTER',
    description: '',
    price: 0,
    salePrice: 0,
    images: [],
  })
  const [imageFiles, setImageFiles] = useState<File[]>([])
  const [imagePreviews, setImagePreviews] = useState<string[]>([]) // 로컬 미리보기용 (base64)
  const [uploadedImageUrls, setUploadedImageUrls] = useState<string[]>([]) // 업로드된 public URL들
  const [uploadingImageIndex, setUploadingImageIndex] = useState<Set<number>>(new Set()) // 업로드 중인 이미지 인덱스
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')


  const handleInputChange = (field: keyof ProductCreateRequest, value: any) => {
    setFormData(prev => ({ ...prev, [field]: value }))
  }

  const handleFileSelect = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files
    if (!files) return

    const newFiles: File[] = []

    // 파일 검증 및 미리보기 생성
    Array.from(files).forEach((file) => {
      if (!file.type.startsWith('image/')) {
        alert(`${file.name}은(는) 이미지 파일이 아닙니다.`)
        return
      }

      if (imageFiles.length + newFiles.length >= 5) {
        alert('이미지는 최대 5개까지 추가할 수 있습니다.')
        return
      }
      newFiles.push(file)
      const reader = new FileReader()
      reader.onloadend = () => {
        const result = reader.result as string
        setImagePreviews(prev => [...prev, result])
      }
      reader.readAsDataURL(file)
    })

    if (newFiles.length === 0) return

    // 파일 추가
    setImageFiles(prev => [...prev, ...newFiles])

    newFiles.forEach(async (file, i) => {
      const uploadIndex = imageFiles.length + i
      setUploadingImageIndex(prev => new Set(prev).add(uploadIndex))

      try {
        const downloadUrl = await uploadImageToS3(file)
        setUploadedImageUrls(prev => [...prev, downloadUrl])
      } catch (e) {
        handleRemoveImage(uploadIndex)
        alert(`이미지 업로드 실패: ${file.name}`)
      } finally {
        setUploadingImageIndex(prev => {
          const s = new Set(prev)
          s.delete(uploadIndex)
          return s
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
  const uploadImageToS3 = async (file: File): Promise<string> => {
    const fileApiUrl = process.env.NEXT_PUBLIC_API_URL!

    /** 1. presigned url 요청 */
    const presignedRes = await api.post(`${fileApiUrl}/api/v1/files/presigned-url`,
      JSON.stringify({
        domainType: 'PRODUCT',
        ext: 'png',
        contentType: file.type,
      }))

    if (!presignedRes.data.isSuccess) {
      throw new Error('Presigned URL 발급 실패')
    }

    const data: PresignedUrlApiResponse = await presignedRes.data

    /** 2. S3에 직접 PUT 업로드 */
    const uploadRes = await fetch(data.result.presignedUrl, {
      method: 'PUT',
      headers: {
        'Content-Type': file.type,
      },
      body: file,
    })

    if (!uploadRes.ok) {
      throw new Error('S3 업로드 실패')
    }

    /** 3. public-read URL 반환 */
    const publicUrlApi = `${fileApiUrl}/api/v1/files/public-url`;
    const downloadRes = await api.post(publicUrlApi,
      JSON.stringify({
          rawKey: data.result.key,
          domainType: 'PRODUCT',
          contentType: file.type,
        })
    )

    if (!downloadRes.data.isSuccess) {
      throw new Error('S3 다운로드 실패')
    }

    const downloadData: PublicUrlApiResponse = await downloadRes.data

    return downloadData.result.imageUrl
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()

    if (uploadingImageIndex.size > 0) {
      alert('이미지 업로드가 완료될 때까지 기다려주세요.')
      return
    }

    const images = uploadedImageUrls.filter(Boolean)

    if (images.length !== imageFiles.length) {
      alert('업로드되지 않은 이미지가 있습니다.')
      return
    }

    if (!formData.name.trim()) {
      alert('상품명을 입력해주세요.')
      return
    }

    if (formData.price < 0 || formData.salePrice < 0) {
      alert('가격은 0 이상이어야 합니다.')
      return
    }

    if (formData.salePrice < formData.price) {
      alert('판매가는 정가보다 크거나 같아야 합니다.')
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
      const apiUrl = process.env.NEXT_PUBLIC_API_URL || ''
      if (!apiUrl) {
        setErrorMessage('API URL이 설정되지 않았습니다. (NEXT_PUBLIC_API_URL)')
        setIsSubmitting(false)
        return
      }

      const url = `${apiUrl}/api/v1/products`
      const res = await api.post(url,
        JSON.stringify({
          name: formData.name,
          category: formData.category,
          description: formData.description,
          price: formData.price,
          salePrice: formData.salePrice,
          images: uploadedImageUrls
        })
      )

      if (!res.data.isSuccess) {
        const errData = await res.data.catch(() => ({}))
        const errorMessage = errData.message
        setErrorMessage(errorMessage)
        setIsSubmitting(false)
        return
      }
      alert('상품 등록이 완료되었습니다.')
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
                    정가
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
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="salePrice" className="form-label">
                    판매가
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
                  />
                </div>
              </div>

              {/* Images */}
              <div className="form-group">
                <label className="form-label">상품 이미지 (최대 5개)</label>
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
                  disabled={isSubmitting || uploadingImageIndex.size > 0 || !formData.name.trim()}
                >
                  {uploadingImageIndex.size > 0 ? `이미지 업로드 중... (${uploadingImageIndex.size})` : isSubmitting ? '등록 중...' : '임시 저장'}
                </button>
              </div>
            </form>
      </div>
    </MypageLayout>
  )
}
