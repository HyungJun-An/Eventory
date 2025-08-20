import React, { useEffect, useState } from "react";
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  Tooltip,
  CartesianGrid,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  Legend,
} from "recharts";
import api from "../../../api/axiosInstance";
import "../../../assets/css/revenue/RevenueSalesChart.css";

const RevenueSalesChart = ({ expoId }) => {
  const [dailyData, setDailyData] = useState([]);
  const [salesData, setSalesData] = useState({
    reservationCount: 0,
    refundCount: 0,
  });

  // 일별 매출 데이터 가져오기
  useEffect(() => {
    api
      .get(`/admin/expos/${expoId}/stats?range=daily`)
      .then((res) => {
        const sorted = res.data.sort((a, b) => a.date - b.date);
        setDailyData(sorted);
      })
      .catch((err) => console.error("Failed to fetch daily sales", err));
  }, [expoId]);

  // 총 결제/환불 데이터 가져오기
  useEffect(() => {
    api
      .get(`/admin/expos/${expoId}/sales`)
      .then((res) => {
        setSalesData(res.data);
      })
      .catch((err) => console.error("Failed to fetch sales summary", err));
  }, [expoId]);

  // 도넛 차트 데이터
  const refundChartData = [
    {
      name: "결제 건수",
      value: salesData.reservationCount - salesData.refundCount,
    },
    { name: "환불 건수", value: salesData.refundCount },
  ];

  const COLORS = ["#FFC107", "#FE5C73"];

  return (
    <div className="sales-chart">
      <div className="group-19">
        <div className="text-wrapper-25">환불 비율</div>

        <PieChart width={200} height={200}>
          <Pie
            data={refundChartData}
            cx="50%"
            cy="50%"
            innerRadius={50}
            outerRadius={80}
            paddingAngle={3}
            dataKey="value"
          >
            {refundChartData.map((entry, index) => (
              <Cell key={`cell-${index}`} fill={COLORS[index]} />
            ))}
          </Pie>
          <Tooltip formatter={(value, name) => [`${value}건`, name]} />
          <Legend />
        </PieChart>
        
      </div>

      <div className="group-21">
        <div className="group-22">
          <div className="group-23">
            <ResponsiveContainer>
              <LineChart data={dailyData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="date" tickFormatter={(date) => `${date}월`} />
                <YAxis tickFormatter={(amount) => amount.toLocaleString()} />
                <Tooltip formatter={(value) => `${value.toLocaleString()}원`} />
                <Line type="monotone" dataKey="totalAmount" stroke="#8884d8" />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>

        <div className="text-wrapper-36">주간 매출 추이</div>
      </div>
    </div>
  );
};

export default RevenueSalesChart;
