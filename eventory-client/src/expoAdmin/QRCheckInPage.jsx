import React from 'react';
import { useNavigate } from "react-router-dom"; // [CHANGED] 네비게이션 훅 추가
import "../assets/css/checkIn/QRCheckInPage.css";

function QRCheckInPage() {
  const navigate = useNavigate(); // [CHANGED] 네비게이션 훅 사용

  return (
    <div className="qr-checkin-container">
      <div className="qr-checkin-wrapper">
        {/* Title Section */}
        <div className="title-section">
          <h1 className="main-title">
            QR 코드 체크인
          </h1>
          <p className="subtitle">
            예약자의 QR 코드를 스캔하여 체크인을 진행하세요
          </p>
        </div>

        {/* QR Scanner Card */}
        <div className="scanner-card">
          <div className="scanner-content">
            {/* Camera Icon Container */}
            <div className="camera-icon-container">
              <svg className="camera-icon" viewBox="0 0 61 60" fill="none">
                <path d="M23 5L18.425 10H10.5C7.75 10 5.5 12.25 5.5 15V45C5.5 47.75 7.75 50 10.5 50H50.5C53.25 50 55.5 47.75 55.5 45V15C55.5 12.25 53.25 10 50.5 10H42.575L38 5H23ZM30.5 17.5C37.4 17.5 43 23.1 43 30C43 36.9 37.4 42.5 30.5 42.5C23.6 42.5 18 36.9 18 30C18 23.1 23.6 17.5 30.5 17.5ZM30.5 22.5C26.35 22.5 23 25.85 23 30C23 34.15 26.35 37.5 30.5 37.5C34.65 37.5 38 34.15 38 30C38 25.85 34.65 22.5 30.5 22.5Z" fill="#718EBF"/>
              </svg>
            </div>

            {/* Text Content */}
            <h2 className="scanner-title">
              카메라를 시작하세요
            </h2>
            <p className="scanner-description">
              {/* [CHANGED] 3) 두 줄로 고정 표시 (span 블록화) */}
              <span className="desc-line1">QR 코드를 스캔하기 위해</span>
              <span className="desc-line2">카메라를 활성화해주세요</span>
            </p>
          </div>
        </div>

        {/* Action Buttons */}
        <div className="button-group">
          {/* Start Camera Button */}
          <button className="start-camera-btn">
            카메라 시작
          </button>

          {/* Go to Reservation List Button */}
          <button
            className="reservation-list-btn"
            onClick={() => navigate("/admin/reservation/list")} // [CHANGED] 클릭 시 이동
            type="button" // [CHANGED] 폼 내에서 의도치 않은 submit 방지
            aria-label="예약자 명단 페이지로 이동"
          >
            예약자 명단으로 이동
          </button>
        </div>
      </div>
    </div>
  );
}
export default QRCheckInPage;