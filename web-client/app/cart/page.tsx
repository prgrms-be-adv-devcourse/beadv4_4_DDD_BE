'use client'

import Link from 'next/link'
import { useState, useEffect } from 'react'
import Header from '../components/Header'

interface CartItemDto {
  productId: number
  name: string
  quantity: number
  salePrice: number | string // BigDecimal can be string or number
  isAvailable: boolean
}

interface CartItemsResponseDto {
  memberId: number
  totalQuantity: number
  totalAmount: number | string // BigDecimal can be string or number
  cartItems: CartItemDto[]
}

// salePrice를 number로 변환하는 헬퍼 함수
const toNumber = (value: number | string): number => {
  return typeof value === 'number' ? value : parseFloat(value.toString() || '0')
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
    // Mock 장바구니 데이터
    const mockCartData: CartItemsResponseDto = {
      memberId: 1,
      totalQuantity: 2,
      totalAmount: 39600,
      cartItems: [
        {
          productId: 1,
          name: '베이직 레더 가방 130004',
          quantity: 2,
          salePrice: 19800,
          isAvailable: true,
        },
      ],
    }
    
    setTimeout(() => {
      setCartData(mockCartData)
      const availableItems = new Set(
        mockCartData.cartItems
          .filter((item: CartItemDto) => item.isAvailable)
          .map((item: CartItemDto) => item.productId)
      )
      setSelectedItems(availableItems)
      setIsLoading(false)
    }, 300)
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
      .reduce((sum, item) => sum + (toNumber(item.salePrice) * item.quantity), 0)
    
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
      .reduce((sum, item) => sum + (toNumber(item.salePrice) * item.quantity), 0)
    
    setCartData({
      ...cartData,
      cartItems: updatedItems,
      totalQuantity,
      totalAmount,
    })
  }

  const handleDeleteSelected = () => {
    if (selectedItems.size === 0) {
      alert('삭제할 상품을 선택해주세요.')
      return
    }

    if (!confirm(`선택한 ${selectedItems.size}개의 상품을 삭제하시겠습니까?`)) {
      return
    }

    // TODO: 선택된 아이템들 삭제 API 연동 필요
    if (!cartData) return
    
    const updatedItems = cartData.cartItems.filter(item => !selectedItems.has(item.productId))
    setSelectedItems(new Set())
    
    const totalQuantity = updatedItems
      .filter(item => item.isAvailable)
      .reduce((sum, item) => sum + item.quantity, 0)
    
    const totalAmount = updatedItems
      .filter(item => item.isAvailable)
      .reduce((sum, item) => sum + (toNumber(item.salePrice) * item.quantity), 0)
    
    setCartData({
      ...cartData,
      cartItems: updatedItems,
      totalQuantity,
      totalAmount,
    })
  }

  const selectedCartItems = items.filter(item => selectedItems.has(item.productId))
  const totalPrice = selectedCartItems.reduce((sum, item) => sum + (toNumber(item.salePrice) * item.quantity), 0)
  const deliveryFee = 0
  const finalTotal = totalPrice + deliveryFee

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('ko-KR').format(price)
  }

  return (
    <div className="home-page">
      {/* Header */}
      <Header />

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
                  {someSelected && (
                    <button 
                      className="delete-selected-btn"
                      onClick={handleDeleteSelected}
                    >
                      선택 삭제 ({selectedItems.size})
                    </button>
                  )}
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
                        <div className="cart-item-price">₩{formatPrice(toNumber(item.salePrice))}</div>
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
                          ₩{formatPrice(toNumber(item.salePrice) * item.quantity)}
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
