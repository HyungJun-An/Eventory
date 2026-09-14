import { ChevronLeft, ChevronRight } from "lucide-react";

const WINDOW = 5; // 한 번에 보여줄 페이지 번호 수

/** 0부터 시작하는 page 기준 페이지 이동 (Spring Page 응답과 동일한 기준) */
export default function Pager({ page, totalPages, onChange }) {
  if (!totalPages || totalPages <= 1) return null;

  const start = Math.max(0, Math.min(page - Math.floor(WINDOW / 2), totalPages - WINDOW));
  const pages = Array.from({ length: Math.min(WINDOW, totalPages) }, (_, i) => start + i);

  return (
    <nav className="adm-pager" aria-label="페이지 이동">
      <button className="adm-pager__btn" disabled={page === 0} onClick={() => onChange(page - 1)} aria-label="이전 페이지">
        <ChevronLeft size={16} />
      </button>
      {pages.map((p) => (
        <button
          key={p}
          className={`adm-pager__btn${p === page ? " is-active" : ""}`}
          aria-current={p === page ? "page" : undefined}
          onClick={() => onChange(p)}
        >
          {p + 1}
        </button>
      ))}
      <button
        className="adm-pager__btn"
        disabled={page >= totalPages - 1}
        onClick={() => onChange(page + 1)}
        aria-label="다음 페이지"
      >
        <ChevronRight size={16} />
      </button>
      <span className="adm-pager__info">
        {page + 1} / {totalPages}
      </span>
    </nav>
  );
}
