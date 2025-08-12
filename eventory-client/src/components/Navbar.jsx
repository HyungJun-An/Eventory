import React, { useState } from "react";
import "../assets/css/Navbar.css"

export default function Navbar() {
  
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
            <button className="login-register-btn">
              <img
                className="user-icon"
                alt="User"
                src="https://c.animaapp.com/me2azzmxxO3KsY/img/peoples-user.svg"
              />
              Login/Register
            </button>
          </div>
        </div>
    </nav>
    );
  };