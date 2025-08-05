import React from "react";
import "../../assets/css/PaymentView.css";

 const PaymentView = () => {
  return (
    <div className="view">
      <div className="group-4">
        <div className="group-5">
          <div className="group-6">
            <div className="text-wrapper-26">+5.80%</div>

            <div className="text-wrapper-27">총 환불 건수</div>
          </div>

          <img
            className="group-7"
            alt="Group"
            src="https://c.animaapp.com/mdwrr278Hhu1fG/img/group-307.png"
          />
        </div>
      </div>

      <div className="group-8">
        <div className="group-9">
          <div className="group-10">
            <div className="text-wrapper-28">$150,000</div>

            <div className="text-wrapper-27">누적 매출</div>
          </div>

          <img
            className="group-7"
            alt="Group"
            src="https://c.animaapp.com/mdwrr278Hhu1fG/img/group-303.png"
          />
        </div>
      </div>

      <div className="group-11">
        <div className="group-12">
          <div className="group-6">
            <div className="text-wrapper-28">1,250</div>

            <div className="text-wrapper-27">총 결제 건수</div>
          </div>

          <img
            className="group-7"
            alt="Group"
            src="https://c.animaapp.com/mdwrr278Hhu1fG/img/group-305.png"
          />
        </div>
      </div>
    </div>
  );
};

export default PaymentView;