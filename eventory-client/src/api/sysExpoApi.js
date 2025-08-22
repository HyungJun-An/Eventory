// src/api/sysExpoApi.js
const API_BASE_URL = "/api/sys/expos"; // mock server 또는 실제 API 주소

// 모든 박람회 리스트 불러오기
export async function getExpos(status, title, page, size) {
  const res = await fetch(`${API_BASE_URL}?status=${status?status:""}&title=${title?title:""}&page=${page?page:""}&size=${size?size:""}`, {
      method: "GET"
    });
  if (!res.ok) {
    throw new Error("박람회 목록을 불러오지 못했습니다.");
  }
  return res.json();
}

// 특정 박람회 승인
export async function approveExpo(id) {
  const res = await fetch(`${API_BASE_URL}/${id}/status`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ status: "APPROVED" })
  });
  if (!res.ok) {
    throw new Error("박람회 승인에 실패했습니다.");
  }
  return res;
}

// 특정 박람회 거절
export async function rejectExpo(id, reason) {
  const res = await fetch(`${API_BASE_URL}/${id}/status`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ status: "REJECTED", reason: reason })
  });
  if (!res.ok) {
    throw new Error("박람회 거절에 실패했습니다.");
  }
  return res;
}
