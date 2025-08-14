import React, { useState } from 'react';
import '../assets/css/UserRegistration.css';
import { Link } from 'react-router-dom';

const UserRegistration = () => {


    return (
        <div className="user-registration">
            {/* Main Content */}
            <div className="card-wrapper">
                <div className="registration-card">
                    <div className="registration-form">
                        <h2 className="form-title">Please select a user type</h2>

                        <div className="user-type-options">
                            <button
                                className={`user-type-btn `}
                                onClick={() => window.location.href='/register/company'}
                            >
                                <img
                                    src= '/src/assets/img/peoples-user-business.svg'
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
                                className={`user-type-btn` }
                                onClick={() => window.location.href='/register/customer'}
                            >
                                <img
                                    src="/src/assets/img/base-mail-1.svg"
                                    alt="Customer"
                                    className="user-type-icon"
                                />
                                <span>Customer</span>
                            </button>
                        </div>

                        <div className="login-link">
                          <span className="login-text">Already have an account?</span>
                          <Link to="/login" className="login-link-text">Log In</Link>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default UserRegistration;
