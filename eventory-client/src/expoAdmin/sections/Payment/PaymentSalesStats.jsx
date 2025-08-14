import React, { useEffect, useState } from 'react';
import api from "../../../api/axiosInstance";
import "../../../assets/css/Payment/PaymentSalesStats.css";

 const PaymentSalesStats = ({ expoId }) => {
  const [data, setData] = useState(null);

  useEffect(() => {
    api.get(`/admin/expos/${expoId}/sales`)
      .then((res) => {
        setData(res.data);
      })
      .catch((err) => {
        console.error('Error fetching sales data:', err);
      });
  }, [expoId]);

  if (!data) return <p>Loading...</p>;

  const formatNumber = (num) => num?.toLocaleString();
  const formatCurrency = (num) =>
    new Intl.NumberFormat('ko-KR', {
      style: 'currency',
      currency: 'KRW',
    }).format(num);

  return (
    <div className="payment-sales-stats">
      <div className="group-8">
        <div className="group-9">
          <div className="group-10">
            <div className="text-wrapper-27">누적 매출</div>
            <div className="text-wrapper-28">{formatCurrency(data.paymentTotal)}</div>
          </div>

          <img
            className="group-7"
            alt="Group"
            src="https://c.animaapp.com/mdwrr278Hhu1fG/img/group-303.png"
          />
        </div>
      </div>

      <div className="group-11">
        <div className="group-12">
          <div className="group-6">
            <div className="text-wrapper-27">총 결제 건수</div>
            <div className="text-wrapper-28">{formatNumber(data.reservationCount)}건</div>
          </div>

          <img
            className="group-7"
            alt="Group"
            src="https://c.animaapp.com/mdwrr278Hhu1fG/img/group-305.png"
          />
        </div>
      </div>

      <div className="group-4">
        <div className="group-5">
          <div className="group-6">
            <div className="text-wrapper-27">총 환불 건수</div>
            <div className="text-wrapper-26">{formatNumber(data.refundCount)}건</div>
          </div>

          <img
            className="group-7"
            alt="Group"
            src="https://c.animaapp.com/mdwrr278Hhu1fG/img/group-307.png"
          />
        </div>
      </div>
    </div>
  );
};

export default PaymentSalesStats;