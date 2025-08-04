import React from "react";
import "../../assets/css/GroupWrapper.css";

const GroupWrapper = () => {
  return (
    <div className="group-wrapper">
      <div className="group-2">
        <div className="text-wrapper-8">연간 매출</div>

        <div className="group-3">
          <div className="group-4">
            <div className="year">
              <div className="text-wrapper-9">2016</div>

              <div className="text-wrapper-10">2017</div>

              <div className="text-wrapper-11">2018</div>

              <div className="text-wrapper-12">2019</div>

              <div className="text-wrapper-13">2020</div>

              <div className="text-wrapper-14">2021</div>
            </div>

            <div className="overlap-group-2">
              <div className="amount">
                <div className="text-wrapper-15">$10,000</div>

                <div className="text-wrapper-16">$20,000</div>

                <div className="text-wrapper-17">$30,000</div>

                <div className="text-wrapper-18">$40,000</div>

                <div className="text-wrapper-19">$0</div>
              </div>

              <div className="line">
                <div className="rectangle-2" />

                <div className="rectangle-3" />

                <div className="rectangle-4" />

                <div className="rectangle-5" />

                <div className="rectangle-6" />
              </div>

              <img
                className="statistics"
                alt="Statistics"
                src="https://c.animaapp.com/mdvjmhhqHfUAE6/img/statistics.png"
              />
            </div>
          </div>
        </div>
      </div>

      <div className="group-5">
        <div className="text-wrapper-8">월간 매출</div>

        <img
          className="img"
          alt="Group"
          src="https://c.animaapp.com/mdvjmhhqHfUAE6/img/group-254.png"
        />
      </div>
    </div>
  );
};

export default GroupWrapper;