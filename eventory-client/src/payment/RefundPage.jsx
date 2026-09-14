import { useState } from "react";
import { requestRefund } from "../api/paymentApi";

/** 환불 요청 폼 — 공통 axios 로 로그인 토큰을 붙여 본인 예약만 환불 (기존: 토큰 없이 호출) */
export default function RefundPage({ reservationId, onSuccess }) {
  const [reason, setReason] = useState("");
  const [agree, setAgree] = useState(false);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");

  const handleRefund = async () => {
    if (!reason.trim()) {
      setMessage("환불 사유를 입력해 주세요.");
      return;
    }
    if (!agree) {
      setMessage("환불 안내에 동의해야 합니다.");
      return;
    }
    setLoading(true);
    setMessage("");
    try {
      await requestRefund(reservationId, reason.trim());
      onSuccess?.();
    } catch (err) {
      setMessage(err.response?.data?.message || "환불 요청에 실패했습니다.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="co-refund">
      <h3>환불 요청</h3>
      <label className="co-field">
        <span>환불 사유</span>
        <textarea rows={3} maxLength={200} value={reason} onChange={(e) => setReason(e.target.value)} placeholder="예: 일정이 변경되어 참석이 어렵습니다" />
      </label>
      <label className="co-check">
        <input type="checkbox" checked={agree} onChange={(e) => setAgree(e.target.checked)} />
        입장 전 예약은 결제 금액이 전액 환불되며, 환불 후에는 되돌릴 수 없습니다.
      </label>
      <button type="button" className="co-btn co-btn--danger" onClick={handleRefund} disabled={loading}>
        {loading ? "처리 중…" : "전액 환불하기"}
      </button>
      {message && (
        <p className="co-error" role="alert">
          {message}
        </p>
      )}
    </div>
  );
}
