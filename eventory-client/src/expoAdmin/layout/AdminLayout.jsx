import { useCallback, useEffect, useMemo, useState } from "react";
import { Outlet } from "react-router-dom";
import api from "../../api/axiosInstance";
import AdminSidebar from "./AdminSidebar";
import AdminHeader from "./AdminHeader";
import { todayString } from "../components/adminFormat";
import "../../assets/css/admin/AdminUI.css";

const STORAGE_KEY = "adminSelectedExpoId";

// 저장소 접근이 막힌 환경(시크릿 모드 등)에서도 화면은 정상 동작해야 한다
const readSavedExpoId = () => {
  try {
    return Number(localStorage.getItem(STORAGE_KEY)) || null;
  } catch {
    return null;
  }
};
const saveExpoId = (id) => {
  try {
    localStorage.setItem(STORAGE_KEY, String(id));
  } catch {
    /* 저장 실패는 무시 (다음 진입 시 기본 박람회로 선택됨) */
  }
};

/** 기본 선택: 진행 중 → 가장 가까운 예정 → 가장 최근에 끝난 박람회 */
function pickDefaultExpo(expos) {
  if (expos.length === 0) return null;
  const today = todayString();
  const ongoing = expos.find((e) => e.startDate <= today && today <= e.endDate);
  if (ongoing) return ongoing;
  const upcoming = expos.filter((e) => e.startDate > today).sort((a, b) => a.startDate.localeCompare(b.startDate));
  if (upcoming.length) return upcoming[0];
  return [...expos].sort((a, b) => b.endDate.localeCompare(a.endDate))[0];
}

/**
 * 박람회관리자 공통 레이아웃 (사이드바 + 헤더 + 본문)
 * - 선택 박람회를 한 곳에서 관리하고 Outlet context 로 하위 페이지에 전달한다.
 * - 박람회를 바꾸면 Outlet 을 새로 마운트해 페이지 상태(페이지 번호, 필터 등)를 초기화한다.
 */
export default function AdminLayout() {
  const [expos, setExpos] = useState([]);
  const [expoId, setExpoId] = useState(null);
  const [loading, setLoading] = useState(true);

  // index.css 의 body { display:flex; place-items:center } 가 관리자 레이아웃 폭을 줄이지 않도록 해제
  useEffect(() => {
    document.body.classList.add("adm-body");
    return () => document.body.classList.remove("adm-body");
  }, []);

  const reloadExpos = useCallback(async () => {
    const res = await api.get("/admin/expos");
    const list = Array.isArray(res.data) ? res.data : [];
    setExpos(list);
    setExpoId((prev) => {
      const keep = list.find((e) => e.expoId === (prev ?? readSavedExpoId()));
      return (keep ?? pickDefaultExpo(list))?.expoId ?? null;
    });
  }, []);

  useEffect(() => {
    reloadExpos()
      .catch(() => setExpos([]))
      .finally(() => setLoading(false));
  }, [reloadExpos]);

  const selectExpo = useCallback((id) => {
    setExpoId(id);
    saveExpoId(id);
  }, []);

  const expo = useMemo(() => expos.find((e) => e.expoId === expoId) ?? null, [expos, expoId]);
  const context = useMemo(
    () => ({ expoId, expo, expos, selectExpo, reloadExpos }),
    [expoId, expo, expos, selectExpo, reloadExpos]
  );

  return (
    <div className="adm-shell">
      <AdminSidebar />
      <div className="adm-main">
        <AdminHeader expos={expos} expo={expo} onSelectExpo={selectExpo} loading={loading} />
        <main className="adm-content">
          {loading ? (
            <div className="adm-empty">박람회 정보를 불러오는 중…</div>
          ) : expoId ? (
            <Outlet key={expoId} context={context} />
          ) : (
            <div className="adm-empty">담당 중인 승인된 박람회가 없습니다.</div>
          )}
        </main>
      </div>
    </div>
  );
}
