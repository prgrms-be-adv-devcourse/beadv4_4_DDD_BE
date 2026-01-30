'use client'

import Link from 'next/link'
import { useParams, useRouter } from 'next/navigation'
import { useEffect, useState } from 'react'
import Header from '../../components/Header'

interface ProductDetailResponse {
  id: number
  sellerId: number
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
  createdAt: string
  updatedAt: string
  createdBy: number
  updatedBy: number
}

interface ApiResponse {
  isSuccess: boolean
  code: string
  message: string
  result: ProductDetailResponse
}

export default function ProductDetailPage() {
  const params = useParams()
  const router = useRouter()
  const productId = params.id as string
  
  const [product, setProduct] = useState<ProductDetailResponse | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    // Mock 데이터로 상품 정보 표시
    const mockProduct: ProductDetailResponse = {
      id: parseInt(productId) || 1,
      sellerId: 1,
      name: '베이직 레더 가방 130004',
      category: '지오다노',
      description: '고급 가죽으로 제작된 베이직 레더 가방입니다. 실용적이면서도 세련된 디자인으로 일상생활과 여행 모두에 적합합니다. 넉넉한 수납공간과 내구성이 뛰어난 소재를 사용하여 오래 사용하실 수 있습니다.',
      price: 25000,
      salePrice: 19800,
      currency: 'KRW',
      productStatus: 'ACTIVE',
      saleStatus: 'ON_SALE',
      stock: 10,
      isFavorite: false,
      favoriteCount: 0,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
      createdBy: 1,
      updatedBy: 1,
    }
    
    setTimeout(() => {
      setProduct(mockProduct)
      setIsLoading(false)
    }, 300) // 로딩 효과를 위한 약간의 지연
  }, [productId])

  const [quantity, setQuantity] = useState(1)
  const [isCreatingOrder, setIsCreatingOrder] = useState(false)
  const [isAddingToCart, setIsAddingToCart] = useState(false)

  const handleOrder = async () => {
    if (!product) return
    
    if (product.stock <= 0) {
      alert('품절된 상품입니다.')
      return
    }

    if (quantity > product.stock) {
      alert(`재고가 부족합니다. (재고: ${product.stock}개)`)
      return
    }

    setIsCreatingOrder(true)

    try {
      // Mock 데이터로 주문 페이지로 이동
      router.push('/order')
    } catch (error) {
      console.error('주문 생성 실패:', error)
      const errorMessage = error instanceof Error ? error.message : '주문 생성 중 오류가 발생했습니다.'
      alert(errorMessage)
    } finally {
      setIsCreatingOrder(false)
    }
  }

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('ko-KR').format(price)
  }

  const handleAddToCart = async () => {
    if (!product) return
    
    if (product.stock <= 0) {
      alert('품절된 상품입니다.')
      return
    }

    if (quantity > product.stock) {
      alert(`재고가 부족합니다. (재고: ${product.stock}개)`)
      return
    }

    setIsAddingToCart(true)

    try {
      // Mock 데이터로 장바구니에 추가하고 페이지 이동
      alert('장바구니에 상품이 추가되었습니다.')
      router.push('/cart')
    } catch (error) {
      console.error('장바구니 추가 실패:', error)
      const errorMessage = error instanceof Error ? error.message : '장바구니 추가 중 오류가 발생했습니다.'
      alert(errorMessage)
    } finally {
      setIsAddingToCart(false)
    }
  }

  if (isLoading) {
    return (
      <div className="home-page">
        <Header />
        <div className="product-detail-container">
          <div className="container">
            <div style={{ textAlign: 'center', padding: '80px 20px' }}>
              <p>상품 정보를 불러오는 중...</p>
            </div>
          </div>
        </div>
      </div>
    )
  }

  if (error || !product) {
    return (
      <div className="home-page">
        <Header />
        <div className="product-detail-container">
          <div className="container">
            <div style={{ textAlign: 'center', padding: '80px 20px' }}>
              <p style={{ color: '#f44336', marginBottom: '20px' }}>{error || '상품을 찾을 수 없습니다.'}</p>
              <Link href="/" style={{ color: '#667eea', textDecoration: 'underline' }}>
                홈으로 돌아가기
              </Link>
            </div>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="home-page">
      {/* Header */}
      <Header />

      {/* Product Detail Section */}
      <div className="product-detail-container">
        <div className="container">
          <div className="product-detail-content">
            {/* Product Images */}
            <div className="product-images">
              <div className="main-image">
                <div className="image-placeholder-large">상품 이미지</div>
              </div>
              <div className="thumbnail-images">
                {[1, 2, 3, 4].map((item) => (
                  <div key={item} className="thumbnail">
                    <div className="image-placeholder-small">이미지 {item}</div>
                  </div>
                ))}
              </div>
            </div>

            {/* Product Info */}
            <div className="product-info-section">
              <div className="product-brand-name">{product.category}</div>
              <h1 className="product-title">{product.name}</h1>
              <div className="product-price-section">
                {product.salePrice < product.price ? (
                  <>
                    <span className="product-price original-price">₩{formatPrice(product.price)}</span>
                    <span className="product-price sale-price">₩{formatPrice(product.salePrice)}</span>
                  </>
                ) : (
                  <span className="product-price">₩{formatPrice(product.salePrice)}</span>
                )}
              </div>

              <div className="product-divider"></div>

              {/* Delivery Info */}
              <div className="delivery-info">
                <div className="info-row">
                  <span className="info-label">배송비</span>
                  <span className="info-value">무료배송</span>
                </div>
                <div className="info-row">
                  <span className="info-label">배송예정</span>
                  <span className="info-value">01.14(수) 도착 예정</span>
                </div>
              </div>

              <div className="product-divider"></div>

              {/* 상품명 왼쪽 위, 수량·구매금액 같은 row (구매하기 버튼 위) */}
              <div className="product-options">
                <div className="option-group quantity-with-amount">
                  <span className="option-label product-name-row">{product.name}</span>
                  <div className="quantity-amount-row">
                    <div className="quantity-selector">
                      <button 
                        className="quantity-btn minus"
                        onClick={() => setQuantity(Math.max(1, quantity - 1))}
                        disabled={quantity <= 1}
                      >
                        -
                      </button>
                      <span className="quantity-value">{quantity}</span>
                      <button 
                        className="quantity-btn plus"
                        onClick={() => setQuantity(Math.min(product.stock, quantity + 1))}
                        disabled={quantity >= product.stock}
                      >
                        +
                      </button>
                    </div>
                    <span className="quantity-amount">₩{formatPrice(product.salePrice * quantity)}</span>
                  </div>
                </div>
              </div>

              <div className="product-divider"></div>

              {/* Action Buttons */}
              <div className="action-buttons">
                <button 
                  className="cart-button" 
                  onClick={handleAddToCart}
                  disabled={isAddingToCart || product.stock <= 0}
                >
                  {isAddingToCart ? '추가 중...' : product.stock <= 0 ? '품절' : '장바구니'}
                </button>
                <button 
                  className="buy-button" 
                  onClick={handleOrder}
                  disabled={isCreatingOrder || product.stock <= 0}
                >
                  {isCreatingOrder ? '주문 처리 중...' : product.stock <= 0 ? '품절' : '구매하기'}
                </button>
              </div>
            </div>
          </div>

          {/* Product Description */}
          <div className="product-description-section">
            <h2 className="description-title">상품 상세 정보</h2>
            <div className="description-content">
              <p>{product.description || '상품 상세 설명이 없습니다.'}</p>
            </div>
          </div>
        </div>
      </div>

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
