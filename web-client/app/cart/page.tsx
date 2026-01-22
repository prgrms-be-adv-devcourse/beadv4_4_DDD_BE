'use client'

import Link from 'next/link'
import { useState, useEffect } from 'react'

interface CartItemDto {
  productId: number
  name: string
  quantity: number
  salePrice: number
  isAvailable: boolean
}

interface CartItemsResponseDto {
  memberId: number
  totalQuantity: number
  totalAmount: number
  cartItems: CartItemDto[]
}

interface ApiResponse {
  isSuccess: boolean
  code: string
  message: string
  result: CartItemsResponseDto
}

export default function CartPage() {
  const [cartData, setCartData] = useState<CartItemsResponseDto | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [selectedItems, setSelectedItems] = useState<Set<number>>(new Set())

  useEffect(() => {
    const fetchCartItems = async () => {
      try {
        const apiUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'
        const response = await fetch(`${apiUrl}/api/v1/orders/cart-items`)
        
        if (!response.ok) {
          const errorText = await response.text()
          console.error('API 응답 에러:', response.status, errorText)
          throw new Error(`장바구니 정보를 불러올 수 없습니다 (${response.status})`)
        }
        
        const apiResponse: ApiResponse = await response.json()
        
        if (apiResponse.isSuccess && apiResponse.result) {
          setCartData(apiResponse.result)
          // 사용 가능한 상품만 기본 선택
          const availableItems = new Set(
            apiResponse.result.cartItems
              .filter(item => item.isAvailable)
              .map(item => item.productId)
          )
          setSelectedItems(availableItems)
          setError(null)
        } else {
          throw new Error(apiResponse.message || '장바구니 정보를 가져올 수 없습니다.')
        }
      } catch (error) {
        console.error('장바구니 정보 조회 실패:', error)
        const errorMessage = error instanceof Error ? error.message : '알 수 없는 오류가 발생했습니다.'
        setError(errorMessage)
      } finally {
        setIsLoading(false)
      }
    }

    fetchCartItems()
  }, [])

  const items = cartData?.cartItems || []

  const allSelected = items.length > 0 && items.every(item => selectedItems.has(item.productId))
  const someSelected = items.some(item => selectedItems.has(item.productId))

  const handleSelectAll = (checked: boolean) => {
    if (checked) {
      const allItemIds = new Set(items.map(item => item.productId))
      setSelectedItems(allItemIds)
    } else {
      setSelectedItems(new Set())
    }
  }

  const handleSelectItem = (productId: number, checked: boolean) => {
    const newSelected = new Set(selectedItems)
    if (checked) {
      newSelected.add(productId)
    } else {
      newSelected.delete(productId)
    }
    setSelectedItems(newSelected)
  }

  const updateQuantity = (productId: number, delta: number) => {
    // TODO: 수량 변경 API 연동 필요
    if (!cartData) return
    
    const updatedItems = cartData.cartItems.map(item => 
      item.productId === productId 
        ? { ...item, quantity: Math.max(1, item.quantity + delta) }
        : item
    )
    
    // 총 수량과 총 금액 재계산
    const totalQuantity = updatedItems
      .filter(item => item.isAvailable)
      .reduce((sum, item) => sum + item.quantity, 0)
    
    const totalAmount = updatedItems
      .filter(item => item.isAvailable)
      .reduce((sum, item) => sum + (item.salePrice * item.quantity), 0)
    
    setCartData({
      ...cartData,
      cartItems: updatedItems,
      totalQuantity,
      totalAmount,
    })
  }

  const removeItem = (productId: number) => {
    // TODO: 장바구니 삭제 API 연동 필요
    if (!cartData) return
    
    const updatedItems = cartData.cartItems.filter(item => item.productId !== productId)
    const newSelected = new Set(selectedItems)
    newSelected.delete(productId)
    setSelectedItems(newSelected)
    
    const totalQuantity = updatedItems
      .filter(item => item.isAvailable)
      .reduce((sum, item) => sum + item.quantity, 0)
    
    const totalAmount = updatedItems
      .filter(item => item.isAvailable)
      .reduce((sum, item) => sum + (item.salePrice * item.quantity), 0)
    
    setCartData({
      ...cartData,
      cartItems: updatedItems,
      totalQuantity,
      totalAmount,
    })
  }

  const selectedCartItems = items.filter(item => selectedItems.has(item.productId))
  const totalPrice = selectedCartItems.reduce((sum, item) => sum + (item.salePrice * item.quantity), 0)
  const deliveryFee = 0
  const finalTotal = totalPrice + deliveryFee

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('ko-KR').format(price)
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

      {/* Cart Section */}
      <div className="cart-page-container">
        <div className="container">
          <h1 className="cart-page-title">장바구니</h1>

          {isLoading ? (
            <div style={{ textAlign: 'center', padding: '80px 20px' }}>
              <p>장바구니 정보를 불러오는 중...</p>
            </div>
          ) : error ? (
            <div style={{ textAlign: 'center', padding: '80px 20px' }}>
              <p style={{ color: '#f44336', marginBottom: '20px' }}>{error}</p>
              <Link href="/" style={{ color: '#667eea', textDecoration: 'underline' }}>
                홈으로 돌아가기
              </Link>
            </div>
          ) : items.length > 0 ? (
            <>
              {/* Cart Items */}
              <div className="cart-items-section">
                <div className="cart-items-header">
                  <div className="select-all">
                    <input 
                      type="checkbox" 
                      id="select-all" 
                      checked={allSelected}
                      onChange={(e) => handleSelectAll(e.target.checked)}
                    />
                    <label htmlFor="select-all">전체 선택</label>
                  </div>
                </div>

                <div className="cart-items-list">
                  {items.map((item) => (
                    <div key={item.productId} className="cart-item">
                      <div className="cart-item-checkbox">
                        <input 
                          type="checkbox" 
                          id={`item-${item.productId}`} 
                          checked={selectedItems.has(item.productId)}
                          onChange={(e) => handleSelectItem(item.productId, e.target.checked)}
                          disabled={!item.isAvailable}
                        />
                      </div>
                      <div className="cart-item-image">
                        <div className="image-placeholder-small">이미지</div>
                      </div>
                      <div className="cart-item-info">
                        <div className="cart-item-name">{item.name}</div>
                        <div className="cart-item-price">₩{formatPrice(item.salePrice)}</div>
                        {!item.isAvailable && (
                          <div style={{ fontSize: '13px', color: '#f44336', marginTop: '4px' }}>
                            구매 불가
                          </div>
                        )}
                      </div>
                      <div className="cart-item-actions">
                        <div className="quantity-control">
                          <button 
                            className="quantity-btn"
                            onClick={() => updateQuantity(item.productId, -1)}
                            disabled={item.quantity <= 1 || !item.isAvailable}
                          >
                            -
                          </button>
                          <span className="quantity-value">{item.quantity}</span>
                          <button 
                            className="quantity-btn"
                            onClick={() => updateQuantity(item.productId, 1)}
                            disabled={!item.isAvailable}
                          >
                            +
                          </button>
                        </div>
                        <div className="cart-item-total">
                          ₩{formatPrice(item.salePrice * item.quantity)}
                        </div>
                        <button 
                          className="remove-item-btn"
                          onClick={() => removeItem(item.productId)}
                        >
                          삭제
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              {/* Order Summary */}
              <div className="cart-summary-card">
                <div className="cart-promo-banner">
                  <div className="promo-content">
                    <h3 className="promo-title">특별 할인</h3>
                    <p className="promo-text">추가 상품 구매 시<br />최대 20% 할인</p>
                    <div className="promo-icon">✨</div>
                  </div>
                </div>
                <div className="cart-summary-section">
                  <div className="summary-card">
                    <h2 className="summary-title">주문 요약</h2>
                    <div className="summary-details">
                      <div className="summary-row">
                        <span>상품 금액</span>
                        <span>₩{formatPrice(totalPrice)}</span>
                      </div>
                      <div className="summary-row">
                        <span>배송비</span>
                        <span style={{ color: '#667eea' }}>무료배송</span>
                      </div>
                      <div className="summary-divider"></div>
                      <div className="summary-row total">
                        <span>총 결제 금액</span>
                        <span>₩{formatPrice(finalTotal)}</span>
                      </div>
                    </div>
                    <Link href="/order" className="order-button">
                      주문하기
                    </Link>
                  </div>
                </div>
              </div>
            </>
          ) : (
            <div className="empty-cart">
              <div className="empty-cart-icon">🛒</div>
              <h2 className="empty-cart-title">장바구니가 비어있습니다</h2>
              <p className="empty-cart-text">원하는 상품을 담아보세요</p>
              <Link href="/" className="shopping-button">
                쇼핑하러 가기
              </Link>
            </div>
          )}
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
