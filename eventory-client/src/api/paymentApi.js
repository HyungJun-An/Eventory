// 백엔드(스프링) 엔드포인트에 맞춘 간단한 API 래퍼
// - /api/payments/ready : 결제창 호출 준비 데이터 수신
// - /api/payments/complete : 결제 완료 검증/DB 반영

export async function postReady(payload) {
  const res = await fetch('/api/payment/ready', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include', // JWT 쿠키/크리덴셜 쓰는 경우
    body: JSON.stringify(payload),
  });
  if (!res.ok) throw new Error(await res.text());
  return res.json();
}

export async function postComplete(payload) {
  const res = await fetch('/api/payment/complete', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify(payload),
  });
  if (!res.ok) throw new Error(await res.text());
  return res.json();
}