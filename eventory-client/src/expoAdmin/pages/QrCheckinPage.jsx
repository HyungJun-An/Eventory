import { useCallback, useEffect, useRef, useState } from "react";
import { Link } from "react-router-dom";
import { CameraOff, CheckCircle2, ScanLine, Users, XCircle } from "lucide-react";
import api from "../../api/axiosInstance";
import { useAdminExpo } from "../layout/useAdminExpo";

// 백엔드 CheckinResponse.status → 안내 문구
const RESULT_TEXT = {
  OK: "입장 처리되었습니다.",
  INVALID_OR_EXPIRED: "유효하지 않거나 만료된 QR입니다.",
  RESERVATION_NOT_FOUND: "존재하지 않는 예약입니다.",
  TOKEN_MISMATCH: "재발급 등으로 더 이상 사용할 수 없는 QR입니다.",
  ALREADY_CHECKED_IN: "이미 입장한 예약입니다.",
  RESERVATION_CANCELLED: "취소된 예약입니다.",
  QR_NOT_ISSUED: "티켓 정보가 없는 예약입니다.",
};
const SCAN_INTERVAL_MS = 400;
const SAME_CODE_COOLDOWN_MS = 3000; // 같은 QR이 연속으로 인식돼 중복 요청되는 것 방지

/**
 * QR 체크인
 * - 카메라: 브라우저 내장 BarcodeDetector 사용 (Chrome·Edge). 추가 라이브러리 없이 동작
 * - 미지원 브라우저/카메라 없음: QR 값을 직접 입력해 체크인
 */
export default function QrCheckinPage() {
  const { expo } = useAdminExpo();
  const videoRef = useRef(null);
  const streamRef = useRef(null);
  const timerRef = useRef(null);
  const busyRef = useRef(false);
  const [cameraOn, setCameraOn] = useState(false);
  const [cameraError, setCameraError] = useState("");
  const [token, setToken] = useState("");
  const [result, setResult] = useState(null);
  const [history, setHistory] = useState([]);

  const supported = typeof window !== "undefined" && "BarcodeDetector" in window && !!navigator.mediaDevices?.getUserMedia;

  const stopCamera = useCallback(() => {
    clearInterval(timerRef.current);
    streamRef.current?.getTracks().forEach((t) => t.stop());
    streamRef.current = null;
    setCameraOn(false);
  }, []);

  useEffect(() => stopCamera, [stopCamera]); // 페이지를 떠나면 카메라 끄기

  const submitToken = useCallback(async (raw) => {
    const value = raw.trim();
    if (!value || busyRef.current) return;
    busyRef.current = true;
    let body;
    try {
      body = (await api.post("/checkin/scan", { token: value })).data;
    } catch (e) {
      body = e.response?.data ?? {};
    } finally {
      busyRef.current = false;
    }
    const status = body?.status ?? "ERROR";
    const entry = {
      ok: status === "OK",
      message: RESULT_TEXT[status] ?? body?.message ?? "체크인 처리 중 오류가 발생했습니다.",
      data: body?.data ?? null,
      at: new Date(),
    };
    setResult(entry);
    if (entry.ok) setHistory((h) => [entry, ...h].slice(0, 8));
  }, []);

  const startCamera = async () => {
    setCameraError("");
    if (!supported) {
      setCameraError("이 브라우저는 카메라 QR 인식을 지원하지 않습니다(Chrome·Edge 권장). 아래에 QR 값을 직접 입력해주세요.");
      return;
    }
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ video: { facingMode: "environment" }, audio: false });
      streamRef.current = stream;
      videoRef.current.srcObject = stream;
      await videoRef.current.play();
      setCameraOn(true);

      const detector = new window.BarcodeDetector({ formats: ["qr_code"] });
      let lastValue = "";
      timerRef.current = setInterval(async () => {
        if (busyRef.current || !videoRef.current) return;
        try {
          const codes = await detector.detect(videoRef.current);
          const value = codes[0]?.rawValue;
          if (value && value !== lastValue) {
            lastValue = value;
            submitToken(value);
            setTimeout(() => {
              lastValue = "";
            }, SAME_CODE_COOLDOWN_MS);
          }
        } catch {
          /* 프레임 단위 인식 실패는 무시 */
        }
      }, SCAN_INTERVAL_MS);
    } catch (e) {
      stopCamera();
      setCameraError(
        e?.name === "NotAllowedError"
          ? "카메라 권한이 거부되었습니다. 브라우저 주소창의 권한 설정에서 허용해주세요."
          : "카메라를 시작할 수 없습니다. 연결된 카메라가 있는지 확인해주세요."
      );
    }
  };

  const submitManual = (e) => {
    e.preventDefault();
    submitToken(token);
    setToken("");
  };

  return (
    <div className="adm-qr">
      <section className="adm-card">
        <h2 className="adm-card__title">
          QR 스캔 <small>{expo?.title}</small>
        </h2>
        <div className="adm-qr__viewport">
          <video ref={videoRef} muted playsInline hidden={!cameraOn} />
          {cameraOn ? (
            <div className="adm-qr__frame" aria-hidden="true" />
          ) : (
            <div className="adm-qr__placeholder">
              <ScanLine size={44} />
              <strong>카메라를 켜고 QR을 화면 중앙에 맞춰주세요</strong>
              <span>인식되면 자동으로 입장 처리됩니다.</span>
            </div>
          )}
        </div>
        <div className="adm-qr__controls">
          {cameraOn ? (
            <button className="adm-btn adm-btn--outline" onClick={stopCamera}>
              <CameraOff size={16} />
              카메라 끄기
            </button>
          ) : (
            <button className="adm-btn adm-btn--primary" onClick={startCamera}>
              <ScanLine size={16} />
              카메라 시작
            </button>
          )}
        </div>
        {cameraError && <div className="adm-alert">{cameraError}</div>}

        <form className="adm-qr__manual" onSubmit={submitManual}>
          <input
            className="adm-input"
            value={token}
            onChange={(e) => setToken(e.target.value)}
            placeholder="QR 값 직접 입력 (스캐너 연결 시 자동 입력)"
            aria-label="QR 값 직접 입력"
          />
          <button className="adm-btn adm-btn--primary" disabled={!token.trim()}>
            체크인
          </button>
        </form>
        <p className="adm-hint">
          QR이 없는 방문객은{" "}
          <Link to="/admin/reservation/list" className="adm-link-btn">
            예약자 명단
          </Link>
          에서 이름·전화번호로 찾아 수동 체크인할 수 있습니다.
        </p>
      </section>

      <section className="adm-card">
        <h2 className="adm-card__title">체크인 결과</h2>
        {result ? (
          <div className={`adm-qr__result adm-qr__result--${result.ok ? "ok" : "fail"}`} role="status">
            {result.ok ? <CheckCircle2 size={28} color="#16a34a" /> : <XCircle size={28} color="#dc2626" />}
            <div>
              <h3>{result.message}</h3>
              {result.data && (
                <p>
                  {result.data.expoTitle} · 예약번호 <span className="adm-mono">{result.data.reservationCode}</span>
                </p>
              )}
            </div>
          </div>
        ) : (
          <div className="adm-empty adm-empty--inline">아직 스캔한 QR이 없습니다.</div>
        )}

        <h2 className="adm-card__title adm-card__title--spaced">
          최근 입장 <small>이 화면에서 처리한 최근 8건</small>
        </h2>
        {history.length ? (
          <ul className="adm-history">
            {history.map((h) => (
              <li key={`${h.data?.reservationId}-${h.at.getTime()}`}>
                <span className="adm-mono">{h.data?.reservationCode}</span>
                <span className="adm-muted">{h.at.toLocaleTimeString("ko-KR")}</span>
              </li>
            ))}
          </ul>
        ) : (
          <p className="adm-hint">입장 처리된 예약이 여기에 쌓입니다.</p>
        )}
        <Link to="/admin/reservation/list" className="adm-btn adm-btn--outline adm-btn--block">
          <Users size={16} />
          예약자 명단 전체 보기
        </Link>
      </section>
    </div>
  );
}
