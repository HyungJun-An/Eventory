import React from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import "../../assets/css/ExpoAdminSideBar.css";
import { DashBoardIcon, ReserveIcon, ContentIcon, BoothIcon
, PaymentIcon, SalesIcon, RefundIcon } from "../../components/SideBarIcon";

const ExpoAdminSideBar = () => {
  const navigate = useNavigate();
  const path = location.pathname;

  return (
    <div className="expoadmin-sideBar">
              

      <div  className={`div-8 ${path === "/admin/dashboard" ? "active" : ""}`}
      onClick={() => navigate("/admin/dashboard")}>
        <div className="text-wrapper-2">대시보드</div>
        <div className="icon-wrapper">
        <DashBoardIcon active={path === "/admin/dashboard"} />
        </div>
      </div>

      <div className={`div-2 ${path === "/admin/reservation" ? "active" : ""}`}
        onClick={() => navigate("/admin/reservation")}>
        <div className="text-wrapper-2">예약 관리</div>

        <div className="icon-wrapper">
        <ReserveIcon active={path === "/admin/reservation"} />
        </div>
      </div>

      <div className={`div-7 ${path === "/admin/contents" ? "active" : ""}`} 
        onClick={()=> navigate("/admin/contents")}>
        <div className="text-wrapper-6">콘텐츠 관리</div>

        <div className="icon-wrapper">
        <ContentIcon active={path === "/admin/contents"} />
        </div>
      </div>

      <div className={`div-3 ${path === "/admin/booth" ? "active" : ""}`} 
      onClick={() => navigate("/admin/booth")}>
        <div className="text-wrapper-2">부스 관리</div>

        <div className="icon-wrapper">
        <BoothIcon active={path === "/admin/booth"} />
        </div>
      </div>

      <div className={`div-4 ${path === "/admin/sales" ? "active" : ""}`} 
      onClick={() => navigate("/admin/sales")}>
        <div className="text-wrapper-3">매출 분석</div>
        <div className="icon-wrapper">
        <SalesIcon active={path === "/admin/sales"} />
        </div>
      </div>

      <div className={`div-5 ${path === "/admin/payment" ? "active" : ""}`} 
      onClick={() => navigate("/admin/payment")}>
        <div className="text-wrapper-4">정산 관리</div>
        <div className="icon-wrapper">
        <PaymentIcon active={path === "/admin/payment"} />
        </div>
      </div>

      <div className="div-6" onClick={() => navigate("/admin/refund")}>
        <div className="text-wrapper-5">환불 처리</div>
        <div className="icon-wrapper">
        <RefundIcon active={path === "/admin/refund"} />
        </div>
      </div>   
      <div className="rectangle" />
    </div>
  );
};

export default ExpoAdminSideBar;