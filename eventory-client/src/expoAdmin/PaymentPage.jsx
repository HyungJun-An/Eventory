import React from "react";
import Element from "./sections/payment/PaymentElement";
import Group from "./sections/payment/PaymentGroup";
import SalesStats from "./sections/payment/PaymentSalesStats";
import "../assets/css/payment/PaymentPage.css";

const PaymentPage = ({ expoId }) => {
  return (
    <div className="payment-page" data-model-id="11051:2424">
      <div className="overlap-wrapper-2">
        <div className="overlap-4">
          <div className="overlap-5">
            <div className="rectangle-10" />
            <div className="rectangle-11" />
          </div>

          <div className="overlap-6">
            <div className="text-wrapper-31">결제 내역 관리</div>

            <div className="view-2">
              <div className="group-15">
                <div className="text-wrapper-32">Next</div>
                <img
                  className="vector-2"
                  alt="Vector"
                  src="https://c.animaapp.com/mdwrr278Hhu1fG/img/vector-2.svg"
                />
              </div>

              <div className="group-16">
                <div className="text-wrapper-33">Previous</div>
                <img
                  className="vector-3"
                  alt="Vector"
                  src="https://c.animaapp.com/mdwrr278Hhu1fG/img/vector-2-1.svg"
                />
              </div>

              <div className="text-wrapper-34">3</div>
              <div className="text-wrapper-35">4</div>
              <div className="overlap-group-3">
                <div className="text-wrapper-36">1</div>
              </div>
              <div className="text-wrapper-37">2</div>
            </div>

            {expoId && (
              <>
                <SalesStats expoId={expoId} />
                <Group expoId={expoId} />
                <Element expoId={expoId} />
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default PaymentPage;
