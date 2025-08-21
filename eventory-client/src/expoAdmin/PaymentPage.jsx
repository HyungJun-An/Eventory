import React, { useEffect, useState } from "react";
import api from "../api/axiosInstance";
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

    if (!expoId) return;

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
    fetchPayments();
  }, [expoId, page]);

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

            <button
              onClick={() => page > 0 && setPage(page - 1)}
              disabled={page === 0}
            >
              Previous
            </button>
            <button
              onClick={() => page + 1 < totalPages && setPage(page + 1)}
              disabled={page + 1 >= totalPages}
            >
              Next
            </button>

            {expoId && (
              <>
                <SalesStats expoId={expoId} />
                <Group expoId={expoId} />
                <Element payments={payments} page={page} pageSize={pageSize} />
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default PaymentPage;
