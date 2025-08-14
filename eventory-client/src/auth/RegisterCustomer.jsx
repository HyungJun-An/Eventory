import React, { useState } from "react";
import '../assets/css/Register.css'
import api from '../api/axiosInstance';

const RegisterForm = () => {
  const [form, setForm] = useState({
    customerId: "",
    name: "",
    email: "",
    password: "",
    confirmPassword: "",
    birth: "",
    gender: "",
    phoneNumber: "",
    typeId: 4,
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
    setMsg(); 
  };
  
  function setMsg() {
    const msg = document.getElementById("msg");
    

    if(form.password.length<8) {
      msg.innerText="비밀번호는 최소 8자 이상입니다."  
    }
    if(!form.password===form.confirmPassword) {
      msg.innerText="비밀번호가 다릅니다."
    }
    if(form.password.length>=8) {
      if(form.password===form.confirmPassword) {
        msg.innerText="사용할 수 있는 비밀번호 입니다."
      }
    }
  
  }

  const handleSubmit = async (e) => {
    e.preventDefault();
    console.log(form);
    try {
      const res = await api.post('/auth/signup', form, {
        headers: { 'Content-Type': 'application/json' },
      });
      alert("성공")

    } catch (err) {
      console.error('로그인 실패:', err);
      alert('로그인 실패. 아이디/비밀번호를 확인하거나 잠시 후 다시 시도해주세요.');
    }
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
              <small id="msg">비밀번호는 8글자 이상이어야 합니다.</small>
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
                <label htmlFor="birth">Birth Date</label>
                <div className="input-wrapper">
                  <input type="date" id="birth" name="birth" value={form.birth} onChange={handleChange}/>
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
