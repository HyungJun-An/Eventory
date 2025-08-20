// src/api/sysExpoAdminApi.js
const API_BASE_URL = "http://localhost:8080/api/sys/admins"; // mock server 또는 실제 API 주소

// 모든 박람회 관리자 리스트 불러오기
export async function getManagers(keyword, page, size) {
  const res = await fetch(`${API_BASE_URL}?keyword=${keyword?keyword:""}&page=${page?page:""}&size=${size?size:""}`, {
      method: "GET"
    });
  if (!res.ok) {
    throw new Error("박람회 관리자 목록을 불러오지 못했습니다.");
  }
  return res.json();
}

// 박람회 관리자별 박람회 리스트 불러오기
export async function getExposByManager(id, page, size) {
  const res = await fetch(`${API_BASE_URL}/${id}/expos?page=${page?page:""}&size=${size?size:""}`, {
    method: "GET"
  });
  if (!res.ok) {
    throw new Error("박람회 목록을 불러오지 못했습니다.");
  }
  return res.json();
}
