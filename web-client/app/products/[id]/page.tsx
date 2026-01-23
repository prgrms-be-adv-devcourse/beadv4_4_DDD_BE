'use client'

import Link from 'next/link'
import { useParams, useRouter } from 'next/navigation'
import { useEffect, useState } from 'react'

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
    const fetchProduct = async () => {
      try {
        const apiUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'
        const response = await fetch(`${apiUrl}/api/v1/products/${productId}`)
        
        if (!response.ok) {
          const errorText = await response.text()
          console.error('API 응답 에러:', response.status, errorText)
          throw new Error(`상품 정보를 불러올 수 없습니다 (${response.status})`)
        }
        
        const apiResponse: ApiResponse = await response.json()
        
        if (apiResponse.isSuccess && apiResponse.result) {
          setProduct(apiResponse.result)
          setError(null)
        } else {
          throw new Error(apiResponse.message || '상품 정보를 가져올 수 없습니다.')
        }
      } catch (error) {
        console.error('상품 정보 조회 실패:', error)
        const errorMessage = error instanceof Error ? error.message : '알 수 없는 오류가 발생했습니다.'
        setError(errorMessage)
      } finally {
        setIsLoading(false)
      }
    }

    if (productId) {
      fetchProduct()
    }
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
      const apiUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'
      
      // 주문 생성 API 호출
      const response = await fetch(`${apiUrl}/api/v1/orders`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          productId: product.id,
          quantity: quantity,
          recipientName: '홍길동', // TODO: 실제 사용자 정보로 변경
          recipientPhone: '010-1234-5678', // TODO: 실제 사용자 정보로 변경
          zipCode: '12345', // TODO: 실제 사용자 정보로 변경
          address: '서울시 강남구', // TODO: 실제 사용자 정보로 변경
          addressDetail: '테헤란로 123', // TODO: 실제 사용자 정보로 변경
        }),
      })

      if (!response.ok) {
        const errorText = await response.text()
        console.error('주문 생성 API 에러:', response.status, errorText)
        throw new Error(`주문 생성 실패 (${response.status})`)
      }

      const apiResponse = await response.json()

      if (apiResponse.isSuccess && apiResponse.result) {
        // 주문 성공 시 주문 페이지로 이동
        router.push('/order')
      } else {
        throw new Error(apiResponse.message || '주문 생성에 실패했습니다.')
      }
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
      const apiUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'
      
      // 장바구니 추가 API 호출
      const response = await fetch(`${apiUrl}/api/v1/orders/cart/item`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          productId: product.id,
          quantity: quantity,
        }),
      })

      if (!response.ok) {
        const errorText = await response.text()
        console.error('장바구니 추가 API 에러:', response.status, errorText)
        throw new Error(`장바구니 추가 실패 (${response.status})`)
      }

      const apiResponse = await response.json()

      if (apiResponse.isSuccess) {
        alert('장바구니에 상품이 추가되었습니다.')
        // 장바구니 페이지로 이동할지 선택할 수 있도록
        if (confirm('장바구니로 이동하시겠습니까?')) {
          router.push('/cart')
        }
      } else {
        throw new Error(apiResponse.message || '장바구니 추가에 실패했습니다.')
      }
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
              {product.stock > 0 ? (
                <div style={{ fontSize: '14px', color: '#666', marginTop: '8px' }}>
                  재고: {product.stock}개
                </div>
              ) : (
                <div style={{ fontSize: '14px', color: '#f44336', marginTop: '8px' }}>
                  품절
                </div>
              )}
              
              <div className="product-divider"></div>

              {/* Product Options */}
              <div className="product-options">
                <div className="option-group">
                  <label className="option-label">수량</label>
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
                </div>
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
