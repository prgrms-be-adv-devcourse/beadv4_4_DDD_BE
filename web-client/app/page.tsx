'use client'

import Link from 'next/link'
import Header from './components/Header'

export default function Home() {
  return (
    <div className="home-page">
      {/* Header */}
      <Header />

      {/* Main Banner */}
      <section className="main-banner">
        <div className="banner-content">
          <h1>새로운 시즌 컬렉션</h1>
          <p>트렌디한 패션과 프리미엄 뷰티를 만나보세요</p>
          <button className="banner-cta">지금 쇼핑하기</button>
        </div>
      </section>

      {/* Category Section */}
      <section className="category-section">
        <div className="container">
          <h2 className="section-title">카테고리</h2>
          <div className="category-grid">
            <div className="category-item">
              <div className="category-icon">👗</div>
              <span>여성패션</span>
            </div>
            <div className="category-item">
              <div className="category-icon">👔</div>
              <span>남성패션</span>
            </div>
            <div className="category-item">
              <div className="category-icon">💄</div>
              <span>화장품</span>
            </div>
            <div className="category-item">
              <div className="category-icon">🧴</div>
              <span>스킨케어</span>
            </div>
            <div className="category-item">
              <div className="category-icon">👠</div>
              <span>신발/가방</span>
            </div>
            <div className="category-item">
              <div className="category-icon">⌚</div>
              <span>액세서리</span>
            </div>
          </div>
        </div>
      </section>

      {/* Products */}
      <section className="products-section">
        <div className="container">
          <div className="products-grid">
            {[1, 2, 3, 4, 5, 6, 7, 8, 9].map((item) => (
              <Link key={item} href={`/products/${item}`} className="product-card">
                <div className="product-image">
                  <div className="image-placeholder">이미지</div>
                </div>
                <div className="product-info">
                  <div className="product-brand">브랜드명</div>
                  <div className="product-name">상품명 {item}</div>
                  <div className="product-price">₩{((item * 10000) + 9000).toLocaleString()}</div>
                </div>
              </Link>
            ))}
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
