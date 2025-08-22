import React from "react";
import SalesChart from "./sections/revenue/RevenueSalesChart";
import SalesStats from "./sections/revenue/RevenueSalesStats";
import SeriesChart from "./sections/revenue/RevenueSeriesChart";
import "../assets/css/revenue/RevenuePage.css";

const RevenuePage = ({ expoId }) => {
  return (
    <div className="revenue-page" data-model-id="11051:3062">
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
