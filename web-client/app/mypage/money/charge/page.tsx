'use client'

import Link from 'next/link'
import MypageLayout from '../../../components/MypageLayout'

export default function MoneyChargePage() {
  const presetAmounts = [10000, 30000, 50000]

  return (
    <MypageLayout>
      <div style={{ maxWidth: '600px' }}>
        <h1 style={{ fontSize: '24px', fontWeight: 700, marginBottom: '24px' }}>뭐든사 머니 충전</h1>

          <div
            style={{
              background: 'white',
              borderRadius: '12px',
              padding: '24px',
              boxShadow: '0 2px 8px rgba(0, 0, 0, 0.1)',
              marginBottom: '24px',
            }}
          >
            <div
              style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                marginBottom: '20px',
              }}
            >
              <div>
                <div style={{ fontSize: '14px', color: '#666', marginBottom: '4px' }}>현재 보유 머니</div>
                <div style={{ fontSize: '22px', fontWeight: 700 }}>50,000원</div>
              </div>
              <div style={{ fontSize: '12px', color: '#999' }}>test@example.com</div>
            </div>

            <div style={{ marginBottom: '16px' }}>
              <div style={{ fontSize: '14px', fontWeight: 500, marginBottom: '8px' }}>충전 금액 선택</div>
              <div style={{ display: 'flex', gap: '8px', marginBottom: '12px' }}>
                {presetAmounts.map((amount) => (
                  <button
                    key={amount}
                    type="button"
                    onClick={() =>
                      alert(`${amount.toLocaleString()}원 충전은 데모 화면입니다.\n실제 결제는 연동되어 있지 않습니다.`)
                    }
                    style={{
                      flex: 1,
                      padding: '10px 0',
                      borderRadius: '8px',
                      border: '1px solid #e0e0e0',
                      background: '#f9f9f9',
                      cursor: 'pointer',
                      fontSize: '14px',
                      fontWeight: 500,
                    }}
                  >
                    {amount.toLocaleString()}원
                  </button>
                ))}
              </div>
              <div style={{ display: 'flex', gap: '8px' }}>
                <input
                  type="number"
                  placeholder="직접 입력 (원)"
                  style={{
                    flex: 1,
                    padding: '10px 12px',
                    borderRadius: '8px',
                    border: '1px solid #e0e0e0',
                    fontSize: '14px',
                  }}
                  onChange={() => {}}
                />
                <button
                  type="button"
                  onClick={() =>
                    alert('입력하신 금액으로의 충전은 데모 화면입니다.\n실제 결제는 연동되어 있지 않습니다.')
                  }
                  style={{
                    padding: '10px 20px',
                    borderRadius: '8px',
                    border: 'none',
                    background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                    color: 'white',
                    cursor: 'pointer',
                    fontSize: '14px',
                    fontWeight: 600,
                    whiteSpace: 'nowrap',
                  }}
                >
                  충전하기
                </button>
              </div>
            </div>
          </div>

      </div>
    </MypageLayout>
  )
}

