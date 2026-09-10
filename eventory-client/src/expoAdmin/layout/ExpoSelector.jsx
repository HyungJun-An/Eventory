import { useEffect, useMemo, useRef, useState } from "react";
import { CalendarDays, Check, ChevronDown, MapPin, Search } from "lucide-react";
import { expoPhase, formatDate } from "../components/adminFormat";

function ExpoThumb({ src, title }) {
  const [broken, setBroken] = useState(false);
  if (!src || broken) {
    return <span className="adm-expo-thumb adm-expo-thumb--fallback">{title?.[0] ?? "E"}</span>;
  }
  return <img className="adm-expo-thumb" src={src} alt="" onError={() => setBroken(true)} />;
}

function PhaseBadge({ expo }) {
  const phase = expoPhase(expo);
  return <span className={`adm-badge adm-badge--${phase.tone}`}>{phase.label}</span>;
}

/**
 * 헤더의 박람회 선택 드롭다운
 * - 포스터·기간·장소·진행 상태를 한눈에 보여주고, 검색과 키보드(↑↓ Enter Esc) 조작을 지원한다.
 */
export default function ExpoSelector({ expos, expo, onSelect, loading }) {
  const [open, setOpen] = useState(false);
  const [query, setQuery] = useState("");
  const [cursor, setCursor] = useState(0);
  const rootRef = useRef(null);
  const searchRef = useRef(null);

  const filtered = useMemo(() => {
    const q = query.trim().toLowerCase();
    if (!q) return expos;
    return expos.filter((e) => e.title.toLowerCase().includes(q) || (e.location ?? "").toLowerCase().includes(q));
  }, [expos, query]);

  // 바깥 클릭 시 닫기
  useEffect(() => {
    if (!open) return undefined;
    const onDown = (e) => rootRef.current && !rootRef.current.contains(e.target) && setOpen(false);
    document.addEventListener("mousedown", onDown);
    return () => document.removeEventListener("mousedown", onDown);
  }, [open]);

  useEffect(() => {
    if (open) searchRef.current?.focus();
  }, [open]);

  const toggle = () => {
    if (!open) {
      setQuery("");
      setCursor(Math.max(0, expos.findIndex((e) => e.expoId === expo?.expoId)));
    }
    setOpen((o) => !o);
  };

  const choose = (target) => {
    onSelect(target.expoId);
    setOpen(false);
  };

  const onKeyDown = (e) => {
    if (e.key === "Escape") setOpen(false);
    else if (e.key === "ArrowDown") {
      e.preventDefault();
      setCursor((c) => Math.min(c + 1, filtered.length - 1));
    } else if (e.key === "ArrowUp") {
      e.preventDefault();
      setCursor((c) => Math.max(c - 1, 0));
    } else if (e.key === "Enter" && filtered[cursor]) {
      e.preventDefault();
      choose(filtered[cursor]);
    }
  };

  return (
    <div className="adm-expo-select" ref={rootRef}>
      <button
        type="button"
        className={`adm-expo-select__trigger${open ? " is-open" : ""}`}
        onClick={toggle}
        aria-haspopup="listbox"
        aria-expanded={open}
        aria-label="관리할 박람회 선택"
        disabled={loading || expos.length === 0}
      >
        {expo ? (
          <>
            <ExpoThumb src={expo.imageUrl} title={expo.title} />
            <span className="adm-expo-select__text">
              <span className="adm-expo-select__title">{expo.title}</span>
              <span className="adm-expo-select__meta">
                {formatDate(expo.startDate)} ~ {formatDate(expo.endDate)} · {expo.location}
              </span>
            </span>
            <PhaseBadge expo={expo} />
          </>
        ) : (
          <span className="adm-expo-select__placeholder">{loading ? "박람회 불러오는 중…" : "담당 박람회 없음"}</span>
        )}
        <ChevronDown size={18} className="adm-expo-select__chevron" />
      </button>

      {open && (
        <div className="adm-expo-select__panel" onKeyDown={onKeyDown}>
          <div className="adm-expo-select__search">
            <Search size={16} />
            <input
              ref={searchRef}
              value={query}
              onChange={(e) => {
                setQuery(e.target.value);
                setCursor(0);
              }}
              placeholder="박람회명 또는 장소 검색"
              aria-label="박람회 검색"
            />
          </div>
          <div className="adm-expo-select__count">담당 박람회 {expos.length}개</div>
          <ul role="listbox" className="adm-expo-select__list" aria-label="담당 박람회">
            {filtered.map((e, i) => {
              const selected = e.expoId === expo?.expoId;
              return (
                <li
                  key={e.expoId}
                  role="option"
                  aria-selected={selected}
                  className={`adm-expo-option${selected ? " is-selected" : ""}${i === cursor ? " is-cursor" : ""}`}
                  onMouseEnter={() => setCursor(i)}
                  onClick={() => choose(e)}
                >
                  <ExpoThumb src={e.imageUrl} title={e.title} />
                  <span className="adm-expo-option__text">
                    <span className="adm-expo-option__title">{e.title}</span>
                    <span className="adm-expo-option__meta">
                      <CalendarDays size={13} />
                      {formatDate(e.startDate)} ~ {formatDate(e.endDate)}
                      <MapPin size={13} className="adm-expo-option__pin" />
                      {e.location}
                    </span>
                  </span>
                  <PhaseBadge expo={e} />
                  {selected && <Check size={16} className="adm-expo-option__check" />}
                </li>
              );
            })}
            {filtered.length === 0 && <li className="adm-expo-select__empty">검색 결과가 없습니다.</li>}
          </ul>
        </div>
      )}
    </div>
  );
}
