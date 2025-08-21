import React from "react";
import { useState, useEffect } from "react";
import { Dropdown, Button, Space } from "antd";
import { DownOutlined, UserOutlined } from "@ant-design/icons";
import "../assets/css/systemAdmin/systemAdmin.css";
import "../assets/css/systemAdmin/sysDashboard.css";
import totalAmount from "../../public/totalAmount.png";
import SysAdminButton from "../components/SysAdminButton";
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
  BarChart,
  Bar,
} from "recharts";

export const SysDashboard = ({ onClose, id, manager }) => {
  const [chartData, setChartData] = useState([
    {
      date: "2024-12-20",
      name: "Page A",
      uv: 4000,
      pv: 2400,
      amt: 2400,
    },
    {
      date: "2024-12-21",
      // name: "Page A",
      uv: 4000,
      // pv: 2400,
      // amt: 2400,
    },
    {
      date: "2024-12-22",
      name: "Page A",
      uv: 4000,
      pv: 2400,
      amt: 2400,
    },
    {
      date: "2024-12-23",
      name: "Page A",
      uv: 4000,
      pv: 2400,
      amt: 2400,
    },
    {
      date: "2025-01-01",
      name: "Page A",
      // uv: 4000,
    },
    {
      date: "2025-01-02",
      name: "Page B",
      uv: 3000,
      pv: 1398,
      amt: 2210,
    },
    {
      // the data of the first day of each month must exist
      date: "2025-03-01",
      name: "Page D",
      // uv: 2000,
    },
    {
      date: "2025-03-02",
      name: "Page E",
      uv: 2000,
      pv: 9800,
      amt: 2290,
    },
  ]);
  const [chartViewMode, setChartViewMode] = useState("월별");
  const items = [
    {
      label: "월별",
      key: "월별",
    },
    {
      label: "주별",
      key: "주별",
    },
    {
      label: "일별",
      key: "일별",
    },
  ];

  async function fetChChartData() {
    try {
      // Chart Data API
      // setChartData();
    } catch (error) {}
  }

  useEffect(() => {
    fetChChartData();
  }, [chartViewMode]);

  const handleChartModeChange = (e) => {};

  const handleMenuClick = (e) => {
    setChartViewMode(e.key);
  };
  const menuProps = {
    items,
    onClick: handleMenuClick,
  };

  function RevenueChart() {
    return (
      <LineChart
        width={1200}
        height={300}
        data={chartData}
        margin={{
          top: 20,
          right: 30,
          left: 20,
          bottom: 5,
        }}
      >
        <XAxis
          dataKey={"date"}
          axisLine={false}
          tickLine={false}
          tick={{ fontSize: 11 }}
          tickFormatter={(dateString) => {
            const date = new Date(dateString);
            const day = date.getDate();
            console.log(day);

            if (day === 1) {
              return `${date.getFullYear()} ${date.getMonth() + 1}`;
            }
            return "";
          }}
        />
        <YAxis axisLine={false} tickLine={false} tick={{ fontSize: 11 }} />
        <CartesianGrid strokeDasharray="3 3" vertical={false} />

        <Line
          stroke="#007BFF"
          connectNulls
          dataKey="uv"
          strokeWidth={3}
          type="monotone"
          dot={false}
        />
      </LineChart>
    );
  }

  function ReserveChart() {
    return (
      <BarChart
        width={600}
        height={300}
        data={chartData}
        margin={{
          top: 20,
          right: 30,
          left: 20,
          bottom: 5,
        }}
      >
        <XAxis
          dataKey={"date"}
          axisLine={false}
          tickLine={false}
          tick={{ fontSize: 11 }}
          tickFormatter={(dateString) => {
            const date = new Date(dateString);
            const day = date.getDate();
            console.log(day);

            if (day === 1) {
              return `${date.getFullYear()} ${date.getMonth() + 1}`;
            }
            return "";
          }}
        />
        <YAxis axisLine={false} tickLine={false} tick={{ fontSize: 11 }} />
        <CartesianGrid strokeDasharray="3 3" vertical={false} />
        <Bar
          // connectNulls
          fill="#007BFF"
          dataKey="uv"
          strokeWidth={3}
          type="monotone"
          dot={false}
        />
      </BarChart>
    );
  }

  function VisitorChart() {
    return (
      <BarChart
        width={600}
        height={300}
        data={chartData}
        margin={{
          top: 20,
          right: 30,
          left: 20,
          bottom: 5,
        }}
      >
        <XAxis
          dataKey={"date"}
          axisLine={false}
          tickLine={false}
          tick={{ fontSize: 11 }}
          tickFormatter={(dateString) => {
            const date = new Date(dateString);
            const day = date.getDate();
            console.log(day);

            if (day === 1) {
              return `${date.getFullYear()} ${date.getMonth() + 1}`;
            }
            return "";
          }}
        />
        <YAxis axisLine={false} tickLine={false} tick={{ fontSize: 11 }} />
        <CartesianGrid strokeDasharray="3 3" vertical={false} />
        <Bar
          // connectNulls
          fill="#007BFF"
          dataKey="uv"
          strokeWidth={3}
          type="monotone"
          dot={false}
        />
      </BarChart>
    );
  }
  return (
    <>
      <div className="wrapper">
        <div
          style={{ marginTop: "5vh", marginLeft: "3vw", marginRight: "3vw" }}
        >
          <div
            style={{
              display: "grid",
              gridTemplateColumns: "repeat(4, 1fr)",
              gap: "1.5vw",
            }}
          >
            <div className="card">
              <img src={"../../public/totalAmount.png"} />

              <div>
                <div style={{ fontSize: "1.2rem", color: "#718EBF" }}>
                  총 결제금액
                </div>
                <div style={{ fontSize: "1.5rem", fontWeight: "600" }}>
                  150,000
                </div>
              </div>
            </div>
            <div className="card">
              <img src={"../../public/reservationCount.png"} />
              <div>
                <div style={{ fontSize: "1.2rem", color: "#718EBF" }}>
                  누적 예약 수
                </div>
                <div style={{ fontSize: "1.5rem", fontWeight: "600" }}>
                  1,250
                </div>
              </div>
            </div>
            <div className="card">
              <img src={"../../public/visitorCount.png"} />
              <div>
                <div style={{ fontSize: "1.2rem", color: "#718EBF" }}>
                  전체 방문자 수
                </div>
                <div style={{ fontSize: "1.5rem", fontWeight: "600" }}>
                  1,189
                </div>
              </div>
            </div>
            <div className="card">
              <img src={"../../public/newUserCount.png"} />
              <div>
                <div style={{ fontSize: "1.2rem", color: "#718EBF" }}>
                  신규 가입자 수
                </div>
                <div style={{ fontSize: "1.5rem", fontWeight: "600" }}>150</div>
              </div>
            </div>
          </div>
          {/* 버튼 3개 */}
          <div
            style={{
              display: "flex",
              flexDirection: "row",
              justifyContent: "flex-end",
              marginTop: "3vh",
              gap: "1vw",
            }}
          >
            <div>
              <Dropdown menu={menuProps}>
                <Button
                  style={{
                    padding: "1.4rem",
                  }}
                >
                  <Space style={{ paddingLeft: "2rem" }}>
                    {chartViewMode}
                    <DownOutlined style={{ paddingLeft: "2rem" }} />
                  </Space>
                </Button>
              </Dropdown>
            </div>
            <div>
              <SysAdminButton
                text={"CSV 다운로드"}
                textColor={"#007BFF"}
                borderColor={"#007BFF"}
                borderRadius="0.4rem"
                padding="0.8rem 2rem"
              ></SysAdminButton>
            </div>
            <div>
              <SysAdminButton
                text={"엑셀 다운로드"}
                textColor={"#28A745"}
                borderColor={"#28A745"}
                borderRadius="0.4rem"
                padding="0.8rem 2rem"
              ></SysAdminButton>
            </div>
          </div>
          <div
            style={{
              justifySelf: "start",
              fontWeight: 700,
              fontSize: "1.3rem",
            }}
          >
            전체 수익 내역
          </div>
          {/* 차트 */}
          <div
            style={{
              backgroundColor: "white",
              marginTop: "1vh",
              borderRadius: "1rem",
            }}
          >
            <RevenueChart></RevenueChart>
          </div>
          <div
            style={{
              display: "flex",
              flexDirection: "row",
              gap: "1vw",
              marginTop: "4vh",
            }}
          >
            <div>
              <div
                style={{
                  fontWeight: 700,
                  fontSize: "1.3rem",
                }}
              >
                예약 현황
              </div>
              <div
                style={{
                  backgroundColor: "white",
                  borderRadius: "1rem",
                  marginTop: "1vh",
                }}
              >
                <ReserveChart></ReserveChart>
              </div>
            </div>
            <div>
              <div
                style={{
                  fontWeight: 700,
                  fontSize: "1.3rem",
                }}
              >
                방문 현황
              </div>
              <div
                style={{
                  backgroundColor: "white",
                  borderRadius: "1rem",
                  marginTop: "1vh",
                }}
              >
                <VisitorChart></VisitorChart>
              </div>
            </div>
          </div>
        </div>
      </div>
    </>
  );
};

export default SysDashboard;
