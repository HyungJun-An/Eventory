import React from "react";
import "../../../assets/css/payment/PaymentElement.css";

const PaymentElement = ({ payments, page, pageSize }) => {
  return (
    <div className="payment-element">
      <div className="navbar-2">
        <div className="text-wrapper-19">예약자명</div>

        <div className="text-wrapper-20">SL No</div>

        <div className="text-wrapper-21">예약 번호</div>

        <div className="text-wrapper-22">예약 인원</div>

        <div className="text-wrapper-23">결제 수단</div>

        <div className="text-wrapper-24">결제 금액</div>

        <div className="text-wrapper-25">결제 시각</div>
      </div>
      {payments && payments.length > 0 ? (
        payments.map((p, index) => (
          <div key={index} className={`navbar SL-no-${index + 1}`}>
            <div className="text-wrapper-12">{page * pageSize + index + 1}</div>
            <div className="text-wrapper-11">{p.name}</div>
            <div className="text-wrapper-13">{p.code}</div>
            <div className="text-wrapper-14">{p.people}</div>
            <div className="text-wrapper-15">{p.method}</div>
            <div className="text-wrapper-16">{p.amount}</div>
            <div className="text-wrapper-17">{new Date(p.paidAt).toLocaleString()}</div>
          </div>
        ))
      ) : (
        <div className="no-data">결제 내역이 없습니다.</div>
      )}
    </div>
  );
};

export default PaymentElement;
