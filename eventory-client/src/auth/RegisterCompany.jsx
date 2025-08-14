import React, { useState } from "react";
import '../assets/css/auth/Register.css'
const RegisterForm = () => {
  const [form, setForm] = useState({
    customerId: "",
    name: "",
    email: "",
    password: "",
    confirmPassword: "",
    birthDate: "",
    gender: "",
    phoneNumber: "",
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    console.log(form);
  };

  return (
    <div className="register">
      <div className="frame">
        <div className="frame-wrapper">
          <form className="div" onSubmit={handleSubmit}>

            {/* Id */}
            <div className="div-2">
              <label>Id</label>
              <div className="input-wrapper">
                <img
                  className="input-icon"
                  alt="User"
                  src="https://c.animaapp.com/me2azzmxxO3KsY/img/peoples-user.svg"
                />
                <input
                  type="text"
                  name="customerId"
                  value={form.customerId}
                  onChange={handleChange}
                  placeholder="Enter your id"
                  className="form-input"
                />
              </div>
            </div>

            {/* Name */}
            <div className="div-2">
              <label>Name</label>
              <div className="input-wrapper">
                <img
                  className="input-icon"
                  alt="Peoples edit name"
                  src="https://c.animaapp.com/me9i7i1uM2B4jc/img/peoples-edit-name.svg"
                />
                <input
                  type="text"
                  name="name"
                  value={form.name}
                  onChange={handleChange}
                  placeholder="Enter your first name"
                  className="form-input"
                />
              </div>
            </div>

            {/* Email */}
            <div className="div-2">
              <label>Email</label>
              <div className="input-wrapper">
                <img
                  className="input-icon"
                  alt="Base mail"
                  src="https://c.animaapp.com/me9i7i1uM2B4jc/img/base-mail-2.svg"
                />
                <input
                  type="email"
                  name="email"
                  value={form.email}
                  onChange={handleChange}
                  placeholder="Enter your email"
                  className="form-input"
                />
              </div>
            </div>

            {/* Password */}
            <div className="div-2">
              <label>Password</label>
              <div className="input-wrapper">
                <img
                  className="design-component-instance-node-2"
                  alt="Base lock"
                  src="https://c.animaapp.com/me9i7i1uM2B4jc/img/base-lock.svg"
                />
                <input
                  type="password"
                  name="password"
                  value={form.password}
                  onChange={handleChange}
                  placeholder="Create a password"
                  className="form-input"
                />
                <img
                  className="design-component-instance-node-3"
                  alt="Base preview close"
                  src="https://c.animaapp.com/me9i7i1uM2B4jc/img/base-preview-close-one.svg"
                />
              </div>
              <small>Must be at least 8 characters.</small>
            </div>

            {/* Confirm Password */}
            <div className="div-2">
              <label>Confirm Password</label>
              <div className="input-wrapper">
                <img
                  className="design-component-instance-node-2"
                  alt="Base lock"
                  src="https://c.animaapp.com/me9i7i1uM2B4jc/img/base-lock.svg"
                />
                <input
                  type="password"
                  name="confirmPassword"
                  value={form.confirmPassword}
                  onChange={handleChange}
                  placeholder="Confirm your password"
                  className="form-input"
                />
                <img
                  className="design-component-instance-node-3"
                  alt="Base preview close"
                  src="https://c.animaapp.com/me9i7i1uM2B4jc/img/base-preview-close-one.svg"
                />
              </div>
            </div>

            <div className="form-group">
              {/* Birth Date */}
              <div className="form-field">
                <label htmlFor="birthDate">Birth Date</label>
                <div className="input-wrapper">

                  <input type="date" id="birthDate" name="birthDate" />
                </div>
              </div>

              {/* Gender */}
              <div className="form-field">
                <label htmlFor="gender">Gender</label>
                <div className="input-wrapper">

                  <select id="gender" name="gender" defaultValue="">
                    <option value="" disabled>Select</option>
                    <option value="male">Male</option>
                    <option value="female">Female</option>
                    <option value="other">Other</option>
                  </select>
                </div>
              </div>
            </div>
            {/* Phone Number */}
            <div className="div-2">
              <label>Phone Number</label>
              <div className="input-wrapper">
                <img
                  className="design-component-instance-node-2"
                  alt="Base phone telephone"
                  src="https://c.animaapp.com/me9i7i1uM2B4jc/img/base-mail.svg"
                />
                <input
                  type="tel"
                  name="phoneNumber"
                  value={form.phoneNumber}
                  onChange={handleChange}
                  placeholder="Enter your phone number"
                  className="form-input"
                />
              </div>
            </div>

            {/* Next Button */}
            <button type="submit" className="buttons">
              <span className="text-2">next</span>
            </button>

            {/* Login Link */}
            <p className="text-wrapper-5">
              Already have an account? <a href="/login">Log In</a>
            </p>
          </form>
        </div>
      </div>
    </div>
  );
};

export default RegisterForm;
