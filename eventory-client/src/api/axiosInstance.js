import axios from "axios";
import qs from "qs";

/**
 * 공통 Axios 인스턴스 (Vite + React, JS 버전)
 * - baseURL: /api(prod) | http://localhost:8090/api(dev)
 * - withCredentials: true (HttpOnly RefreshToken 쿠키 사용 전제)
 * - 요청 시 AccessToken 자동 부착(localStorage)
 * - 401이면 RefreshToken으로 1회 자동 재발급 후 원요청 재시도
 * - 동시 401 단일 재발급 보장 (Promise 직렬화)
 */

const mode = import.meta.env.VITE_MODE;

export const api = axios.create({
    baseURL: mode === "prod" ? "https://eventory.kro.kr:8090/api" : "http://localhost:8090/api",
    withCredentials: true,
    paramsSerializer: (params) => qs.stringify(params, { arrayFormat: "repeat" }),
});

// ===== 유틸 =====
const extractPurePath = (url = "") => {
    try {
        const u = new URL(url, window.location.origin);
        return u.pathname.replace(/^\/api/, "") || "/";
    } catch {
        return url;
    }
};

// ExpoAdmin, SysAdmin, 일반 유저를 명확히 분리
function tokenKeys(url = "") {
    const target = localStorage.getItem("loginTarget"); // USER | EXPO_ADMIN | SYSTEM_ADMIN

    if (target === "SYSTEM_ADMIN") {
        return {
            atKey: "sysAdminAccessToken",
            rtKey: "sysAdminRefreshToken",
            refreshUrl: "/api/admin/sys/refresh",
        };
    }

    if (target === "EXPO_ADMIN") {
        return {
            atKey: "adminAccessToken",
            rtKey: "adminRefreshToken",
            refreshUrl: "/api/admin/refresh",
        };
    }

    // 기본 USER
    return {
        atKey: "accessToken",
        rtKey: "refreshToken",
        refreshUrl: "/api/auth/refresh",
    };
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
