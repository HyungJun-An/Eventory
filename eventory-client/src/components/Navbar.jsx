import React, { useState, useEffect } from "react";
import "../assets/css/Navbar.css"
import LogoutButton from './LogoutButton';

export default function Navbar() {
    const [isLoggedIn, setIsLoggedIn] = useState(false);

      useEffect(() => {
        // 페이지 로드시 토큰 존재 여부 확인
        const token = localStorage.getItem('accessToken');
        setIsLoggedIn(!!token);
      }, []);

  return (
    <nav className="navbar-navbar">
      {/* Header */}
      <div className="navbar-header">
        <div className="navbar-header-menu">
          <div className="navbar-menu-item">Home</div>
          <div className="navbar-menu-item">Expo</div>
          <div className="navbar-menu-item">Category</div>
        </div>

        <div className="navbar-website-logos">
          <img
            className="navbar-logo-icon"
            alt="Logo"
            src="https://c.animaapp.com/me2azzmxxO3KsY/img/--------removebg-preview-1-2.png"
          />
          <img
            className="navbar-logo-text"
            alt="Logo text"
            src="https://c.animaapp.com/me2azzmxxO3KsY/img/-------removebg-preview-1-2.png"
          />
        </div>

        <div className="navbar-header-actions">
          <img
            className="navbar-shopping-icon"
            alt="Shopping"
            src="https://c.animaapp.com/me2azzmxxO3KsY/img/money-shopping.svg"
          />

          {/* 로그인 o  로그아웃 버튼 / X 로그인회원가입 버튼 */}
          {isLoggedIn ? (
              <LogoutButton /> //
          ) : (
            <button className="navbar-login-register-btn" onClick={() => navigate('/login')}>
                <img
                    className="navbar-user-icon"
                    alt="User"
                    src="https://c.animaapp.com/me2azzmxxO3KsY/img/peoples-user.svg"
                />
                Login/Register
            </button>
          )}
        </div>
      </div>
    </nav>
  );
};