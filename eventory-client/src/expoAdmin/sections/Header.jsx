import React, { useEffect, useState } from "react";
import api from "../../api/axiosInstance";
import WebsiteLogos from "../../components/WebsiteLogos";
import "../../assets/css/Header.css";

const Header = ({ expoId, setExpoId }) => {
  const [expos, setExpos] = useState([]);

  useEffect(() => {
    const fetchExpos = async () => {
      try {
        const response = await api.get("/expos");
        console.log("응답 데이터:", response.data); 
        /*const response = await api.get("/expos", {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("accessToken")}`,
          },
        });*/
        setExpos(response.data);
      } catch (error) {
        console.error("박람회 목록 불러오기 실패", error);
      }
    };

    fetchExpos();
  }, []);

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
              <div className="text-wrapper-8">로그아웃</div>
            </div>
          </div>

          <div className="overlap-wrapper">
            <div className="overlap-2">
              <img
                className="vector"
                alt="Vector"
                src="https://c.animaapp.com/mdwrr278Hhu1fG/img/vector.svg"
              />
              <select className="text-wrapper-9" onChange={handleChange} value={expoId || ""}>
                <option value="">박람회 선택</option>
                {expos.map((expo) => (
                  <option key={expo.expoId} value={expo.expoId}>
                    {expo.title}
                  </option>
                ))}
              </select>
            </div>
          </div>
        </div>
      </div>

      <div className="rectangle-2" />
    </header>
  );
};

export default Header;