import { useEffect, useMemo, useState } from "react";
import client from "../api/client.js";
import Header from "../components/Header.jsx";
import Tabs from "../components/Tabs.jsx";
import BannerCard from "../components/BannerCard.jsx";

export default function VipBannerList() {
  const [tab, setTab] = useState("ALL");
  const [q, setQ] = useState("");
  const [loading, setLoading] = useState(false);
  const [banners, setBanners] = useState([]);
  const [error, setError] = useState("");

  const params = useMemo(() => {
    const p = {};
    if (tab !== "ALL") p.status = tab; // 서버에서 WAITING/APPROVED/REJECTED로 받는다고 가정
    if (q.trim()) p.expoName = q.trim();
    return p;
  }, [tab, q]);

  useEffect(() => {
    let cancelled = false;
    async function fetchList() {
      setLoading(true);
      setError("");
      try {
        const res = await client.get("/api/sys/banners", { params });
        // API 응답 예시 가정
        // { data: [{ id, expoName, appliedAt, status, imageUrl }, ...] }
        if (!cancelled) setBanners(res.data?.data ?? []);
      } catch (e) {
        if (!cancelled) {
          setError(e.message);
          // 데모용 목업(백엔드 연결 전 임시)
          setBanners([
            {
              id: 1,
              expoName: "스마트 건축엑스포",
              appliedAt: "2025-08-01",
              status: "WAITING",
              imageUrl: "/vip배너목록.png",
            },
            {
              id: 2,
              expoName: "스마트 건축엑스포",
              appliedAt: "2025-08-01",
              status: "APPROVED",
              imageUrl: "/vip배너목록.png",
            },
            {
              id: 3,
              expoName: "스마트 건축엑스포",
              appliedAt: "2025-08-01",
              status: "REJECTED",
              imageUrl: "/vip배너목록.png",
            },
          ]);
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    }
    fetchList();
    return () => {
      cancelled = true;
    };
  }, [params]);

  return (
    <div className="page">
      <Header />

      <div className="toolbar">
        <Tabs value={tab} onChange={setTab} />
        <div className="search">
          <input
            className="input"
            placeholder="박람회 명을 입력해주세요"
            value={q}
            onChange={(e) => setQ(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && setQ(e.currentTarget.value)}
          />
        </div>
      </div>

      <section className="grid-horizontal">
        {loading && <div className="hint">불러오는 중…</div>}
        {!loading && banners.length === 0 && (
          <div className="hint">표시할 배너가 없습니다.</div>
        )}
        {!loading &&
          banners.map((b) => <BannerCard key={b.id} banner={b} />)}
      </section>

      {error && <div className="error">{error}</div>}
    </div>
  );
}

