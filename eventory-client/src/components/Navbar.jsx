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
    <nav className="navbar">
      {/* Header */}
      <div className="header">
        <div className="header-menu">
          <div className="menu-item">Home</div>
          <div className="menu-item">Expo</div>
          <div className="menu-item">Category</div>
        </div>

        <div className="website-logos">
          <img
            className="logo-icon"
            alt="Logo"
            src="https://c.animaapp.com/me2azzmxxO3KsY/img/--------removebg-preview-1-2.png"
          />
          <img
            className="logo-text"
            alt="Logo text"
            src="https://c.animaapp.com/me2azzmxxO3KsY/img/-------removebg-preview-1-2.png"
          />
        </div>

        <div className="header-actions">
          <img
            className="shopping-icon"
            alt="Shopping"
            src="https://c.animaapp.com/me2azzmxxO3KsY/img/money-shopping.svg"
          />

          {/* 로그인 o  로그아웃 버튼 / X 로그인회원가입 버튼 */}
          {isLoggedIn ? (
              <LogoutButton /> //
          ) : (
            <button className="login-register-btn" onClick={() => navigate('/login')}>
                <img
                    className="user-icon"
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