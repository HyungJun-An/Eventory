// 모바일 결제 리디렉션 복귀 처리 (/payment/redirect?paymentId=...&code=...&message=...)
// - 주문 정보(박람회·인원·금액)는 서버가 결제 준비 때 저장해 두었으므로 paymentId 만으로 완료 처리한다
//   (기존: sessionStorage 에 값이 없어 userId=1, expoId=101 같은 임의값으로 완료를 시도)

import { useEffect, useRef, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { postComplete } from "../api/paymentApi";
import "../assets/css/payment/Checkout.css";

export function PaymentRedirect() {
  const navigate = useNavigate();
  const started = useRef(false); // StrictMode 이중 실행 방지 (서버도 멱등 처리하지만 불필요한 요청을 막는다)
  const [message, setMessage] = useState("결제 결과를 확인하는 중입니다…");
  const [failed, setFailed] = useState(false);

  useEffect(() => {
    if (started.current) return;
    started.current = true;

    const params = new URLSearchParams(window.location.search);
    const paymentId = params.get("paymentId");
    const code = params.get("code");

    if (code) {
      setFailed(true);
      setMessage(params.get("message") || "결제가 취소되었습니다.");
      return;
    }
    if (!paymentId) {
      setFailed(true);
      setMessage("결제 정보가 없습니다.");
      return;
    }

    postComplete(paymentId)
      .then((done) =>
        navigate(`/payment/reservation/${done.reservationId}`, {
          replace: true,
          state: { reservationId: done.reservationId, reservationCode: done.reservationCode, paymentStatus: done.status },
        })
      )
      .catch((e) => {
        setFailed(true);
        setMessage(e?.response?.data?.message || "결제 확인에 실패했습니다.");
      });
  }, [navigate]);

  return (
    <div className="co-page co-page--narrow">
      <div className="co-card co-empty">
        <p>{message}</p>
        {failed && <Link to="/">메인으로 돌아가기</Link>}
      </div>
    </div>
  );
}
