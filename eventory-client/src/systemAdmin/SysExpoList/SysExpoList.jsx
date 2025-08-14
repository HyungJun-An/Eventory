import React from "react";
import { Frame } from "./sections/Frame";
import { Group } from "./sections/Group";
import "./style.css";

export const SysExpoList = () => {
  return (
    <div className="div-wrapper" data-model-id="11260:15697">
      <div className="div-9">
        <div className="rectangle-7" />

        <div className="overlap">
          <div className="navbar-2">
            <div className="rectangle-8" />

            <div className="text-wrapper-21">전체</div>

            <div className="rectangle-9" />

            <div className="text-wrapper-22">대기</div>

            <div className="rectangle-9" />

            <div className="text-wrapper-23">승인</div>

            <div className="rectangle-9" />

            <div className="text-wrapper-24">거절</div>
          </div>

          <Frame />
          <div className="group-3">
            <div className="group-4">
              <div className="text-wrapper-25">Next</div>

              <img
                className="vector"
                alt="Vector"
                src="https://c.animaapp.com/me7tg6ywkSLruI/img/vector-2-1.svg"
              />
            </div>

            <div className="group-5">
              <div className="text-wrapper-26">Previous</div>

              <img
                className="vector-2"
                alt="Vector"
                src="https://c.animaapp.com/me7tg6ywkSLruI/img/vector-2.svg"
              />
            </div>

            <div className="text-wrapper-27">3</div>

            <div className="text-wrapper-28">4</div>

            <div className="overlap-2">
              <div className="text-wrapper-29">1</div>
            </div>

            <div className="text-wrapper-30">2</div>
          </div>

          <div className="group-6">
            <div className="overlap-3">
              <img
                className="magnifying-glass"
                alt="Magnifying glass"
                src="https://c.animaapp.com/me7tg6ywkSLruI/img/magnifying-glass-1.svg"
              />

              <div className="text-wrapper-31">박람회명을 입력해주세요.</div>
            </div>
          </div>
        </div>

        <div className="overlap-4">
          <div className="rectangle-10" />

          <Group />
        </div>
      </div>
    </div>
  );
};
