import React, { useState } from 'react';
import '../assets/css/UserRegistration.css';

const UserRegistration = () => {
  const [selectedUserType, setSelectedUserType] = useState(null);

  const handleUserTypeSelect = (type) => {
    setSelectedUserType(type);
    // Handle user type selection logic here
    console.log('Selected user type:', type);
  };

  return (
    <div className="user-registration">
      {/* Header */}
      <header className="header">
        <nav className="nav-menu">
          <span className="nav-item active">Home</span>
          <span className="nav-item">Expo</span>
          <span className="nav-item">Category</span>
        </nav>
        
        <div className="logo-section">
          <img 
            src="https://c.animaapp.com/me6rzqglc3Fy3X/img/--------removebg-preview-1-2.png" 
            alt="Logo" 
            className="logo-icon"
          />
          <img 
            src="https://c.animaapp.com/me6rzqglc3Fy3X/img/-------removebg-preview-1-2.png" 
            alt="Logo Text" 
            className="logo-text"
          />
        </div>

        <div className="header-actions">
          <img 
            src="https://c.animaapp.com/me6rzqglc3Fy3X/img/money-shopping.svg" 
            alt="Shopping" 
            className="shopping-icon"
          />
          <button className="login-btn">
            <img 
              src="https://c.animaapp.com/me6rzqglc3Fy3X/img/peoples-user.svg" 
              alt="User" 
              className="user-icon"
            />
            Login/Register
          </button>
        </div>
      </header>

      {/* Main Content */}
      <main className="main-content">
        <div className="registration-card">
          <div className="registration-form">
            <h2 className="form-title">Please select a user type</h2>
            
            <div className="user-type-options">
              <button 
                className={`user-type-btn ${selectedUserType === 'company' ? 'selected' : ''}`}
                onClick={() => handleUserTypeSelect('company')}
              >
                <img 
                  src="https://c.animaapp.com/me6rzqglc3Fy3X/img/peoples-user-business.svg" 
                  alt="Company" 
                  className="user-type-icon"
                />
                <span>Company</span>
              </button>

              <div className="divider">
                <div className="divider-line"></div>
                <span className="divider-text">or</span>
                <div className="divider-line"></div>
              </div>

              <button 
                className={`user-type-btn ${selectedUserType === 'customer' ? 'selected' : ''}`}
                onClick={() => handleUserTypeSelect('customer')}
              >
                <img 
                  src="https://c.animaapp.com/me6rzqglc3Fy3X/img/base-mail-1.svg" 
                  alt="Customer" 
                  className="user-type-icon"
                />
                <span>Customer</span>
              </button>
            </div>

            <div className="login-link">
              <span className="login-text">Already have an account?</span>
              <a href="#" className="login-link-text">Log In</a>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
};

export default UserRegistration;
