'use client'

import Link from 'next/link'
import { useSearchParams } from 'next/navigation'
import Header from '../components/Header'

const BEAUTY_CATEGORIES = [
  { label: '전체', href: '/beauty', value: null },
  { label: '스킨케어', href: '/beauty?category=skincare', value: 'skincare' },
  { label: '메이크업', href: '/beauty?category=makeup', value: 'makeup' },
  { label: '헤어', href: '/beauty?category=hair', value: 'hair' },
  { label: '바디', href: '/beauty?category=body', value: 'body' },
  { label: '네일', href: '/beauty?category=nail', value: 'nail' },
  { label: '향수', href: '/beauty?category=fragrance', value: 'fragrance' },
] as const

export default function BeautyPage() {
  const searchParams = useSearchParams()
  const currentCategory = searchParams.get('category')

  return (
    <div className="home-page">
      <Header />

      {/* Page Header */}
      <div className="page-header">
        <div className="container">
          <h1 className="page-title">뷰티</h1>
          <p className="page-subtitle">프리미엄 뷰티 제품을 만나보세요</p>
        </div>
      </div>

      {/* Category Section */}
      <section className="category-section beauty-category-section">
        <div className="container">
          <div className="category-filters">
            {BEAUTY_CATEGORIES.map(({ label, href, value }) => {
              const isActive = value === null ? !currentCategory : currentCategory === value
              return (
                <Link
                  key={value ?? 'all'}
                  href={href}
                  className={`category-filter-btn${isActive ? ' active' : ''}`}
                >
                  {label}
                </Link>
              )
            })}
          </div>
        </div>
      </section>

      {/* Products Section */}
      <section className="products-section">
        <div className="container">
          <div className="products-header">
            <h2 className="section-title">전체 상품</h2>
            <div className="filter-options">
              <select className="filter-select">
                <option>정렬순</option>
                <option>인기순</option>
                <option>최신순</option>
                <option>가격 낮은순</option>
                <option>가격 높은순</option>
              </select>
            </div>
          </div>
          <div className="products-grid">
            {[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12].map((item) => (
              <Link key={item} href={`/products/${item + 20}`} className="product-card">
                <div className="product-image">
                  <div className="image-placeholder">이미지</div>
                </div>
                <div className="product-info">
                  <div className="product-brand">브랜드명</div>
                  <div className="product-name">뷰티 상품 {item}</div>
                  <div className="product-price">₩{((item * 20000) + 15000).toLocaleString()}</div>
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
