import React from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axiosInstance';

const LogoutButton = () => {
  const navigate = useNavigate();

  const handleLogout = async () => {
    try {
      // 1. 백엔드 로그아웃 API 호출 (RefreshToken 블랙리스트 처리)
      await api.post('/auth/logout', null, { withCredentials: true });

      // 2. 로컬 저장소의 AccessToken 삭제
      localStorage.removeItem('accessToken');

      // 3. 메인 페이지로 이동
      navigate('/userMain', { replace: true });
    } catch (err) {
      console.error('로그아웃 실패:', err);
      alert('로그아웃 처리 중 오류가 발생했음.');
    }
  };



  return (
    <button className="nav-logout-btn" onClick={handleLogout}>
      로그아웃
    </button>
  );
};

export default LogoutButton;