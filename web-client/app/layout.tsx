import type { Metadata } from 'next'
import './globals.css'

export const metadata: Metadata = {
  title: 'modeunsa',
  description: 'modeunsa',
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
