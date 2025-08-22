import React, { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { findBooths } from "../api/booth";
import "../assets/css/companyUser/BoothList.css";

export default function BoothList() {
  const [loading, setLoading] = useState(true);
  const [booths, setBooths]   = useState([]);
  const [error, setError]     = useState("");
  const nav = useNavigate();

  useEffect(() => {
    let mounted = true;

    const normalize = (x) => ({
      boothId:     x.boothId ?? x.id,
      expoId:      x.expoId,
      boothName:   x.boothName ?? x.name,
      size:        x.size,
      location:    x.location,
      managerName: x.managerName,
      department:  x.department,
      phone:       x.phone,
      email:       x.email,
    });

    findBooths()
      .then(({ data }) => {
        if (!mounted) return;
        const list = Array.isArray(data) ? data : data?.items || [];
        setBooths(list.map(normalize));
      })
      .catch(() => {
        setError("부스 목록을 불러오지 못했습니다.");
        setBooths([]);
      })
      .finally(() => setLoading(false));

    return () => { mounted = false; };
  }, []);

  if (loading) return <div className="booth-list">로딩중…</div>;

  return (
    <div className="booth-list">
      <div className="bl-header">
        <h2 className="bl-title">내 부스</h2>
        <button className="bl-primary" onClick={() => nav("/company/booths/new")}>
          새 부스 등록
        </button>
      </div>

      {error && <div className="bl-error">{error}</div>}

      {booths.length === 0 ? (
        <div className="bl-empty">등록된 부스가 없습니다.</div>
      ) : (
        <ul className="bl-list">
          {booths.map((b) => (
            <li key={b.boothId} className="bl-card">
              <div className="bl-row">
                <div className="bl-name">{b.boothName}</div>
                <div className="bl-meta">
                  <span>박람회ID: {b.expoId ?? "-"}</span>
                  <span className="bl-dot">·</span>
                  <span>{b.location ?? "-"}</span>
                </div>
              </div>
              <div className="bl-actions">
                <Link className="bl-link" to={`/company/booths/${b.boothId}/edit`}>
                  수정
                </Link>
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
