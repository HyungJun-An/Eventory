import React from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axiosInstance';
import { useAuth } from '../auth/AuthContext';

// loginTarget이 저장 안되어 있거나 꼬였어도 AccessToken의 role을 읽어서 맞는 로그아웃 엔드포인트로 보냄
const mapByTarget = { USER: '/auth/logout', EXPO_ADMIN: '/admin/logout', SYSTEM_ADMIN: '/admin/sys/logout' };
const mapByRole = { ROLE_EXPO_ADMIN: '/admin/logout', ROLE_SYSTEM_ADMIN: '/admin/sys/logout' };
const parseJwtRole = t => { try { return JSON.parse(atob(t.split('.')[1]))?.role || null; } catch { return null; } };

const tokenKeyMap = {
    USER: { at: "accessToken", rt: "refreshToken" },
    EXPO_ADMIN: { at: "adminAccessToken", rt: "adminRefreshToken" },
    SYSTEM_ADMIN: { at: "adminAccessToken", rt: "adminRefreshToken" },
};

const LogoutButton = () => {
    const navigate = useNavigate();
    const { logoutLocal } = useAuth();

    const handleLogout = async () => {
        try {
            const target = localStorage.getItem("loginTarget");
            const { at, rt } = tokenKeyMap[target] || tokenKeyMap.USER;

            const token = localStorage.getItem(at);
            const refresh = localStorage.getItem(rt);

            // ⭐ loginTarget 꼬였을 경우 role 로 추론
            const role = token ? parseJwtRole(token) : null;
            const endpoint = target
                ? (mapByTarget[target] || "/auth/logout")
                : (mapByRole[role] || "/auth/logout");

            if (!refresh) {
                logoutLocal();
                navigate("/login", { replace: true });
                return;
            }

            await api.post(endpoint, { refreshToken: refresh }, {
                withCredentials: true,
                headers: { "Content-Type": "application/json" }
            });

        } catch (err) {
            const st = err?.response?.status;
            if (st !== 401 && st !== 403) {
                alert(err?.response?.data?.message || err.message || "로그아웃 처리 중 오류가 발생했음.");
            }
        } finally {
            logoutLocal();
            navigate("/login", { replace: true });
        }
    };

    return (
        <button className="nav-logout-btn" onClick={handleLogout}>
            로그아웃
        </button>
    );
}

export default LogoutButton;