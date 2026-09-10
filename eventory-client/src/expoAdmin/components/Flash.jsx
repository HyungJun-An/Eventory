import { useCallback, useEffect, useRef, useState } from "react";

/** 처리 결과 알림 상태 — show("success" | "error", 메시지) 후 3.5초 뒤 자동으로 사라진다 */
export function useFlash() {
  const [flash, setFlash] = useState(null);
  const timer = useRef(null);

  const show = useCallback((type, text) => {
    setFlash({ type, text });
    clearTimeout(timer.current);
    timer.current = setTimeout(() => setFlash(null), 3500);
  }, []);

  useEffect(() => () => clearTimeout(timer.current), []);
  return [flash, show];
}

export default function Flash({ flash }) {
  if (!flash) return null;
  return (
    <div className={`adm-flash adm-flash--${flash.type}`} role={flash.type === "error" ? "alert" : "status"}>
      {flash.text}
    </div>
  );
}
