import axios from "axios";
import qs from "qs";
import { keysOf } from "../auth/tokenKeys";

/**
 * 공통 Axios 인스턴스 (Vite + React, JS 버전)
 * - baseURL: 항상 상대경로 /api (같은 origin으로 요청 → CORS 불필요)
 *   · 개발(npm run dev): vite.config.js 프록시가 백엔드로 전달
 *   · Docker/운영: Nginx(default.conf)가 백엔드로 전달
 * - withCredentials: true (HttpOnly RefreshToken 쿠키 사용 전제)
 * - 요청 시 AccessToken 자동 부착(localStorage)
 * - 401이면 RefreshToken으로 1회 자동 재발급 후 원요청 재시도
 * - 동시 401 단일 재발급 보장 (Promise 직렬화)
 */

export const api = axios.create({
    baseURL: "/api",
    withCredentials: true,
    paramsSerializer: (params) => qs.stringify(params, { arrayFormat: "repeat" }),
});

// 로그인 대상(USER | EXPO_ADMIN | SYSTEM_ADMIN)별 토큰 키 — 로그인 화면·로그아웃과 같은 정의(auth/tokenKeys.js)를 쓴다
function tokenKeys() {
    const keys = keysOf();
    return { atKey: keys.access, rtKey: keys.refresh, refreshUrl: keys.refreshUrl };
}

// ===== 요청 인터셉터 =====
api.interceptors.request.use(
    (config) => {
        const { atKey } = tokenKeys(config.url || "");
        const token = localStorage.getItem(atKey);
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);

// ===== 401 처리: UUID RT로 경로별 재발급 (Promise 직렬화) =====
let refreshPromise = null;
function refreshAccessTokenOnce(baseOnUrl = "") {
    if (!refreshPromise) {
        const { rtKey, atKey, refreshUrl } = tokenKeys(baseOnUrl);
        const rt = localStorage.getItem(rtKey);

        if (!rt) {
            localStorage.removeItem(atKey);
            localStorage.removeItem(rtKey);
            const current = window.location.pathname + window.location.search;
            window.location.replace(`/login?reason=noRefresh&redirect=${encodeURIComponent(current)}`);
            return Promise.reject(new Error("no refresh token"));
        }

        refreshPromise = axios
            .post(refreshUrl, null, {
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${rt}`, // UUID 그대로 전송 (절대 파싱 금지)
                },
                withCredentials: true,
            })
            .then((res) => {
                const at = res?.data?.accessToken;
                if (!at) throw new Error("accessToken 누락");
                localStorage.setItem(atKey, at);

                const newRt = res?.data?.refreshToken;
                if (newRt) localStorage.setItem(rtKey, newRt);

                return at;
            })
            .finally(() => (refreshPromise = null));
    }
    return refreshPromise;
}

// ===== 응답 인터셉터 =====
api.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config;

        if ((error.response?.status === 401 || error.response?.status === 403) && !originalRequest._retry) {
            const current = localStorage.getItem(tokenKeys().atKey);
            const sent = String(originalRequest.headers?.Authorization || "").replace(/^Bearer /, "");

            // 토큰을 실어 보냈는데 지금은 토큰이 없다 → 로그아웃한 뒤 도착한 응답.
            // 재발급을 시도하면 "리프레시 토큰 없음"으로 실패해 페이지를 강제로 새로고침하게 되므로 조용히 실패시킨다
            if (sent && !current) {
                return Promise.reject(error);
            }
            // 그사이 다른 요청이 재발급을 끝냈다 → 재발급을 또 하지 않고 새 토큰으로 한 번만 다시 시도
            if (sent && current && sent !== current) {
                originalRequest._retry = true;
                originalRequest.headers.Authorization = `Bearer ${current}`;
                return api(originalRequest);
            }

            originalRequest._retry = true;

            try {
                const newAt = await refreshAccessTokenOnce(originalRequest.url);
                originalRequest.headers.Authorization = `Bearer ${newAt}`;
                return api(originalRequest);
            } catch (err) {
                console.error("토큰 갱신 실패:", err);
                localStorage.clear();
                window.location.href = "/login?reason=refreshFail";
            }
        }
        return Promise.reject(error);
    }
);

export default api;
