// 회사 사용자 프로필 API
import api from "./axiosInstance";

// 조회 (GET /api/auth/profile)
export const findProfile = () => api.get("/auth/profile");

// 수정 (PUT /api/auth/profile)
export const updateProfile = (payload) => api.put("/auth/profile", payload);
