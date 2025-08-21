import { useNavigate } from "react-router-dom";

export default function MainPage() {
  const navigate = useNavigate();

  return (
    <div className="container center">
      <h1>관리자 메인</h1>
      <button
        className="btn-primary"
        onClick={() => navigate("/vip-banners")}
        type="button"
      >
        배너 관리
      </button>
    </div>
  );
}
