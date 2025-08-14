import React from "react";
import "../../../assets/css/Payment/PaymentGroup.css";

const PaymentGroup = () => {
  return (
    <div className="payment-group">
      <div className="group-13">
        <div className="overlap-group-2">
          <img
            className="magnifying-glass"
            alt="Magnifying glass"
            src="https://c.animaapp.com/mdwrr278Hhu1fG/img/magnifying-glass-1.svg"
          />

          <div className="text-wrapper-29">Search for something</div>
        </div>
      </div>

      <button className="button">
        <img
          className="huge-icon-device"
          alt="Huge icon device"
          src="https://c.animaapp.com/mdwrr278Hhu1fG/img/huge-icon-device-outline-filter.svg"
        />

        <button className="button-2">Filter</button>
      </button>

      <div className="group-14">
        <div className="overlap-3">
          <div className="text-wrapper-30">다운로드</div>
        </div>
      </div>
    </div>
  );
};

export default PaymentGroup;