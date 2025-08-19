import React, { useState, useEffect } from "react";
import "../assets/css/Navbar.css"
import LogoutButton from './LogoutButton';
import { useAuth } from "../auth/AuthContext";

export default function Navbar() {
  const { isAuthed } = useAuth();

    const go = (path) => (window.location.href = path);

    return (
      <nav className="navbar">
        {/* Header */}
        <div className="header">
          <div className="header-menu">
            <div className="menu-item" onClick={() => go("/")}>Home</div>
            <div className="menu-item" onClick={() => go("/")}>Expo</div>
            <div className="menu-item" onClick={() => go("/")}>Category</div>
          </div>

          <div className="website-logos">
            <img
              className="logo-icon"
              alt="Logo"
              src="https://c.animaapp.com/me2azzmxxO3KsY/img/--------removebg-preview-1-2.png"
              onClick={() => go("/")}
              style={{ cursor: "pointer" }}
            />
            <img
              className="logo-text"
              alt="Logo text"
              src="https://c.animaapp.com/me2azzmxxO3KsY/img/-------removebg-preview-1-2.png"
              onClick={() => go("/")}
              style={{ cursor: "pointer" }}
            />
          </div>

          <div className="header-actions">
            <img
              className="shopping-icon"
              alt="Shopping"
              src="https://c.animaapp.com/me2azzmxxO3KsY/img/money-shopping.svg"
              onClick={() => go("/payment")}
              style={{ cursor: "pointer" }}
            />

            {/* ✅ 로그인 상태에 따라 버튼 분기 (isAuthed로만 제어) */}
            {isAuthed ? (
              <LogoutButton />
            ) : (
              <button className="login-register-btn" onClick={() => go("/login")}>
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