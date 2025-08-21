import axios from "axios";

const client = axios.create({
  baseURL: "", // 같은 도메인이라면 공백. 다른 서버면 http(s)://... 로 교체
  timeout: 15000,
});

// 필요 시 토큰/에러 공통 처리
client.interceptors.response.use(
  (res) => res,
  (err) => {
    console.error(err);
    const message =
      err.response?.data?.message || "요청 처리 중 오류가 발생했습니다.";
    return Promise.reject(new Error(message));
  }
);

export default client;

