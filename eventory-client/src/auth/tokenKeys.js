// 로그인 대상(역할)별 토큰 저장 키 — 토큰을 읽고 쓰는 모든 코드는 이 파일만 사용한다.
// 기존: 로그인 화면·AuthContext·axios·로그아웃 버튼이 키를 각자 정의해 서로 달랐고,
//       시스템관리자 토큰이 박람회관리자 키(adminAccessToken)에 저장돼 /api/sys 요청에 토큰이 붙지 않았다.

export const TOKEN_KEYS = {
  USER: {
    access: "accessToken",
    refresh: "refreshToken",
    refreshUrl: "/api/auth/refresh",
    logoutPath: "/auth/logout",
  },
  EXPO_ADMIN: {
    access: "adminAccessToken",
    refresh: "adminRefreshToken",
    refreshUrl: "/api/admin/refresh",
    logoutPath: "/admin/logout",
  },
  SYSTEM_ADMIN: {
    access: "sysAdminAccessToken",
    refresh: "sysAdminRefreshToken",
    refreshUrl: "/api/admin/sys/refresh",
    logoutPath: "/admin/sys/logout",
  },
};

const TARGET_KEY = "loginTarget";

export const ALL_TOKEN_KEYS = Object.values(TOKEN_KEYS).flatMap((k) => [k.access, k.refresh]);

export const currentTarget = () => localStorage.getItem(TARGET_KEY) || "USER";

export const keysOf = (target = currentTarget()) => TOKEN_KEYS[target] ?? TOKEN_KEYS.USER;

export const getAccessToken = (target) => localStorage.getItem(keysOf(target).access);

export const getRefreshToken = (target) => localStorage.getItem(keysOf(target).refresh);

/** 모든 역할의 토큰과 로그인 대상을 지운다 */
export function clearAllTokens() {
  [...ALL_TOKEN_KEYS, TARGET_KEY].forEach((k) => localStorage.removeItem(k));
}

/**
 * 로그인 성공 시 저장. 다른 역할의 토큰이 남아 있으면 요청마다 어떤 토큰을 쓸지 헷갈리므로 먼저 모두 지운다.
 * (한 브라우저에서는 한 역할로만 로그인)
 */
export function saveTokens(target, { accessToken, refreshToken }) {
  const keys = TOKEN_KEYS[target];
  if (!keys) throw new Error(`알 수 없는 로그인 대상: ${target}`);
  clearAllTokens();
  localStorage.setItem(TARGET_KEY, target);
  localStorage.setItem(keys.access, accessToken);
  if (refreshToken) localStorage.setItem(keys.refresh, refreshToken);
}
