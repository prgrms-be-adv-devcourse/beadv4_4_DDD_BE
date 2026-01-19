export default function Home() {
  return (
    <main className="order-page">
      {/* 주문서 섹션 */}
      <section className="order-section card">
        <h2 className="section-title">주문서</h2>
        <div className="order-info">
          <div className="name">김정인</div>
          <div className="delivery-tag">기본 배송지</div>
          <div className="address">서울 강남구 자곡동 123-456</div>
          <div className="phone">010-1234-5678</div>
        </div>
      </section>

      {/* 결제 금액 섹션 */}
      <section className="payment-section">
        <h2 className="section-title">결제 금액</h2>
        <div className="payment-details">
          <div className="payment-row">
            <span>상품 금액</span>
            <span>19,800원</span>
          </div>
          <div className="payment-row">
            <span>배송비</span>
            <span>무료배송</span>
          </div>
          <div className="payment-divider"></div>
          <div className="payment-row total">
            <span>총 결제 금액</span>
            <span>19,800원</span>
          </div>
        </div>
      </section>

      {/* 주문 상품 섹션 */}
      <section className="product-section">
        <h2 className="section-title">주문 상품 1개</h2>
        <div className="product-item">
          <div className="product-image">
            <svg className="bag-image" viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg">
              {/* 가방 본체 */}
              <rect x="20" y="30" width="60" height="50" rx="4" fill="#d4a574" stroke="#b8945f" strokeWidth="1.5"/>
              {/* 가방 손잡이 */}
              <path d="M 30 30 Q 30 20 40 20 L 60 20 Q 70 20 70 30" stroke="#b8945f" strokeWidth="2" fill="none" strokeLinecap="round"/>
              <path d="M 30 30 Q 30 20 40 20 L 60 20 Q 70 20 70 30" stroke="#b8945f" strokeWidth="2" fill="none" strokeLinecap="round" transform="translate(0, 50)"/>
              {/* 가방 지퍼/라인 */}
              <line x1="30" y1="45" x2="70" y2="45" stroke="#b8945f" strokeWidth="1" opacity="0.6"/>
              <line x1="30" y1="55" x2="70" y2="55" stroke="#b8945f" strokeWidth="1" opacity="0.6"/>
              {/* 가방 장식 라인 */}
              <rect x="25" y="35" width="50" height="40" rx="2" fill="none" stroke="#b8945f" strokeWidth="1" opacity="0.4"/>
            </svg>
          </div>
          <div className="product-info">
            <div className="product-brand">지오다노</div>
            <div className="product-name">베이직 레더 가방 130004</div>
            <div className="product-price">19,800원</div>
            <div className="product-delivery">01.14(수) 도착 예정</div>
          </div>
        </div>
      </section>

      {/* 결제 수단 섹션 */}
      <section className="payment-method-section">
        <h2 className="section-title">결제 수단</h2>
        <div className="payment-method">
          <input type="radio" id="toss" name="payment" defaultChecked />
          <label htmlFor="toss">
            <div className="toss-logo">토스</div>
            <span>토스페이</span>
          </label>
        </div>
      </section>

      {/* 약관 안내 (카드 밖) */}
      <div className="terms-outside">
        <div className="terms-item-outside">
          <span>주문 내용을 확인했으며 결제에 동의합니다.</span>
          <a href="#" className="detail-link">자세히</a>
        </div>
        <div className="terms-item-outside">
          <span>회원님의 개인정보는 안전하게 관리됩니다.</span>
          <a href="#" className="detail-link">자세히</a>
        </div>
        <div className="terms-item-outside">
          <span>뭐든사는 통신판매중개자로, 업체 배송 상품의 상품/상품정보/거래 등에 대한 책임은 뭐든사가 아닌 판매자에게 있습니다.</span>
        </div>
      </div>

      {/* 결제 버튼 */}
      <section className="terms-section">
        <div className="points-info">토스페이 결제 최대 1,600원 적립</div>
        <button className="payment-button">19,800원 결제하기</button>
      </section>
    </main>
  )
}
