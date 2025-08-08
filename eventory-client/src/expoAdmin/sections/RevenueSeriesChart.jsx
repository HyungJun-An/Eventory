import React, { useEffect, useState } from 'react';
import { LineChart, Line, XAxis, YAxis, Tooltip, CartesianGrid, ResponsiveContainer } from 'recharts';
import api from "../../api/axiosInstance";
import "../../assets/css/RevenueSeriesChart.css";

const RevenueSeriesChart = ({ expoId }) => {
  const [yearlyData, setYearlyData] = useState([]);
  const [monthlyData, setMonthlyData] = useState([]);

  useEffect(() => {
    api.get(`/admin/${expoId}/stats?range=yearly`)
      .then((res) => {
        const sorted = res.data.sort((a, b) => a.year - b.year);
        setYearlyData(sorted);
      })
      .catch((err) => console.error("Failed to fetch yearly sales", err));
  }, [expoId]);

  useEffect(() => {
    api.get(`/admin/${expoId}/stats?range=monthly`)
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
        <div className="group-6">
          <div className="group-7">
            <div className="year">
              <div className="text-wrapper-11">2016</div>

              <div className="text-wrapper-12">2017</div>

              <div className="text-wrapper-13">2018</div>

              <div className="text-wrapper-14">2019</div>

              <div className="text-wrapper-15">2020</div>

              <div className="text-wrapper-16">2021</div>
            </div>

            <div className="overlap-group-2">
              <div className="amount">
                <div className="text-wrapper-17">$10,000</div>

                <div className="text-wrapper-18">$20,000</div>

                <div className="text-wrapper-19">$30,000</div>

                <div className="text-wrapper-20">$40,000</div>

                <div className="text-wrapper-21">$0</div>
              </div>

              <div className="line">
                <div className="rectangle-3" />

                <div className="rectangle-4" />

                <div className="rectangle-5" />

                <div className="rectangle-6" />

                <div className="rectangle-7" />
              </div>

              <img
                className="statistics"
                alt="Statistics"
                src="https://c.animaapp.com/mdwp8vqiW7o1mV/img/statistics.png"
              />
            </div>
          </div>
        </div>
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
        <img
          className="group-9"
          alt="Group"
          src="https://c.animaapp.com/mdwp8vqiW7o1mV/img/group-254.png"
        />
      </div>
    </div>
  );
};

export default RevenueSeriesChart;