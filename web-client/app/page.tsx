'use client'

import Link from 'next/link'
import {useState, useEffect, useRef, useCallback} from 'react'
import Header from './components/Header'

interface ProductResponse {
  id: number
  sellerId: number
  sellerBusinessName: string
  name: string
  category: string
  description: string
  price: number
  salePrice: number
  currency: string
  productStatus: string
  saleStatus: string
  favoriteCount: number
  primaryImageUrl: string
  createdAt: string
  updatedAt: string
  createdBy: number
  updatedBy: number
}

const POPULAR_KEYWORDS = ['가방', '신발', '화장품', '향수', '시계']

export default function Home() {
  const [searchQuery, setSearchQuery] = useState('')
  const [searchResults, setSearchResults] = useState<ProductResponse[]>([])
  const [cursor, setCursor] = useState<string | null>(null)
  const [hasNext, setHasNext] = useState(false)
  const [loading, setLoading] = useState(false)

  const observerRef = useRef<HTMLDivElement | null>(null)

  const API_URL = process.env.NEXT_PUBLIC_PRODUCT_API_URL!
  const WINDOW_SIZE = 9

  const fetchSearchResults = async (
      keyword: string,
      cursorParam?: string | null,
      append: boolean = false
  ) => {
    if (loading) return
    try {
      setLoading(true)

      const params = new URLSearchParams({
        keyword,
        size: `${WINDOW_SIZE}`,
      })

      if (cursorParam) {
        params.append('cursor', cursorParam)
      }

      const response = await fetch(
          `${API_URL}/api/v1/products/search?${params.toString()}`,
          {
            method: 'GET',
          }
      )

      if (!response.ok) {
        throw new Error('Search API failed')
      }

      const data = await response.json()

      if (append) {
        setSearchResults(prev => [...prev, ...(data.result ?? [])])
      } else {
        setSearchResults(data.result ?? [])
      }
      setCursor(data.cursorInfo?.nextCursor ?? null)
      setHasNext(data.cursorInfo?.hasNext ?? false)
    } catch (error) {
      console.error(error)
    } finally {
      setLoading(false)
    }
  };

  // =========== 첫 진입 시 1회 호출 ===========
  useEffect(() => {
    fetchSearchResults(searchQuery)
  }, []) // 빈 배열 → mount 시 1회만 실행

  // =========== 다음 페이지 로딩 ===========
  const handleLoadMore = useCallback(() => {
    if (!hasNext || !cursor || loading) return
    fetchSearchResults(searchQuery, cursor, true)
  }, [cursor, hasNext, loading, searchQuery])

  useEffect(() => {
    if (!observerRef.current) return

    const observer = new IntersectionObserver(
        entries => {
          if (entries[0].isIntersecting) {
            handleLoadMore()
          }
        },
        { threshold: 1 }
    )

    observer.observe(observerRef.current)

    return () => observer.disconnect()
  }, [handleLoadMore])

  // =========== 검색 실행 ===========
  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault()

    if (!searchQuery.trim()) {
      setSearchResults([])
      return
    }
    // 새 검색이므로 초기화
    setCursor(null)
    setHasNext(false)
    await fetchSearchResults(searchQuery)
  }

  const handlePopularClick = async (keyword: string) => {
    setSearchQuery(keyword)
    await fetchSearchResults(keyword)
  }



  return (
    <div className="home-page">
      {/* Header */}
      <Header />

      {/* Main Banner */}
      <section className="main-banner">
        <div className="banner-content">
          <h1>새로운 시즌 컬렉션</h1>
          <p>트렌디한 패션과 프리미엄 뷰티를 만나보세요</p>

          {/* 검색창 */}
          <form onSubmit={handleSearch} className="banner-search-form">
            <div className="banner-search-wrap">
              <input
                type="search"
                placeholder="상품명, 브랜드명을 입력하세요"
                className="banner-search-input"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                autoComplete="off"
              />
              <button type="submit" className="banner-search-btn" aria-label="검색">
                <svg width="22" height="22" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M21 21L15 15M17 10C17 13.866 13.866 17 10 17C6.13401 17 3 13.866 3 10C3 6.13401 6.13401 3 10 3C13.866 3 17 6.13401 17 10Z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
                </svg>
              </button>
            </div>
          </form>

          {/* 인기검색어 */}
          <div className="banner-popular">
            <span className="banner-popular-label">인기 검색어</span>
            <div className="banner-popular-tags">
              {POPULAR_KEYWORDS.map((keyword) => (
                <button
                  key={keyword}
                  type="button"
                  className="banner-popular-tag"
                  onClick={() => handlePopularClick(keyword)}
                >
                  {keyword}
                </button>
              ))}
            </div>
          </div>
        </div>
      </section>

      {/* 검색 결과 (기존 검색화면처럼) */}
      {searchResults.length > 0 ? (
        <section className="search-results-section">
          <div className="container">
            <div className="search-results-header">
              <div className="results-info">
                <h2 className="results-title">
                  &apos;<span className="query-highlight">{searchQuery}</span>&apos; 검색 결과
                </h2>
                <p className="results-count-text">총 {searchResults.length}개의 상품을 찾았습니다</p>
              </div>
            </div>
            <div className="products-grid">
              {searchResults.map((item) => (
                <Link key={item.id} href={`/products/${item.id}`} className="product-card">
                  <div className="product-image">
                    <div className="image-placeholder">이미지</div>
                  </div>
                  <div className="product-info">
                    <div className="product-brand">{item.sellerBusinessName}</div>
                    <div className="product-name">{item.name}</div>
                    <div className="product-price">₩{item.salePrice.toLocaleString()}</div>
                  </div>
                </Link>
              ))}
            </div>
          </div>
        </section>
      ) : (
        <section className="search-results-section">
          <div className="container">
            <div className="no-results-container">
              <div className="no-results-icon">🔍</div>
              <h3 className="no-results-title">검색 결과가 없습니다</h3>
              <p className="no-results-text">&apos;{searchQuery}&apos;에 대한 검색 결과를 찾을 수 없습니다.</p>
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
      )}

      <div ref={observerRef} style={{ height: 1 }} />

      {loading && <p>Loading...</p>}

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
