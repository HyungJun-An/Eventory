import React from "react";
import "../../assets/css/Group.css";

const Group = () => {
  return (
    <div className="group">
      <div className="overlap-group">
        <div className="text-wrapper">부스 관리</div>

        <img
          className="credit-card"
          alt="Credit card"
          src="https://c.animaapp.com/mdvjmhhqHfUAE6/img/credit-card-1.svg"
        />

        <img
          className="transfer"
          alt="Transfer"
          src="https://c.animaapp.com/mdvjmhhqHfUAE6/img/transfer-1.svg"
        />

        <div className="text-wrapper-2">예약 관리</div>

        <div className="text-wrapper-3">콘텐츠 관리</div>

        <div className="text-wrapper-4">매출분석</div>

        <img
          className="user"
          alt="User"
          src="https://c.animaapp.com/mdvjmhhqHfUAE6/img/user-3-1.svg"
        />

        <div className="div-2">
          <img
            className="home"
            alt="Home"
            src="https://c.animaapp.com/mdvjmhhqHfUAE6/img/home-2.svg"
          />

          <div className="text-wrapper-5">대시보드</div>

          <div className="rectangle" />
        </div>

        <img
          className="economic-investment"
          alt="Economic investment"
          src="https://c.animaapp.com/mdvjmhhqHfUAE6/img/economic-investment-1.svg"
        />
      </div>

      <img
        className="service"
        alt="Service"
        src="https://c.animaapp.com/mdvjmhhqHfUAE6/img/service-1.svg"
      />

      <img
        className="loan"
        alt="Loan"
        src="https://c.animaapp.com/mdvjmhhqHfUAE6/img/loan-1.svg"
      />

      <div className="text-wrapper-6">정산관리</div>

      <div className="text-wrapper-7">환불처리</div>
    </div>
  );
};

export default Group;