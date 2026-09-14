// 결제 API 래퍼 — 공통 axios(api) 사용: 로그인 토큰 자동 첨부 + 만료 시 재발급
// 결제자는 서버가 JWT 로 식별하므로 요청 본문에 userId·금액을 보내지 않는다.
import api from "./axiosInstance";

/** 현재 결제 채널(결제수단) 정보 { type, label } */
export const getPaymentChannel = () => api.get("/payment/channel").then((r) => r.data);

/** 결제 준비 — 서버가 금액을 계산해 PortOne SDK 에 넘길 값을 돌려준다 */
export const postReady = ({ expoId, people }) =>
  api.post("/payment/ready", { expoId, people }).then((r) => r.data);

/** 결제 완료 — 서버가 PortOne 실제 결제와 주문을 대조한 뒤 예약을 확정한다 */
export const postComplete = (paymentId) => api.post("/payment/complete", { paymentId }).then((r) => r.data);

/** 본인 예약 환불 */
export const requestRefund = (reservationId, reason) => api.post(`/payment/${reservationId}/refund`, { reason });
