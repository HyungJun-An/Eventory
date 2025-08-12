import React, { useState } from "react";
import "../assets/css/LoginPage.css";
import api from '../api/axiosInstance';
export const LoginPage = () => {
  const [formData, setFormData] = useState({
    customerId: "",
    password: "",
    rememberMe: false
  });

  const handleInputChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const { rememberMe, ...loginData } = formData;
      const res = await api.post('/auth/login', loginData, {
        headers: { 'Content-Type': 'application/json' },
        withCredentials: true,  // 쿠키 (refreshToken) 받으려면 꼭 넣어야 해!!!
      });

      console.log('서버 응답:', res.data);

      // accessToken만 상태에 저장
      const accessToken = res.data.accessToken;
      setAccessToken(accessToken); // React state나 Context에 넣어줘!

    } catch (err) {
      console.error('로그인 실패:', err);
    }
  };






  return (
    <div>
      {/* Main Content */}
      <div className="login-main-content">
        {/* Login Form */}
        <div className="login-section">
          <form className="login-form" onSubmit={handleSubmit}>
            <div className="form-group">
              <label className="form-label">Id</label>
              <div className="input-wrapper">
                <img
                  className="input-icon"
                  alt="User"
                  src="https://c.animaapp.com/me2azzmxxO3KsY/img/peoples-user.svg"
                />
                <input
                  type="text"
                  name="customerId"
                  placeholder="Enter your id"
                  value={formData.customerId}
                  onChange={handleInputChange}
                  className="form-input"
                />
                <img
                  className="input-icon-right"
                  alt="Close"
                  src="https://c.animaapp.com/me2azzmxxO3KsY/img/character-close-small.svg"
                />
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">Password</label>
              <div className="input-wrapper">
                <img
                  className="input-icon"
                  alt="Lock"
                  src="https://c.animaapp.com/me2azzmxxO3KsY/img/base-lock.svg"
                />
                <input
                  type="password"
                  name="password"
                  placeholder="Enter your password"
                  value={formData.password}
                  onChange={handleInputChange}
                  className="form-input"
                />
                <img
                  className="input-icon-right"
                  alt="Show"
                  src="https://c.animaapp.com/me2azzmxxO3KsY/img/arrows-right-small-1.svg"
                />
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
              <button type="submit" className="login-btn">
                Log in
              </button>
            </div>

            <p className="signup-link">
              <span className="signup-text">Don't have an account? </span>
              <a href="#" className="signup-link-text">Sign Up</a>
            </p>
          </form>
        </div>

        {/* Welcome Section */}
        <div className="welcome-section">
          <div className="welcome-overlay">
            <div className="welcome-content">
              <h1 className="welcome-title">Welcome Back</h1>
              <p className="welcome-subtitle">Access your personal account by logging in</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;