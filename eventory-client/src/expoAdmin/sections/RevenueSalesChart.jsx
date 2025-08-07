import React from "react";
import "../../assets/css/RevenueSalesChart.css";

const RevenueSalesChart = () => {
  return (
    <div className="sales-chart">
      <div className="group-19">
        <div className="text-wrapper-25">환불 비율</div>

        <img
          className="group-20"
          alt="Group"
          src="https://c.animaapp.com/mdwp8vqiW7o1mV/img/group-351.png"
        />
      </div>

      <div className="group-21">
        <div className="group-22">
          <div className="group-23">
            <p className="element">
              <span className="span">이번주 총 매출 금액은</span>

              <span className="text-wrapper-26"> ₩5,420 </span>

              <span className="span">입니다.</span>
            </p>

            <div className="group-24">
              <div className="group-25">
                <div className="rectangle-8" />

                <div className="rectangle-9" />
              </div>

              <div className="group-26">
                <div className="rectangle-10" />

                <div className="rectangle-11" />
              </div>

              <div className="group-27">
                <div className="rectangle-12" />

                <div className="rectangle-13" />
              </div>

              <div className="group-28">
                <div className="rectangle-14" />

                <div className="rectangle-15" />
              </div>

              <div className="group-29">
                <div className="rectangle-16" />

                <div className="rectangle-17" />
              </div>

              <div className="group-30">
                <div className="rectangle-18" />

                <div className="rectangle-19" />
              </div>

              <div className="group-31">
                <div className="rectangle-20" />

                <div className="rectangle-21" />
              </div>

              <div className="text-wrapper-27">Sat</div>

              <div className="text-wrapper-28">Sun</div>

              <div className="text-wrapper-29">Mon</div>

              <div className="text-wrapper-30">Tue</div>

              <div className="text-wrapper-31">Thu</div>

              <div className="text-wrapper-32">Fri</div>

              <div className="text-wrapper-33">Wed</div>
            </div>

            <div className="group-32">
              <div className="group-33">
                <div className="rectangle-22" />

                <div className="text-wrapper-34">저번주</div>
              </div>

              <div className="group-34">
                <div className="rectangle-23" />

                <div className="text-wrapper-35">이번주</div>
              </div>
            </div>
          </div>
        </div>

        <div className="text-wrapper-36">주간 매출 추이</div>
      </div>
    </div>
  );
};

export default RevenueSalesChart;