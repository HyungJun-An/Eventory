// src/user/userMain.jsx
import React, { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/axiosInstance";
import BannerCarousel from "./sections/BannerCarousel";
import ExpoCardList from "../components/ExpoCardList";
import "../assets/css/UserMain.css";

import banner1 from '../assets/demo/busanExpo.jpg';
import banner2 from '../assets/demo/start_up.png';
import banner3 from '../assets/demo/game.jpg';

import {DEFAULT_EXPO_IMAGE, SUWON_CAMP_POSTER,SUWON_CAMP_POSTER_ID, SUWON_CAMP_TITLE,SUWON_CAMP_LOCATION} from "../constants/images";

import HeroSection from "./sections/HeroSection";
import Footer from "./sections/Footer";

export const UserMainPage = () => {
  const [expos, setExpos] = useState([]);
  const [loading, setLoading] = useState(true);
  // const base = import.meta.env.BASE_URL;
  const nav = useNavigate();

  // Banner images (public/demo/*)
  const bannerImages = [banner1, banner2, banner3];

  // 수원 Expo
const isSuwonCampExpo = (raw) => {
  const id = raw.id ?? raw.expoId ?? raw.expo_id;
  if (SUWON_CAMP_POSTER_ID && String(id) === String(SUWON_CAMP_POSTER_ID)) return true;

  // 제목으로도 보수적으로 매칭 (공백/대소문자 무시 + 한/영 혼용)
  const key = (raw.title ?? raw.expoName ?? "").replace(/\s+/g, "").toLowerCase();
  return key.includes("수원") && (key.includes("gocaf") || key.includes("고카프") || key.includes("캠핑"));
};



useEffect(() => {
  let mounted = true;

  const demo = [
    { id: 1, title: "World EXPO 2030", image_url: "/demo/busanExpo.jpg", start_date: "2025-08-20", end_date: "2025-09-20", location: "Busan" },
    { id: 2, title: "AI & Robotics Fair", image_url: "/demo/AiKorea.jpg", start_date: "2025-10-05", end_date: "2025-10-09", location: "Seoul" },
    { id: 3, title: "Green Energy Expo", image_url: "/demo/greenEnergy.jpg", start_date: "2025-07-01", end_date: "2025-08-30", location: "Busan" },
  ];

  api.get("/user/expos")
    .then(({ data }) => {
      if (!mounted) return;

      // 1) 리스트 형태 보정
      const list = Array.isArray(data) ? data : data?.items || [];

      // 2) 키 이름 정규화 (서버/데모/기존 UI 키 전부 흡수)
 const normalized = list.map(x => {
   const id       = x.id ?? x.expoId ?? x.expo_id;
   const titleRaw = x.title ?? x.expoName ?? "";
   const isSuwon  = isSuwonCampExpo({ ...x, id, title: titleRaw });

   return {
     id,
     //  수원 25gocaf면 제목/장소/이미지 오버라이드
     title:    isSuwon ? SUWON_CAMP_TITLE    : titleRaw,
     location: isSuwon ? SUWON_CAMP_LOCATION : (x.location ?? ""),
     imageUrl: (isSuwon ? SUWON_CAMP_POSTER : null)
               ?? x.imageUrl ?? x.image_url ?? x.thumbnailUrl ?? DEFAULT_EXPO_IMAGE,
     startDate: x.startDate ?? x.start_date,
     endDate:   x.endDate   ?? x.end_date,
     categories: x.categories ?? x.categoryNames ?? [],
   };
 });

      // 3) 빈 배열이면 데모로 대체
      setExpos(normalized.length > 0 ? normalized : demo.map(d => ({
        id: d.id,
        title: d.title,
        imageUrl: d.image_url ?? DEFAULT_EXPO_IMAGE,
        startDate: d.start_date,
        endDate: d.end_date,
        location: d.location,
      })));
    })
    .catch(() => {
      // 요청 자체가 실패했을 때 데모 주입
      if (!mounted) return;
      setExpos(demo.map(d => ({
        id: d.id,
        title: d.title,
        imageUrl: d.image_url,
        startDate: d.start_date,
        endDate: d.end_date,
        location: d.location,
      })));
    })
    .finally(() => setLoading(false));

  return () => { mounted = false; };
}, []);

  const now = new Date();
  const currentExpos = useMemo(() => expos.filter(e => {
    const s = new Date(e.startDate || e.start_date);
    const ed = new Date(e.endDate || e.end_date);
    return !Number.isNaN(s.getTime()) && !Number.isNaN(ed.getTime()) && s <= now && now <= ed;
  }), [expos]);

  const upcomingExpos = useMemo(() => expos.filter(e => {
    const s = new Date(e.startDate || e.start_date);
    return !Number.isNaN(s.getTime()) && s > now;
  }), [expos]);

  return (
    <div className="user-screen">
      <div className="userMain__container">
        {/* 상단 파란 영역(필요 없으면 이 블록 삭제) */}
        <HeroSection />


        {/* Banner (캐러셀) */}
        <div className="main-content">
          <BannerCarousel images={bannerImages} />
        </div>

        {/* Our Benefits */}
        <div className="benefits-section">
          <div className="benefits-header">
            <div className="section-title">Our Benefits</div>
            <p className="section-subtitle">
              we promise users with the standard of these 4 services
            </p>
          </div>

          <div className="benefits-grid">
            <div className="benefit-card">
              <img
                className="benefit-icon"
                alt="Instalment Payment"
                src="https://c.animaapp.com/me6sahcjqNWWBm/img/7176685-1--1--1.svg"
              />
              <div className="benefit-title">Instalment Payment!</div>
              <p className="benefit-description">
                You can pay a ticket in 2 portions
                <br />
                throughout a fixed period of time.
                <br />
                Start invoicing for free.
              </p>
            </div>

            <div className="benefit-card">
              <img
                className="benefit-icon"
                alt="Online Booking"
                src="https://c.animaapp.com/me6sahcjqNWWBm/img/3886130-3.svg"
              />
              <div className="benefit-content">
                <div className="benefit-title">Online Booking!</div>
                <p className="benefit-description">
                  You can pay a ticket in 2 portions
                  <br />
                  throughout a fixed period of time.
                  <br />
                  Start invoicing for free.
                </p>
              </div>
            </div>

            <div className="benefit-card">
              <img
                className="benefit-icon"
                alt="Refundable Tickets"
                src="https://c.animaapp.com/me6sahcjqNWWBm/img/wavy-tech-19-single-01-1-1.svg"
              />
              <div className="benefit-title">Refundable Tickets!</div>
              <p className="benefit-description">
                You can pay a ticket in 2 portions
                <br />
                throughout a fixed period of time.
                <br />
                Start invoicing for free.
              </p>
            </div>

            <div className="benefit-card">
              <img
                className="benefit-icon"
                alt="Cheapest Tickets"
                src="https://c.animaapp.com/me6sahcjqNWWBm/img/wavy-gen-04-single-03-1-1.svg"
              />
              <div className="benefit-title">Cheapest Tickets!</div>
              <p className="benefit-description">
                You can pay a ticket in 2 portions
                <br />
                throughout a fixed period of time.
                <br />
                Start invoicing for free.
              </p>
            </div>
          </div>
        </div>

        {/* Time is Running Out! (현재 진행) */}
        <div className="expo-lists">
          <ExpoCardList
            title="Time is Running Out!"
            items={currentExpos}
            onItemClick={(expo) => {
       const id = expo?.id ?? expo?.expoId;
       if (id) nav(`/expos/${id}`);
     }}
          />
        </div>

        {/* 4 Easy Steps To Buy a Ticket */}
        <div className="steps-wrapper">
          <div className="steps-section" >
            <div className="steps-header">
              <div className="steps-info">
                <p className="steps-title">4 Easy Steps To Buy a Ticket!</p>
                <p className="steps-subtitle">
                  Get Familiar with our 4 easy working process
                </p>
              </div>
            </div>

            <div className="steps-content">
              <div className="steps-background">
                <div className="steps-grid">
                  <div className="step-item">
                    <img
                      className="step-icon"
                      alt="Choose Concert"
                      src="https://c.animaapp.com/me6sahcjqNWWBm/img/20944608-1-1.svg"
                    />
                    <div className="step-content">
                      <div className="step-title">Choose A Expo</div>
                      <p className="step-description">
                        You can see expo tickets in our website and check
                        which one is good for you.
                      </p>
                    </div>
                  </div>

                  <div className="step-item">
                    <img
                      className="step-icon"
                      alt="Choose Date & Time"
                      src="https://c.animaapp.com/me6sahcjqNWWBm/img/3886130-2-1.svg"
                    />
                    <div className="step-content">
                      <div className="step-title">Choose Date & Time</div>
                      <p className="step-description">
                        You Can check date and time of your favorite expo in
                        our website
                      </p>
                    </div>
                  </div>

                  <div className="step-item">
                    <img
                      className="step-icon"
                      alt="Pay Your Bill"
                      src="https://c.animaapp.com/me6sahcjqNWWBm/img/hand-holding-phone-with-credit-card-screen-man-making-purchase-s.svg"
                    />
                    <div className="step-title">Pay Your Bill</div>
                    <p className="step-description">
                      After choosing your date and time and your preferred seat
                      you can pay ticket online
                    </p>
                  </div>

                  <div className="step-item">
                    <img
                      className="step-icon"
                      alt="Download Ticket"
                      src="https://c.animaapp.com/me6sahcjqNWWBm/img/hand-drawn-online-ticket-illustration-23-2151074791-1.svg"
                    />
                    <div className="step-title">Download Your Ticket!</div>
                    <p className="step-description">
                      After completing checkout process you can download your
                      ticket and get ready for expo
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

<div className="expo-lists">
        {upcomingExpos.length > 0 ? (
  <ExpoCardList
    title="Upcoming Expos"
    items={upcomingExpos}
    onItemClick={(expo) => {
       const id = expo?.id ?? expo?.expoId;
       if (id) nav(`/expos/${id}`);
     }}
  />
) : (
  <div className="expo-empty">No upcoming expos right now.</div>
)}

        {loading && (
          <div style={{ textAlign: "center", padding: "8px 0", color: "#546179" }}>
            Loading...
          </div>
        )}
</div>
        {/* Footer */}
        <Footer />

        {/* FAQ / Sponsors 필요 시 아래로 이동 */}
      </div>
    </div>
  );
};

export default UserMainPage;
