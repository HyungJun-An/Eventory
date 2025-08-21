import React, { useState } from "react";
import axios from "axios";

/* 예약 상세 페이지(마이페이지 → 예약 상세)에서 “환불 요청” 버튼을 누르면 이 컴포넌트를 띄워주면 됩니다!!! */
// 환불 요청 페이지 컴포넌트
// 요구사항 기반: 환불 사유 입력 + 환불 정책 동의 체크박스【45†source】
export default function RefundPage({ reservationId, onSuccess }) {
  const [reason, setReason] = useState("");
  const [agree, setAgree] = useState(false);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");

  const handleRefund = async () => {
    if (!agree) {
      setMessage("환불 정책에 동의해야 합니다.");
      return;
    }
    if (!reason.trim()) {
      setMessage("환불 사유를 입력해 주세요.");
      return;
    }

    try {
      setLoading(true);
      setMessage("환불 요청 중...");

      await axios.post(`/api/payment/${reservationId}/refund`, {
          reason,
      }, {
        withCredentials: true,
      });

      setMessage("환불 요청이 완료되었습니다.");
      if (onSuccess) onSuccess();
    } catch (err) {
      console.error(err);
      setMessage("환불 요청 실패: " + (err.response?.data?.message || err.message));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-lg mx-auto bg-white p-6 rounded-2xl shadow-md space-y-4">
      <h1 className="text-2xl font-bold">예약 환불 요청</h1>

      <div>
        <label className="block text-sm font-medium mb-1">환불 사유</label>

      </div>
      <div>
          <textarea
                    className="w-full border rounded-lg p-2"
                    rows={3}
                    value={reason}
                    onChange={(e) => setReason(e.target.value)}
                    placeholder="환불 요청 사유를 입력하세요"
                  />
      </div>
      <div className="flex items-center space-x-2">
        <input
          type="checkbox"
          id="agree"
          checked={agree}
          onChange={(e) => setAgree(e.target.checked)}
        />
        <label htmlFor="agree" className="text-sm">
          환불 규정에 동의합니다 (7일 전 90%, 3일 전 50%, 2일 이내 불가)
        </label>
      </div>

      <button
        onClick={handleRefund}
        disabled={loading}
        className="w-full py-2 px-4 bg-red-500 text-white font-semibold rounded-lg shadow hover:bg-red-600 disabled:opacity-50"
      >
        {loading ? "처리 중..." : "환불 요청"}
      </button>

      {message && <p className="text-sm text-gray-700 mt-2">{message}</p>}
    </div>
  );
}
