import React from "react";
import Div from "./sections/Div";
import DivWrapper from "./sections/DivWrapper";
import Group from "./sections/Group";
import GroupWrapper from "./sections/GroupWrapper";
import Header from "./sections/Header";
import "../assets/css/RevenuePage.css";

const RevenuePage = () => {
  return (
    <div className="div-wrapper-screen" data-model-id="11057:3338">
      <div className="overlap-wrapper-2">
        <div className="overlap-3">
          <Group />
          <div className="overlap-4">
            <div className="rectangle-24" />

            <div className="rectangle-25" />

            <Header />
          </div>

          <div className="overlap-5">
            <GroupWrapper />
            <DivWrapper />
            <Div />
          </div>
        </div>
      </div>
    </div>
  );
};

export default RevenuePage;