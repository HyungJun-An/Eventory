import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import "../assets/css/auth/LoginPage.css";
import api from '../api/axiosInstance';
import Register from './RegisterCompany';
import { useAuth } from '../auth/AuthContext';

// 로그인 대상(엔드포인트) 맵핑
const LOGIN_TARGETS = {
    USER: "USER",            // 일반 사용자(참관객/참가업체)
    EXPO_ADMIN: "EXPO_ADMIN", // 박람회 관리자
    SYSTEM_ADMIN: "SYSTEM_ADMIN", // 시스템 관리자
};

const endpointMap = {
    USER: "/auth/login",
    EXPO_ADMIN: "/admin/login",
    SYSTEM_ADMIN: "/admin/sys/login",
};

// 로그인 후 이동 경로
const redirectMap = {
    USER: "/",
    EXPO_ADMIN: "/admin/dashboard",
    SYSTEM_ADMIN: "/sys/expos",
};

export default function LoginPage() {
    const navigate = useNavigate();
    const { login } = useAuth(); // 성공 시 전역 상태 갱신 → Nav 즉시 변경

    const [loginTarget, setLoginTarget] = useState(LOGIN_TARGETS.USER);
    const [loading, setLoading] = useState(false);
    const [formData, setFormData] = useState({
        customerId: "",
        password: "",
        rememberMe: false,
    });

    const handleInputChange = (e) => {
        const { name, value, type, checked } = e.target;
        setFormData((prev) => ({
            ...prev,
            [name]: type === "checkbox" ? checked : value,
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (loading) return;
        setLoading(true);
        try {
            const { rememberMe, ...loginData } = formData;
            const endpoint = endpointMap[loginTarget];

            const res = await api.post(endpoint, loginData, {
                headers: { "Content-Type": "application/json" },
                withCredentials: true,
            });

            // 성공 시 AuthContext.login() 호출 → Nav 즉시 반응
            const accessToken = res?.data?.accessToken;
            const refreshToken = res?.data?.refreshToken || null;
            if (!accessToken) throw new Error("토큰이 없습니다");

            // ⭐ 관리자/일반 구분해서 저장 키 다르게
            if (loginTarget === "EXPO_ADMIN" || loginTarget === "SYSTEM_ADMIN") {
            localStorage.setItem("adminAccessToken", accessToken);
            if (refreshToken) localStorage.setItem("adminRefreshToken", refreshToken);
            } else {
            localStorage.setItem("accessToken", accessToken);
            if (refreshToken) localStorage.setItem("refreshToken", refreshToken);
            }

            // 전역 상태 갱신
            login({ accessToken, refreshToken, target: loginTarget });

            // 대상별 리다이렉트 (기존 경로 사용)
            const to = redirectMap[loginTarget] || "/";
            navigate(to, { replace: true });
        } catch (err) {
            const serverMsg = err?.response?.data?.message || err?.message || "로그인에 실패했습니다";
            console.error("로그인 실패:", err);
            alert(serverMsg);
        } finally {
            setLoading(false);
        }
    };

    const isDisabled = !formData.customerId || !formData.password || loading;

    return (
        <div className="login-page">
            {/* 로그인 카드  */}
            <div className="login-section">
                <div className="login-target-tabs center-tabs">
                    <button
                        type="button"
                        className={`tab-btn ${loginTarget === LOGIN_TARGETS.USER ? "active" : ""}`}
                        onClick={() => setLoginTarget(LOGIN_TARGETS.USER)}
                    >
                        일반 사용자
                    </button>
                    <button
                        type="button"
                        className={`tab-btn ${loginTarget === LOGIN_TARGETS.EXPO_ADMIN ? "active" : ""}`}
                        onClick={() => setLoginTarget(LOGIN_TARGETS.EXPO_ADMIN)}
                    >
                        박람회 관리자
                    </button>
                    <button
                        type="button"
                        className={`tab-btn ${loginTarget === LOGIN_TARGETS.SYSTEM_ADMIN ? "active" : ""}`}
                        onClick={() => setLoginTarget(LOGIN_TARGETS.SYSTEM_ADMIN)}
                    >
                        시스템 관리자
                    </button>
                </div>
                <form className="login-form" onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label className="form-label">Id</label>
                        <div className="input-wrapper">
                            <img className="input-icon" alt="User" src="https://c.animaapp.com/me2azzmxxO3KsY/img/peoples-user.svg" />
                            <input
                                type="text"
                                name="customerId"
                                placeholder="Enter your id"
                                value={formData.customerId}
                                onChange={handleInputChange}
                                className="form-input"
                                autoComplete="username"
                            />
                            <img className="input-icon-right" alt="Close" src="https://c.animaapp.com/me2azzmxxO3KsY/img/character-close-small.svg" />
                        </div>
                    </div>

                    <div className="form-group">
                        <label className="form-label">Password</label>
                        <div className="input-wrapper">
                            <img className="input-icon" alt="Lock" src="https://c.animaapp.com/me2azzmxxO3KsY/img/base-lock.svg" />
                            <input
                                type="password"
                                name="password"
                                placeholder="Enter your password"
                                value={formData.password}
                                onChange={handleInputChange}
                                className="form-input"
                                autoComplete="current-password"
                            />
                            <img className="input-icon-right" alt="Show" src="https://c.animaapp.com/me2azzmxxO3KsY/img/arrows-right-small-1.svg" />
                        </div>
                    </div>

                    <div className="form-options">
                        <label className="remember-me">
                            <input
                                type="checkbox"
                                name="rememberMe"
                                checked={formData.rememberMe}
                                onChange={handleInputChange}
                            />
                            <span className="checkbox-custom"></span>
                            Remember me
                        </label>
                        <a href="#" className="forgot-password">Forgot Password</a>
                    </div>

                    <div className="form-buttons">
                        <button type="submit" className="login-btn" disabled={isDisabled}>
                            {loading ? "Signing in..." : "Log in"}
                        </button>
                    </div>

                    {loginTarget === LOGIN_TARGETS.USER && (
                        <p className="signup-link">
                            <span className="signup-text">Don't have an account? </span>
                            <Link to="/signup" className="signup-link-text">Sign Up</Link>
                        </p>
                    )}

                    {/* 현재 호출 엔드포인트 표시 (UI 영향 없음) */}
                    {/* <div className="endpoint-hint">
                        <small>POST {endpointMap[loginTarget]}</small>
                    </div> */}
                </form>
            </div>

            {/* 우측 환영 섹션 — 기존 클래스 유지 */}
            <div className="welcome-section">
                <div className="welcome-overlay">
                    <div className="welcome-content">
                        <h1 className="welcome-title">Welcome Back</h1>
                        <p className="welcome-subtitle">Access your account by logging in</p>
                    </div>
                </div>
            </div>
        </div>
    );
};

// export default LoginPage;