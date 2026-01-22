'use client'

import Link from 'next/link'
import { useState } from 'react'

export default function CartPage() {
  const [items, setItems] = useState([
    { id: 1, name: '베이직 레더 가방 130004', brand: '지오다노', price: 19800, quantity: 1, image: '이미지' },
    { id: 2, name: '패션 상품 2', brand: '브랜드명', price: 35000, quantity: 2, image: '이미지' },
    { id: 3, name: '뷰티 상품 1', brand: '브랜드명', price: 45000, quantity: 1, image: '이미지' },
  ])

  const updateQuantity = (id: number, delta: number) => {
    setItems(items.map(item => 
      item.id === id 
        ? { ...item, quantity: Math.max(1, item.quantity + delta) }
        : item
    ))
  }

  const removeItem = (id: number) => {
    setItems(items.filter(item => item.id !== id))
  }

  const totalPrice = items.reduce((sum, item) => sum + (item.price * item.quantity), 0)
  const deliveryFee = totalPrice >= 50000 ? 0 : 3000
  const finalTotal = totalPrice + deliveryFee

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
          </nav>
          <div className="header-actions">
            <Link href="/search" className="search-btn">검색</Link>
            <Link href="/cart" className="cart-btn">장바구니</Link>
            <button className="user-btn">로그인</button>
          </div>
        </div>
      </header>

      {/* Cart Section */}
      <div className="cart-page-container">
        <div className="container">
          <h1 className="cart-page-title">장바구니</h1>

          {items.length > 0 ? (
            <>
              {/* Cart Items */}
              <div className="cart-items-section">
                <div className="cart-items-header">
                  <div className="select-all">
                    <input type="checkbox" id="select-all" defaultChecked />
                    <label htmlFor="select-all">전체 선택</label>
                  </div>
                  <button className="delete-selected">선택 삭제</button>
                </div>

                <div className="cart-items-list">
                  {items.map((item) => (
                    <div key={item.id} className="cart-item">
                      <div className="cart-item-checkbox">
                        <input type="checkbox" id={`item-${item.id}`} defaultChecked />
                      </div>
                      <div className="cart-item-image">
                        <div className="image-placeholder-small">{item.image}</div>
                      </div>
                      <div className="cart-item-info">
                        <div className="cart-item-brand">{item.brand}</div>
                        <div className="cart-item-name">{item.name}</div>
                        <div className="cart-item-price">₩{item.price.toLocaleString()}</div>
                      </div>
                      <div className="cart-item-actions">
                        <div className="quantity-control">
                          <button 
                            className="quantity-btn"
                            onClick={() => updateQuantity(item.id, -1)}
                          >
                            -
                          </button>
                          <span className="quantity-value">{item.quantity}</span>
                          <button 
                            className="quantity-btn"
                            onClick={() => updateQuantity(item.id, 1)}
                          >
                            +
                          </button>
                        </div>
                        <div className="cart-item-total">
                          ₩{(item.price * item.quantity).toLocaleString()}
                        </div>
                        <button 
                          className="remove-item-btn"
                          onClick={() => removeItem(item.id)}
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
                        <span>₩{totalPrice.toLocaleString()}</span>
                      </div>
                      <div className="summary-row">
                        <span>배송비</span>
                        <span>
                          {deliveryFee === 0 ? (
                            <span style={{ color: '#667eea' }}>무료배송</span>
                          ) : (
                            `₩${deliveryFee.toLocaleString()}`
                          )}
                        </span>
                      </div>
                      {totalPrice < 50000 && (
                        <div className="summary-notice">
                          ₩{(50000 - totalPrice).toLocaleString()}원 더 구매하면 무료배송!
                        </div>
                      )}
                      <div className="summary-divider"></div>
                      <div className="summary-row total">
                        <span>총 결제 금액</span>
                        <span>₩{finalTotal.toLocaleString()}</span>
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
