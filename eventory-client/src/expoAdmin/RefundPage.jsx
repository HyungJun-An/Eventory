import React, { useState } from "react";
import Element from "./sections/refund/RefundElement";
import Group from "./sections/refund/RefundGroup";
import SalesStats from "./sections/refund/RefundSalesStats";
import "../assets/css/refund/RefundPage.css";

const RefundPage = ({ expoId }) => {

  const [status, setStatus] = useState("ALL");

  return (
    <div className="refund-page" data-model-id="11051:3062">
      <div className="overlap-wrapper-2">
        <div className="overlap-4">
          <div className="overlap-5">
            <div className="rectangle-14" />
            <div className="rectangle-15" />
          </div>

          <div className="overlap-6">
            <div className="text-wrapper-32">환불 요청 관리</div>

            {expoId && (
              <>
                <SalesStats expoId={expoId} />
                <Group onStatusChange={setStatus}/>
                <Element expoId={expoId} status={status}/>
              </>
            )}
            <div className="view-2">
              <div className="group-23">
                <div className="text-wrapper-33">Next</div>
                <img
                  className="vector-2"
                  alt="Vector"
                  src="https://c.animaapp.com/mdxxi4aqdC9oEE/img/vector-2.svg"
                />
              </div>

              <div className="group-24">
                <div className="text-wrapper-34">Previous</div>
                <img
                  className="vector-3"
                  alt="Vector"
                  src="https://c.animaapp.com/mdxxi4aqdC9oEE/img/vector-2-1.svg"
                />
              </div>

              <div className="text-wrapper-35">3</div>
              <div className="text-wrapper-36">4</div>
              <div className="overlap-7">
                <div className="text-wrapper-37">1</div>
              </div>
              <div className="text-wrapper-38">2</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default RefundPage;
