import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import client from "../api/client.js";
import Header from "../components/Header.jsx";
import BannerManageModal from "../components/BannerManageModal.jsx";
import RejectReasonModal from "../components/RejectReasonModal.jsx";

export default function VipBannerDetail() {
  const { bannerId } = useParams();
  const navigate = useNavigate();

  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState("");

  const [manageOpen, setManageOpen] = useState(false);
  const [rejectOpen, setRejectOpen] = useState(false);

  useEffect(() => {
    let cancelled = false;
    async function load() {
      setLoading(true);
      try {
        const res = await client.get(`/api/sys/banners/${bannerId}`);
        if (!cancelled) {
          setData(res.data?.data ?? null);
        }
      } catch (e) {
        if (!cancelled) {
          setErr(e.message);
          // 목업 데이터
          setData({
            id: bannerId,
            expoName: "박람회123",
            applicant: "홍길동",
            email: "abc123@email.com",
            status: "WAITING",
            appliedAt: "2025-08-01",
            startDate: "2025-08-10",
            endDate: "2025-08-20",
            clicks: 153,
            fee: 98000,
            imageUrl: "/vip배너상세보기.png",
          });
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    }
    load();
    return () => {
      cancelled = true;
    };
  }, [bannerId]);

  const approve = async () => {
    try {
      await client.put(`/api/sys/banners/${bannerId}`, { status: "APPROVED" });
      alert("승인되었습니다.");
      navigate(-1);
    } catch (e) {
      alert(e.message);
    }
  };

  const reject = async (reason) => {
    try {
      await client.put(`/api/sys/banners/${bannerId}`, {
        status: "REJECTED",
        reason,
      });
      alert("거절되었습니다.");
      navigate(-1);
    } catch (e) {
      alert(e.message);
    }
  };

  const saveManage = async (payload) => {
    try {
      await client.put(`/api/sys/banners/${bannerId}`, payload);
      alert("배너가 수정되었습니다.");
      setManageOpen(false);
    } catch (e) {
      alert(e.message);
    }
  };

  if (loading) return <div>불러오는 중...</div>;

  if (!data) return <div>데이터 없음</div>;

  return (
    <div className="page">
      <Header />
      <div className="container">
        <h2 style={{ color: "#2b6ffb", fontWeight: "bold" }}>{data.expoName}</h2>
        <p>
          신청자: {data.applicant} ({data.email})
        </p>

        <img src={data.imageUrl} alt="박람회 배너" style={{ width: "60%" }} />

        <div className="detail-box">
          <div>요청 상태: {data.status}</div>
          <div>신청일: {data.appliedAt}</div>
          <div>
            노출 기간: {data.startDate} ~ {data.endDate}
          </div>
          <div>클릭 수: {data.clicks}</div>
          <div>광고 수수료: ₩{data.fee?.toLocaleString()}</div>
        </div>

        <div className="actions">
          <button className="btn-primary" onClick={approve}>
            승인
          </button>
          <button className="btn-danger" onClick={() => setRejectOpen(true)}>
            거절
          </button>
        </div>

        <button className="btn-outline-primary" onClick={() => setManageOpen(true)}>
          배너 관리
        </button>
      </div>

      {/* 모달 */}
      <BannerManageModal
        open={manageOpen}
        onClose={() => setManageOpen(false)}
        banner={data}
        onSave={saveManage}
      />

      <RejectReasonModal
        open={rejectOpen}
        onClose={() => setRejectOpen(false)}
        onConfirm={reject}
      />
    </div>
  );
}
