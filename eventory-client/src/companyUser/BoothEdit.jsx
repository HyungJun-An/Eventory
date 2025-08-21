import React, { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { findBooth, createBooth, updateBooth } from "../api/booth";
import "../assets/css/companyUser/BoothEdit.css";

const emptyForm = {
  expoId: "",
  boothName: "",
  size: "",
  location: "",
  managerName: "",
  department: "",
  phone: "",
  email: "",
};

export default function BoothEdit() {
  const { boothId } = useParams(); // /company/booths/:boothId/edit 또는 /company/booths/new
  const isCreate = boothId === "new" || boothId === undefined;
  const nav = useNavigate();

  const [loading, setLoading] = useState(!isCreate);
  const [saving, setSaving]   = useState(false);
  const [error, setError]     = useState("");
  const [form, setForm]       = useState(emptyForm);

  useEffect(() => {
    if (isCreate) return;
    let mounted = true;

    const normalize = (x) => ({
      expoId:      x.expoId ?? "",
      boothName:   x.boothName ?? x.name ?? "",
      size:        x.size ?? "",
      location:    x.location ?? "",
      managerName: x.managerName ?? "",
      department:  x.department ?? "",
      phone:       x.phone ?? "",
      email:       x.email ?? "",
    });

    findBooth(boothId)
      .then(({ data }) => { if (mounted) setForm(normalize(data)); })
      .catch(() => setError("부스 정보를 불러오지 못했습니다."))
      .finally(() => setLoading(false));

    return () => { mounted = false; };
  }, [boothId, isCreate]);

  const onChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const onSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError("");
    try {
      if (isCreate) {
        await createBooth(form);
        alert("부스가 등록되었습니다.");
      } else {
        await updateBooth(boothId, form);
        alert("부스가 수정되었습니다.");
      }
      nav("/company/booths");
    } catch {
      setError("저장 중 오류가 발생했습니다.");
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <div className="booth-edit">로딩중…</div>;

  return (
    <div className="booth-edit">
      <h2 className="be-title">{isCreate ? "새 부스 등록" : "부스 수정"}</h2>
      {error && <div className="be-error">{error}</div>}

      <form className="be-form" onSubmit={onSubmit}>
        <div className="be-grid">
          <label className="be-label">
            박람회 ID (expoId)
            <input className="be-input" name="expoId" value={form.expoId} onChange={onChange} required />
          </label>

          <label className="be-label">
            부스명
            <input className="be-input" name="boothName" value={form.boothName} onChange={onChange} required />
          </label>

          <label className="be-label">
            크기
            <input className="be-input" name="size" value={form.size} onChange={onChange} />
          </label>

          <label className="be-label">
            위치
            <input className="be-input" name="location" value={form.location} onChange={onChange} />
          </label>

          <label className="be-label">
            담당자명
            <input className="be-input" name="managerName" value={form.managerName} onChange={onChange} />
          </label>

          <label className="be-label">
            부서
            <input className="be-input" name="department" value={form.department} onChange={onChange} />
          </label>

          <label className="be-label">
            연락처
            <input className="be-input" name="phone" value={form.phone} onChange={onChange} />
          </label>

          <label className="be-label">
            이메일
            <input className="be-input" type="email" name="email" value={form.email} onChange={onChange} />
          </label>
        </div>

        <div className="be-actions">
          <button className="be-secondary" type="button" onClick={() => nav("/company/booths")}>목록</button>
          <button className="be-primary" type="submit" disabled={saving}>
            {saving ? "저장 중…" : isCreate ? "등록" : "수정"}
          </button>
        </div>
      </form>
    </div>
  );
}
