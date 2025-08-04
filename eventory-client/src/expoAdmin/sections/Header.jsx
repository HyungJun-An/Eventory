import React from "react";
import "../../assets/css/Header.css";

const Header = () => {
  return (
    <header className="header">
      <div className="group-31">
        <div className="group-32">
          <img
            className="mask-group"
            alt="Mask group"
            src="https://c.animaapp.com/mdvjmhhqHfUAE6/img/mask-group.png"
          />

          <img
            className="group-33"
            alt="Group"
            src="https://c.animaapp.com/mdvjmhhqHfUAE6/img/group-4.png"
          />

          <img
            className="group-34"
            alt="Group"
            src="https://c.animaapp.com/mdvjmhhqHfUAE6/img/group-3.png"
          />

          <div className="text-wrapper-35">매출분석</div>

          <div className="overlap-group-wrapper">
            <div className="overlap-group-3">
              <img
                className="magnifying-glass"
                alt="Magnifying glass"
                src="https://c.animaapp.com/mdvjmhhqHfUAE6/img/magnifying-glass-1.svg"
              />

              <div className="text-wrapper-36">Search for something</div>
            </div>
          </div>
        </div>

        <div className="logo">
          <div className="iconfinder-vector" />

          <div className="text-wrapper-37">Eventory</div>
        </div>
      </div>
    </header>
  );
};

export default Header;