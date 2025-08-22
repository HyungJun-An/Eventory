import React, { useEffect, useState } from 'react';
import { LineChart, Line, XAxis, YAxis, Tooltip, CartesianGrid, ResponsiveContainer } from 'recharts';
import api from "../../../api/axiosInstance";
import "../../../assets/css/revenue/RevenueSeriesChart.css";

const RevenueSeriesChart = ({ expoId }) => {
  const [yearlyData, setYearlyData] = useState([]);
  const [monthlyData, setMonthlyData] = useState([]);

  useEffect(() => {
    api.get(`/admin/expos/${expoId}/stats?range=yearly`)
      .then((res) => {
        const sorted = res.data.sort((a, b) => a.year - b.year);
        setYearlyData(sorted);
      })
      .catch((err) => console.error("Failed to fetch yearly sales", err));
  }, [expoId]);

  useEffect(() => {
    api.get(`/admin/expos/${expoId}/stats?range=monthly`)
      .then((res) => {
        const sorted = res.data.sort((a, b) => a.month - b.month);
        setMonthlyData(sorted);
      })
      .catch((err) => console.error("Failed to fetch monthly sales", err));
  }, [expoId]);

  return (
    <div className="series-chart">
      <div className="group-5">
        <div className="text-wrapper-10">연간 매출</div>

        <ResponsiveContainer>
          <LineChart data={yearlyData}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="year" />
            <YAxis tickFormatter={(amount) => amount.toLocaleString()} />
            <Tooltip formatter={(value) => `${value.toLocaleString()}원`} />
            <Line type="monotone" dataKey="totalAmount" stroke="#82ca9d" />
          </LineChart>
        </ResponsiveContainer>
        
      </div>

      <div className="group-8">
        <div className="text-wrapper-10">월간 매출</div>
        <ResponsiveContainer>
        <LineChart data={monthlyData}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis 
            dataKey="month" 
            tickFormatter={(month) => `${month}월`} 
          />
          <YAxis 
            tickFormatter={(amount) => amount.toLocaleString()} 
          />
          <Tooltip formatter={(value) => `${value.toLocaleString()}원`} />
          <Line type="monotone" dataKey="totalAmount" stroke="#8884d8" />
        </LineChart>
      </ResponsiveContainer>
        
      </div>
    </div>
  );
};

export default RevenueSeriesChart;