import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import "../../assets/css/ExpoAdminSideBar.css";

const menuItems = [
  { id: 1, name: "대시보드", path: "/admin/dashboard", img: "https://c.animaapp.com/mdwp8vqiW7o1mV/img/home-2.svg" },
  { id: 2, name: "예약 관리", path: "/admin/reservation", img: "https://c.animaapp.com/mdwp8vqiW7o1mV/img/transfer-1.svg" },
  { id: 3, name: "콘텐츠 관리", path: "/admin/contents", img: "https://c.animaapp.com/mdwp8vqiW7o1mV/img/user-3-1.svg" },
  { id: 4, name: "부스 관리", path: "/admin/booth", img: "https://c.animaapp.com/mdwp8vqiW7o1mV/img/economic-investment-1.svg" },
  { id: 5, name: "매출 분석", path: "/admin/sales", img: "https://c.animaapp.com/mdwp8vqiW7o1mV/img/credit-card-1.svg" },
  { id: 6, name: "정산 관리", path: "/admin/payment", img: "https://c.animaapp.com/mdwp8vqiW7o1mV/img/loan-1.svg" },
  { id: 7, name: "환불 처리", path: "/admin/refund", img: "https://c.animaapp.com/mdwp8vqiW7o1mV/img/service-1.svg" },
];

const ExpoAdminSideBar = () => {
  const navigate = useNavigate();
  const [activeId, setActiveId] = useState(null);

  const handleClick = (id, path) => {
    setActiveId(id);
    navigate(path);
  };

  return (
    <div className="expoadmin-sideBar">
      <div className="div-8" onClick={() => handleClick(1, "/admin/dashboard")}>
        <div className="text-wrapper-2" style={{ color: activeId === 1 ? "#007BFF" : "rgba(52, 58, 64, 0.5)", transition: "color 0.2s", }}>대시보드</div>

        <img
          className="img"
          alt="Home"
          src="https://c.animaapp.com/mdwp8vqiW7o1mV/img/home-2.svg"
        />
      </div>

      <div className="div-2"onClick={() => handleClick(2, "/admin/reservation")}>
        <div className="text-wrapper-2" style={{ color: activeId === 2 ? "#007BFF" : "rgba(52, 58, 64, 0.5)", transition: "color 0.2s", }}>예약 관리</div>

        <img
          className="img"
          alt="Transfer"
          src="https://c.animaapp.com/mdwp8vqiW7o1mV/img/transfer-1.svg"
        />
      </div>

      <div className="div-7"  onClick={() => handleClick(3, "/admin/contents")}>
        <div className="text-wrapper-6" style={{ color: activeId === 3 ? "#007BFF" : "rgba(52, 58, 64, 0.5)", transition: "color 0.2s", }}>콘텐츠 관리</div>

        <img
          className="img"
          alt="User"
          src="https://c.animaapp.com/mdwp8vqiW7o1mV/img/user-3-1.svg"
        />
      </div>

      <div className="div-3"  onClick={() => handleClick(4, "/admin/booth")}>
        <div className="text-wrapper-2" style={{ color: activeId === 4 ? "#007BFF" : "rgba(52, 58, 64, 0.5)", transition: "color 0.2s", }}>부스 관리</div>

        <img
          className="img"
          alt="Economic investment"
          src="https://c.animaapp.com/mdwp8vqiW7o1mV/img/economic-investment-1.svg"
        />
      </div>

      <div className="div-4"  onClick={() => handleClick(5, "/admin/sales")}>
        <div className="text-wrapper-3" style={{ color: activeId === 5 ? "#007BFF" : "rgba(52, 58, 64, 0.5)", transition: "color 0.2s", }}>매출 분석</div>

        <img
          className="credit-card"
          alt="Credit card"
          src="https://c.animaapp.com/mdwp8vqiW7o1mV/img/credit-card-1.svg"
        />
      </div>

      <div className="div-5"  onClick={() => handleClick(6, "/admin/payment")}>
        <div className="text-wrapper-4" style={{ color: activeId === 6 ? "#007BFF" : "rgba(52, 58, 64, 0.5)", transition: "color 0.2s", }}>정산 관리</div>

        <img
          className="credit-card"
          alt="Loan"
          src="https://c.animaapp.com/mdwp8vqiW7o1mV/img/loan-1.svg"
        />
      </div>

      <div className="div-6"  onClick={() => handleClick(7, "/admin/refund")}>
        <div className="text-wrapper-5" style={{ color: activeId === 7 ? "#007BFF" : "rgba(52, 58, 64, 0.5)", transition: "color 0.2s", }}>환불 처리</div>

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