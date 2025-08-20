import React from "react";
import "../../../assets/css/refund/RefundGroup.css";

const RefundGroup = ({ expoId }) => {
  return (
    <div className="refund-group">
      <div className="text-wrapper-26">전체 내역</div>

      <div className="text-wrapper-27">환불 대기</div>

      <div className="text-wrapper-28">환불 완료</div>

      <div className="overlap-3">
        <div className="rectangle-12" />

        <div className="rectangle-13" />
      </div>
    </div>
  );
};

export default RefundGroup;