'use client'

import Link from 'next/link'
import { useParams, useRouter } from 'next/navigation'

export default function ProductDetailPage() {
  const params = useParams()
  const router = useRouter()
  const productId = params.id as string

  const handleOrder = () => {
    router.push('/order')
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
          </nav>
          <div className="header-actions">
            <Link href="/search" className="search-btn">검색</Link>
            <Link href="/cart" className="cart-btn">장바구니</Link>
            <button className="user-btn">로그인</button>
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
              <div className="product-brand-name">브랜드명</div>
              <h1 className="product-title">상품명 {productId}</h1>
              <div className="product-price-section">
                <span className="product-price">₩{((parseInt(productId) * 15000) + 10000).toLocaleString()}</span>
              </div>
              
              <div className="product-divider"></div>

              {/* Product Options */}
              <div className="product-options">
                <div className="option-group">
                  <label className="option-label">색상</label>
                  <div className="option-buttons">
                    {['블랙', '화이트', '베이지'].map((color) => (
                      <button key={color} className="option-button">{color}</button>
                    ))}
                  </div>
                </div>
                <div className="option-group">
                  <label className="option-label">사이즈</label>
                  <div className="option-buttons">
                    {['S', 'M', 'L', 'XL'].map((size) => (
                      <button key={size} className="option-button">{size}</button>
                    ))}
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
                <button className="cart-button">장바구니</button>
                <button className="buy-button" onClick={handleOrder}>구매하기</button>
              </div>
            </div>
          </div>

          {/* Product Description */}
          <div className="product-description-section">
            <h2 className="description-title">상품 상세 정보</h2>
            <div className="description-content">
              <p>상품 상세 설명이 들어갑니다.</p>
              <p>트렌디한 디자인과 고품질 소재로 제작된 상품입니다.</p>
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
