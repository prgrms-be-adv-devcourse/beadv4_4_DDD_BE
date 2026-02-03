'use client'

import Link from 'next/link'
import { useParams, useRouter } from 'next/navigation'
import { useEffect, useState } from 'react'
import MypageLayout from '../../../../components/MypageLayout'

type ProductCategory = 'OUTER' | 'UPPER' | 'LOWER' | 'CAP' | 'SHOES' | 'BAG' | 'BEAUTY'
type ProductStatus = 'CANCELED' | 'DRAFT' | 'COMPLETED'
type SaleStatus = 'SALE' | 'NOT_SALE' | 'SOLD_OUT'

interface ProductEditForm {
  name: string
  category: ProductCategory
  description: string
  saleStatus: SaleStatus
  price: number
  salePrice: number
  stock: number
  images: string[]
}

interface ProductDetailResponse {
  id: number
  sellerId: number
  sellerBusinessName: string
  name: string
  category: string
  description: string
  price: number
  salePrice: number
  currency: string
  productStatus: string
  saleStatus: string
  stock: number
  isFavorite: boolean
  favoriteCount: number
  images: ProductImageDto[]
  createdAt: string
  updatedAt: string
  createdBy: number
  updatedBy: number
}

interface ProductImageDto {
  id: number
  imageUrl: string
  isPrimary: boolean
  sortOrder: number
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

const saleStatusOptions: { value: SaleStatus; label: string }[] = [
  { value: 'SALE', label: '판매중' },
  { value: 'NOT_SALE', label: '판매중지' },
  { value: 'SOLD_OUT', label: '품절' },
]

const baseButtonStyle = {
  padding: '8px 18px',
  borderRadius: '8px',
  fontSize: '14px',
  fontWeight: 500,
  cursor: 'pointer',
  minWidth: '100px',
}

const cancelStyle = {
  ...baseButtonStyle,
  border: '1px solid #ddd',
  background: '#fff',
  color: '#666',
}

const draftStyle = {
  ...baseButtonStyle,
  border: '1px solid #667eea',
  background: '#fff',
  color: '#667eea',
}

const completeStyle = {
  ...baseButtonStyle,
  border: '1px solid #4f46e5',
  background: '#f5f7ff',
  color: '#4f46e5',
  fontWeight: 600,
}

const apiUrl = process.env.NEXT_PUBLIC_API_URL || ''

export default function ProductEditPage() {
  const params = useParams()
  const router = useRouter()
  const productId = typeof params?.id === 'string' ? params.id : undefined

  const accessToken = localStorage.getItem('accessToken')

  const fetchProductDetail = async (productId: string) => {
    if (!apiUrl) throw new Error('API URL 없음')

    const res = await fetch(
        `${apiUrl}/api/v1/products/${productId}`,
        {
          headers: {
            Authorization: `Bearer ${accessToken}`,
          },
        }
    )

    const data = await res.json()

    if (!res.ok || !data.isSuccess) {
      throw new Error(data.message || '상품 정보를 불러오지 못했습니다.')
    }

    return data.result
  }

  const [formData, setFormData] = useState<ProductEditForm>({
    name: '',
    category: 'OUTER',
    description: '',
    saleStatus: 'NOT_SALE',
    price: 0,
    salePrice: 0,
    stock: 0,
    images: [],
  })
  const [imageFiles, setImageFiles] = useState<File[]>([])
  const [product, setProduct] = useState<ProductDetailResponse>()
  const [originalProduct, setOriginalProduct] = useState<ProductEditForm | null>(null)
  const [imagePreviews, setImagePreviews] = useState<string[]>([])
  const [uploadedImageUrls, setUploadedImageUrls] = useState<string[]>([])
  const [uploadingImageIndex, setUploadingImageIndex] = useState<Set<number>>(new Set())
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [initialized, setInitialized] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')
  const [submitAction, setSubmitAction] = useState<ProductStatus | null>(null)

  const isCompleted = product?.productStatus === 'COMPLETED'


  const buildUpdatePayload = () => {
    if (!originalProduct) return {}

    const payload: Record<string, any> = {}

    const addIfChanged = <T,>(
        key: keyof ProductEditForm,
        value: T,
        original: T
    ) => {
      if (value !== original) {
        payload[key] = value
      }
    }

    addIfChanged('description', formData.description, originalProduct.description)
    addIfChanged('stock', formData.stock, originalProduct.stock)
    addIfChanged('images', formData.images, originalProduct.images)
    addIfChanged('saleStatus', formData.saleStatus, originalProduct.saleStatus)
    // 아래 필드는 변경 시 에러 발생
    addIfChanged('name', formData.name, originalProduct.name)
    addIfChanged('category', formData.category, originalProduct.category)
    addIfChanged('price', formData.price, originalProduct.price)
    addIfChanged('salePrice', formData.salePrice, originalProduct.salePrice)

    return payload
  }


  useEffect(() => {
    if (!productId || initialized) return

    const loadProduct = async () => {
      try {
        const product = await fetchProductDetail(productId)

        setFormData({
          name: product.name,
          category: product.category,
          description: product.description ?? '',
          saleStatus: product.saleStatus,
          price: product.price,
          salePrice: product.salePrice ?? 0,
          stock: product.stock,
          images: product.images.map((it: {imageUrl: string}) => it.imageUrl) ?? [],
        })
        setOriginalProduct(product)
        setImagePreviews(product.images.map((it: { imageUrl: string }) => it.imageUrl) ?? [])
        setUploadedImageUrls(product.images.map((it: { imageUrl: string }) => it.imageUrl) ?? [])
        setInitialized(true)
        setProduct(product);
      } catch (e) {
        console.error(e)
        alert(
            e instanceof Error ? e.message : '상품 정보를 불러오지 못했습니다.'
        )
        router.push('/mypage/products')
      }
    }
    loadProduct()
  }, [productId, product, initialized])

  const getDraftButtonLabel = () => {
    if (uploadingImageIndex.size > 0) {
      return `이미지 업로드 중... (${uploadingImageIndex.size})`
    }

    if (isSubmitting) {
      return '저장 중...'
    }

    return product?.productStatus === 'COMPLETED'
        ? '저장'
        : '임시 저장'
  }

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
          alert('이미지는 최대 5개까지 추가할 수 있습니다.')
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

  const handleSubmit = async (e?: React.FormEvent) => {
    e?.preventDefault()
    if (!submitAction) return

    if (!formData.name.trim()) {
      alert('상품명을 입력해주세요.')
      return
    }
    setIsSubmitting(true)

    try {
      if (!apiUrl) {
        setErrorMessage('API URL이 설정되지 않았습니다. (NEXT_PUBLIC_API_URL)')
        setIsSubmitting(false)
        return
      }

      const statusChangeUrl = `${apiUrl}/api/v1/products/${productId}/status?status=${submitAction}`
      const updateUrl = `${apiUrl}/api/v1/products/${productId}`

      // 등록 취소
      if (submitAction === 'CANCELED') {
        const cancelResponse = await fetch(statusChangeUrl, {
          method: 'PATCH',
          headers: {
            Authorization: `Bearer ${accessToken}`
          },
        });
        if (!cancelResponse.ok) {
          const errData = await cancelResponse.json().catch(() => ({}))
          const errorMessage = errData.message
          setErrorMessage(errorMessage)
          setIsSubmitting(false)
          return
        } else {
          alert('상품 등록 취소가 완료되었습니다.')
          router.push('/mypage/products')
        }
      } else if (submitAction === 'DRAFT') {
        // 임시저장 || 저장
        const payload = buildUpdatePayload()
        if (Object.keys(payload).length === 0) {
          alert('변경된 내용이 없습니다.')
          return
        }

        const res = await fetch(updateUrl, {
          method: 'PATCH',
          headers: {
            Authorization: `Bearer ${accessToken}`,
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(payload),
        })

        if (!res.ok) {
          const errData = await res.json().catch(() => ({}))
          const errorMessage = errData.message
          alert(errorMessage)
          setErrorMessage(errorMessage)
          setIsSubmitting(false)
          return
        }
        alert('상품 수정이 완료되었습니다.')
        router.push('/mypage/products')
      } else if (submitAction === 'COMPLETED') {
        // 등록 완료
        const payload = buildUpdatePayload()
        if (Object.keys(payload).length === 0) {
          alert('변경된 내용이 없습니다.')
          return
        }
        if (formData.price < 0 || formData.salePrice < 0) {
          alert('가격은 0 이상이어야 합니다.');
          return;
        }
        if (formData.salePrice < formData.price) {
          alert('판매가는 정가보다 크거나 같아야 합니다.')
          return
        }
        if (formData.stock < 0) {
          alert('재고는 0 이상이어야 합니다.')
          return
        }
        const completeResponse = await fetch(statusChangeUrl, {
          method: 'PATCH',
          headers: {
            Authorization: `Bearer ${accessToken}`,
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            name: formData.name,
            category: formData.category,
            description: formData.description,
            price: formData.price,
            salePrice: formData.salePrice,
            stock: formData.stock,
            images: formData.images
          })
        });

        if (!completeResponse.ok) {
          const errData = await completeResponse.json().catch(() => ({}))
          const errorMessage = errData.message
          console.log(errorMessage)
          setErrorMessage(errorMessage)
          setIsSubmitting(false)
          return
        }
        alert('상품 등록이 완료되었습니다.')
        router.push('/mypage/products')
      }
    } catch (error) {
      console.error('상품 수정 실패:', error)
      alert(error instanceof Error ? error.message : '상품 수정 중 오류가 발생했습니다.')
    } finally {
      setIsSubmitting(false)
    }
  }

  if (!productId) {
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
            뒤로가기
          </Link>
        </div>

        <form className="create-form" onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="id" className="form-label">
              상품번호
            </label>
            <div
              className="form-readonly"
              aria-readonly="true">
              {productId}
            </div>
          </div>
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
              disabled={isCompleted}
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
              disabled={isCompleted}
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

          <div className="form-group">
            <label htmlFor="saleStatus" className="form-label">
              판매 상태
            </label>
            <select
                id="category"
                className="form-select"
                value={formData.saleStatus}
                onChange={(e) => handleInputChange('saleStatus', e.target.value as SaleStatus)}
                required
            >
              {saleStatusOptions.map((option) => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
              ))}
            </select>
          </div>

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
                min={0}
                step={1}
                disabled={isCompleted}
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
                min={0}
                step={1}
                disabled={isCompleted}
              />
            </div>
          </div>

          <div className="form-group">
            <label htmlFor="stock" className="form-label">
              재고
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
                const isFromExisting = index < (product?.images.length ?? 0) && !imageFiles[index]
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
            <div
                style={{
                  display: 'flex',
                  justifyContent: 'center',
                  gap: '12px',
                  marginTop: '32px',
                }}
            >
              {/* 등록 취소 */}
              {product?.productStatus === 'DRAFT' ?
              <button
                  type="button"
                  style={cancelStyle}
                  disabled={isSubmitting}
                  onClick={() => {
                    const confirmed = window.confirm(
                        '등록 취소는 상품 수정이 불가합니다.\n' +
                        '정말 등록을 취소하시겠습니까?'
                    )
                    if (!confirmed) return

                    setSubmitAction('CANCELED')
                    handleSubmit()

                  }}
              >
                등록 취소
              </button>
                  : <></>
              }

              {/* 임시 저장 / 저장 */}
              {product?.productStatus === 'DRAFT' || product?.productStatus === 'COMPLETED' ?
              <button
                  type="submit"
                  style={draftStyle}
                  disabled={
                      isSubmitting ||
                      uploadingImageIndex.size > 0 ||
                      !formData.name.trim()
                  }
                  onClick={() => setSubmitAction('DRAFT')}
              >
                {getDraftButtonLabel()}
              </button>
                  : <></>
              }
              {/* 완료 */}
              { product?.productStatus === 'DRAFT' ?
              <button
                  type="submit"
                  style={completeStyle}
                  disabled={
                      isSubmitting ||
                      uploadingImageIndex.size > 0 ||
                      !formData.name.trim()
                  }
                  onClick={() => setSubmitAction('COMPLETED')}
              >
                {uploadingImageIndex.size > 0
                    ? `이미지 업로드 중... (${uploadingImageIndex.size})`
                    : isSubmitting
                        ? '저장 중...'
                        : '완료'}
              </button>
                  : <></>
              }
            </div>
          </div>
        </form>
      </div>
    </MypageLayout>
  )
}
