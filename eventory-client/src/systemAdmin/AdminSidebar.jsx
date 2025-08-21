import React from "react";
import { useState, useEffect } from "react";
import { HomeFilled, UserOutlined } from "@ant-design/icons";
import "../assets/css/systemAdmin/AdminSidebar.css";

export const AdminSidebar = ({ activeTab }) => {
  return (
    <>
      <div
        style={{
          width: "20vw",
          display: "flex",
          flexDirection: "column",
          alignItems: "flex-start",
        }}
      >
        <div className="sidebarMenu">
          <HomeFilled style={{ color: "#007BFF", fontSize: "2rem" }} />
          <div style={{ fontSize: "1.2rem", fontWeight: "500" }}>대시보드</div>
        </div>
        <div className="sidebarMenu">
          <UserOutlined
            fill="blue"
            style={{
              //   fill: "blue",
              //   color: "#007BFF",
              fontSize: "2rem",
            }}
          />
          <div style={{ fontSize: "1.2rem", fontWeight: "500" }}>
            박람회 관리자 관리
          </div>
        </div>
        <div className="sidebarMenu">
          <HomeFilled style={{ color: "#007BFF", fontSize: "2rem" }} />
          <div style={{ fontSize: "1.2rem", fontWeight: "500" }}>
            박람회 관리
          </div>
        </div>
        <div className="sidebarMenu">
          <HomeFilled style={{ color: "#007BFF", fontSize: "2rem" }} />
          <div style={{ fontSize: "1.2rem", fontWeight: "500" }}>
            VIP 배너 관리
          </div>
        </div>
      </div>
    </>
  );
};

export default AdminSidebar;
