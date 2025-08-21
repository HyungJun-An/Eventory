export default function Tabs({ value, onChange }) {
  const items = [
    { key: "ALL", label: "전체" },
    { key: "WAITING", label: "대기" },
    { key: "APPROVED", label: "승인" },
    { key: "REJECTED", label: "거절" }, // 요구에 '서절' 표기가 있었으나 '거절'로 반영
  ];

  return (
    <div className="tabs">
      {items.map((it) => (
        <button
          key={it.key}
          className={`tab ${value === it.key ? "active" : ""}`}
          onClick={() => onChange(it.key)}
          type="button"
        >
          {it.label}
        </button>
      ))}
    </div>
  );
}

