import React from "react";
import "../../../assets/css/payment/PaymentElement.css";

const PaymentElement = ({ payments, page, pageSize }) => {
  return (
    <div className="payment-element">

      {payments && payments.length > 0 ? (
        payments.map((p, index) => (
          <div key={index} className={`SL-no-${index + 1}`}>
            <div className="text-wrapper-1">{page * pageSize + index + 1}</div>
            <div className="text-wrapper-2">{p.code}</div>
            <div className="text-wrapper-3">{p.name}</div>
            <div className="text-wrapper-4">{p.people}</div>
            <div className="text-wrapper-5">{p.method}</div>
            <div className="text-wrapper-6">{p.amount}</div>
            <div className="text-wrapper-7">{new Date(p.paidAt).toLocaleString()}</div>
            <div className={`rectangle-${index + 3}`} />
          </div>
        ))
      ) : (
        <div className="no-data">결제 내역이 없습니다.</div>
      )}

      
    </div>
  );
};

export default PaymentElement;