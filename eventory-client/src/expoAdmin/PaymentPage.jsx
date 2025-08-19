import React, { useEffect, useState } from "react";
import api from '../api/axiosInstance';
import Element from "./sections/payment/PaymentElement";
import Group from "./sections/payment/PaymentGroup";
import SalesStats from "./sections/payment/PaymentSalesStats";
import "../assets/css/payment/PaymentPage.css";

const pageSize = 7;

const PaymentPage = ({ expoId }) => {

  const [payments, setPayments] = useState([]);
  const [page, setPage] = useState(0); // 0부터 시작
  const [totalPages, setTotalPages] = useState(1); // 총 페이지 수

  useEffect(() => {
    const fetchPayments = async () => {
      try {
        const res = await api.get(`/admin/expos/${expoId}/payment`, {
          params: { page, size: pageSize },
        });
        setPayments(res.data.content);
        setTotalPages(res.data.totalPages);
      } catch (err) {
        console.error(err);
      }
    };

    if (expoId) fetchPayments();
  }, [expoId, page]);

  const handleNext = () => {
    if (page + 1 < totalPages) setPage(page + 1);
  };

  const handlePrev = () => {
    if (page > 0) setPage(page - 1);
  };

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
              <div className="overlap-group-3">
                <div className="text-wrapper-36">1</div>
              </div>
              <div className="text-wrapper-37">2</div>
              <div className="text-wrapper-34">3</div>
              <div className="text-wrapper-35">4</div>

              <div className="group-15" onClick={handleNext} style={{ cursor: page + 1 >= totalPages ? "not-allowed" : "pointer" }}>
                <div className="text-wrapper-32">Next</div>
                <img
                  className="vector-2"
                  alt="Vector"
                  src="https://c.animaapp.com/mdwrr278Hhu1fG/img/vector-2.svg"
                />
              </div>

              <div className="group-16" onClick={handlePrev} style={{ cursor: page === 0 ? "not-allowed" : "pointer" }}>
                <div className="text-wrapper-33">Previous</div>
                <img
                  className="vector-3"
                  alt="Vector"
                  src="https://c.animaapp.com/mdwrr278Hhu1fG/img/vector-2-1.svg"
                />
              </div>
            </div>

            {expoId && (
              <>
                <SalesStats expoId={expoId} />
                <Group expoId={expoId} />
                <Element payments={payments} page={page} pageSize={pageSize}/>
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default PaymentPage;
