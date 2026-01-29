'use client'

import Link from 'next/link'
import { useParams, useRouter } from 'next/navigation'
import { useEffect, useState } from 'react'
import MypageLayout from '../../../../components/MypageLayout'

type ProductCategory = 'OUTER' | 'UPPER' | 'LOWER' | 'CAP' | 'SHOES' | 'BAG' | 'BEAUTY'

interface ProductEditForm {
  name: string
  category: ProductCategory
  description: string
  price: number
  salePrice: number
  stock: number
  images: string[]
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

// 상품 목록과 동일한 id 기준 목 데이터 (상세 수정용 필드 포함)
function getProductDetailById(id: string | undefined): ProductEditForm | null {
  if (!id) return null
  const numId = parseInt(id, 10)
  const map: Record<number, ProductEditForm> = {
    1: {
      name: '데일리 베이직 티셔츠',
      category: 'UPPER',
      description: '편안한 데일리 착용용 베이직 티셔츠입니다.',
      price: 29000,
      salePrice: 25000,
      stock: 50,
      images: [],
    },
    2: {
      name: '루즈핏 오버코트',
      category: 'OUTER',
      description: '루즈핏으로 착용감이 좋은 오버코트입니다.',
      price: 89000,
      salePrice: 89000,
      stock: 12,
      images: [],
    },
    3: {
      name: '니트 풀오버 세트',
      category: 'UPPER',
      description: '니트 풀오버와 하의 세트 상품입니다.',
      price: 44000,
      salePrice: 39600,
      stock: 0,
      images: [],
    },
    4: {
      name: '미니 크로스백',
      category: 'BAG',
      description: '데일리로 사용하기 좋은 미니 크로스백입니다.',
      price: 35000,
      salePrice: 31500,
      stock: 8,
      images: [],
    },
  }
  return map[numId] ?? null
}

export default function ProductEditPage() {
  const params = useParams()
  const router = useRouter()
  const productId = typeof params?.id === 'string' ? params.id : undefined
  const product = getProductDetailById(productId)

  const [formData, setFormData] = useState<ProductEditForm>({
    name: '',
    category: 'UPPER',
    description: '',
    price: 0,
    salePrice: 0,
    stock: 0,
    images: [],
  })
  const [imageFiles, setImageFiles] = useState<File[]>([])
  const [imagePreviews, setImagePreviews] = useState<string[]>([])
  const [uploadedImageUrls, setUploadedImageUrls] = useState<string[]>([])
  const [uploadingImageIndex, setUploadingImageIndex] = useState<Set<number>>(new Set())
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [initialized, setInitialized] = useState(false)

  useEffect(() => {
    if (product && !initialized) {
      setFormData(product)
      setImagePreviews(product.images.filter(Boolean))
      setUploadedImageUrls(product.images.filter(Boolean))
      setInitialized(true)
    }
  }, [product, initialized])

  const handleInputChange = (field: keyof ProductEditForm, value: any) => {
    setFormData((prev) => ({ ...prev, [field]: value }))
  }

  const handleFileSelect = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files
    if (!files) return

    const newFiles: File[] = []
    const startIndex = imageFiles.length

    Array.from(files).forEach((file) => {
      if (file.type.startsWith('image/')) {
        if (imageFiles.length + newFiles.length < 10) {
          newFiles.push(file)
          const reader = new FileReader()
          reader.onloadend = () => {
            const result = reader.result as string
            setImagePreviews((prev) => [...prev, result])
          }
          reader.readAsDataURL(file)
        } else {
          alert('이미지는 최대 10개까지 추가할 수 있습니다.')
        }
      } else {
        alert(`${file.name}은(는) 이미지 파일이 아닙니다.`)
      }
    })

    setImageFiles((prev) => [...prev, ...newFiles])

    newFiles.forEach(async (file, relativeIndex) => {
      const absoluteIndex = startIndex + relativeIndex
      setUploadingImageIndex((prev) => new Set(prev).add(absoluteIndex))
      try {
        const reader = new FileReader()
        reader.onloadend = () => {
          const url = reader.result as string
          setUploadedImageUrls((prev) => {
            const next = [...prev]
            next[absoluteIndex] = url
            return next
          })
        }
        reader.readAsDataURL(file)
      } finally {
        setUploadingImageIndex((prev) => {
          const next = new Set(prev)
          next.delete(absoluteIndex)
          return next
        })
      }
    })
  }

  const handleRemoveImage = (index: number) => {
    setImageFiles((prev) => prev.filter((_, i) => i !== index))
    setImagePreviews((prev) => prev.filter((_, i) => i !== index))
    setUploadedImageUrls((prev) => prev.filter((_, i) => i !== index))
    setFormData((prev) => ({
      ...prev,
      images: prev.images.filter((_, i) => i !== index),
    }))
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

    setIsSubmitting(true)
    try {
      alert('상품 수정 기능이 비활성화되어 있습니다. (데모)')
      router.push('/mypage/products')
    } catch (error) {
      console.error('상품 수정 실패:', error)
      alert(error instanceof Error ? error.message : '상품 수정 중 오류가 발생했습니다.')
    } finally {
      setIsSubmitting(false)
    }
  }

  if (productId && !product) {
    return (
      <MypageLayout>
        <div style={{ maxWidth: '900px' }}>
          <p style={{ color: '#666', marginBottom: '16px' }}>해당 상품을 찾을 수 없습니다.</p>
          <Link href="/mypage/products" style={{ color: '#667eea', fontWeight: 600 }}>
            상품 목록으로
          </Link>
        </div>
      </MypageLayout>
    )
  }

  const displayPreviews = uploadedImageUrls.length > 0 ? uploadedImageUrls : imagePreviews

  return (
    <MypageLayout>
      <div className="product-create-container" style={{ maxWidth: '900px' }}>
        <div className="create-header">
          <h1 className="create-title">상품 수정</h1>
          <Link href="/mypage/products" className="create-cancel-btn">
            취소
          </Link>
        </div>

        <form className="create-form" onSubmit={handleSubmit}>
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

          <div className="form-group">
            <label htmlFor="description" className="form-label">
              상품 설명
            </label>
            <textarea
              id="description"
              className="form-textarea"
              placeholder="상품 설명을 입력하세요"
              value={formData.description}
              onChange={(e) => handleInputChange('description', e.target.value)}
              rows={6}
            />
          </div>

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
                min={0}
                step={1}
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
                min={0}
                step={1}
                required
              />
            </div>
          </div>

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
              onChange={(e) => handleInputChange('stock', parseInt(e.target.value, 10) || 0)}
              min={0}
              step={1}
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label">상품 이미지 (최대 10개)</label>
            <div className="image-upload-wrapper">
              <label htmlFor="image-upload-edit" className="image-upload-label">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M12 5V19M5 12H19" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
                </svg>
                <span>이미지 선택</span>
              </label>
              <input
                type="file"
                id="image-upload-edit"
                className="image-upload-input"
                accept="image/*"
                multiple
                onChange={handleFileSelect}
                disabled={displayPreviews.length >= 10}
              />
              {displayPreviews.length > 0 && (
                <span className="image-count">({displayPreviews.length} / 10)</span>
              )}
            </div>
            <div className="images-preview">
              {displayPreviews.map((url, index) => {
                const isUploading = uploadingImageIndex.has(index)
                const isFromExisting = index < (product?.images?.length ?? 0) && !imageFiles[index]
                return (
                  <div key={index} className="image-preview-item">
                    <img src={url} alt={`상품 이미지 ${index + 1}`} className="preview-image" />
                    {isUploading && (
                      <div style={{ fontSize: '12px', color: '#667eea', marginTop: '4px', textAlign: 'center', fontWeight: 600 }}>
                        업로드 중...
                      </div>
                    )}
                    {!isUploading && (
                      <div style={{ fontSize: '12px', color: '#4CAF50', marginTop: '4px', textAlign: 'center' }}>
                        {isFromExisting ? '기존 이미지' : '✓ 업로드 완료'}
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

          <div className="create-actions">
            <button
              type="submit"
              className="create-submit-btn"
              disabled={
                isSubmitting ||
                uploadingImageIndex.size > 0 ||
                !formData.name.trim() ||
                formData.price <= 0 ||
                formData.salePrice <= 0
              }
            >
              {uploadingImageIndex.size > 0
                ? `이미지 업로드 중... (${uploadingImageIndex.size})`
                : isSubmitting
                  ? '저장 중...'
                  : '수정 완료'}
            </button>
          </div>
        </form>
      </div>
    </MypageLayout>
  )
}
