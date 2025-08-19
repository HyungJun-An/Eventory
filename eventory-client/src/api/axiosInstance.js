import axios from "axios";
import qs from "qs";

/**
 * 공통 Axios 인스턴스
 * - baseURL: /api(prod) / http://localhost:8090/api (dev)
 * - withCredentials: true (서버가 HttpOnly RefreshToken 쿠키를 쓰는 전제)
 * - 요청시 AccessToken 자동 부착 (localStorage)
 * - 401 발생 시 RefreshToken으로 AccessToken 1회 자동 재발급 후 재시도
 * - 동시다발 401에 대한 단일 재발급 보장(멀티탭/멀티요청 race 방지)
 * - 로그인/회원가입/리프레시 호출은 토큰 주입/재귀 방지 예외 처리
 */

const mode = import.meta.env.VITE_MODE;

const api = axios.create({
    baseURL: mode === "prod"
        ? "https://eventory.kro.kr:8080/api"
        //    : "/api",
        : "http://localhost:8080/api",   // ← dev도 백엔드로 직통
    withCredentials: true,
    paramsSerializer: (params) => qs.stringify(params, { arrayFormat: "repeat" }),
});

// 토큰을 붙이지 않을 경로 (정확한 path 기준)
const SKIP_AUTH_PATHS = new Set([
    "/auth/login",
    "/admin/login",
    "/admin/sys/login",
    "/auth/signup",
    "/auth/refresh",
    "/user/expos", // 비로그인 공개 API 예시 (목록)
]);

// 로그아웃 경로 모음 (항상 인증 헤더 부착 대상)
const LOGOUT_PATHS = new Set([
    "/auth/logout",
    "/admin/logout",
    "/admin/sys/logout",
]);

// prefix-only로 스킵해야 하는 경우 (예: /user/expos/{id})
const SKIP_AUTH_PREFIXES = ["/user/expos/"];

function extractPurePath(url = "") {
    try {
        // 절대/상대 혼재 가능 → URL 인스턴스 실패 시 fallback
        // /^\/api/  -> baseURL 제거 대비
        const pure = new URL(url, window.location.origin).pathname.replace(/^\/api/, "");
        return pure || "/";
    } catch {
        return url; // 파싱 실패 시 원본문자열 반환
    }
}

function shouldSkipAuth(url = "") {
    const purePath = extractPurePath(url);
    if (LOGOUT_PATHS.has(purePath)) return false; // 로그아웃은 절대 스킵하지 않음
    if (SKIP_AUTH_PATHS.has(purePath)) return true;
    return SKIP_AUTH_PREFIXES.some((p) => purePath.startsWith(p));
}

// ----- 요청 인터셉터: AccessToken 주입 ---------------------------------
api.interceptors.request.use(
    (config) => {
        const url = config.url || "";
        if (!shouldSkipAuth(url)) {
            const token = localStorage.getItem("accessToken");
            if (token) {
                config.headers = config.headers || {};
                config.headers.Authorization = `Bearer ${token}`; // 로그아웃 포함 보호 API에 토큰 부착
            }
        } else {
            // 로그인/회원가입/리프레시 등은 토큰 미부착
        }
        return config;
    },
    (error) => Promise.reject(error)
);

// ----- 재발급 제어: 동시 호출 단일화 -------------------------------------
let refreshPromise = null; // 진행 중인 재발급 Promise (있으면 공유)

async function refreshAccessTokenOnce() {
    if (!refreshPromise) {
        // 전역 axios 로 직접 호출 → 우리 인스턴스 인터셉터 영향 배제
        refreshPromise = axios
            .post("/api/auth/refresh", null, { withCredentials: true })
            .then((res) => {
                const newToken = res?.data?.accessToken;
                if (!newToken) throw new Error("accessToken 누락");
                localStorage.setItem("accessToken", newToken);
                const newRt = res?.data?.refreshToken; // 리프레시 토큰 로테이션 시 갱신 저장
                if (newRt) localStorage.setItem("refreshToken", newRt);
                return newToken;
            })
            .catch((err) => {
                // 재발급 실패 → 토큰 정리 및 로그인 페이지로 보내는게 안전함
                localStorage.removeItem("accessToken");
                return Promise.reject(err);
            })
            .finally(() => {
                // 다음 재발급을 위해 해제
                refreshPromise = null;
            });
    }
    return refreshPromise;
}

// ----- 응답 인터셉터: 401 처리 & 재시도 ----------------------------------
api.interceptors.response.use(
    (res) => res,
    async (error) => {
        const { response, config } = error || {};
        const status = response?.status;

        // 네트워크 에러 등
        if (!response) return Promise.reject(error);

        // 재귀 방지 플래그
        const original = config || {};
        const purePath = extractPurePath(original.url);
        const isLogout = LOGOUT_PATHS.has(purePath);

        // 로그아웃 요청은 재발급/재시도 금지 (여기서 그냥 종료)
        if (isLogout) return Promise.reject(error);

        // 401 → 한 번만 재발급 후 재시도
        if (status === 401 && !original._retry && !shouldSkipAuth(original.url)) {
            original._retry = true; // 같은 요청에서 딱 한 번만 재시도
            try {
                const newToken = await refreshAccessTokenOnce();
                // 새 토큰으로 Authorization 갱신 후 재시도
                original.headers = original.headers || {};
                original.headers.Authorization = `Bearer ${newToken}`; // 재시도에도 최신 토큰 부착
                return api(original);
            } catch (e) {
                // 재발급 실패 → 전역 로그아웃 UX 권장
                // 여기서 라우터 접근 가능하면 로그인 페이지로 이동
                if (window?.location) {
                    const current = window.location.pathname + window.location.search;
                    const redirect = encodeURIComponent(current);
                    window.location.replace(`/login?reason=expired&redirect=${redirect}`);
                }
                return Promise.reject(e);
            }
        }
        // 403 등 공통 에러 로깅 (서버 표준 에러 스키마에 맞춰 메시지 추출 권장)
        //    if (status === 403) {
        //      console.warn("[API FORBIDDEN]", response.data);
        //    }
        return Promise.reject(error);
    }
);

export default api;