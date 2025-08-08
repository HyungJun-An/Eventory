import React from "react";
import SalesChart from "./sections/RevenueSalesChart";
import SalesStats from "./sections/RevenueSalesStats";
import SeriesChart from "./sections/RevenueSeriesChart";
import "../assets/css/RevenuePage.css";

const RevenuePage = ({expoId}) => {
  return (
    <div className="div-wrapper-screen" data-model-id="11057:3338">
      <div className="overlap-wrapper-2">
        <div className="overlap-3">
          <div className="overlap-4">
            <div className="rectangle-24" />
            <div className="rectangle-25" />
          </div>

          <div className="overlap-5">
            {expoId && (
              <>
                <SalesStats expoId={expoId} />
                <SeriesChart expoId={expoId} />
                <SalesChart expoId={expoId} />
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default RevenuePage;