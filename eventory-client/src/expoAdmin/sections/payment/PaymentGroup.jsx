import React from "react";
import "../../../assets/css/payment/PaymentGroup.css";
import api from "../../../api/axiosInstance";

const PaymentGroup = ({ expoId }) => {

  const handleDownload = async () => {
    try {
      const response = await api.post(
        `/admin/expos/${expoId}/payment/report`,
        {},
        {
          responseType: "blob", // 파일 다운로드 위해 blob 타입 지정
        }
      );

      // blob 객체 생성
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement("a");
      link.href = url;
      link.setAttribute("download", "payment.xlsx"); // 다운로드 파일 이름
      document.body.appendChild(link);
      link.click();

      // cleanup
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (error) {
      console.error("엑셀 다운로드 실패:", error);
      alert("다운로드 중 오류가 발생했습니다.");
    }
  };

  return (
    <div className="payment-group">
      <div className="group-13">
        <div className="overlap-group-2">
          <img
            className="magnifying-glass"
            alt="Magnifying glass"
            src="https://c.animaapp.com/mdwrr278Hhu1fG/img/magnifying-glass-1.svg"
          />

          <div className="text-wrapper-29">Search for something</div>
        </div>
      </div>

      <div className="button">
        <img
          className="huge-icon-device"
          alt="Huge icon device"
          src="https://c.animaapp.com/mdwrr278Hhu1fG/img/huge-icon-device-outline-filter.svg"
        />

        <button className="button-2">Filter</button>
      </div>

      <div className="group-14" onClick={handleDownload} style={{ cursor: "pointer" }}>
        <div className="overlap-3">
          <div className="text-wrapper-30">다운로드</div>
        </div>
      </div>
    </div>
  );
};

export default PaymentGroup;