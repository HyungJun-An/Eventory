import React from "react";
import WebsiteLogos from "../../components/WebsiteLogos";
import "../../assets/css/Header.css";

const Header = () => {
  return (
    <header className="header">
      <div className="overlap">
        <div className="group-2">
          <div className="group-3">
            <div className="text-wrapper-7">대시보드</div>
          </div>

          <div className="logo">
            <WebsiteLogos
              className="website-logos-instance"
              color="primary"
              removebgPreviewClassName="design-component-instance-node"
              removebgPreviewClassNameOverride="website-logos-2"
              size="small"
            />
          </div>
        </div>

        <div className="group-4">
          <div className="overlap-group-wrapper">
            <div className="overlap-group">
              <div className="text-wrapper-8">로그아웃</div>
            </div>
          </div>

          <div className="overlap-wrapper">
            <div className="overlap-2">
              <img
                className="vector"
                alt="Vector"
                src="https://c.animaapp.com/mdwp8vqiW7o1mV/img/vector.svg"
              />

              <div className="text-wrapper-9">박람회 선택</div>
            </div>
          </div>
        </div>
      </div>

      <div className="rectangle-2" />
    </header>
  );
};

export default Header;