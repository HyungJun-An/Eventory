import React, { useEffect, useState } from 'react';
import api from "../../../api/axiosInstance";
import "../../../assets/css/Refund/RefundSalesStats.css";

const RefundSalesStats = ({ expoId }) => {
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
    <div className="refund-sales-stats">
      <div className="group-18">
        <div className="group-19">
          <div className="group-20">
            <div className="text-wrapper-30">누적 매출</div>
            <div className="text-wrapper-31">{formatCurrency(data.paymentTotal)}</div>
          </div>

          <img
            className="group-17"
            alt="Group"
            src="https://c.animaapp.com/mdxxi4aqdC9oEE/img/group-303.png"
          />
        </div>
      </div>

      <div className="group-21">
        <div className="group-22">
          <div className="group-16">
            <div className="text-wrapper-30">총 결제 건수</div>
            <div className="text-wrapper-31">{formatNumber(data.reservationCount)}건</div>
          </div>

          <img
            className="group-17"
            alt="Group"
            src="https://c.animaapp.com/mdxxi4aqdC9oEE/img/group-305.png"
          />
        </div>
      </div>

      <div className="group-14">
        <div className="group-15">
          <div className="group-16">
            <div className="text-wrapper-30">총 환불 건수</div>
            <div className="text-wrapper-29">{formatNumber(data.refundCount)}건</div>
          </div>

          <img
            className="group-17"
            alt="Group"
            src="https://c.animaapp.com/mdxxi4aqdC9oEE/img/group-307.png"
          />
        </div>
      </div>      
    </div>
  );
};

export default RefundSalesStats;