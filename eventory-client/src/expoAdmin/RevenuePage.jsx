import React from "react";
import SalesChart from "./sections/RevenueSalesChart";
import SalesStats from "./sections/RevenueSalesStats";
import SideBar from "./sections/SideBar";
import SeriesChart from "./sections/RevenueSeriesChart";
import Header from "./sections/Header";
import "../assets/css/RevenuePage.css";

const RevenuePage = () => {
  return (
    <div className="div-wrapper-screen" data-model-id="11057:3338">
      <div className="overlap-wrapper-2">
        <div className="overlap-3">
          <SideBar />
          <div className="overlap-4">
            <div className="rectangle-24" />

            <div className="rectangle-25" />

            <Header />
          </div>

          <div className="overlap-5">
            <SalesStats />
            <SeriesChart />
            <SalesChart />
          </div>
        </div>
      </div>
    </div>
  );
};

export default RevenuePage;