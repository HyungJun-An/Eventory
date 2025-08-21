import api from "./axiosInstance";

// 목록 (GET /api/user/booths)
export const findBooths = () => api.get("/user/booths");

// 상세 (GET /api/user/booths/{boothId})
export const findBooth = (boothId) => api.get(`/user/booths/${boothId}`);

// 생성 (POST /api/auth/booth)
export const createBooth = (payload) => api.post("/auth/booth", payload);

// 수정 (PUT /api/user/booths/{boothId})
export const updateBooth = (boothId, payload) =>
  api.put(`/user/booths/${boothId}`, payload);
