import React, { useEffect, useState } from "react";
import "../../../assets/css/content/ContentManagement.css";
import api from "../../../api/axiosInstance";

const ContentManagement = ({ expoId }) => {
  const [content, setContent] = useState(null);

  useEffect(() => {
    if (!expoId) return;

    const fetchContents = async () => {
      try {
        const res = await api.get(`/admin/expos/${expoId}/contents`);
        console.log(res.data);
        setContent(res.data);
      } catch (err) {
        console.error(err);
      }
    };

    fetchContents();
  }, [expoId]);

  if (!content) return <div>로딩 중...</div>;

  return (
    <div className="content-management">
      <div className="div">
        <div className="div-2">
          <div className="text-wrapper">박람회명</div>
          <div className="overlap-group-wrapper">
            <div className="overlap-group">
              <input
                type="text"
                className="text-wrapper-2"
                value={content.title}
                readOnly
              />
            </div>
          </div>
        </div>

        <div className="div-3">
          <div className="text-wrapper">카테고리</div>
          <div className="overlap">
            <input
              type="text"
              className="text-wrapper-2"
              value={
                content.categories && content.categories.length > 0
                  ? content.categories.join(", ")
                  : "카테고리 없음"
              }
              readOnly
            />
            <img
              className="vector"
              alt="Vector"
              src="https://c.animaapp.com/meb0epimTKuUQo/img/vector-4.svg"
            />
          </div>
        </div>
      </div>

      <div className="div-4">
        <div className="text-wrapper">박람회 소개문</div>
        <div className="input-field-default-wrapper">
          <input
            type="text"
            className="input-field-default"
            value={content.description || "소개문 없음"}
            readOnly
          />
        </div>
      </div>

      <div className="div-6">
        <div className="div-7">
          <div className="text-wrapper-8">박람회 이미지</div>
          {content.imgURL ? (
            <img
              src={"/src/assets/expoThumbnail/"+content.imgURL}
              alt="박람회 이미지"
              style={{ width: "300px", borderRadius: "8px" }}
            />
          ) : (
            <div className="input-field-drag-and">이미지 없음</div>
          )}
        </div>

        <div className="div-8">
          <div className="div-2">
            <div className="text-wrapper">박람회 담당자 이름</div>
            <input
              type="text"
              className="text-wrapper-2"
              value={content.expoAdminName || "정보 없음"}
              readOnly
            />
          </div>

          <div className="div-9">
            <div className="text-wrapper">박람회 담당자 이메일</div>
            <input
              type="text"
              className="text-wrapper-2"
              value={content.email || "정보 없음"}
              readOnly
            />
          </div>

          <div className="div-10">
            <div className="text-wrapper">박람회 담당자 전화번호</div>
            <input
              type="text"
              className="text-wrapper-2"
              value={content.phone || "정보 없음"}
              readOnly
            />
          </div>
        </div>
      </div>

      <div className="div-11">
        <div className="div-12">
          <div className="text-wrapper">티켓 가격</div>
          <input
            type="text"
            className="text-wrapper-2"
            value={content.price?.toLocaleString() || 0}
            readOnly
          />
        </div>

        <div className="div-2">
          <div className="text-wrapper">박람회 시작일</div>
          <input
            type="text"
            className="text-wrapper-2"
            value={content.startDate}
            readOnly
          />
        </div>
      </div>

      <div className="group-wrapper-2">
        <div className="div-13">
          <div className="text-wrapper">박람회 종료일</div>
          <input
            type="text"
            className="text-wrapper-2"
            value={content.endDate}
            readOnly
          />
        </div>
      </div>
    </div>
  );
};

export default ContentManagement;
