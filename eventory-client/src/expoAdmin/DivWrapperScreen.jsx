import React from "react";
import Div from "./sections/Div";
import DivWrapper from "./sections/DivWrapper";
import Group from "./sections/Group";
import GroupWrapper from "./sections/GroupWrapper";
import Header from "./sections/Header";
import "../assets/css/DivWrapperScreen.css";

const DivWrapperScreen = () => {
  return (
    <div className="div-wrapper-screen" data-model-id="11057:3338">
      <div className="overlap-wrapper">
        <div className="overlap">
          <Group />
          <div className="rectangle-23" />

          <div className="overlap-2">
            <GroupWrapper />
            <DivWrapper />
            <Div />
          </div>

          <div className="overlap-3">
            <Header />
            <div className="rectangle-24" />
          </div>
        </div>
      </div>
    </div>
  );
};

export default DivWrapperScreen;