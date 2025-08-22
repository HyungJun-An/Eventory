import React, { useState } from "react";
import "../../../assets/css/refund/RefundGroup.css";

const RefundGroup = ({ onStatusChange }) => {
  const [selected, setSelected] = useState("ALL");

  const handleClick = (status) => {
    setSelected(status);
    onStatusChange(status);
  };

  const getColor = (status) => (selected === status ? "#007BFF" : "#343A40");

  return (
    <div className="refund-group">
      <div
        className={`text-wrapper-26 ${selected === "ALL" ? "active" : ""}`}
        onClick={() => handleClick("ALL")}
        style={{ cursor: "pointer", color: getColor("ALL") }}
      >
        전체 내역
      </div>

      <div
        className={`text-wrapper-27 ${selected === "PENDING" ? "active" : ""}`}
        onClick={() => handleClick("PENDING")}
        style={{ cursor: "pointer", color: getColor("PENDING") }}
      >
        환불 대기
      </div>

      <div
        className={`text-wrapper-28 ${selected === "APPROVED" ? "active" : ""}`}
        onClick={() => handleClick("APPROVED")}
        style={{ cursor: "pointer", color: getColor("APPROVED") }}
      >
        환불 완료
      </div>

      <div className="overlap-3">
        <div className="rectangle-12" />

        <div
          className="rectangle-13"
          style={{
            left:
              selected === "ALL"
                ? "0"
                : selected === "PENDING"
                ? "130px"
                : "240px",
            transition: "left 0.3s ease",
          }}
        />
      </div>
    </div>
  );
};

export default RefundGroup;
