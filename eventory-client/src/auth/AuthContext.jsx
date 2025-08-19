/* 전역 로그인 상태가 반응형이 아니어서 AuthContext를 추가해서
   로그인/로그아웃 시 전역 상태를 갱신하고, Nav가 그 상태를 구독하도록 구성 */

import React, { createContext, useContext, useMemo, useState, useEffect } from 'react';

const AuthCtx = createContext(null);
const decodeRole = (t) => { try { return JSON.parse(atob(t.split('.')[1]))?.role || null; } catch { return null; } };
const hasToken = () => !!localStorage.getItem('accessToken');

export function AuthProvider({ children }) {
  const [isAuthed, setAuthed] = useState(hasToken());
  const [role, setRole] = useState(() => decodeRole(localStorage.getItem('accessToken') || ''));
  const [loginTarget, setLoginTarget] = useState(localStorage.getItem('loginTarget') || 'USER');

  // 스토리지/커스텀 이벤트로 다른 컴포넌트 갱신 유도 (멀티탭 포함)
  useEffect(() => {
    const onStorage = (e) => { if (e.key === 'accessToken' || e.key === 'loginTarget') syncFromStorage(); };
    const onCustom = () => syncFromStorage();
    window.addEventListener('storage', onStorage);
    window.addEventListener('auth-changed', onCustom);
    return () => { window.removeEventListener('storage', onStorage); window.removeEventListener('auth-changed', onCustom); };
  }, []);

  const syncFromStorage = () => {
    const token = localStorage.getItem('accessToken') || '';
    setAuthed(!!token);
    setRole(decodeRole(token));
    setLoginTarget(localStorage.getItem('loginTarget') || 'USER');
  };

  const login = ({ accessToken, refreshToken, target }) => {
    if (accessToken) localStorage.setItem('accessToken', accessToken);
    if (refreshToken) localStorage.setItem('refreshToken', refreshToken);
    if (target) localStorage.setItem('loginTarget', target);
    syncFromStorage();
    window.dispatchEvent(new Event('auth-changed'));
  };

  const logoutLocal = () => {
    ['accessToken','refreshToken','loginTarget'].forEach(k=>localStorage.removeItem(k));
    syncFromStorage();
    window.dispatchEvent(new Event('auth-changed'));
  };

  const value = useMemo(() => ({ isAuthed, role, loginTarget, login, logoutLocal }), [isAuthed, role, loginTarget]);
  return <AuthCtx.Provider value={value}>{children}</AuthCtx.Provider>;
}

export const useAuth = () => useContext(AuthCtx);