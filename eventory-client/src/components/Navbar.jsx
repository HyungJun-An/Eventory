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
          <div className="menu-item" onClick={()=> window.location.href='/'}>Home</div>
          <div className="menu-item" onClick={()=> window.location.href='/'}>Expo</div>
          <div className="menu-item" onClick={()=> window.location.href='/'}>Category</div>
        </div>

        <div className="website-logos">
          <img
            className="logo-icon"
            alt="Logo"
            src="https://c.animaapp.com/me2azzmxxO3KsY/img/--------removebg-preview-1-2.png"
            onClick={() => window.location.href = '/'} 
            style={{ cursor: 'pointer' }}
          />
          <img
            className="logo-text"
            alt="Logo text"
            src="https://c.animaapp.com/me2azzmxxO3KsY/img/-------removebg-preview-1-2.png"
            onClick={() => window.location.href = '/'} 
            style={{ cursor: 'pointer' }}
          />
        </div>

        <div className="header-actions">
          <img
            className="shopping-icon"
            alt="Shopping"
            src="https://c.animaapp.com/me2azzmxxO3KsY/img/money-shopping.svg"
            onClick={() => window.location.href = '/payment'} 
            style={{ cursor: 'pointer' }}
          />

          {/* 로그인 o  로그아웃 버튼 / X 로그인회원가입 버튼 */}
          {isLoggedIn ? (
            <LogoutButton /> //
          ) : (
            <button className="login-register-btn" onClick={() => window.location.href= '/login'}>
              <img
                className="user-icon"
                alt="User"
                src="https://c.animaapp.com/me2azzmxxO3KsY/img/peoples-user.svg"
              />
              로그인
            </button>
          )}
        </div>
      </div>
    </nav>
  );
};