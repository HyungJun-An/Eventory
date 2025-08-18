import React, { useState } from "react";
import '../assets/css/auth/Register.css'
import api from '../api/axiosInstance';

const RegisterForm = () => {
  const [step, setStep] = useState(1); 

  const [form, setForm] = useState({
    customerId: "",
    name: "",
    email: "",
    password: "",
    typeId: 3,
    passVisible: false,
    confirmPassword: "",
    confirmVisible: false,
    birth: "",
    gender: "",
    phone: "",
    companyNameKr: "",
    companyNameEng: "",
    ceoNameKr: "",
    ceoNameEng: "",
    companyAddress: "",
    registrationNum: ""
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
    setMsg();
  };

  const changePassVisible = () => {
    setForm((prev) => ({
      ...prev,
      passVisible: !prev.passVisible,
    }));
  };

  const changePassConfrimVisible = () => {
    setForm((prev) => ({
      ...prev,
      confirmVisible: !prev.confirmVisible
    }));
  };

  function setMsg() {
    const msg = document.getElementById("msg");

    if (form.password.length <= 7) {
      msg.innerText = "비밀번호는 최소 8자 이상입니다."
      return;
    }
    else if (form.password !== form.confirmPassword) {
      msg.innerText = "비밀번호가 다릅니다."
      return;
    }

    if (form.password.length >= 8 && form.confirmPassword.length >= 8 && form.password === form.confirmPassword) {
      msg.innerText = "사용할 수 있는 비밀번호 입니다."
      return;
    }
  }

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const { confirmPassword, ...signupData } = form
      console.log(signupData);
      const res = await api.post('/auth/signup', signupData, {
        headers: { 'Content-Type': 'application/json' },
      });
      alert("회원가입 성공");
      location.href = '/login'

    } catch (err) {
      console.error('회원가입 실패:', err);
      alert('회원가입 실패. 아이디/비밀번호를 확인하거나 잠시 후 다시 시도해주세요.');
    }
  };

  return (
    <div className="register">
      <div className="frame">
        <div className="frame-wrapper">
          <form className="div" onSubmit={handleSubmit}>

            {step === 1 && (
              <>
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
                      placeholder="Enter your name"
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
                      type={form.passVisible ? "text" : "password"}
                      name="password"
                      value={form.password}
                      onChange={handleChange}
                      placeholder="Create a password"
                      className="form-input"
                    />
                    <img
                      className="img"
                      alt="Toggle visibility"
                      src={form.passVisible
                        ? "https://www.svgrepo.com/show/331934/preview.svg"
                        : "https://c.animaapp.com/me9i7i1uM2B4jc/img/base-preview-close-one.svg"}
                      onClick={changePassVisible}
                    />
                  </div>
                  <small id="msg">비밀번호는 최소 8자 이상입니다.</small>
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
                      type={form.confirmVisible ? "text" : "password"}
                      name="confirmPassword"
                      value={form.confirmPassword}
                      onChange={handleChange}
                      placeholder="Confirm your password"
                      className="form-input"
                    />
                    <img
                      className="img"
                      alt="Toggle visibility"
                      src={form.confirmVisible
                        ? "https://www.svgrepo.com/show/331934/preview.svg"
                        : "https://c.animaapp.com/me9i7i1uM2B4jc/img/base-preview-close-one.svg"}
                      onClick={changePassConfrimVisible}
                    />
                  </div>
                </div>

                <div className="form-group">
                  {/* Birth Date */}
                  <div className="form-field">
                    <label htmlFor="birth">Birth Date</label>
                    <div className="input-wrapper">
                      <input type="date" id="birth" name="birth" value={form.birth} onChange={handleChange} />
                    </div>
                  </div>

                  {/* Gender */}
                  <div className="form-field">
                    <label htmlFor="gender">Gender</label>
                    <div className="input-wrapper">
                      <select id="gender" name="gender" value={form.gender} onChange={handleChange}>
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
                    <input
                      type="tel"
                      name="phone"
                      value={form.phone}
                      onChange={handleChange}
                      placeholder="Enter your phone number"
                      className="form-input"
                    />
                  </div>
                </div>

                {/* Next Button */}
                <button type="button" className="buttons" onClick={() => setStep(2)}>
                  <span className="text-2">Next</span>
                </button>
              </>
            )}

            {step === 2 && (
              <>
                {/* Company Name (Korean) */}
                <div className="div-2">
                  <label>Company Name (Korean)</label>
                  <div className="input-wrapper">
                  <img
                      className="input-img"
                      alt = "user-business"
                      src = "https://www.svgrepo.com/show/337506/user-business.svg"
                    />
                    <input
                      type="text"
                      name="companyNameKr"
                      value={form.companyNameKr}
                      onChange={handleChange}
                      placeholder="Enter company name in Korean"
                      className="form-input"
                    />
                  </div>
                </div>

                {/* Company Name (English) */}
                <div className="div-2">
                  <label>Company Name (English)</label>
                  <div className="input-wrapper">
                    <img
                        className="input-img"
                        alt = "user-business"
                        src = "https://www.svgrepo.com/show/337506/user-business.svg"
                    />
                    <input
                      type="text"
                      name="companyNameEng"
                      value={form.companyNameEng}
                      onChange={handleChange}
                      placeholder="Enter company name in English"
                      className="form-input"
                    />
                  </div>
                </div>

                {/* CEO Name (Korean) */}
                <div className="div-2">
                  <label>CEO Name (Korean)</label>
                  <div className="input-wrapper">
                  <img
                        className="input-img"
                        alt = "user-business"
                        src="https://c.animaapp.com/me9i7i1uM2B4jc/img/peoples-edit-name.svg"
                    />
                    <input
                      type="text"
                      name="ceoNameKr"
                      value={form.ceoNameKr}
                      onChange={handleChange}
                      placeholder="Enter CEO name in Korean"
                      className="form-input"
                    />
                  </div>
                </div>

                {/* CEO Name (English) */}
                <div className="div-2">
                  <label>CEO Name (English)</label>
                  <div className="input-wrapper">
                  <img
                        className="input-img"
                        alt = "user-business"
                        src="https://c.animaapp.com/me9i7i1uM2B4jc/img/peoples-edit-name.svg"
                    />
                    <input
                      type="text"
                      name="ceoNameEng"
                      value={form.ceoNameEng}
                      onChange={handleChange}
                      placeholder="Enter CEO name in English"
                      className="form-input"
                    />
                  </div>
                </div>

                {/* Company Address */}
                <div className="div-2">
                  <label>Company Address</label>
                  <div className="input-wrapper">
                  <img
                        className="input-img"
                        alt = "user-business"
                        src="https://www.svgrepo.com/show/488127/company.svg"
                    />
                    <input
                      type="text"
                      name="companyAddress"
                      value={form.companyAddress}
                      onChange={handleChange}
                      placeholder="Enter company address"
                      className="form-input"
                    />
                  </div>
                </div>

                {/* Business Registration Number */}
                <div className="div-2">
                  <label>Business Registration Number</label>
                  <div className="input-wrapper">
                  <img
                        className="input-img"
                        alt = "user-business"
                        src="https://www.svgrepo.com/show/509179/number-list.svg"
                    />
                    <input
                      type="text"
                      name="registrationNum"
                      value={form.registrationNum}
                      onChange={handleChange}
                      placeholder="Enter registration number"
                      className="form-input"
                    />
                  </div>
                </div>

                {/* Previous Button */}
                <button type="button" className="buttons" onClick={() => setStep(1)}>
                  <span className="text-2">Previous</span>
                </button>
                {/* Submit Button */}
                <button type="submit" className="buttons">
                  <span className="text-2">Create an account</span>
                </button>
              </>
            )}

            {/* Login Link (공통) */}
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
