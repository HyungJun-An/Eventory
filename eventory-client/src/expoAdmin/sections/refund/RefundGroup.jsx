import React, { useState } from "react";
import "../../../assets/css/refund/RefundGroup.css";

const RefundGroup = ({ onStatusChange }) => {
  const [selected, setSelected] = useState("ALL");

  const handleClick = (status) => {
    setSelected(status);
    onStatusChange(status);
  };

  return (
    <div className="refund-group">
      <div
        className={`text-wrapper-26 ${selected === "ALL" ? "active" : ""}`}
        onClick={() => handleClick("ALL")}
        style={{ cursor: "pointer" }}
      >
        전체 내역
      </div>

      <div
        className={`text-wrapper-27 ${selected === "PENDING" ? "active" : ""}`}
        onClick={() => handleClick("PENDING")}
        style={{ cursor: "pointer" }}
      >
        환불 대기
      </div>

      <div
        className={`text-wrapper-28 ${selected === "APPROVED" ? "active" : ""}`}
        onClick={() => handleClick("APPROVED")}
        style={{ cursor: "pointer" }}
      >
        환불 완료
      </div>

      <div className="overlap-3">
        <div className="rectangle-12" />

        <div className="rectangle-13" />
      </div>
    </div>
  );
};

export default RefundGroup;
