// PortOne v2 브라우저 SDK 기반 결제창 호출 컴포넌트
// - KG이니시스 채널(channelKey) 사용
// - 모바일 리디렉션 대비 redirectUrl 사용
// - 결제 완료 후 /api/payments/complete 호출로 서버 검증

import React, { useCallback, useMemo, useState } from 'react';
import { postReady, postComplete } from '../api/paymentApi';
import * as PortOne from "@portone/browser-sdk/v2";

export default function PaymentCheckout() {
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');

  // 데모 입력값(실제론 장바구니/선택 정보로 구성)
  const demoReq = useMemo(() => ({
    userId: 1,
    expoId: 3,
    people: 1,
    orderName: 'Eventory 입장권(1인)',
    totalAmount: 1000, // KRW는 정수 금액
  }), []);

  const handlePay = useCallback(async () => {
    setLoading(true);
    setMessage('결제 준비 중...');
    try {
      // 1) 서버에 결제 준비 요청하여 paymentId / storeId / channelKey 수신
      const ready = await postReady(demoReq);
      // ready: { paymentId, storeId, channelKey, orderName, totalAmount, currency, payMethod, redirectUrl }

      setMessage('결제창 호출 중...');

      // 2) PortOne 결제창 호출
      //    - KG이니시스 사용 시 channelKey에 이니시스 채널 키 지정
      //    - 필수 구매자 정보(KG이니시스: fullName, phoneNumber, email) 전달 권장
      const response = await PortOne.requestPayment({
        storeId: ready.storeId,
        channelKey: ready.channelKey,
        paymentId: ready.paymentId,
        orderName: ready.orderName,
        totalAmount: Number(ready.totalAmount),
        currency: ready.currency ?? 'KRW',
        payMethod: ready.payMethod ?? 'CARD',
        redirectUrl: ready.redirectUrl, // 모바일 리디렉션 대비
        customer: {
          fullName: '정현해',
          phoneNumber: '01059504782',
          email: 'coachofgi@gmail.com',
        },
      });

      // 리디렉션 방식이 아닌 경우(PC IFRAME/POPUP 등)엔 여기서 응답을 받음
      // response: { transactionType: 'PAYMENT', txId, paymentId, code?, message? }
      if (response?.code) {
        // 결제창 단계 오류
        throw new Error(`결제창 오류(${response.code}): ${response.message ?? '원인 미상'}`);
      }

      // 3) 결제 성공 시 서버 확정 처리(금액/상태 검증 및 DB 반영)
      setMessage('서버로 결제 완료 검증 중...');
      const complete = await postComplete({
        paymentId: ready.paymentId,
        userId: demoReq.userId,
        expoId: demoReq.expoId,
        people: demoReq.people,
        orderName: ready.orderName,
        expectedAmount: ready.totalAmount,
      });

      setMessage(`결제 성공! 예약번호: ${complete.reservationCode}`);
    } catch (e) {
      console.error(e);
      setMessage(e.message ?? '결제 실패함.');
    } finally {
      setLoading(false);
    }
  }, [demoReq]);

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 p-6">
      <div className="w-full max-w-lg rounded-2xl shadow-lg bg-white p-6 space-y-4">
        <h1 className="text-2xl font-bold">Eventory 결제 데모 (KG이니시스)</h1>
        <p className="text-sm text-gray-600">테스트 결제 금액: {demoReq.totalAmount.toLocaleString()}원</p>

        <button
          onClick={handlePay}
          disabled={loading}
          className="w-full rounded-2xl py-3 font-semibold shadow hover:shadow-md transition disabled:opacity-50 bg-black text-white"
        >
          {loading ? '진행 중...' : '결제하기'}
        </button>

        <div className="text-sm text-gray-700 whitespace-pre-line min-h-[2.5rem]">{message}</div>

        <div className="text-xs text-gray-500">
          * 모바일 환경에서는 리디렉션 후 돌아오면 <code>/payment/redirect</code> 라우트에서
          <code>paymentId</code>를 서버로 전달하여 <code>/api/payments/complete</code> 호출해야 함.
        </div>
      </div>
    </div>
  );
}