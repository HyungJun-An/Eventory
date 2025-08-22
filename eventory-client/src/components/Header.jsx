import React, { useState, useRef, useEffect } from "react";
import { useLocation } from "react-router-dom";
import api from "../api/axiosInstance";
import WebsiteLogos from "./WebsiteLogos";
import "../assets/css/Header.css";
import LogoutButton from "./LogoutButton";

const Header = ({ expoId, setExpoId }) => {
  const location = useLocation();
  const [expos, setExpos] = useState([]);
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const [isProfileMenuOpen, setIsProfileMenuOpen] = useState(false);
  const [isProfileModalOpen, setIsProfileModalOpen] = useState(false);
  const [profileData, setProfileData] = useState(null);
  const profileMenuRef = useRef(null);
  const profileButtonRef = useRef(null);

  const getTitle = () => {
    switch (location.pathname) {
      case "/admin/dashboard":
        return "대시보드";
      case "/admin/reservation":
        return "예약 관리";
      case "/admin/reservation/list":
        return "예약자 명단";
      case "/admin/contents":
        return "콘텐츠 관리";
      case "/admin/booth":
        return "부스 관리";
      case "/admin/sales":
        return "매출 분석";
      case "/admin/payment":
        return "정산 관리";
      case "/admin/refund":
        return "환불 처리";
      case "/sys/expos":
        return "박람회 관리";
      case "/sys/manage":
        return "박람회 관리자 관리";
      case "/sys/dashboard":
        return "대시보드";
      default:
        return "대시보드"; // 기본값
    }
  };

  const fetchExpos = async () => {
    try {
      const response = await api.get("/admin/expos");
      setExpos(response.data);
      setIsDropdownOpen((prev) => !prev); // 목록 토글
    } catch (error) {
      console.error("박람회 목록 불러오기 실패", error);
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

  const handleMenuItemClick = async (action) => {
    if (action === "관리자정보") {
      try {
        const response = await api.get("/admin/profile");
        setProfileData(response.data);
        setIsProfileModalOpen(true);
      } catch (error) {
        console.error("관리자 정보 조회 실패", error);
      }
    }
    setIsProfileMenuOpen(false);
  };

  const closeModal = () => {
    setIsProfileModalOpen(false);
    setProfileData(null);
  };

  return (
    <header className="header">
      <div className="overlap">
        <div className="group-2">
          <div className="div-wrapper">
            <div className="text-wrapper-7">{getTitle()}</div>
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
          <div className="overlap-wrapper">
            <div className="overlap-2">
              {!isDropdownOpen && (
                <>
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
                </>
              )}
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

                <LogoutButton className="header__menu-item header__menu-item--logout">
                  로그아웃
                </LogoutButton>
              </div>
            )}
          </div>
        </div>
      </div>

      <div className="rectangle-2" />
      {/* Profile Modal */}
      {isProfileModalOpen && profileData && (
        <div className="modal-overlay" onClick={closeModal}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h2>관리자 정보 수정</h2>
            <form
              onSubmit={async (e) => {
                e.preventDefault();
                try {
                  await api.put("/admin/profile", {
                    name: profileData.name,
                    email: profileData.email,
                    phone: profileData.phone,
                  });
                  alert("관리자 정보가 수정되었습니다.");
                  setIsProfileModalOpen(false);
                } catch (error) {
                  console.error("관리자 정보 수정 실패", error);
                  alert(
                    "수정 실패: " + error.response?.data?.message ||
                      error.message
                  );
                }
              }}
            >
              <label>
                이름:
                <input
                  type="text"
                  value={profileData.name}
                  onChange={(e) =>
                    setProfileData({ ...profileData, name: e.target.value })
                  }
                  required
                />
              </label>
              <label>
                이메일:
                <input
                  type="email"
                  value={profileData.email}
                  onChange={(e) =>
                    setProfileData({ ...profileData, email: e.target.value })
                  }
                  required
                />
              </label>
              <label>
                전화번호:
                <input
                  type="text"
                  value={profileData.phone}
                  onChange={(e) =>
                    setProfileData({ ...profileData, phone: e.target.value })
                  }
                  required
                />
              </label>

              <div className="modal-buttons">
                <button type="submit">수정</button>
                <button type="button" onClick={closeModal}>
                  닫기
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </header>
  );
};

export default Header;
