import { useState } from "react";
import { Link, useLocation } from "react-router-dom";
import { CheckCircle2, RotateCcw } from "lucide-react";
import RefundPage from "./RefundPage";
import "../assets/css/payment/Checkout.css";

const krw = (v) => `${Number(v ?? 0).toLocaleString("ko-KR")}원`;

/** 예매 완료 화면 (결제 직후 이동) — 예약 정보 확인 + 입장 전 환불 */
export default function ReservationDetail() {
  const { state } = useLocation();
  const [showRefund, setShowRefund] = useState(false);
  const [refunded, setRefunded] = useState(false);

  if (!state) {
    return (
      <div className="co-page co-page--narrow">
        <div className="co-card co-empty">
          예약 정보가 없습니다. <Link to="/">메인으로</Link>
        </div>
      </div>
    );
  }

  return (
    <div className="co-page co-page--narrow">
      <section className="co-card co-done">
        {refunded ? <RotateCcw size={44} className="co-done__icon co-done__icon--muted" /> : <CheckCircle2 size={44} className="co-done__icon" />}
        <h1>{refunded ? "환불이 완료되었습니다" : "예매가 완료되었습니다"}</h1>
        <p className="co-hint">{refunded ? "결제 금액이 전액 환불됩니다." : "입장 QR은 가입한 이메일로 발송됩니다. 현장에서 QR을 보여주세요."}</p>

        <dl className="co-dl co-dl--boxed">
          <dt>예약번호</dt>
          <dd className="co-mono">{state.reservationCode}</dd>
          {state.expoTitle && (
            <>
              <dt>박람회</dt>
              <dd>{state.expoTitle}</dd>
            </>
          )}
          {state.people && (
            <>
              <dt>인원</dt>
              <dd>{state.people}명</dd>
            </>
          )}
          {state.amount != null && (
            <>
              <dt>결제 금액</dt>
              <dd>{krw(state.amount)}</dd>
            </>
          )}
          <dt>상태</dt>
          <dd>{refunded ? "환불 완료" : "결제 완료"}</dd>
        </dl>

        <div className="co-actions">
          <Link to="/" className="co-btn">
            메인으로
          </Link>
          {!refunded && !showRefund && (
            <button type="button" className="co-btn co-btn--danger-outline" onClick={() => setShowRefund(true)}>
              환불 요청
            </button>
          )}
        </div>

        {showRefund && !refunded && (
          <RefundPage
            reservationId={state.reservationId}
            onSuccess={() => {
              setRefunded(true);
              setShowRefund(false);
            }}
          />
        )}
      </section>
    </div>
  );
}
