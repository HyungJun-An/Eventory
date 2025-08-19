// 모바일 리디렉션 복귀 처리 전용
// - 결제 리디렉션 복귀 URL로 설정: {BASE}/payment/redirect
// - 쿼리스트링이나 세션/스토리지에 저장해 둔 paymentId를 꺼내 서버 완료 검증 호출

import React, { useEffect, useState } from 'react';
import { postComplete } from '../api/paymentApi';

export function PaymentRedirect() {
  const [msg, setMsg] = useState('결제 결과 확인 중...');

  useEffect(() => {
    (async () => {
      try {
        // 1) paymentId 확보
        const url = new URL(window.location.href);
        const paymentId = url.searchParams.get('paymentId') || sessionStorage.getItem('paymentId');
        if (!paymentId) throw new Error('paymentId 없음');

        // 2) 서버 완료 검증 호출(프로젝트에 맞게 userId/expoId/people를 보존·복원)
        const userId = Number(sessionStorage.getItem('userId')) || 1;
        const expoId = Number(sessionStorage.getItem('expoId')) || 101;
        const people = Number(sessionStorage.getItem('people')) || 1;
        const orderName = sessionStorage.getItem('orderName') || 'Eventory 입장권(1인)';
        const expectedAmount = Number(sessionStorage.getItem('expectedAmount')) || 1000;

        const res = await postComplete({ paymentId, userId, expoId, people, orderName, expectedAmount });
        setMsg(`결제 성공! 예약번호: ${res.reservationCode}`);
      } catch (e) {
        console.error(e);
        setMsg(e.message ?? '결제 검증 실패함.');
      }
    })();
  }, []);

  return (
    <div className="min-h-screen flex items-center justify-center p-8">
      <div className="max-w-md w-full bg-white rounded-2xl shadow p-6 text-center">
        <h2 className="text-xl font-semibold mb-2">결제 결과</h2>
        <p className="text-gray-700">{msg}</p>
      </div>
    </div>
  );
}