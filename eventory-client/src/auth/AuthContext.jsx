/* 전역 로그인 상태가 반응형이 아니어서 AuthContext를 추가해서
   로그인/로그아웃 시 전역 상태를 갱신하고, Nav가 그 상태를 구독하도록 구성 */

import React, { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { ALL_TOKEN_KEYS, clearAllTokens, currentTarget, getAccessToken, saveTokens } from './tokenKeys';

const AuthCtx = createContext(null);
const decodeRole = (t) => { try { return JSON.parse(atob(t.split('.')[1]))?.role || null; } catch { return null; } };

/** 현재 로그인 대상(역할)의 토큰 기준으로 상태를 계산 (기존: 항상 참관객 키 accessToken 만 봄) */
const readState = () => {
  const token = getAccessToken() || '';
  return { isAuthed: !!token, role: decodeRole(token), loginTarget: currentTarget() };
};

export function AuthProvider({ children }) {
  const [state, setState] = useState(readState);
  const sync = useCallback(() => setState(readState()), []);

  // 스토리지/커스텀 이벤트로 다른 컴포넌트 갱신 유도 (멀티탭 포함)
  useEffect(() => {
    const onStorage = (e) => {
      if (e.key === null || e.key === 'loginTarget' || ALL_TOKEN_KEYS.includes(e.key)) sync();
    };
    window.addEventListener('storage', onStorage);
    window.addEventListener('auth-changed', sync);
    return () => {
      window.removeEventListener('storage', onStorage);
      window.removeEventListener('auth-changed', sync);
    };
  }, [sync]);

  /** 로그인 성공 — 역할에 맞는 키에 토큰 저장 (axiosInstance 가 같은 키에서 읽는다) */
  const login = useCallback(({ accessToken, refreshToken, target }) => {
    saveTokens(target, { accessToken, refreshToken });
    sync();
    window.dispatchEvent(new Event('auth-changed'));
  }, [sync]);

  const logoutLocal = useCallback(() => {
    clearAllTokens();
    sync();
    window.dispatchEvent(new Event('auth-changed'));
  }, [sync]);

  const value = useMemo(() => ({ ...state, login, logoutLocal }), [state, login, logoutLocal]);
  return <AuthCtx.Provider value={value}>{children}</AuthCtx.Provider>;
}

export const useAuth = () => useContext(AuthCtx);
