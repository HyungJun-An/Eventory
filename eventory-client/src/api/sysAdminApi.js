// 시스템관리자 API 래퍼 — 공통 axios(api) 사용: 시스템관리자 토큰 자동 첨부 + 만료 시 재발급
// (기존 sys*Api.js 는 인증 헤더 없이 fetch 로 호출해, 서버가 /api/sys/** 를 공개해 둬야만 동작했다)
import api from "./axiosInstance";

const data = (r) => r.data;

export const getSysMe = () => api.get("/sys/me").then(data);

// 대시보드
export const getSysStats = () => api.get("/sys/stats").then(data);
export const getSysChart = (period) => api.get("/sys/chart", { params: { period } }).then(data);

// 박람회 승인
export const getSysExpos = ({ status, title, page, size }) =>
  api.get("/sys/expos", { params: { status: status || undefined, title: title || undefined, page, size } }).then(data);
export const getSysExpoDetail = (id) => api.get(`/sys/expos/${id}`).then(data);
export const approveSysExpo = (id) => api.put(`/sys/expos/${id}/status`, { status: "APPROVED" }).then(data);
export const rejectSysExpo = (id, reason) => api.put(`/sys/expos/${id}/status`, { status: "REJECTED", reason }).then(data);

// 박람회관리자 계정
export const getExpoAdmins = ({ keyword, page, size }) =>
  api.get("/sys/admins", { params: { keyword: keyword || undefined, page, size } }).then(data);
export const getExpoAdminExpos = (id, { page, size }) => api.get(`/sys/admins/${id}/expos`, { params: { page, size } }).then(data);
export const updateExpoAdmin = (id, body) => api.put(`/sys/admins/${id}`, body);
export const resetExpoAdminPassword = (id) => api.post(`/sys/admins/${id}/password-reset`).then(data);
export const deleteExpoAdmin = (id) => api.delete(`/sys/admins/${id}`);
