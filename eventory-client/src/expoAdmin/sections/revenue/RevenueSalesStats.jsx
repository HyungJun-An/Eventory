import React, { useEffect, useState } from 'react';
import api from "../../../api/axiosInstance";
import "../../../assets/css/revenue/RevenueSalesStats.css";

const RevenueSalesStats = ({ expoId }) => {
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
    <div className="sales-stats">

      <div className="group-14">
        <div className="group-15">
          <div className="group-16">
            <div className="text-wrapper-23">누적 매출</div>
            <div className="text-wrapper-24">{formatCurrency(data.paymentTotal)}</div>
          </div>

          <img
            className="group-13"
            alt="Group"
            src="https://c.animaapp.com/mdwp8vqiW7o1mV/img/group-303.png"
          />
        </div>
      </div>

      <div className="group-17">
        <div className="group-18">
          <div className="group-12">
            <div className="text-wrapper-23">총 결제 건수</div>
            <div className="text-wrapper-24">{formatNumber(data.reservationCount)}건</div>
          </div>

          <img
            className="group-13"
            alt="Group"
            src="https://c.animaapp.com/mdwp8vqiW7o1mV/img/group-305.png"
          />
        </div>
      </div>

      <div className="group-10">
        <div className="group-11">
          <div className="group-12">
            <div className="text-wrapper-23">총 환불 건수</div>
            <div className="text-wrapper-22">{formatNumber(data.refundCount)}건</div>
          </div>

          <img
            className="group-13"
            alt="Group"
            src="https://c.animaapp.com/mdwp8vqiW7o1mV/img/group-307.png"
          />
        </div>
      </div>      
    </div>
  );
};

export default RevenueSalesStats;