import React, { useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import RefundPage from "./RefundPage";

export default function ReservationDetail() {
    const { state } = useLocation();
    const navigate = useNavigate();
    const [showRefund, setShowRefund] = useState(false);

    if (!state) return <p>예약 데이터가 없습니다.</p>;

    return (
        <div className="p-6 space-y-4">
            <h1 className="text-2xl font-bold">예약 상세</h1>
            <p><strong>예약번호:</strong> {state.reservationId}</p>
            <p><strong>예약코드:</strong> {state.reservationCode}</p>
            <p><strong>결제 상태:</strong> {state.paymentStatus}</p>

            {!showRefund ? (
                <button
                    onClick={() => setShowRefund(true)}
                    className="px-4 py-2 bg-red-500 text-white rounded-lg"
                >
                    환불 요청
                </button>
            ) : (
                <RefundPage
                    reservationId={state.reservationId}
                    portonePaymentId={state.portonePaymentId}
                    onSuccess={() => {
                        alert("환불 완료!");
                        navigate("/");
                    }}
                />
            )}
        </div>
    );
}
