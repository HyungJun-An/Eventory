import React from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axiosInstance';
import { useAuth } from '../auth/AuthContext';
import { getRefreshToken, keysOf } from '../auth/tokenKeys';

/** 현재 로그인 대상(역할)의 로그아웃 API 호출 후 모든 역할의 토큰을 지운다 */
const LogoutButton = () => {
    const navigate = useNavigate();
    const { logoutLocal } = useAuth();

    const handleLogout = async () => {
        const { access, logoutPath } = keysOf();
        const token = localStorage.getItem(access);
        try {
            if (token) {
                // 서버: AccessToken 블랙리스트 등록 + RefreshToken 삭제
                await api.post(logoutPath, { refreshToken: getRefreshToken() }, {
                    headers: { "Content-Type": "application/json", Authorization: `Bearer ${token}` },
                });
            }
        } catch (err) {
            const st = err?.response?.status;
            if (st !== 401 && st !== 403) {
                alert(err?.response?.data?.message || err.message || "로그아웃 처리 중 오류가 발생했습니다.");
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
};

export default LogoutButton;
