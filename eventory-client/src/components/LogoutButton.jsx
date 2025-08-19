import React from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axiosInstance';
import { useAuth } from '../auth/AuthContext';

// loginTarget이 저장 안되어 있거나 꼬였어도 AccessToken의 role을 읽어서 맞는 로그아웃 엔드포인트로 보냄
const mapByTarget = { USER: '/auth/logout', EXPO_ADMIN: '/admin/logout', SYSTEM_ADMIN: '/admin/sys/logout' };
const mapByRole = { ROLE_EXPO_ADMIN: '/admin/logout', ROLE_SYSTEM_ADMIN: '/admin/sys/logout' };
const parseJwtRole = t => { try { return JSON.parse(atob(t.split('.')[1]))?.role || null; } catch { return null; } };
const clear = () => ['accessToken', 'refreshToken', 'loginTarget'].forEach(k => localStorage.removeItem(k));

const LogoutButton = () => {
    const navigate = useNavigate();
    const { logoutLocal } = useAuth();

    const handleLogout = async () => {
        try {
            // 1) loginTarget 우선, 2) 없으면 JWT role로 추론
            const target = localStorage.getItem('loginTarget');
            const token = localStorage.getItem('accessToken');
            const role = token ? parseJwtRole(token) : null;
            const endpoint = target ? (mapByTarget[target] || '/auth/logout')
                : (mapByRole[role] || '/auth/logout');

            // rt 가드: 없으면 세션 정리 후 로그인 페이지로 유도
            const rt = localStorage.getItem('refreshToken');
            if (!rt) {
                logoutLocal();
                navigate('/login', { replace: true });
                return;
            }

            await api.post(endpoint, { refreshToken: rt }, {
                withCredentials: true, headers: { 'Content-Type': 'application/json' }
            });

        } catch (err) {
            const st = err?.response?.status;
            if (st === 401 || st === 403) {
                // 조용히 처리: 토큰 정리 후 이동 (알림 없음)
            } else {
                console.error('로그아웃 실패:', err);
                alert(err?.response?.data?.message || err.message || '로그아웃 처리 중 오류가 발생했음.');
            }
        } finally {
            //  성공/실패 무관 항상 정리
            logoutLocal(); // 내부에서 accessToken/refreshToken/loginTarget 제거
            navigate('/login', { replace: true });
        };
    };
    return (
        <button className="nav-logout-btn" onClick={handleLogout}>
            로그아웃
        </button>
    );
}

export default LogoutButton;