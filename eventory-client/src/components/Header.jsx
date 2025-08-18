import React, { useState, useRef, useEffect } from "react";
import api from "../api/axiosInstance";
import WebsiteLogos from "./WebsiteLogos";
import "../assets/css/Header.css";

const Header = ({ expoId, setExpoId }) => {
  const [expos, setExpos] = useState([]);
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const [isProfileMenuOpen, setIsProfileMenuOpen] = useState(false);
  const profileMenuRef = useRef(null);
  const profileButtonRef = useRef(null);

  const fetchExpos = async () => {
    try {
      const response = await api.get("/admin/expos");
      setExpos(response.data);
      setIsDropdownOpen((prev) => !prev); // 목록 토글
    } catch (error) {
      console.error("박람회 목록 불러오기 실패", error);
    }
  };

  const handleLogout = async () => {
    try {
      await api.post("/auth/logout");
      console.log("로그아웃 성공");
      window.location.href = "/";
    } catch (error) {
      console.error("로그아웃 실패", error);
    }
  };

  const handleChange = (e) => {
    const selectedExpoId = Number(e.target.value);
    setExpoId(selectedExpoId); // 부모로 전달
  };

  // 외부 클릭 시 메뉴 닫기
  useEffect(() => {
    function handleClickOutside(event) {
      if (
        profileMenuRef.current &&
        !profileMenuRef.current.contains(event.target) &&
        profileButtonRef.current &&
        !profileButtonRef.current.contains(event.target)
      ) {
        setIsProfileMenuOpen(false);
      }
    }

    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, []);

  const handleProfileClick = () => {
    setIsProfileMenuOpen(!isProfileMenuOpen);
  };

  const handleMenuItemClick = (action) => {
    console.log(`${action} 클릭됨`); // 실제 구현에서는 각각의 기능을 구현
    setIsProfileMenuOpen(false);
  };

  return (
    <header className="header">
      <div className="overlap">
        <div className="group-2">
          <div className="div-wrapper">
            <div className="text-wrapper-7">대시보드</div>
          </div>

          <div className="logo">
            <WebsiteLogos
              className="website-logos-instance"
              color="primary"
              removebgPreviewClassName="design-component-instance-node"
              removebgPreviewClassNameOverride="website-logos-2"
              size="small"
            />
          </div>
        </div>

        <div className="group-3">

          {/* Profile image with dropdown */}
          <div className="header__profile-container">
            <button
              ref={profileButtonRef}
              onClick={handleProfileClick}
              className="header__profile-button"
            >
              <img
                src="https://api.builder.io/api/v1/image/assets/TEMP/a5ac4ea72b9d86e6309ab90b90ced1fddc46b50b?width=277"
                alt="Profile"
                className="header__profile-image"
              />
            </button>

            {/* Profile Dropdown Menu */}
            {isProfileMenuOpen && (
              <div ref={profileMenuRef} className="header__dropdown-menu">
                <button
                  onClick={() => handleMenuItemClick("관리자정보")}
                  className="header__menu-item"
                >
                  <svg
                    className="header__menu-icon"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth="2"
                      d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"
                    />
                  </svg>
                  관리자정보
                </button>

                <div className="header__menu-divider"></div>

                <button
                  onClick={() => handleMenuItemClick("로그아웃")}
                  className="header__menu-item header__menu-item--logout"
                >
                  <svg
                    className="header__menu-icon"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth="2"
                      d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"
                    />
                  </svg>
                  로그아웃
                </button>
              </div>
            )}
          </div>

          <div className="overlap-wrapper">
            <div className="overlap-2">
              <span className="text-wrapper-9">박람회 선택</span>
              <img
                className="vector"
                alt="Vector"
                src="https://c.animaapp.com/mdwrr278Hhu1fG/img/vector.svg"
                onClick={fetchExpos}
                style={{
                  cursor: "pointer",
                  position: "relative",
                  zIndex: 9999,
                }}
              />
              {isDropdownOpen && (
                <select
                  className="expo-select"
                  onChange={handleChange}
                  value={expoId || ""}
                >
                  <option value="">박람회 선택</option>
                  {expos.map((expo) => (
                    <option key={expo.expoId} value={expo.expoId}>
                      {expo.title}
                    </option>
                  ))}
                </select>
              )}
            </div>
          </div>
        </div>
      </div>

      <div className="rectangle-2" />
    </header>
  );
};

export default Header;
