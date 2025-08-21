import { useNavigate } from "react-router-dom";

function StatusChip({ status }) {
  const map = {
    WAITING: { text: "대기", className: "chip chip--wait" },
    APPROVED: { text: "승인", className: "chip chip--ok" },
    REJECTED: { text: "거절", className: "chip chip--deny" },
  };
  const s = map[status] || { text: "-", className: "chip" };
  return <span className={s.className}>{s.text}</span>;
}

export default function BannerCard({ banner }) {
  const navigate = useNavigate();
  const { id, expoName, appliedAt, status, imageUrl } = banner;

  return (
    <div className="card">
      <div className="card__image">
        <img
          src={imageUrl || "/placeholder-banner.png"}
          alt={`${expoName} 포스터`}
        />
      </div>

      <div className="card__meta">
        <div className="meta-row">
          <span className="meta-label">박람회명</span>
          <span className="meta-value">{expoName}</span>
        </div>
        <div className="meta-row">
          <span className="meta-label">신청일</span>
          <span className="meta-value">{appliedAt}</span>
        </div>
        <div className="meta-row">
          <span className="meta-label">상태</span>
          <StatusChip status={status} />
        </div>
      </div>

      <div className="card__actions">
        <button
          className="btn-outline"
          onClick={() => navigate(`/vip-banners/${id}`)}
        >
          상세보기
        </button>
      </div>
    </div>
  );
}
