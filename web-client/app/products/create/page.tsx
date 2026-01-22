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

interface ApiResponse {
  isSuccess: boolean
  code: string
  message: string
  result: any
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
  const [imageUrl, setImageUrl] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  const handleInputChange = (field: keyof ProductCreateRequest, value: any) => {
    setFormData(prev => ({ ...prev, [field]: value }))
  }

  const handleAddImage = () => {
    if (imageUrl.trim()) {
      if (formData.images.length < 10) {
        setFormData(prev => ({ ...prev, images: [...prev.images, imageUrl.trim()] }))
        setImageUrl('')
      } else {
        alert('이미지는 최대 10개까지 추가할 수 있습니다.')
      }
    }
  }

  const handleRemoveImage = (index: number) => {
    setFormData(prev => ({
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
      const apiUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'
      
      const productRequest: ProductCreateRequest = {
        name: formData.name.trim(),
        category: formData.category,
        description: formData.description.trim() || '',
        price: formData.price,
        salePrice: formData.salePrice,
        stock: formData.stock,
        images: formData.images,
      }

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
        throw new Error(`상품 등록 실패 (${response.status})`)
      }

      const apiResponse: ApiResponse = await response.json()

      if (apiResponse.isSuccess) {
        alert('상품이 성공적으로 등록되었습니다.')
        router.push(`/products/${apiResponse.result?.id || ''}`)
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
                <label className="form-label">상품 이미지</label>
                <div className="image-input-wrapper">
                  <input
                    type="text"
                    className="image-url-input"
                    placeholder="이미지 URL을 입력하세요"
                    value={imageUrl}
                    onChange={(e) => setImageUrl(e.target.value)}
                    onKeyPress={(e) => {
                      if (e.key === 'Enter') {
                        e.preventDefault()
                        handleAddImage()
                      }
                    }}
                  />
                  <button
                    type="button"
                    className="image-add-btn"
                    onClick={handleAddImage}
                    disabled={!imageUrl.trim() || formData.images.length >= 10}
                  >
                    추가
                  </button>
                </div>
                <div className="images-preview">
                  {formData.images.map((url, index) => (
                    <div key={index} className="image-preview-item">
                      <img src={url} alt={`상품 이미지 ${index + 1}`} className="preview-image" />
                      <button
                        type="button"
                        className="image-remove-btn"
                        onClick={() => handleRemoveImage(index)}
                      >
                        삭제
                      </button>
                    </div>
                  ))}
                </div>
              </div>

              {/* Submit Button */}
              <div className="create-actions">
                <button
                  type="submit"
                  className="create-submit-btn"
                  disabled={isSubmitting || !formData.name.trim()}
                >
                  {isSubmitting ? '등록 중...' : '상품 등록'}
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
