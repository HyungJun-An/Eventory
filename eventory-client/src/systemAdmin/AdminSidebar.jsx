import React from "react";
import { useState, useEffect } from "react";
import { HomeFilled, UserOutlined } from "@ant-design/icons";
import "../assets/css/systemAdmin/AdminSidebar.css";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  faUser,
  faHouse,
  faChartSimple,
  faChartLine,
} from "@fortawesome/free-solid-svg-icons";
import { useLocation } from "react-router-dom";
import { useNavigate } from "react-router-dom";

export const AdminSidebar = () => {
  const location = useLocation();
  const [activeTab, setActiveTab] = useState("");
  const navigate = useNavigate();
  const currentUrl = location.pathname.split("/")[2];
  // console.log(location.pathname.split("/")[2]);
  return (
    <>
      <div
        style={{
          display: "flex",
          flexDirection: "column",
          alignItems: "flex-start",
          backgroundColor: "white",
        }}
      >
        <div
          className="sidebarMenu"
          onClick={() => {
            setActiveTab("dashboard");
            navigate("/sys/dashboard", { replace: true });
          }}
        >
          <div
            style={{
              height: "5rem",
              width: "0.4rem",
            }}
          >
            {currentUrl == "dashboard" && (
              <ActiveTabIndicator></ActiveTabIndicator>
            )}
            {/* {activeTab == "dashboard" && (
              <ActiveTabIndicator></ActiveTabIndicator>
            )} */}
          </div>
          <FontAwesomeIcon
            style={{
              color: currentUrl == "dashboard" ? "#007BFF" : "#B1B1B1",
              fontSize: "1.9rem",
              marginLeft: "0.5rem",
            }}
            icon={faHouse}
          />
          <div
            style={{
              color: currentUrl == "dashboard" ? "#007BFF" : "#B1B1B1",
              fontSize: "1.2rem",
              fontWeight: "500",
            }}
          >
            대시보드
          </div>
        </div>
        <div
          className="sidebarMenu"
          onClick={() => {
            setActiveTab("managerManagement");
            navigate("/sys/manage", { replace: true });
          }}
        >
          <div
            style={{
              height: "5rem",
              width: "0.4rem",
            }}
          >
            {currentUrl == "manage" && (
              <ActiveTabIndicator></ActiveTabIndicator>
            )}
            {/* {activeTab == "managerManagement" && (
              <ActiveTabIndicator></ActiveTabIndicator>
            )} */}
          </div>
          <FontAwesomeIcon
            style={{
              color: currentUrl == "manage" ? "#007BFF" : "#B1B1B1",
              fontSize: "1.9rem",
              marginLeft: "0.5rem",
            }}
            icon={faUser}
          />
          <div
            style={{
              color: currentUrl == "manage" ? "#007BFF" : "#B1B1B1",
              fontSize: "1.2rem",
              fontWeight: "500",
            }}
          >
            박람회 관리자 관리
          </div>
        </div>
        <div
          className="sidebarMenu"
          onClick={() => setActiveTab("expoManagement")}
        >
          <div
            style={{
              height: "5rem",
              width: "0.4rem",
            }}
          >
            {currentUrl == "expoManagement" && (
              <ActiveTabIndicator></ActiveTabIndicator>
            )}
            {/* {activeTab == "expoManagement" && (
              <ActiveTabIndicator></ActiveTabIndicator>
            )} */}
          </div>
          <FontAwesomeIcon
            style={{
              color: currentUrl == "expoManagement" ? "#007BFF" : "#B1B1B1",
              fontSize: "1.9rem",
              marginLeft: "0.5rem",
            }}
            icon={faChartSimple}
          />
          <div
            style={{
              color: currentUrl == "expoManagement" ? "#007BFF" : "#B1B1B1",
              fontSize: "1.2rem",
              fontWeight: "500",
            }}
          >
            박람회 관리
          </div>
        </div>
        <div
          className="sidebarMenu"
          onClick={() => setActiveTab("bannerManagement")}
        >
          <div
            style={{
              height: "5rem",
              width: "0.4rem",
            }}
          >
            {currentUrl == "bannerManagement" && (
              <ActiveTabIndicator></ActiveTabIndicator>
            )}
            {/* {activeTab == "bannerManagement" && (
              <ActiveTabIndicator></ActiveTabIndicator>
            )} */}
          </div>
          <FontAwesomeIcon
            style={{
              color: currentUrl == "bannerManagement" ? "#007BFF" : "#B1B1B1",
              fontSize: "1.9rem",
              marginLeft: "0.5rem",
            }}
            icon={faChartLine}
          />
          <div
            style={{
              color: currentUrl == "bannerManagement" ? "#007BFF" : "#B1B1B1",
              fontSize: "1.2rem",
              fontWeight: "500",
            }}
          >
            VIP 배너 관리
          </div>
        </div>
      </div>
    </>
  );
};

export function ActiveTabIndicator() {
  return (
    <>
      <div
        style={{
          backgroundColor: "#007BFF",
          height: "5rem",
          width: "0.4rem",
          borderTopRightRadius: "0.5rem",
          borderBottomRightRadius: "0.5rem",
        }}
      ></div>
    </>
  );
}

export function SpaceFiller() {
  return (
    <>
      <div
        style={{
          height: "4rem",
          width: "0.4rem",
        }}
      ></div>
    </>
  );
}

export default AdminSidebar;
