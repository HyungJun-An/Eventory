import { useEffect, useState } from "react";
import api from "../../api/axiosInstance";
import { useAdminExpo } from "../layout/useAdminExpo";
import Flash, { useFlash } from "../components/Flash";
import { errorMessage, formatKRW, formatPhone } from "../components/adminFormat";

const DESCRIPTION_MAX = 255; // expo.description 컬럼 길이

function PosterPreview({ src }) {
  const [broken, setBroken] = useState(false);
  useEffect(() => setBroken(false), [src]);
  return (
    <div className="adm-poster-preview">
      {src && !broken ? <img src={src} alt="포스터 미리보기" onError={() => setBroken(true)} /> : <span>이미지를 불러올 수 없습니다</span>}
    </div>
  );
}

/** 콘텐츠 관리 — 박람회 소개·일정·포스터·공개 여부 수정 (기존: 읽기 전용, 저장 불가) */
export default function ContentEditPage() {
  const { expoId, reloadExpos } = useAdminExpo();
  const [form, setForm] = useState(null);
  const [original, setOriginal] = useState(null);
  const [info, setInfo] = useState(null);
  const [saving, setSaving] = useState(false);
  const [flash, showFlash] = useFlash();

  useEffect(() => {
    let alive = true;
    Promise.all([api.get(`/admin/expos/${expoId}`), api.get(`/admin/expos/${expoId}/contents`)])
      .then(([expoRes, contentRes]) => {
        if (!alive) return;
        const e = expoRes.data;
        const next = {
          title: e.title ?? "",
          imageUrl: e.imageUrl ?? "",
          description: e.description ?? "",
          startDate: e.startDate ?? "",
          endDate: e.endDate ?? "",
          visibility: Boolean(e.visibility),
        };
        setForm(next);
        setOriginal(next);
        setInfo(contentRes.data);
      })
      .catch((err) => alive && showFlash("error", errorMessage(err, "콘텐츠를 불러오지 못했습니다.")));
    return () => {
      alive = false;
    };
  }, [expoId, showFlash]);

  if (!form || !info) {
    return (
      <>
        <Flash flash={flash} />
        <div className="adm-empty">불러오는 중…</div>
      </>
    );
  }

  const dirty = JSON.stringify(form) !== JSON.stringify(original);
  const dateError = form.startDate && form.endDate && form.endDate < form.startDate;
  const invalid = !form.title.trim() || !form.imageUrl.trim() || !form.description.trim() || !form.startDate || !form.endDate || dateError;

  const update = (key) => (e) => {
    const value = e.target.type === "checkbox" ? e.target.checked : e.target.value;
    setForm((f) => ({ ...f, [key]: value }));
  };

  const save = async (e) => {
    e.preventDefault();
    if (invalid) return;
    const payload = { ...form, title: form.title.trim(), description: form.description.trim(), imageUrl: form.imageUrl.trim() };
    setSaving(true);
    try {
      await api.put(`/admin/expos/${expoId}`, payload);
      setForm(payload);
      setOriginal(payload);
      showFlash("success", "박람회 정보를 저장했습니다.");
      reloadExpos().catch(() => {}); // 헤더 선택 박스의 제목·포스터 갱신
    } catch (err) {
      showFlash("error", errorMessage(err, "저장에 실패했습니다. 입력값을 확인해주세요."));
    } finally {
      setSaving(false);
    }
  };

  return (
    <>
      <Flash flash={flash} />
      <form className="adm-grid-2" onSubmit={save}>
        <section className="adm-card">
          <h2 className="adm-card__title">기본 정보</h2>
          <label className="adm-field">
            <span>박람회명</span>
            <input className="adm-input" value={form.title} onChange={update("title")} maxLength={255} required />
          </label>
          <label className="adm-field">
            <span>
              소개문
              <em>
                {form.description.length}/{DESCRIPTION_MAX}
              </em>
            </span>
            <textarea className="adm-input" rows={5} maxLength={DESCRIPTION_MAX} value={form.description} onChange={update("description")} required />
          </label>
          <div className="adm-field-row">
            <label className="adm-field">
              <span>시작일</span>
              <input type="date" className="adm-input" value={form.startDate} onChange={update("startDate")} required />
            </label>
            <label className="adm-field">
              <span>종료일</span>
              <input type="date" className="adm-input" value={form.endDate} min={form.startDate} onChange={update("endDate")} required />
            </label>
          </div>
          {dateError && <p className="adm-field-error">종료일은 시작일 이후여야 합니다.</p>}
          <label className="adm-switch">
            <input type="checkbox" checked={form.visibility} onChange={update("visibility")} />
            <span className="adm-switch__track" />
            <span>{form.visibility ? "메인 화면에 공개 중" : "비공개 (메인 화면에 노출되지 않음)"}</span>
          </label>
          <div className="adm-form-actions">
            <button type="button" className="adm-btn adm-btn--ghost" disabled={!dirty || saving} onClick={() => setForm(original)}>
              되돌리기
            </button>
            <button type="submit" className="adm-btn adm-btn--primary" disabled={!dirty || invalid || saving}>
              {saving ? "저장 중…" : "변경사항 저장"}
            </button>
          </div>
        </section>

        <section className="adm-card">
          <h2 className="adm-card__title">포스터</h2>
          <PosterPreview src={form.imageUrl} />
          <label className="adm-field">
            <span>이미지 경로 (URL)</span>
            <input className="adm-input" value={form.imageUrl} onChange={update("imageUrl")} required />
          </label>

          <h2 className="adm-card__title adm-card__title--spaced">
            운영 정보 <small>변경은 플랫폼 운영팀에 문의</small>
          </h2>
          <dl className="adm-dl">
            <dt>장소</dt>
            <dd>{info.location}</dd>
            <dt>티켓 가격</dt>
            <dd>{formatKRW(info.price)}</dd>
            <dt>카테고리</dt>
            <dd>{info.categories?.length ? info.categories.join(", ") : "-"}</dd>
            <dt>담당자</dt>
            <dd>
              {info.expoAdminName} · {info.email} · {formatPhone(info.phone)}
            </dd>
          </dl>
        </section>
      </form>
    </>
  );
}
