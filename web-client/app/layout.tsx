import type { Metadata } from 'next'
import './globals.css'

export const metadata: Metadata = {
  title: '주문 페이지',
  description: '상품 주문 및 결제 페이지',
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="ko">
      <body suppressHydrationWarning>{children}</body>
    </html>
  )
}
