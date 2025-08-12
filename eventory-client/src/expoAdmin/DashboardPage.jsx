import React from "react";
import WebsiteLogos from "../components/WebsiteLogos";
import "../assets/css/Dashboard.css";

const Dashboard = () => {
  return (
    <div className="dashboard" data-model-id="11194:16485">
      <div className="overlap-wrapper">
        <div className="overlap">
          <div className="overlap-group">
            <div className="group">
              <div className="weekly-activity">
                <div className="text-wrapper">예약 현황</div>

                <div className="group-wrapper">
                  <div className="overlap-group-wrapper">
                    <div className="div">
                      <div className="overlap-2">
                        <div className="text-wrapper-2">일별</div>
                      </div>

                      <div className="text-wrapper-3">주별</div>

                      <div className="text-wrapper-4">월별</div>
                    </div>
                  </div>
                </div>

                <div className="group-2">
                  <div className="overlap-3">
                    <div className="text-wrapper-5">CSV 다운로드</div>
                  </div>
                </div>

                <div className="group-3">
                  <div className="overlap-4">
                    <div className="text-wrapper-6">엑셀 다운로드</div>
                  </div>
                </div>
              </div>

              <div className="expense-stats">
                <div className="text-wrapper">티켓 종류별 예약 비율</div>

                <img
                  className="group-4"
                  alt="Group"
                  src="https://c.animaapp.com/mdwrp9urLhB6f1/img/group-399.png"
                />
              </div>
            </div>

            <div className="group-5">
              <div className="group-6">
                <div className="group-7">
                  <div className="group-8">
                    <div className="text-wrapper-7">5.80%</div>

                    <div className="text-wrapper-8">입장률</div>
                  </div>

                  <img
                    className="group-9"
                    alt="Group"
                    src="https://c.animaapp.com/mdwrp9urLhB6f1/img/group-307.png"
                  />
                </div>
              </div>

              <div className="group-10">
                <div className="group-11">
                  <div className="group-12">
                    <div className="text-wrapper-9">1,250</div>

                    <div className="text-wrapper-10">총 예약 수</div>
                  </div>

                  <img
                    className="group-9"
                    alt="Group"
                    src="https://c.animaapp.com/mdwrp9urLhB6f1/img/group-305.png"
                  />
                </div>
              </div>

              <div className="group-13">
                <div className="group-14">
                  <div className="group-15">
                    <div className="text-wrapper-11">150,000</div>

                    <div className="text-wrapper-12">페이지 조회 수</div>
                  </div>

                  <img
                    className="group-9"
                    alt="Group"
                    src="https://c.animaapp.com/mdwrp9urLhB6f1/img/group-303.png"
                  />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;