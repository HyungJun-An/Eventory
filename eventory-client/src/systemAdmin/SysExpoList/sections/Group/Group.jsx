import React from "react";
// import { MoneyOptional } from "../../../../components/MoneyOptional";
import "./style.css";

export const Group = () => {
  return (
    <div className="group">
      <div className="sidebar-menu" />

      <div className="div-3">
        <div className="div-4">
          <img
            className="img"
            alt="Home"
            src="https://c.animaapp.com/me7tg6ywkSLruI/img/home-2.svg"
          />

          <div className="text-wrapper-18">대시보드</div>
        </div>

        <div className="div-5">
          <div className="text-wrapper-19">박람회 관리자 관리</div>

          <img
            className="img"
            alt="User"
            src="https://c.animaapp.com/me7tg6ywkSLruI/img/user-3-1.svg"
          />
        </div>

        <div className="div-6">
          <div className="rectangle-6" />

          <div className="div-7">
            <img
              className="img"
              alt="Economic investment"
              src="https://c.animaapp.com/me7tg6ywkSLruI/img/economic-investment-1.svg"
            />

            <div className="text-wrapper-20">박람회 관리</div>
          </div>
        </div>

        <div className="div-8">
          <div className="text-wrapper-18">VIP 배너 관리</div>

          <MoneyOptional className="money-optional-instance" />
        </div>
      </div>
    </div>
  );
};
