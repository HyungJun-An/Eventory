import React, { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import api from "../api/axiosInstance";
import "../assets/css/user/ExpoDetail.css";
import Footer from "./sections/Footer";

import { DEFAULT_EXPO_IMAGE, SUWON_CAMP_POSTER,SUWON_CAMP_POSTER_ID,SUWON_CAMP_TITLE, SUWON_CAMP_LOCATION } from "../constants/images";

export default function ExpoDetail() {
  const { expoId } = useParams();
  const nav = useNavigate();
  const [expo, setExpo] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    let mounted = true;

    // 1차: /api/user/expos/{id} (백엔드 준비 시)
    api.get(`/user/expos/${expoId}`)
      .then(({ data }) => {
        if (!mounted) return;
        setExpo(normalize(data));
      })
      .catch(async () => {
        // 폴백: /api/user/expos 목록에서 찾기
        try {
          const { data } = await api.get("/user/expos");
          const list = Array.isArray(data) ? data : data?.items || [];
          const item = list.find(x => String(x.expoId ?? x.id) === String(expoId));
          if (!item) return setError("해당 박람회를 찾을 수 없습니다.");
          setExpo(normalize(item));
        } catch {
          setError("상세 정보를 불러오지 못했습니다.");
        }
      });

    return () => { mounted = false; };
  }, [expoId]);

const normalize = (x) => {
  const id    = x.id ?? x.expoId ?? x.expo_id;
  const title = x.title ?? x.expoName ?? "";
  const key   = title.replace(/\s+/g, "").toLowerCase();

  const isSuwon =
    (SUWON_CAMP_POSTER_ID && String(id) === String(SUWON_CAMP_POSTER_ID)) ||
    (key.includes("수원") && (key.includes("gocaf") || key.includes("고카프") || key.includes("캠핑")));

  return {
    id,
    // 수원 25gocaf면 제목/장소/이미지 오버라이드
    title:    isSuwon ? SUWON_CAMP_TITLE    : title,
    location: isSuwon ? SUWON_CAMP_LOCATION : (x.location ?? "-"),
    imageUrl: (isSuwon ? SUWON_CAMP_POSTER : null)
              ?? x.imageUrl ?? x.image_url ?? x.thumbnailUrl ?? DEFAULT_EXPO_IMAGE,

    startDate: x.startDate ?? x.start_date,
    endDate:   x.endDate   ?? x.end_date,
    timeText:  x.timeText  ?? "10:30 ~ 18:00",
    priceText: x.priceText ?? "입장료 7,000원",
    categories: x.categories ?? x.categoryNames ?? [],
    description: x.description ?? "",
    host: x.host ?? "주최처 정보 미입력",
    contact: x.contact ?? "연락처 정보 미입력",
    notice: x.notice ?? "",
  };
};

  const handleReserve = () => {
    // 예약/결제 페이지로 이동 (쿼리로 expoId 전달)
    nav(`/payment?expoId=${expo?.id}`);
  };

  if (error) return <div className="expo-detail">{error}</div>;
  if (!expo) return <div className="expo-detail">로딩중…</div>;

  const posterSrc =
    expo.imageUrl ?? expo.image_url ?? expo.thumbnailUrl ?? DEFAULT_EXPO_IMAGE;

  return (
    <div className="expo-detail">


      <div className="ed-main">
        {/* 좌측 포스터 */}
        <div className="ed-left">
         <img
           className="ed-image"
           src={posterSrc}
           alt={expo.title}
           onError={(e) => {
             e.currentTarget.onerror = null;
             e.currentTarget.src = DEFAULT_EXPO_IMAGE;
           }}
         />
          {/* 필요시 썸네일 리스트 들어갈 자리 */}
        </div>

        {/* 우측 메타 */}
        <div className="ed-right">
          <h1 className="ed-title">
            {expo.title}
            <span className="ed-verified" title="verified"> </span>
          </h1>

          <div className="ed-datetime">
            {expo.startDate} ~ {expo.endDate}
            {expo.timeText ? <span className="ed-time">({expo.timeText})</span> : null}
          </div>

          <div className="ed-price">{expo.priceText}</div>

          {!!expo.notice && <div className="ed-notice">{expo.notice}</div>}

          {!!expo.categories?.length && (
            <div className="ed-tags">
              {expo.categories.map((c, i) => (
                <span key={i} className="ed-tag">{c}</span>
              ))}
            </div>
          )}

          <div className="ed-info-grid">
            <div className="ed-info">
              <div className="ed-info-label">주최/주관</div>
              <div className="ed-info-value">{expo.host}</div>
            </div>
            <div className="ed-info">
              <div className="ed-info-label">문의</div>
              <div className="ed-info-value">{expo.contact}</div>
            </div>
            <div className="ed-info">
              <div className="ed-info-label">장소</div>
              <div className="ed-info-value">{expo.location}</div>
            </div>
          </div>

          <div className="ed-actions">
            <button className="ed-primary" onClick={handleReserve}>
              예약하기
            </button>
            {/* 필요 시 버튼 등 추가 */}
          </div>
        </div>
      </div>

      {/* 상세 설명 */}
      {expo.description && (
        <div className="ed-desc">
          {expo.description.split("\n").map((line, i) => (
            <p key={i}>{line}</p>
          ))}
        </div>
      )}
      <Footer />
    </div>
  );
}
