'use client'

import Link from 'next/link'
import { useSearchParams } from 'next/navigation'
import { Suspense, useEffect, useState } from 'react'
import Header from '../components/Header'

function SearchContent() {
  const searchParams = useSearchParams()
  const qFromUrl = searchParams.get('q') ?? ''
  const [searchQuery, setSearchQuery] = useState(qFromUrl)
  const [searchResults, setSearchResults] = useState<number[]>([])

  useEffect(() => {
    setSearchQuery(qFromUrl)
    if (qFromUrl.trim()) {
      setSearchResults([1, 2, 3, 4, 5, 6, 7, 8])
    } else {
      setSearchResults([])
    }
  }, [qFromUrl])

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault()
    if (searchQuery.trim()) {
      const results = [1, 2, 3, 4, 5, 6, 7, 8]
      setSearchResults(results)
    } else {
      setSearchResults([])
    }
  }

  return (
      <div className="home-page">
        <Header />

        <section className="search-hero">
          <div className="container">
            <div className="search-hero-content">
              <div className="search-icon-large">🔍</div>
              <h1 className="search-hero-title">무엇을 찾고 계신가요?</h1>
              <form onSubmit={handleSearch} className="search-form-large">
                <div className="search-input-container">
                  <input
                      type="text"
                      className="search-input-large"
                      placeholder="상품명, 브랜드명을 입력하세요"
                      value={searchQuery}
                      onChange={(e) => setSearchQuery(e.target.value)}
                  />
                  <button type="submit" className="search-icon-button">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                      <path d="M21 21L15 15M17 10C17 13.866 13.866 17 10 17C6.13401 17 3 13.866 3 10C3 6.13401 6.13401 3 10 3C13.866 3 17 6.13401 17 10Z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                    </svg>
                  </button>
                </div>
              </form>
              <div className="search-suggestions">
                <span className="suggestions-label">인기 검색어:</span>
                <div className="suggestion-tags">
                  {['가방', '신발', '화장품', '향수', '시계'].map((tag) => (
                      <button
                          key={tag}
                          className="suggestion-tag"
                          onClick={() => {
                            setSearchQuery(tag)
                            handleSearch({ preventDefault: () => {} } as React.FormEvent)
                          }}
                      >
                        {tag}
                      </button>
                  ))}
                </div>
              </div>
            </div>
          </div>
        </section>

        {searchResults.length > 0 ? (
            <section className="search-results-section">
              <div className="container">
                <div className="search-results-header">
                  <div className="results-info">
                    <h2 className="results-title">
                      '<span className="query-highlight">{searchQuery}</span>' 검색 결과
                    </h2>
                    <p className="results-count-text">총 {searchResults.length}개의 상품을 찾았습니다</p>
                  </div>
                </div>
                <div className="products-grid">
                  {searchResults.map((item) => (
                      <Link key={item} href={`/products/${item + 40}`} className="product-card">
                        <div className="product-image">
                          <div className="image-placeholder">이미지</div>
                        </div>
                        <div className="product-info">
                          <div className="product-brand">브랜드명</div>
                          <div className="product-name">검색 상품 {item}</div>
                          <div className="product-price">₩{((item * 15000) + 10000).toLocaleString()}</div>
                        </div>
                      </Link>
                  ))}
                </div>
              </div>
            </section>
        ) : searchQuery ? (
            <section className="search-results-section">
              <div className="container">
                <div className="no-results-container">
                  <div className="no-results-icon">🔍</div>
                  <h3 className="no-results-title">검색 결과가 없습니다</h3>
                  <p className="no-results-text">'{searchQuery}'에 대한 검색 결과를 찾을 수 없습니다.</p>
                  <div className="no-results-suggestions">
                    <p className="suggestions-text">다음과 같이 검색해보세요:</p>
                    <ul className="suggestions-list">
                      <li>오타가 없는지 확인해주세요</li>
                      <li>다른 검색어를 사용해보세요</li>
                      <li>더 일반적인 키워드로 검색해보세요</li>
                    </ul>
                  </div>
                </div>
              </div>
            </section>
        ) : null}

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

export default function SearchPage() {
  return (
      <Suspense fallback={<div>로딩 중...</div>}>
        <SearchContent />
      </Suspense>
  )
}