'use client'

import Link from 'next/link'
import { useParams } from 'next/navigation'
import MypageLayout from '../../../components/MypageLayout'

const MOCK_ORDERS: Record<
  string,
  {
    id: string
    date: string
    dateDisplay: string
    status: string
    statusStyle: React.CSSProperties
    productSummary: string
    quantity: number
    amount: string
    products: { name: string; quantity: number; price: string }[]
    receiverName: string
    receiverPhone: string
    address: string
  }
> = {
  'ORD-2024-001': {
    id: 'ORD-2024-001',
    date: '2024-01-15',
    dateDisplay: '2024.01.15 14:30',
    status: '배송완료',
    statusStyle: { color: '#22c55e', fontWeight: 600 },
    productSummary: '데일리 티셔츠 외 1건',
    quantity: 2,
    amount: '89,000원',
    products: [
      { name: '데일리 베이직 티셔츠', quantity: 1, price: '29,000원' },
      { name: '루즈핏 슬랙스', quantity: 1, price: '60,000원' },
    ],
    receiverName: '홍길동',
    receiverPhone: '010-1234-5678',
    address: '서울시 강남구 테헤란로 123, 101동 1001호',
  },
  'ORD-2024-002': {
    id: 'ORD-2024-002',
    date: '2024-01-10',
    dateDisplay: '2024.01.10 11:20',
    status: '배송중',
    statusStyle: { color: '#667eea', fontWeight: 600 },
    productSummary: '루즈핏 코트',
    quantity: 1,
    amount: '45,000원',
    products: [{ name: '루즈핏 오버코트', quantity: 1, price: '45,000원' }],
    receiverName: '김철수',
    receiverPhone: '010-9876-5432',
    address: '경기도 성남시 분당구 판교역로 456',
  },
  'ORD-2024-003': {
    id: 'ORD-2024-003',
    date: '2024-01-05',
    dateDisplay: '2024.01.05 09:15',
    status: '주문접수',
    statusStyle: { color: '#666', fontWeight: 600 },
    productSummary: '니트 세트 외 2건',
    quantity: 3,
    amount: '132,000원',
    products: [
      { name: '니트 풀오버 세트', quantity: 1, price: '44,000원' },
      { name: '데일리 베이직 티셔츠', quantity: 2, price: '58,000원' },
    ],
    receiverName: '이영희',
    receiverPhone: '010-5555-6666',
    address: '인천시 연수구 송도대로 789',
  },
}

function DetailRow({ label, value }: { label: string; value: React.ReactNode }) {
  return (
    <div style={{ display: 'flex', padding: '12px 0', borderBottom: '1px solid #f0f0f0', gap: '16px' }}>
      <span style={{ minWidth: '120px', color: '#666', fontSize: '14px' }}>{label}</span>
      <span style={{ color: '#333', fontSize: '14px' }}>{value}</span>
    </div>
  )
}

function Card({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <div
      style={{
        background: 'white',
        borderRadius: '12px',
        padding: '20px',
        boxShadow: '0 2px 8px rgba(0, 0, 0, 0.08)',
        border: '1px solid #f0f0f0',
        marginBottom: '24px',
      }}
    >
      <h2 style={{ fontSize: '16px', fontWeight: 600, marginBottom: '16px', color: '#333' }}>{title}</h2>
      {children}
    </div>
  )
}

export default function OrderDetailPage() {
  const params = useParams()
  const id = typeof params?.id === 'string' ? params.id : ''
  const order = id ? MOCK_ORDERS[id] : null

  if (!order) {
    return (
      <MypageLayout>
        <div style={{ maxWidth: '600px' }}>
          <p style={{ color: '#999', marginBottom: '16px' }}>주문을 찾을 수 없습니다.</p>
          <Link
            href="/mypage/orders"
            style={{
              display: 'inline-block',
              padding: '10px 20px',
              borderRadius: '8px',
              background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
              color: 'white',
              fontSize: '14px',
              fontWeight: 500,
              textDecoration: 'none',
            }}
          >
            주문내역으로
          </Link>
        </div>
      </MypageLayout>
    )
  }

  return (
    <MypageLayout>
      <div style={{ maxWidth: '700px' }}>
        <div style={{ marginBottom: '24px' }}>
          <Link
            href="/mypage/orders"
            style={{ fontSize: '14px', color: '#667eea', textDecoration: 'none', marginBottom: '8px', display: 'inline-block' }}
          >
            ← 주문내역
          </Link>
          <h1 style={{ fontSize: '24px', fontWeight: 700, marginBottom: '8px' }}>주문 상세</h1>
          <p style={{ color: '#666', fontSize: '14px' }}>주문번호: {order.id}</p>
        </div>

        <Card title="주문 정보">
          <DetailRow label="주문일시" value={order.dateDisplay} />
          <DetailRow label="주문번호" value={order.id} />
          <DetailRow label="배송상태" value={<span style={order.statusStyle}>{order.status}</span>} />
          <DetailRow label="결제금액" value={<strong>{order.amount}</strong>} />
        </Card>

        <Card title="주문 상품">
          {order.products.map((p, i) => (
            <div
              key={i}
              style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                padding: '12px 0',
                borderBottom: '1px solid #f0f0f0',
              }}
            >
              <span style={{ color: '#333' }}>
                {p.name} <span style={{ color: '#999', fontSize: '13px' }}>x{p.quantity}</span>
              </span>
              <span style={{ fontWeight: 500 }}>{p.price}</span>
            </div>
          ))}
          <div style={{ display: 'flex', justifyContent: 'space-between', padding: '12px 0', fontWeight: 600 }}>
            <span>총 결제금액</span>
            <span>{order.amount}</span>
          </div>
        </Card>

        <Card title="배송 정보">
          <DetailRow label="수령인" value={order.receiverName} />
          <DetailRow label="연락처" value={order.receiverPhone} />
          <DetailRow label="배송주소" value={order.address} />
        </Card>

        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            gap: '12px',
            marginTop: '8px',
            flexWrap: 'wrap',
          }}
        >
          <div style={{ display: 'flex', gap: '12px', flexWrap: 'wrap' }}>
            <button
              type="button"
              onClick={() => {
                if (confirm(`주문 ${order.id}을(를) 취소 요청하시겠습니까?`)) {
                  alert('취소 요청이 접수되었습니다.\n(데모 화면입니다.)')
                }
              }}
              style={{
                padding: '10px 20px',
                borderRadius: '8px',
                border: '1px solid #e0e0e0',
                background: 'white',
                color: '#333',
                fontSize: '14px',
                fontWeight: 500,
                cursor: 'pointer',
              }}
            >
              취소요청
            </button>
            <button
              type="button"
              onClick={() => {
                if (confirm(`주문 ${order.id} 결제금액에 대한 환불을 요청하시겠습니까?`)) {
                  alert('환불 요청이 접수되었습니다.\n(데모 화면입니다.)')
                }
              }}
              style={{
                padding: '10px 20px',
                borderRadius: '8px',
                border: '1px solid #e0e0e0',
                background: 'white',
                color: '#333',
                fontSize: '14px',
                fontWeight: 500,
                cursor: 'pointer',
              }}
            >
              환불요청
            </button>
          </div>
          <Link
            href="/mypage/orders"
            style={{
              display: 'inline-block',
              padding: '10px 20px',
              borderRadius: '8px',
              background: '#666',
              color: 'white',
              fontSize: '14px',
              fontWeight: 500,
              textDecoration: 'none',
            }}
          >
            목록으로
          </Link>
        </div>
      </div>
    </MypageLayout>
  )
}
