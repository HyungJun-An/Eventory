import React, { useState } from "react";
import api from "../../api/axiosInstance";
import WebsiteLogos from "../../components/WebsiteLogos";
import "../../assets/css/Header.css";

const Header = ({ expoId, setExpoId }) => {
  const [expos, setExpos] = useState([]);
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);

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
          <div className="overlap-group-wrapper">
            <div className="overlap-group">
              <div className="text-wrapper-8" style={{ cursor: "pointer" }} onClick={handleLogout}>로그아웃</div>
            </div>
          </div>

          <div className="overlap-wrapper">
            <div className="overlap-2">
              <span className="text-wrapper-9">박람회 선택</span>
              <img
                className="vector"
                alt="Vector"
                src="https://c.animaapp.com/mdwrr278Hhu1fG/img/vector.svg"
                onClick={fetchExpos}
                style={{ cursor: "pointer", position: "relative", zIndex: 9999 }}
              />
              {isDropdownOpen && (
              <select className="expo-select" onChange={handleChange} value={expoId || ""}>
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