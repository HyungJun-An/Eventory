// 예매·결제 화면
// - 박람회 상세의 [예약하기] → /payment?expoId={id}
// - 결제창 파라미터(payMethod 등)는 서버의 결제 채널 전략이 정해 내려준다 → PG를 바꿔도 이 화면은 그대로
// - PC: 결제창 응답을 받아 바로 완료 처리 / 모바일: 결제 후 /payment/redirect 로 돌아와 완료 처리

import { useEffect, useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import * as PortOne from "@portone/browser-sdk/v2";
import api from "../api/axiosInstance";
import { getPaymentChannel, postComplete, postReady } from "../api/paymentApi";
import { DEFAULT_EXPO_IMAGE } from "../constants/images";
import "../assets/css/payment/Checkout.css";

const MAX_PEOPLE = 10;
const krw = (v) => `${Number(v ?? 0).toLocaleString("ko-KR")}원`;
const errorText = (e, fallback) => e?.response?.data?.message || fallback;

// 결제는 참관객·참가업체 계정만 가능 (관리자 토큰으로는 결제 API 가 403)
const isUserLoggedIn = () => {
  try {
    return Boolean(localStorage.getItem("accessToken")) && (localStorage.getItem("loginTarget") ?? "USER") === "USER";
  } catch {
    return false;
  }
};

export default function PaymentCheckout() {
  const [params] = useSearchParams();
  const expoId = params.get("expoId");
  const navigate = useNavigate();
  const loggedIn = isUserLoggedIn();

  const [expo, setExpo] = useState(null);
  const [loadError, setLoadError] = useState("");
  const [channel, setChannel] = useState(null);
  const [people, setPeople] = useState(1);
  const [paying, setPaying] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!expoId) return;
    api
      .get(`/user/expos/${expoId}`)
      .then((r) => setExpo(r.data))
      .catch((e) => setLoadError(errorText(e, "박람회 정보를 불러오지 못했습니다.")));
  }, [expoId]);

  useEffect(() => {
    if (!loggedIn) return;
    getPaymentChannel()
      .then(setChannel)
      .catch(() => setChannel(null));
  }, [loggedIn]);

  const maxPeople = expo ? Math.max(0, Math.min(MAX_PEOPLE, expo.remainingCapacity)) : MAX_PEOPLE;

  // 잔여석이 선택 인원보다 적으면 맞춰 줄인다
  useEffect(() => {
    setPeople((p) => Math.min(Math.max(1, p), Math.max(1, maxPeople)));
  }, [maxPeople]);

  const pay = async () => {
    setError("");
    setPaying(true);
    try {
      const ready = await postReady({ expoId: Number(expoId), people });
      const response = await PortOne.requestPayment({
        storeId: ready.storeId,
        channelKey: ready.channelKey,
        paymentId: ready.paymentId,
        orderName: ready.orderName,
        totalAmount: Number(ready.totalAmount),
        currency: ready.currency,
        payMethod: ready.payMethod,
        ...(ready.easyPay ? { easyPay: ready.easyPay } : {}),
        ...(ready.customer ? { customer: ready.customer } : {}),
        redirectUrl: `${window.location.origin}/payment/redirect`, // 모바일은 결제 후 이 주소로 돌아온다
      });

      if (!response) return; // 리디렉션 방식이면 페이지가 이동하므로 여기로 오지 않는다
      if (response.code) {
        // 사용자가 결제창을 닫은 경우도 여기로 온다
        setError(response.message || "결제가 취소되었습니다.");
        return;
      }

      const done = await postComplete(response.paymentId ?? ready.paymentId);
      navigate(`/payment/reservation/${done.reservationId}`, {
        replace: true,
        state: {
          reservationId: done.reservationId,
          reservationCode: done.reservationCode,
          paymentStatus: done.status,
          expoTitle: expo.title,
          people,
          amount: ready.totalAmount,
        },
      });
    } catch (e) {
      setError(errorText(e, e?.message || "결제 처리 중 오류가 발생했습니다."));
    } finally {
      setPaying(false);
    }
  };

  if (!expoId) {
    return (
      <div className="co-page">
        <div className="co-card co-empty">
          예매할 박람회를 먼저 선택해주세요. <Link to="/">박람회 둘러보기</Link>
        </div>
      </div>
    );
  }
  if (loadError) {
    return (
      <div className="co-page">
        <div className="co-card co-empty">
          {loadError} <Link to="/">메인으로</Link>
        </div>
      </div>
    );
  }
  if (!expo) {
    return (
      <div className="co-page">
        <div className="co-card co-empty">불러오는 중…</div>
      </div>
    );
  }

  const soldOut = maxPeople === 0;
  const total = Number(expo.price) * people;

  return (
    <div className="co-page">
      <h1 className="co-title">예매하기</h1>
      <div className="co-grid">
        <section className="co-card co-expo">
          <img
            className="co-poster"
            src={expo.imageUrl || DEFAULT_EXPO_IMAGE}
            alt={expo.title}
            onError={(e) => {
              e.currentTarget.onerror = null;
              e.currentTarget.src = DEFAULT_EXPO_IMAGE;
            }}
          />
          <div className="co-expo__info">
            <h2>{expo.title}</h2>
            <dl className="co-dl">
              <dt>일정</dt>
              <dd>
                {expo.startDate} ~ {expo.endDate}
              </dd>
              <dt>장소</dt>
              <dd>{expo.location}</dd>
              <dt>입장료</dt>
              <dd>1인 {krw(expo.price)}</dd>
              <dt>잔여석</dt>
              <dd>{soldOut ? "매진" : `${expo.remainingCapacity.toLocaleString("ko-KR")}석`}</dd>
            </dl>
          </div>
        </section>

        <aside className="co-card co-summary">
          <h3>주문 정보</h3>
          <div className="co-row">
            <span>인원</span>
            <div className="co-stepper">
              <button type="button" onClick={() => setPeople((p) => p - 1)} disabled={people <= 1 || paying} aria-label="인원 줄이기">
                −
              </button>
              <span aria-live="polite">{people}명</span>
              <button type="button" onClick={() => setPeople((p) => p + 1)} disabled={people >= maxPeople || paying} aria-label="인원 늘리기">
                +
              </button>
            </div>
          </div>
          <div className="co-row">
            <span>1인 가격</span>
            <span>{krw(expo.price)}</span>
          </div>
          <div className="co-row">
            <span>결제수단</span>
            <span>{channel?.label ?? (loggedIn ? "확인 중…" : "-")}</span>
          </div>
          <div className="co-total">
            <span>총 결제 금액</span>
            <strong>{krw(total)}</strong>
          </div>

          {loggedIn ? (
            <button type="button" className="co-pay" onClick={pay} disabled={paying || soldOut}>
              {soldOut ? "매진" : paying ? "결제 진행 중…" : `${krw(total)} 결제하기`}
            </button>
          ) : (
            <>
              <p className="co-hint">결제하려면 참관객 계정으로 로그인해주세요.</p>
              <Link to="/login" className="co-pay co-pay--link">
                로그인하기
              </Link>
            </>
          )}
          {error && (
            <p className="co-error" role="alert">
              {error}
            </p>
          )}
          <p className="co-hint">결제가 끝나면 입장 QR이 가입한 이메일로 발송됩니다. 입장 전까지 전액 환불할 수 있습니다.</p>
        </aside>
      </div>
    </div>
  );
}
