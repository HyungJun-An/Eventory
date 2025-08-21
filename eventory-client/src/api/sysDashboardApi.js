// src/api/sysDashboardApi.js
const API_BASE_URL = "http://localhost:8080/api/sys"; // mock server 또는 실제 API 주소

// 종합 통계 불러오기
export async function getStats() {
  const res = await fetch(`${API_BASE_URL}/stats`, {
      method: "GET"
    });
  if (!res.ok) {
    throw new Error("종합 통계를 불러오지 못했습니다.");
  }
  return res.json();
}

// 차트 데이터 불러오기
export async function getChart(period) {
  const res = await fetch(`${API_BASE_URL}/chart?period=${period?period:"monthly"}`, {
    method: "GET"
  });
  if (!res.ok) {
    throw new Error("차트 데이터를 불러오지 못했습니다.");
  }
  return res.json();
}
