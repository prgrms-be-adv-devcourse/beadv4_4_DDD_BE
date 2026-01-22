'use client'

import Link from 'next/link'
import { useState } from 'react'

export default function MagazinePage() {
  const [searchQuery, setSearchQuery] = useState('')
  const [selectedCategory, setSelectedCategory] = useState('전체')
  const [sortBy, setSortBy] = useState<'popular' | 'recent'>('popular')
  
  const allPosts = Array.from({ length: 18 }, (_, i) => ({
    id: i + 1,
    image: '이미지',
    likes: Math.floor(Math.random() * 1000) + 100,
    comments: Math.floor(Math.random() * 50) + 5,
    category: i % 3 === 0 ? '패션' : i % 3 === 1 ? '뷰티' : '라이프',
    title: ['봄 코디', '데일리 룩', '스킨케어', '메이크업', '홈 데코', '트렌드'][i % 6],
    date: new Date(2024, 0, 15 - i),
  }))

  // 필터링
  let filteredPosts = allPosts
  
  if (selectedCategory !== '전체') {
    filteredPosts = filteredPosts.filter(post => post.category === selectedCategory)
  }
  
  if (searchQuery) {
    filteredPosts = filteredPosts.filter(post => 
      post.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
      post.category.toLowerCase().includes(searchQuery.toLowerCase())
    )
  }

  // 정렬
  const sortedPosts = [...filteredPosts].sort((a, b) => {
    if (sortBy === 'popular') {
      return b.likes - a.likes
    } else {
      return b.date.getTime() - a.date.getTime()
    }
  })

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

      {/* Page Header */}
      <div className="page-header">
        <div className="container">
          <h1 className="page-title">매거진</h1>
          <p className="page-subtitle">패션과 뷰티의 최신 트렌드를 만나보세요</p>
        </div>
      </div>

      {/* Magazine Search & Filter */}
      <div className="magazine-search-container">
        <div className="container">
          <div className="magazine-controls">
            {/* Search Bar */}
            <div className="magazine-search-bar">
              <svg className="search-icon-svg" width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M21 21L15 15M17 10C17 13.866 13.866 17 10 17C6.13401 17 3 13.866 3 10C3 6.13401 6.13401 3 10 3C13.866 3 17 6.13401 17 10Z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
              </svg>
              <input
                type="text"
                className="magazine-search-input"
                placeholder="검색어를 입력하세요"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
              {searchQuery && (
                <button 
                  className="search-clear-btn"
                  onClick={() => setSearchQuery('')}
                >
                  ✕
                </button>
              )}
            </div>

            {/* Category & Sort */}
            <div className="magazine-filters-row">
              <div className="category-filters">
                {['전체', '패션', '뷰티', '라이프'].map((category) => (
                  <button
                    key={category}
                    className={`category-filter-btn ${selectedCategory === category ? 'active' : ''}`}
                    onClick={() => setSelectedCategory(category)}
                  >
                    {category}
                  </button>
                ))}
              </div>
              
              <div className="sort-controls">
                <button
                  className={`sort-btn ${sortBy === 'popular' ? 'active' : ''}`}
                  onClick={() => setSortBy('popular')}
                >
                  인기순
                </button>
                <button
                  className={`sort-btn ${sortBy === 'recent' ? 'active' : ''}`}
                  onClick={() => setSortBy('recent')}
                >
                  최신순
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Magazine Grid */}
      <section className="magazine-section">
        <div className="container">
          {sortedPosts.length > 0 ? (
            <div className="magazine-grid">
              {sortedPosts.map((post) => (
                <Link key={post.id} href={`/magazine/${post.id}`} className="magazine-post">
                  <div className="post-image">
                    <div className="image-placeholder">이미지</div>
                    <div className="post-overlay">
                      <div className="post-stats">
                        <span className="stat-item">❤️ {post.likes}</span>
                        <span className="stat-item">💬 {post.comments}</span>
                      </div>
                    </div>
                  </div>
                </Link>
              ))}
            </div>
          ) : (
            <div className="magazine-no-results">
              <p>검색 결과가 없습니다</p>
            </div>
          )}
        </div>
      </section>

      {/* Write Button */}
      <Link href="/magazine/write" className="magazine-write-button">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M12 5V19M5 12H19" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
        </svg>
        <span>글쓰기</span>
      </Link>

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
