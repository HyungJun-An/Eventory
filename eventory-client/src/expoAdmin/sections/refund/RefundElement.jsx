import React, { useEffect, useState } from "react";
import api from "../../../api/axiosInstance";
import "../../../assets/css/refund/RefundElement.css";

const RefundElement = ({ expoId }) => {

  const [refunds, setRefunds] = useState([]);

  useEffect(() => {
    if (!expoId) return;

    const fetchRefunds = async () => {
      try {
        const response = await api.get(`/api/admin/expos/${expoId}/refund`, {
          params: {
            page: 0,
            size: 7,
          },
        });
        setRefunds(response.data);
      } catch (error) {
        console.error("환불 조회 실패:", error);
      }
    };

    fetchRefunds();
  }, [expoId]);

  return (
    <div className="refund-element">
      <div className="navbar">
        <div className="text-wrapper-10">예약 번호</div>

        <div className="text-wrapper-11">SL No</div>

        <div className="text-wrapper-12">결제 수단</div>

        <div className="text-wrapper-13">결제 금액</div>

        <div className="text-wrapper-14">결제 시각</div>

        <div className="text-wrapper-15">환불 요청 사유</div>

        <div className="text-wrapper-16">환불 상태</div>
      </div>

      {refunds.length === 0 && <div className="text-wrapper-17">환불 내역이 없습니다.</div>}

      {refunds.map((refund, index) => (
        <div className="navbar-2" key={refund.paymentId}>
          <div className="text-wrapper-17">{refund.amount}</div>
          <div className="text-wrapper-18">{String(index + 1).padStart(2, "0")}</div>
          <div className="text-wrapper-19">{refund.paymentMethod}</div>
          <div className="text-wrapper-20">{refund.paymentTime}</div>
          <div className="text-wrapper-21">{refund.reason}</div>
          <div className="text-wrapper-22">{refund.status}</div>
          <div className="group-5">
            <div className="overlap-group-2">
              <div className="text-wrapper-23">{refund.status}</div>
              <div className={`rectangle-${refund.status === "요청됨" ? 3 : refund.status === "승인됨" ? 4 : 7}`} />
            </div>
          </div>
        </div>
      ))}
    </div>
  );
};

export default RefundElement;