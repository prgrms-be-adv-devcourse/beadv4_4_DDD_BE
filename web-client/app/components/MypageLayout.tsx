'use client'

import Header from './Header'
import MypageNav from './MypageNav'

export default function MypageLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="home-page">
      <Header />
      <div style={{ padding: '40px 20px', minHeight: '60vh' }}>
        <div className="container" style={{ maxWidth: '1000px', margin: '0 auto' }}>
          <div style={{ display: 'flex', gap: '24px', alignItems: 'flex-start' }}>
            <MypageNav />
            <div style={{ flex: 1, minWidth: 0 }}>{children}</div>
          </div>
        </div>
      </div>
    </div>
  )
}
