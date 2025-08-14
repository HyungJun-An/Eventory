import React from "react";
import { useNavigate } from "react-router-dom";
import "../../assets/css/ExpoAdminSideBar.css";

const ExpoAdminSideBar = () => {
  const navigate = useNavigate();

  return (
    <div className="expoadmin-sideBar">
      <div className="div-8">
        <div className="text-wrapper-2">대시보드</div>

        <img
          className="img"
          alt="Home"
          src="https://c.animaapp.com/mdwp8vqiW7o1mV/img/home-2.svg"
        />
      </div>

      <div className="div-2">
        <div className="text-wrapper-2">예약 관리</div>

        <img
          className="img"
          alt="Transfer"
          src="https://c.animaapp.com/mdwp8vqiW7o1mV/img/transfer-1.svg"
        />
      </div>

      <div className="div-7">
        <div className="text-wrapper-6">콘텐츠 관리</div>

        <img
          className="img"
          alt="User"
          src="https://c.animaapp.com/mdwp8vqiW7o1mV/img/user-3-1.svg"
        />
      </div>

      <div className="div-3">
        <div className="text-wrapper-2">부스 관리</div>

        <img
          className="img"
          alt="Economic investment"
          src="https://c.animaapp.com/mdwp8vqiW7o1mV/img/economic-investment-1.svg"
        />
      </div>

      <div className="div-4" onClick={() => navigate("/admin/sales")}>
        <div className="text-wrapper-3">매출 분석</div>

        <img
          className="credit-card"
          alt="Credit card"
          src="https://c.animaapp.com/mdwp8vqiW7o1mV/img/credit-card-1.svg"
        />
      </div>

      <div className="div-5" onClick={() => navigate("/admin/payment")}>
        <div className="text-wrapper-4">정산 관리</div>

        <img
          className="img"
          alt="Loan"
          src="https://c.animaapp.com/mdwp8vqiW7o1mV/img/loan-1.svg"
        />
      </div>

      <div className="div-6" onClick={() => navigate("/admin/refund")}>
        <div className="text-wrapper-5">환불 처리</div>

        <img
          className="img"
          alt="Service"
          src="https://c.animaapp.com/mdwp8vqiW7o1mV/img/service-1.svg"
        />
      </div>   
      <div className="rectangle" />
    </div>
  );
};

export default ExpoAdminSideBar;