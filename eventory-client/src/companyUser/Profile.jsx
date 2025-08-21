import React, { useEffect, useState } from "react";
import { findProfile, updateProfile } from "../api/profile";
import "../assets/css/companyUser/CompanyProfile.css";

export default function CompanyProfile() {
  const [loading, setLoading] = useState(true);
  const [saving, setSaving]   = useState(false);
  const [error, setError]     = useState("");
  const [form, setForm]       = useState({
    companyName: "",
    managerName: "",
    department: "",
    phone: "",
    email: "",
  });

  useEffect(() => {
    let mounted = true;
    findProfile()
      .then(({ data }) => {
        if (!mounted) return;
        setForm({
          companyName: data.companyName ?? data.name ?? "",
          managerName: data.managerName ?? data.manager ?? "",
          department:  data.department  ?? "",
          phone:       data.phone       ?? "",
          email:       data.email       ?? "",
        });
      })
      .catch(() => setError("프로필을 불러오지 못했습니다."))
      .finally(() => setLoading(false));
    return () => { mounted = false; };
  }, []);

  const onChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const onSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError("");
    try {
      await updateProfile(form);
      alert("프로필이 저장되었습니다.");
    } catch {
      setError("저장 중 오류가 발생했습니다.");
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <div className="company-profile">로딩중…</div>;

  return (
    <div className="company-profile">
      <h2 className="cp-title">회사 프로필</h2>

      {error && <div className="cp-error">{error}</div>}

      <form className="cp-form" onSubmit={onSubmit}>
        <label className="cp-label">
          회사명
          <input className="cp-input" name="companyName" value={form.companyName} onChange={onChange} required />
        </label>

        <label className="cp-label">
          담당자명
          <input className="cp-input" name="managerName" value={form.managerName} onChange={onChange} required />
        </label>

        <label className="cp-label">
          부서
          <input className="cp-input" name="department" value={form.department} onChange={onChange} />
        </label>

        <label className="cp-label">
          연락처
          <input className="cp-input" name="phone" value={form.phone} onChange={onChange} />
        </label>

        <label className="cp-label">
          이메일
          <input className="cp-input" type="email" name="email" value={form.email} onChange={onChange} />
        </label>

        <div className="cp-actions">
          <button className="cp-primary" type="submit" disabled={saving}>
            {saving ? "저장 중…" : "저장"}
          </button>
        </div>
      </form>
    </div>
  );
}
