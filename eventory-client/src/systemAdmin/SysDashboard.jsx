import React from "react";
import { useState, useEffect } from "react";
import "../assets/css/systemAdmin/systemAdmin.css";
import "../assets/css/systemAdmin/sysDashboard.css";
import totalAmount from "../../public/totalAmount.png";

export const SysDashboard = ({ onClose, id, manager }) => {
  return (
    <>
      <div className="wrapper">
        <div style={{ marginTop: "5vh", marginLeft: "3vw" }}>
          <div
            style={{
              display: "flex",
              flexDirection: "row",
              gap: "1.5vw",
            }}
          >
            <div className="card">
              <img src={"../../public/totalAmount.png"} />

              <div>
                <div style={{ fontSize: "1.2rem", color: "#718EBF" }}>
                  총 결제금액
                </div>
                <div style={{ fontSize: "1.5rem", fontWeight: "600" }}>
                  150,000
                </div>
              </div>
            </div>
            <div className="card">
              <img src={"../../public/totalAmount.png"} />
              <div>
                <div style={{ fontSize: "1.2rem", color: "#718EBF" }}>
                  누적 예약 수
                </div>
                <div style={{ fontSize: "1.5rem", fontWeight: "600" }}>
                  1,250
                </div>
              </div>
            </div>
            <div className="card">
              <img src={"../../public/totalAmount.png"} />
              <div>
                <div style={{ fontSize: "1.2rem", color: "#718EBF" }}>
                  전체 방문자 수
                </div>
                <div style={{ fontSize: "1.5rem", fontWeight: "600" }}>
                  1,189
                </div>
              </div>
            </div>
            <div className="card">
              <img src={"../../public/totalAmount.png"} />
              <div>
                <div style={{ fontSize: "1.2rem", color: "#718EBF" }}>
                  신규 가입자 수
                </div>
                <div style={{ fontSize: "1.5rem", fontWeight: "600" }}>150</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </>
  );
};

export default SysDashboard;
